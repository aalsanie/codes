package io.github.aalsanie.codes.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.aalsanie.codes.Issue;
import io.github.aalsanie.codes.MappingResult;
import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.OutcomeDefinition;
import io.github.aalsanie.codes.OutcomeState;
import io.github.aalsanie.codes.StandardOutcomes;
import io.github.aalsanie.codes.protocol.http.HttpOutcomeMapper;
import io.github.aalsanie.codes.protocol.http.HttpStatusCode;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class SpringHttpGoldenResponseTest {
    private static final String ABSENT = "<absent>";
    private static final String ABOUT_BLANK = "about:blank";
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final List<String> GOLDEN_CASES = List.of(
        "safe",
        "public",
        "custom",
        "validation",
        "unmapped",
        "sensitive-detail",
        "custom-safe"
    );

    private static final OutcomeDefinition PAYMENT_DECLINED = OutcomeDefinition.custom(
        "com.example.checkout",
        "PAYMENT_DECLINED",
        OutcomeState.FAILED,
        "The payment was declined."
    );

    private static final SpringHttpStatusMapper STATUS_MAPPER = new SpringHttpStatusMapper(
        HttpOutcomeMapper.standard()
            .withMapping(PAYMENT_DECLINED, HttpStatusCode.of(422))
    );

    private static final SpringProblemTypeUriMapper TYPE_MAPPER =
        SpringProblemTypeUriMapper.empty()
            .withMapping(
                StandardOutcomes.INVALID_ARGUMENT,
                URI.create("https://api.example.test/problems/invalid-argument")
            )
            .withMapping(
                PAYMENT_DECLINED,
                URI.create("https://api.example.test/problems/payment-declined")
            );

    private static final OutcomeProblemDetailMapper PUBLIC_MAPPER =
        new OutcomeProblemDetailMapper(
            STATUS_MAPPER,
            SpringOutcomeExposure.publicErrors(),
            TYPE_MAPPER
        );

    private static final OutcomeProblemDetailMapper DETAIL_MAPPER =
        new OutcomeProblemDetailMapper(
            STATUS_MAPPER,
            new SpringOutcomeExposure(true, true, false),
            TYPE_MAPPER
        );

    private static final OutcomeProblemDetailMapper CUSTOM_SAFE_MAPPER =
        new OutcomeProblemDetailMapper(
            STATUS_MAPPER,
            SpringOutcomeExposure.safeDefaults(),
            TYPE_MAPPER
        );

    @Test
    void mvcRenderedHttpResponsesMatchGoldenContract() throws Exception {
        WebTestClient client = MockMvcWebTestClient
            .bindToController(new WireController())
            .controllerAdvice(new MvcProblemAdvice())
            .build();

        assertGoldenResponses("mvc", client);
    }

    @Test
    void webFluxRenderedHttpResponsesMatchGoldenContract() throws Exception {
        WebTestClient client = WebTestClient
            .bindToController(new WireController())
            .controllerAdvice(new WebFluxProblemAdvice())
            .build();

        assertGoldenResponses("webflux", client);
    }

    private static void assertGoldenResponses(String stack, WebTestClient client) throws Exception {
        Map<String, Map<String, String>> expected = readGoldenContract();
        assertEquals(GOLDEN_CASES, List.copyOf(expected.keySet()), stack + " golden case order");

        for (String caseName : GOLDEN_CASES) {
            assertResponse(stack, caseName, expected.get(caseName), client);
        }
    }

    private static void assertResponse(
        String stack,
        String caseName,
        Map<String, String> expected,
        WebTestClient client
    ) throws Exception {
        String context = stack + "/" + caseName;
        String path = "/" + caseName;

        EntityExchangeResult<byte[]> response = client.get()
            .uri(path)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectBody()
            .returnResult();

        int expectedStatus = Integer.parseInt(required(expected, "status"));
        assertEquals(expectedStatus, response.getStatus().value(), context + " HTTP status");

        MediaType contentType = response.getResponseHeaders().getContentType();
        assertNotNull(contentType, context + " content type");
        MediaType expectedContentType = MediaType.parseMediaType(required(expected, "contentType"));
        assertTrue(
            expectedContentType.isCompatibleWith(contentType),
            () -> context + " content type expected " + expectedContentType + " but was " + contentType
        );

        byte[] responseBody = Objects.requireNonNull(
            response.getResponseBody(),
            context + " response body"
        );
        String rawBody = new String(responseBody, StandardCharsets.UTF_8);
        JsonNode body = Objects.requireNonNull(JSON.readTree(responseBody), context + " JSON body");

        assertEquals(expectedStatus, body.path("status").asInt(), context + " problem status");
        assertProblemType(body, required(expected, "type"), context);
        assertTextField(body, "title", required(expected, "title"), context);
        assertTextField(body, "detail", required(expected, "detail"), context);
        assertTextField(body, "instance", required(expected, "instance"), context);
        assertTextField(body, "code", required(expected, "code"), context);

        assertFalse(body.has("properties"), context + " must flatten ProblemDetail extensions");
        assertFalse(body.has("outcomeDetail"), context + " must use RFC detail");

        assertIssues(body, expected, context);
        assertSensitiveValuesAreNotRendered(caseName, rawBody, context);
    }

    private static void assertProblemType(JsonNode body, String expected, String context) {
        JsonNode type = body.get("type");

        if (ABOUT_BLANK.equals(expected)) {
            if (type != null) {
                assertFalse(type.isNull(), context + " type must be absent or about:blank");
                assertEquals(ABOUT_BLANK, type.stringValue(), context + " problem type");
            }
            return;
        }

        assertNotNull(type, context + " problem type");
        assertFalse(type.isNull(), context + " problem type");
        assertEquals(expected, type.stringValue(), context + " problem type");
    }

    private static void assertTextField(
        JsonNode body,
        String field,
        String expected,
        String context
    ) {
        JsonNode value = body.get(field);

        if (ABSENT.equals(expected)) {
            assertTrue(
                value == null,
                () -> context + " expected absent '" + field + "' but body contained " + value
            );
            return;
        }

        assertNotNull(value, context + " missing '" + field + "'");
        assertFalse(value.isNull(), context + " null '" + field + "'");
        assertEquals(expected, value.stringValue(), context + " '" + field + "'");
    }

    private static void assertIssues(
        JsonNode body,
        Map<String, String> expected,
        String context
    ) {
        int expectedCount = Integer.parseInt(required(expected, "issues"));
        JsonNode issues = body.get("issues");

        if (expectedCount == 0) {
            assertTrue(issues == null, context + " issues must be absent");
            return;
        }

        assertNotNull(issues, context + " missing issues");
        assertTrue(issues.isArray(), context + " issues must be a JSON array");
        assertEquals(expectedCount, issues.size(), context + " issue count");

        for (int index = 0; index < expectedCount; index++) {
            JsonNode issue = issues.get(index);
            assertNotNull(issue, context + " missing issue " + index);
            assertTextField(
                issue,
                "code",
                required(expected, "issue." + index + ".code"),
                context + " issue " + index
            );
            assertTextField(
                issue,
                "path",
                required(expected, "issue." + index + ".path"),
                context + " issue " + index
            );
            assertTextField(
                issue,
                "message",
                required(expected, "issue." + index + ".message"),
                context + " issue " + index
            );
        }
    }

    private static void assertSensitiveValuesAreNotRendered(
        String caseName,
        String rawBody,
        String context
    ) {
        String sensitiveValue = switch (caseName) {
            case "safe" -> "database shard=7 customerId=42";
            case "validation" -> "raw request body contained account metadata";
            case "sensitive-detail" -> "gateway_token=secret-123";
            case "custom-safe" -> "gateway_token=custom-safe-secret";
            default -> null;
        };

        if (sensitiveValue != null) {
            assertFalse(
                rawBody.contains(sensitiveValue),
                context + " leaked sensitive occurrence detail"
            );
        }
    }

    private static Map<String, Map<String, String>> readGoldenContract() throws IOException {
        List<String> lines = Files.readAllLines(
            Path.of(System.getProperty("codes.springHttpSnapshot"))
        );
        LinkedHashMap<String, Map<String, String>> result = new LinkedHashMap<>();
        Map<String, String> current = null;

        for (String rawLine : lines) {
            String line = rawLine.strip();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            if (line.startsWith("[") && line.endsWith("]")) {
                String name = line.substring(1, line.length() - 1);
                if (name.isBlank() || result.containsKey(name)) {
                    throw new IllegalStateException("Invalid duplicate or blank golden case: " + line);
                }
                current = new LinkedHashMap<>();
                result.put(name, current);
                continue;
            }

            if (current == null) {
                throw new IllegalStateException("Golden property appears before a case: " + line);
            }

            int separator = line.indexOf('=');
            if (separator <= 0) {
                throw new IllegalStateException("Invalid golden property: " + line);
            }

            String key = line.substring(0, separator);
            String value = line.substring(separator + 1);
            if (current.put(key, value) != null) {
                throw new IllegalStateException("Duplicate golden property: " + key);
            }
        }

        return result;
    }

    private static String required(Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null) {
            throw new IllegalStateException("Missing golden property: " + key);
        }
        return value;
    }

    @RestController
    static final class WireController {
        @GetMapping("/{caseName}")
        Object render(@PathVariable("caseName") String caseName) {
            return switch (caseName) {
                case "safe" -> throw mappedException(
                    Outcome.of(
                        StandardOutcomes.NOT_FOUND,
                        "database shard=7 customerId=42"
                    ),
                    OutcomeProblemDetailMapper.safeDefaults()
                );
                case "public" -> throw mappedException(
                    Outcome.of(StandardOutcomes.INVALID_ARGUMENT),
                    PUBLIC_MAPPER
                );
                case "custom" -> throw mappedException(
                    Outcome.of(
                        PAYMENT_DECLINED,
                        "Ask the customer to use another payment method."
                    ),
                    DETAIL_MAPPER
                );
                case "validation" -> throw mappedException(
                    Outcome.of(
                        StandardOutcomes.INVALID_ARGUMENT,
                        "raw request body contained account metadata",
                        List.of(
                            Issue.at("email", "Invalid email address."),
                            Issue.at("quantity", "Must be greater than zero.")
                        )
                    ),
                    PUBLIC_MAPPER
                );
                case "unmapped" -> unmappedResponse(
                    Outcome.of(StandardOutcomes.FAILED_PRECONDITION)
                );
                case "sensitive-detail" -> throw mappedException(
                    Outcome.of(PAYMENT_DECLINED, "gateway_token=secret-123"),
                    PUBLIC_MAPPER
                );
                case "custom-safe" -> throw mappedException(
                    Outcome.of(PAYMENT_DECLINED, "gateway_token=custom-safe-secret"),
                    CUSTOM_SAFE_MAPPER
                );
                default -> throw new IllegalArgumentException("Unknown golden case: " + caseName);
            };
        }

        private static ErrorResponseException mappedException(
            Outcome outcome,
            OutcomeProblemDetailMapper mapper
        ) {
            MappingResult<ErrorResponseException> result =
                SpringOutcomeExceptions.toErrorResponseException(outcome, mapper);
            ErrorResponseException exception = result.orNull();
            if (exception == null) {
                throw new IllegalStateException("Expected mapped Spring error response for " + outcome.getCode());
            }
            return exception;
        }

        private static ResponseEntity<ProblemDetail> unmappedResponse(Outcome outcome) {
            MappingResult<ErrorResponseException> result =
                SpringOutcomeExceptions.toErrorResponseException(outcome, PUBLIC_MAPPER);
            if (result.isMapped()) {
                throw new IllegalStateException("Expected unmapped Spring error response for " + outcome.getCode());
            }

            ProblemDetail fallback = ProblemDetail.forStatus(500);
            fallback.setProperty(
                OutcomeProblemDetailMapper.CODE_PROPERTY,
                outcome.getCode().getValue()
            );
            return ResponseEntity.internalServerError().body(fallback);
        }
    }

    @ControllerAdvice
    static final class MvcProblemAdvice
        extends org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler {
    }

    @ControllerAdvice
    static final class WebFluxProblemAdvice
        extends org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler {
    }
}
