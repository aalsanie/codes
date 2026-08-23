package io.github.aalsanie.codes

import org.junit.jupiter.api.Test

class OutcomeCodeTest {
    @Test fun createsValidCode() {
        val code = OutcomeCode.of("com.example.payments", "PAYMENT_DECLINED")
        assertEquals("com.example.payments", code.namespace)
        assertEquals("PAYMENT_DECLINED", code.name)
        assertEquals("com.example.payments:PAYMENT_DECLINED", code.value)
        assertEquals(code.value, code.toString())
    }

    @Test fun parsesValidCode() = assertEquals(
        OutcomeCode.of("com.example", "NOT_FOUND"),
        OutcomeCode.parse("com.example:NOT_FOUND"),
    )

    @Test fun parseOrNullReturnsNullForInvalidCode() {
        assertNull(OutcomeCode.parseOrNull("invalid"))
        assertNull(OutcomeCode.parseOrNull("com.example:bad"))
    }

    @Test fun rejectsMissingDelimiter() { assertFails<IllegalArgumentException> { OutcomeCode.parse("com.example") } }
    @Test fun rejectsMultipleDelimiters() { assertFails<IllegalArgumentException> { OutcomeCode.parse("com:example:CODE") } }
    @Test fun rejectsEmptyNamespace() { assertFails<IllegalArgumentException> { OutcomeCode.of("", "CODE") } }
    @Test fun rejectsEmptyNamespaceSegment() { assertFails<IllegalArgumentException> { OutcomeCode.of("com..example", "CODE") } }
    @Test fun rejectsUppercaseNamespace() { assertFails<IllegalArgumentException> { OutcomeCode.of("Com.example", "CODE") } }
    @Test fun rejectsNamespaceSegmentStartingWithDigit() { assertFails<IllegalArgumentException> { OutcomeCode.of("com.1example", "CODE") } }
    @Test fun rejectsNamespaceSegmentEndingWithHyphen() { assertFails<IllegalArgumentException> { OutcomeCode.of("com.example-", "CODE") } }
    @Test fun rejectsNamespaceIllegalCharacter() { assertFails<IllegalArgumentException> { OutcomeCode.of("com.example_foo", "CODE") } }
    @Test fun rejectsNamespaceTooLong() { assertFails<IllegalArgumentException> { OutcomeCode.of("a".repeat(129), "CODE") } }
    @Test fun rejectsNamespaceSegmentTooLong() { assertFails<IllegalArgumentException> { OutcomeCode.of("a".repeat(64), "CODE") } }
    @Test fun rejectsEmptyName() { assertFails<IllegalArgumentException> { OutcomeCode.of("com.example", "") } }
    @Test fun rejectsLowercaseName() { assertFails<IllegalArgumentException> { OutcomeCode.of("com.example", "bad") } }
    @Test fun rejectsNameIllegalCharacter() { assertFails<IllegalArgumentException> { OutcomeCode.of("com.example", "BAD-NAME") } }
    @Test fun rejectsNameTooLong() { assertFails<IllegalArgumentException> { OutcomeCode.of("com.example", "A".repeat(65)) } }

    @Test fun equalityUsesStructuredIdentity() {
        val first = OutcomeCode.of("com.example", "CODE")
        val second = OutcomeCode.of("com.example", "CODE")
        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test fun inequalityUsesBothParts() {
        assertNotEquals(OutcomeCode.of("com.example", "A"), OutcomeCode.of("com.example", "B"))
        assertNotEquals(OutcomeCode.of("com.example", "A"), OutcomeCode.of("org.example", "A"))
    }

    @Test fun comparisonUsesSerializedValue() {
        val first = OutcomeCode.of("com.example", "A")
        val second = OutcomeCode.of("com.example", "B")
        assertTrue(first < second)
    }
}
