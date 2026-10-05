package io.github.aalsanie.codes.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.aalsanie.codes.ProblemType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

class ProblemDetailsTest {
    private static final ProblemType ORDER_NOT_FOUND = ProblemType.of(
        URI.create("https://api.example.com/problems/order-not-found"),
        404,
        "Order not found"
    );

    @Test
    void createsProblemDetailFromReusableType() {
        ProblemDetail problem = ProblemDetails.forType(ORDER_NOT_FOUND);

        assertEquals(ORDER_NOT_FOUND.getType(), problem.getType());
        assertEquals(ORDER_NOT_FOUND.getStatus(), problem.getStatus());
        assertEquals(ORDER_NOT_FOUND.getTitle(), problem.getTitle());
        assertNull(problem.getDetail());
        assertNull(problem.getInstance());
        assertNull(problem.getProperties());
    }

    @Test
    void createsProblemDetailWithOccurrenceDetail() {
        ProblemDetail problem = ProblemDetails.forTypeAndDetail(
            ORDER_NOT_FOUND,
            "Order o-123 was not found."
        );

        assertEquals(ORDER_NOT_FOUND.getType(), problem.getType());
        assertEquals(ORDER_NOT_FOUND.getStatus(), problem.getStatus());
        assertEquals(ORDER_NOT_FOUND.getTitle(), problem.getTitle());
        assertEquals("Order o-123 was not found.", problem.getDetail());
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(NullPointerException.class, () -> ProblemDetails.forType(null));
        assertThrows(
            NullPointerException.class,
            () -> ProblemDetails.forTypeAndDetail(ORDER_NOT_FOUND, null)
        );
    }

    @Test
    void cannotBeConstructedThroughThePublicApi() throws Exception {
        Constructor<ProblemDetails> constructor = ProblemDetails.class.getDeclaredConstructor();

        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
