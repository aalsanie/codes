package com.example.orders.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.rpc.BadRequest;
import com.google.rpc.ErrorInfo;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import org.junit.jupiter.api.Test;

class GrpcReferenceTest {
    private final OrderService service = new OrderService();
    private final OutcomeGrpcBoundary boundary = new OutcomeGrpcBoundary();

    @Test
    void validationMapsToCanonicalStatusAndStructuredDetails() throws Exception {
        OrderResult result = service.create("", "", "book");
        StatusRuntimeException exception = boundary.toException(result.outcome());
        com.google.rpc.Status status = StatusProto.fromThrowable(exception);
        assertNotNull(status);
        assertEquals(3, status.getCode());
        ErrorInfo info = status.getDetails(0).unpack(ErrorInfo.class);
        assertEquals("INVALID_ARGUMENT", info.getReason());
        BadRequest badRequest = status.getDetails(1).unpack(BadRequest.class);
        assertEquals(2, badRequest.getFieldViolationsCount());
    }

    @Test
    void customDomainFailureKeepsItsIdentity() throws Exception {
        OrderResult result = service.create("o-1", "c-1", "blocked");
        com.google.rpc.Status status = StatusProto.fromThrowable(boundary.toException(result.outcome()));
        assertNotNull(status);
        assertEquals(9, status.getCode());
        assertEquals("ORDER_REJECTED", status.getDetails(0).unpack(ErrorInfo.class).getReason());
    }
}
