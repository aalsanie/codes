package com.example.orders.grpc;

import com.google.protobuf.Any;
import com.google.rpc.BadRequest;
import com.google.rpc.ErrorInfo;
import io.github.aalsanie.codes.Issue;
import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.protocol.grpc.GrpcOutcomeMapper;
import io.github.aalsanie.codes.protocol.grpc.GrpcStatusCode;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;

final class OutcomeGrpcBoundary {
    private final GrpcOutcomeMapper mapper = GrpcOutcomeMapper.standard()
        .withMapping(OrderOutcomes.ORDER_CREATED, GrpcStatusCode.OK)
        .withMapping(OrderOutcomes.ORDER_REJECTED, GrpcStatusCode.FAILED_PRECONDITION);

    StatusRuntimeException toException(Outcome outcome) {
        GrpcStatusCode code = mapper.map(outcome).orNull();
        if (code == null) {
            code = GrpcStatusCode.INTERNAL;
        }

        com.google.rpc.Status.Builder status = com.google.rpc.Status.newBuilder()
            .setCode(code.getValue())
            .setMessage(outcome.getCode().getValue())
            .addDetails(Any.pack(
                ErrorInfo.newBuilder()
                    .setDomain(outcome.getCode().getNamespace())
                    .setReason(outcome.getCode().getName())
                    .build()
            ));

        if (outcome.getDefinition() == io.github.aalsanie.codes.StandardOutcomes.INVALID_ARGUMENT) {
            BadRequest.Builder badRequest = BadRequest.newBuilder();
            for (Issue issue : outcome.getIssues()) {
                BadRequest.FieldViolation.Builder violation = BadRequest.FieldViolation.newBuilder()
                    .setDescription(issue.getMessage());
                if (issue.getPath() != null) {
                    violation.setField(issue.getPath());
                }
                badRequest.addFieldViolations(violation);
            }
            status.addDetails(Any.pack(badRequest.build()));
        }

        return StatusProto.toStatusRuntimeException(status.build());
    }
}
