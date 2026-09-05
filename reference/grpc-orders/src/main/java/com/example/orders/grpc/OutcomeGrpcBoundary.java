package com.example.orders.grpc;

import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.grpc.GoogleRpcOutcomeMapper;
import io.github.aalsanie.codes.grpc.GrpcOutcomeExceptions;
import io.github.aalsanie.codes.grpc.GrpcOutcomeExposure;
import io.github.aalsanie.codes.protocol.grpc.GrpcOutcomeMapper;
import io.github.aalsanie.codes.protocol.grpc.GrpcStatusCode;
import io.grpc.StatusRuntimeException;

final class OutcomeGrpcBoundary {
    private final GoogleRpcOutcomeMapper mapper = new GoogleRpcOutcomeMapper(
        GrpcOutcomeMapper.standard()
            .withMapping(
                OrderOutcomes.ORDER_REJECTED,
                GrpcStatusCode.FAILED_PRECONDITION
            ),
        new GrpcOutcomeExposure(false, false, true)
    );

    StatusRuntimeException toException(Outcome outcome) {
        StatusRuntimeException exception = GrpcOutcomeExceptions
            .toStatusRuntimeException(outcome, mapper)
            .orNull();

        if (exception == null) {
            throw new IllegalStateException(
                "No gRPC mapping configured for outcome: " + outcome.getCode()
            );
        }

        return exception;
    }
}
