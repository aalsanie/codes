package com.example.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.aalsanie.codes.StandardOutcomes;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

class OrderReferenceTest {
    private final OrderService service = new OrderService();
    private final OutcomeHttpBoundary boundary = new OutcomeHttpBoundary();

    @Test
    void createUsesCustomSuccessOutcomeAndHttp201() {
        ResponseEntity<?> response = boundary.toResponse(
            service.create(new CreateOrderRequest("o-1", "c-1", "book"))
        );
        assertEquals(201, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof Order);
    }

    @Test
    void validationUsesApplicationOwnedProblemTypeAndAdapterExposurePolicy() {
        ResponseEntity<?> response = boundary.toResponse(
            service.create(new CreateOrderRequest("", "", "book"))
        );

        assertEquals(400, response.getStatusCode().value());
        ProblemDetail problem = (ProblemDetail) response.getBody();
        assertEquals(
            URI.create("https://api.example.com/problems/invalid-argument"),
            problem.getType()
        );
        assertEquals(StandardOutcomes.INVALID_ARGUMENT.getDefaultMessage(), problem.getTitle());
        assertEquals(
            StandardOutcomes.INVALID_ARGUMENT.getCode().getValue(),
            problem.getProperties().get("code")
        );
        assertEquals(2, ((java.util.List<?>) problem.getProperties().get("issues")).size());
        assertNull(problem.getDetail());
    }

    @Test
    void pendingAndDomainFailureKeepApplicationIdentity() {
        boundary.toResponse(service.create(new CreateOrderRequest("o-2", "c-1", "book")));
        ResponseEntity<?> processing = boundary.toResponse(service.process("o-2"));
        assertEquals(202, processing.getStatusCode().value());

        boundary.toResponse(service.create(new CreateOrderRequest("o-3", "c-1", "blocked")));
        ResponseEntity<?> rejected = boundary.toResponse(service.process("o-3"));
        assertEquals(422, rejected.getStatusCode().value());

        ProblemDetail problem = (ProblemDetail) rejected.getBody();
        assertEquals(
            URI.create("https://api.example.com/problems/order-rejected"),
            problem.getType()
        );
        assertEquals(OrderOutcomes.ORDER_REJECTED.getDefaultMessage(), problem.getTitle());
        assertEquals(
            OrderOutcomes.ORDER_REJECTED.getCode().getValue(),
            problem.getProperties().get("code")
        );
    }
}
