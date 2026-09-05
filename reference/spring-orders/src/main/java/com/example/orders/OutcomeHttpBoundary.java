package com.example.orders;

import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.StandardOutcomes;
import io.github.aalsanie.codes.protocol.http.HttpOutcomeMapper;
import io.github.aalsanie.codes.protocol.http.HttpStatusCode;
import io.github.aalsanie.codes.spring.OutcomeProblemDetailMapper;
import io.github.aalsanie.codes.spring.SpringHttpStatusMapper;
import io.github.aalsanie.codes.spring.SpringOutcomeExposure;
import io.github.aalsanie.codes.spring.SpringProblemTypeUriMapper;
import java.net.URI;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
class OutcomeHttpBoundary {
    private final SpringHttpStatusMapper statusMapper;
    private final OutcomeProblemDetailMapper problemMapper;

    OutcomeHttpBoundary() {
        HttpOutcomeMapper httpMapper = HttpOutcomeMapper.standard()
            .withMapping(OrderOutcomes.ORDER_CREATED, HttpStatusCode.CREATED)
            .withMapping(OrderOutcomes.ORDER_PROCESSING, HttpStatusCode.ACCEPTED)
            .withMapping(OrderOutcomes.ORDER_REJECTED, HttpStatusCode.of(422))
            .withMapping(StandardOutcomes.FAILED_PRECONDITION, HttpStatusCode.CONFLICT);

        statusMapper = new SpringHttpStatusMapper(httpMapper);
        problemMapper = new OutcomeProblemDetailMapper(
            statusMapper,
            SpringOutcomeExposure.publicErrors(),
            SpringProblemTypeUriMapper.empty()
                .withMapping(
                    StandardOutcomes.INVALID_ARGUMENT,
                    URI.create("https://api.example.com/problems/invalid-argument")
                )
                .withMapping(
                    OrderOutcomes.ORDER_REJECTED,
                    URI.create("https://api.example.com/problems/order-rejected")
                )
        );
    }

    ResponseEntity<?> toResponse(OrderResult<?> result) {
        Outcome outcome = result.outcome();
        org.springframework.http.HttpStatusCode status = statusMapper.map(outcome).orNull();
        if (status == null) {
            return unmapped(outcome);
        }
        if (!outcome.isFailed()) {
            return ResponseEntity.status(status).body(result.value());
        }

        ProblemDetail problem = problemMapper.map(outcome).orNull();
        if (problem == null) {
            throw new IllegalStateException("mapped HTTP status produced an unmapped problem detail");
        }
        return ResponseEntity.status(status).body(problem);
    }

    private static ResponseEntity<ProblemDetail> unmapped(Outcome outcome) {
        ProblemDetail problem = ProblemDetail.forStatus(500);
        problem.setProperty(OutcomeProblemDetailMapper.CODE_PROPERTY, outcome.getCode().getValue());
        return ResponseEntity.internalServerError().body(problem);
    }
}
