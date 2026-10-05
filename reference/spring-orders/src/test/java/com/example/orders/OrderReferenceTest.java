package com.example.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

class OrderReferenceTest {
    private final OrderProblemHandler handler = new OrderProblemHandler();

    @Test
    void differentApplicationExceptionsReuseTheSameProblemType() {
        ProblemDetail current = handler.handleOrderNotFound(new OrderNotFoundException("o-1"));
        ProblemDetail archived = handler.handleArchivedOrderNotFound(new ArchivedOrderNotFoundException("o-2"));

        assertEquals(OrderProblems.ORDER_NOT_FOUND.getType(), current.getType());
        assertEquals(OrderProblems.ORDER_NOT_FOUND.getType(), archived.getType());
        assertEquals(OrderProblems.ORDER_NOT_FOUND.getTitle(), current.getTitle());
        assertEquals(OrderProblems.ORDER_NOT_FOUND.getTitle(), archived.getTitle());
        assertEquals(404, current.getStatus());
        assertEquals(404, archived.getStatus());
        assertEquals("Order o-1 was not found.", current.getDetail());
        assertEquals("Archived order o-2 was not found.", archived.getDetail());
    }
}
