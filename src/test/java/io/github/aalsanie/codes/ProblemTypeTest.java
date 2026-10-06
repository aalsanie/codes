package io.github.aalsanie.codes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import org.junit.jupiter.api.Test;

class ProblemTypeTest {

    @Test
    void createsReusableProblemType() {
        ProblemType problem = ProblemType.of(
            URI.create("https://api.example.com/problems/order-not-found"),
            404,
            "Order not found"
        );

        assertEquals(URI.create("https://api.example.com/problems/order-not-found"), problem.getType());
        assertEquals(404, problem.getStatus());
        assertEquals("Order not found", problem.getTitle());
    }

    @Test
    void supportsAbsoluteNonHttpUris() {
        assertEquals(
            URI.create("urn:example:problem:order-not-found"),
            ProblemType.of(
                URI.create("urn:example:problem:order-not-found"),
                404,
                "Order not found"
            ).getType()
        );
        assertEquals(
            URI.create("tag:example.com,2026:order-not-found"),
            ProblemType.of(
                URI.create("tag:example.com,2026:order-not-found"),
                404,
                "Order not found"
            ).getType()
        );
    }

    @Test
    void rejectsInvalidDefinitions() {
        assertThrows(NullPointerException.class, () -> ProblemType.of(null, 404, "Missing"));
        assertThrows(
            NullPointerException.class,
            () -> ProblemType.of(URI.create("urn:example:problem:missing"), 404, null)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> ProblemType.of(URI.create("order-not-found"), 404, "Missing")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> ProblemType.of(URI.create("about:blank"), 404, "Missing")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> ProblemType.of(URI.create("ABOUT:blank"), 404, "Missing")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> ProblemType.of(URI.create("urn:example:problem:missing"), 99, "Missing")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> ProblemType.of(URI.create("urn:example:problem:missing"), 600, "Missing")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> ProblemType.of(URI.create("urn:example:problem:missing"), 400, "   ")
        );
    }

    @Test
    void hasValueSemantics() {
        ProblemType first = ProblemType.of(URI.create("urn:problem:missing"), 404, "Missing");
        ProblemType second = ProblemType.of(URI.create("urn:problem:missing"), 404, "Missing");

        assertEquals(first, first);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, "not a problem type");
        assertNotEquals(first, ProblemType.of(URI.create("urn:problem:missing"), 409, "Missing"));
        assertNotEquals(first, ProblemType.of(URI.create("urn:problem:other"), 404, "Missing"));
        assertNotEquals(first, ProblemType.of(URI.create("urn:problem:missing"), 404, "Different"));
        assertTrue(first.toString().contains("urn:problem:missing"));
    }
}
