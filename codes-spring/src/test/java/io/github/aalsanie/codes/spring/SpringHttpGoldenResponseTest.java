package io.github.aalsanie.codes.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

class SpringHttpGoldenResponseTest {
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

    @Test
    void goldenHttpProblemContractsMatch() throws IOException {
        OutcomeProblemDetailMapper publicMapper = new OutcomeProblemDetailMapper(
            STATUS_MAPPER,
            SpringOutcomeExposure.publicErrors(),
            TYPE_MAPPER
        );
        OutcomeProblemDetailMapper detailMapper = new OutcomeProblemDetailMapper(
            STATUS_MAPPER,
            new SpringOutcomeExposure(true, true, false),
            TYPE_MAPPER
        );

        String actual = String.join(
            "\n\n",
            snapshot(
                "safe",
                OutcomeProblemDetailMapper.safeDefaults().map(
                    Outcome.of(StandardOutcomes.NOT_FOUND, "database shard=7 customerId=42")
                )
            ),
            snapshot(
                "public",
                publicMapper.map(Outcome.of(StandardOutcomes.INVALID_ARGUMENT))
            ),
            snapshot(
                "custom",
                detailMapper.map(
                    Outcome.of(
                        PAYMENT_DECLINED,
                        "Ask the customer to use another payment method."
                    )
                )
            ),
            snapshot(
                "validation",
                publicMapper.map(
                    Outcome.of(
                        StandardOutcomes.INVALID_ARGUMENT,
                        "raw request body contained account metadata",
                        List.of(
                            Issue.at("email", "Invalid email address."),
                            Issue.at("quantity", "Must be greater than zero.")
                        )
                    )
                )
            ),
            snapshot(
                "unmapped",
                publicMapper.map(Outcome.of(StandardOutcomes.FAILED_PRECONDITION))
            ),
            snapshot(
                "sensitive-detail",
                publicMapper.map(
                    Outcome.of(PAYMENT_DECLINED, "gateway_token=secret-123")
                )
            )
        );

        String expected = Files.readString(
            Path.of(System.getProperty("codes.springHttpSnapshot"))
        ).replace("\r\n", "\n").stripTrailing();

        assertEquals(expected, actual);
    }

    private static String snapshot(
        String name,
        MappingResult<ProblemDetail> result
    ) {
        if (result.isUnmapped()) {
            return "[" + name + "]\n"
                + "mapped=false";
        }

        ProblemDetail problem = Objects.requireNonNull(result.orNull());
        Map<String, Object> properties = Objects.requireNonNull(problem.getProperties());

        return "[" + name + "]\n"
            + "mapped=true\n"
            + "status=" + problem.getStatus() + "\n"
            + "type=" + (problem.getType() == null ? "about:blank" : problem.getType()) + "\n"
            + "title=" + nullable(problem.getTitle()) + "\n"
            + "detail=" + nullable(problem.getDetail()) + "\n"
            + "code=" + properties.get(OutcomeProblemDetailMapper.CODE_PROPERTY) + "\n"
            + "issues=" + issueSnapshot(properties.get(OutcomeProblemDetailMapper.ISSUES_PROPERTY));
    }

    private static String nullable(String value) {
        return value == null ? "<none>" : value;
    }

    private static String issueSnapshot(Object value) {
        if (value == null) {
            return "<none>";
        }
        if (!(value instanceof List<?> issues)) {
            throw new IllegalStateException("issues property must be a list");
        }

        StringBuilder result = new StringBuilder("[");
        for (int index = 0; index < issues.size(); index++) {
            if (index > 0) {
                result.append(',');
            }
            if (!(issues.get(index) instanceof Map<?, ?> issue)) {
                throw new IllegalStateException("issue payload must be a map");
            }

            result.append('{');
            boolean hasPrevious = false;
            hasPrevious = appendField(result, issue, "code", hasPrevious);
            hasPrevious = appendField(result, issue, "path", hasPrevious);
            appendField(result, issue, "message", hasPrevious);
            result.append('}');
        }
        return result.append(']').toString();
    }

    private static boolean appendField(
        StringBuilder result,
        Map<?, ?> issue,
        String name,
        boolean hasPrevious
    ) {
        Object value = issue.get(name);
        if (value == null) {
            return hasPrevious;
        }
        if (hasPrevious) {
            result.append(',');
        }
        result.append(name).append('=').append(value);
        return true;
    }
}
