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
    void supportsNonEmptyUriReferences() {
        assertEquals(URI.create("about:blank"), ProblemType.of(URI.create("about:blank"), 500, "Error").getType());
        assertEquals(URI.create("order-not-found"), ProblemType.of(URI.create("order-not-found"), 404, "Missing").getType());
    }

    @Test
    void validatesDefinition() {
        assertThrows(NullPointerException.class, () -> ProblemType.of(null, 404, "Missing"));
        assertThrows(NullPointerException.class, () -> ProblemType.of(URI.create("urn:test"), 404, null));
        assertThrows(IllegalArgumentException.class, () -> ProblemType.of(URI.create(""), 404, "Missing"));
        assertThrows(IllegalArgumentException.class, () -> ProblemType.of(URI.create("urn:test"), 99, "Missing"));
        assertThrows(IllegalArgumentException.class, () -> ProblemType.of(URI.create("urn:test"), 600, "Missing"));
        assertThrows(IllegalArgumentException.class, () -> ProblemType.of(URI.create("urn:test"), 400, "   "));
    }

    @Test
    void factoryValuesHaveValueSemantics() {
        ProblemType first = ProblemType.of(URI.create("urn:problem:missing"), 404, "Missing");
        ProblemType second = ProblemType.of(URI.create("urn:problem:missing"), 404, "Missing");
        ProblemType different = ProblemType.of(URI.create("urn:problem:missing"), 409, "Missing");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, different);
        assertTrue(first.toString().contains("urn:problem:missing"));
    }
}
