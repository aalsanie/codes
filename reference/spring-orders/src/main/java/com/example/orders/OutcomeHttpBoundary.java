package com.example.orders;

import io.github.aalsanie.codes.Issue;
import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.StandardOutcomes;
import io.github.aalsanie.codes.protocol.http.HttpOutcomeMapper;
import io.github.aalsanie.codes.protocol.http.HttpStatusCode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
class OutcomeHttpBoundary {
    private final HttpOutcomeMapper mapper = HttpOutcomeMapper.standard()
        .withMapping(OrderOutcomes.ORDER_CREATED, HttpStatusCode.CREATED)
        .withMapping(OrderOutcomes.ORDER_PROCESSING, HttpStatusCode.ACCEPTED)
        .withMapping(OrderOutcomes.ORDER_REJECTED, HttpStatusCode.of(422))
        .withMapping(StandardOutcomes.FAILED_PRECONDITION, HttpStatusCode.CONFLICT);

    ResponseEntity<?> toResponse(OrderResult<?> result) {
        Outcome outcome = result.outcome();
        HttpStatusCode status = mapper.map(outcome).orNull();
        if (status == null) {
            return unmapped(outcome);
        }
        if (!outcome.isFailed()) {
            return ResponseEntity.status(status.getValue()).body(result.value());
        }
        return ResponseEntity.status(status.getValue()).body(problem(outcome, status.getValue()));
    }

    private static ProblemDetail problem(Outcome outcome, int status) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        HttpStatus known = HttpStatus.resolve(status);
        problem.setTitle(known == null ? "HTTP " + status : known.getReasonPhrase());
        problem.setProperty("code", outcome.getCode().getValue());
        if (outcome.getDefinition() == StandardOutcomes.INVALID_ARGUMENT && !outcome.getIssues().isEmpty()) {
            problem.setProperty("issues", safeValidationIssues(outcome.getIssues()));
        }
        return problem;
    }

    private static ResponseEntity<ProblemDetail> unmapped(Outcome outcome) {
        ProblemDetail problem = ProblemDetail.forStatus(500);
        problem.setTitle("Unmapped application outcome");
        problem.setProperty("code", outcome.getCode().getValue());
        return ResponseEntity.internalServerError().body(problem);
    }

    private static List<Map<String, String>> safeValidationIssues(List<Issue> issues) {
        List<Map<String, String>> values = new ArrayList<>(issues.size());
        for (Issue issue : issues) {
            LinkedHashMap<String, String> value = new LinkedHashMap<>();
            if (issue.getPath() != null) {
                value.put("path", issue.getPath());
            }
            value.put("message", issue.getMessage());
            values.add(Map.copyOf(value));
        }
        return List.copyOf(values);
    }
}
