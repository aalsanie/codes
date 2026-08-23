package io.github.aalsanie.codes

import org.junit.jupiter.api.Test

class OutcomeDefinitionTest {
    @Test fun createsCustomDefinition() {
        val definition = OutcomeDefinition.custom("com.example", "DECLINED", OutcomeState.FAILED, "Declined.")
        assertEquals(OutcomeCode.of("com.example", "DECLINED"), definition.code)
        assertEquals(OutcomeState.FAILED, definition.state)
        assertEquals("Declined.", definition.defaultMessage)
    }

    @Test fun createsCustomDefinitionFromCode() {
        val code = OutcomeCode.of("com.example", "READY")
        val definition = OutcomeDefinition.custom(code, OutcomeState.SUCCEEDED, "Ready.")
        assertSame(code, definition.code)
    }

    @Test fun rejectsRootReservedNamespace() {
        assertFails<IllegalArgumentException> {
            OutcomeDefinition.custom("io.github.aalsanie.codes", "FAKE", OutcomeState.FAILED, "No.")
        }
    }

    @Test fun rejectsNestedReservedNamespace() {
        assertFails<IllegalArgumentException> {
            OutcomeDefinition.custom("io.github.aalsanie.codes.standard", "OK", OutcomeState.FAILED, "Forged.")
        }
    }

    @Test fun allowsLookalikeNonReservedNamespace() {
        val definition = OutcomeDefinition.custom("io.github.aalsanie.codesx", "OK", OutcomeState.FAILED, "Custom.")
        assertEquals("io.github.aalsanie.codesx:OK", definition.code.value)
    }

    @Test fun rejectsBlankDefaultMessage() {
        assertFails<IllegalArgumentException> {
            OutcomeDefinition.custom("com.example", "CODE", OutcomeState.FAILED, " ")
        }
    }

    @Test fun rejectsOversizedDefaultMessage() {
        assertFails<IllegalArgumentException> {
            OutcomeDefinition.custom("com.example", "CODE", OutcomeState.FAILED, "x".repeat(1025))
        }
    }

    @Test fun rejectsNulDefaultMessage() {
        assertFails<IllegalArgumentException> {
            OutcomeDefinition.custom("com.example", "CODE", OutcomeState.FAILED, "bad\u0000message")
        }
    }

    @Test fun toStringContainsStableFields() {
        val definition = OutcomeDefinition.custom("com.example", "CODE", OutcomeState.PENDING, "Pending.")
        assertTrue(definition.toString().contains("com.example:CODE"))
        assertTrue(definition.toString().contains("PENDING"))
    }
}
