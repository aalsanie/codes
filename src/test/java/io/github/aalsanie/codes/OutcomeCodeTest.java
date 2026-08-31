package io.github.aalsanie.codes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OutcomeCodeTest {
    @Test
    void createsAndParsesValidCodes() {
        OutcomeCode code = OutcomeCode.of("com.example.payments", "PAYMENT_DECLINED");
        assertEquals("com.example.payments", code.getNamespace());
        assertEquals("PAYMENT_DECLINED", code.getName());
        assertEquals("com.example.payments:PAYMENT_DECLINED", code.getValue());
        assertEquals(code.getValue(), code.toString());
        assertEquals(code, OutcomeCode.parse(code.getValue()));
        assertEquals(code.hashCode(), OutcomeCode.parse(code.getValue()).hashCode());
    }

    @Test
    void rejectsInvalidNamespacesAndNames() {
        assertThrows(NullPointerException.class, () -> OutcomeCode.of(null, "CODE"));
        assertThrows(NullPointerException.class, () -> OutcomeCode.of("com.example", null));
        assertThrows(IllegalArgumentException.class, () -> OutcomeCode.of("", "CODE"));
        assertThrows(IllegalArgumentException.class, () -> OutcomeCode.of("a".repeat(129), "CODE"));
        assertThrows(IllegalArgumentException.class, () -> OutcomeCode.of("com..example", "CODE"));
        assertThrows(IllegalArgumentException.class, () -> OutcomeCode.of("a".repeat(64), "CODE"));
        assertThrows(IllegalArgumentException.class, () -> OutcomeCode.of("1example", "CODE"));
        assertThrows(IllegalArgumentException.class, () -> OutcomeCode.of("com.example-", "CODE"));
        assertThrows(IllegalArgumentException.class, () -> OutcomeCode.of("Com.example", "CODE"));
        assertThrows(IllegalArgumentException.class, () -> OutcomeCode.of("com.example_foo", "CODE"));
        assertThrows(IllegalArgumentException.class, () -> OutcomeCode.of("com.example", ""));
        assertThrows(IllegalArgumentException.class, () -> OutcomeCode.of("com.example", "A".repeat(65)));
        assertThrows(IllegalArgumentException.class, () -> OutcomeCode.of("com.example", "bad"));
        assertThrows(IllegalArgumentException.class, () -> OutcomeCode.of("com.example", "BAD-NAME"));
    }

    @Test
    void parseRejectsMalformedValues() {
        assertThrows(NullPointerException.class, () -> OutcomeCode.parse(null));
        assertThrows(IllegalArgumentException.class, () -> OutcomeCode.parse("com.example"));
        assertThrows(IllegalArgumentException.class, () -> OutcomeCode.parse(":CODE"));
        assertThrows(IllegalArgumentException.class, () -> OutcomeCode.parse("com:example:CODE"));
        assertThrows(IllegalArgumentException.class, () -> OutcomeCode.parse("com.example:"));
        assertThrows(
            NullPointerException.class,
            () -> OutcomeCode.parseOrNull(null)
        );
        assertNull(OutcomeCode.parseOrNull("bad"));
    }

    @Test
    void identityAndOrderingUseBothParts() {
        OutcomeCode a = OutcomeCode.of("com.example", "A");
        OutcomeCode b = OutcomeCode.of("com.example", "B");
        OutcomeCode otherNamespace = OutcomeCode.of("org.example", "A");
        assertTrue(a.compareTo(b) < 0);
        assertNotEquals(a, b);
        assertNotEquals(a, otherNamespace);
        assertNotEquals(a, "com.example:A");
        assertThrows(NullPointerException.class, () -> a.compareTo(null));
    }
}
