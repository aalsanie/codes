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
    void validationUsesAdapterIdentityAndStructuredIssues() throws Exception {
        OrderResult result = service.create("", "", "book");
        StatusRuntimeException exception = boundary.toException(result.outcome());

        com.google.rpc.Status status = StatusProto.fromThrowable(exception);
        assertNotNull(status);
        assertEquals(3, status.getCode());
        assertEquals(result.outcome().getCode().getValue(), status.getMessage());
        assertEquals(2, status.getDetailsCount());

        ErrorInfo info = status.getDetails(0).unpack(ErrorInfo.class);
        assertEquals(result.outcome().getCode().getNamespace(), info.getDomain());
        assertEquals(result.outcome().getCode().getName(), info.getReason());

        BadRequest badRequest = status.getDetails(1).unpack(BadRequest.class);
        assertEquals(2, badRequest.getFieldViolationsCount());
        assertEquals("orderId", badRequest.getFieldViolations(0).getField());
        assertEquals("customerId", badRequest.getFieldViolations(1).getField());
    }

    @Test
    void customDomainFailureKeepsItsIdentityThroughAdapter() throws Exception {
        OrderResult result = service.create("o-1", "c-1", "blocked");

        com.google.rpc.Status status = StatusProto.fromThrowable(
            boundary.toException(result.outcome())
        );

        assertNotNull(status);
        assertEquals(9, status.getCode());
        assertEquals(result.outcome().getCode().getValue(), status.getMessage());
        assertEquals(1, status.getDetailsCount());

        ErrorInfo info = status.getDetails(0).unpack(ErrorInfo.class);
        assertEquals("com.example.orders", info.getDomain());
        assertEquals("ORDER_REJECTED", info.getReason());
    }
}
