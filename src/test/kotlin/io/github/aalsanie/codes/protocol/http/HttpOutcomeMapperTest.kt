package io.github.aalsanie.codes.protocol.http

import io.github.aalsanie.codes.Outcome
import io.github.aalsanie.codes.OutcomeDefinition
import io.github.aalsanie.codes.OutcomeState
import io.github.aalsanie.codes.StandardOutcomes
import io.github.aalsanie.codes.assertEquals
import io.github.aalsanie.codes.assertFails
import io.github.aalsanie.codes.assertFalse
import io.github.aalsanie.codes.assertNotSame
import io.github.aalsanie.codes.assertNull
import io.github.aalsanie.codes.assertSame
import io.github.aalsanie.codes.assertTrue
import org.junit.jupiter.api.Test

class HttpOutcomeMapperTest {
    @Test
    fun validatesHttpCodeRangeBoundaries() {
        assertFails<IllegalArgumentException> {
            HttpStatusCode.of(99)
        }

        assertEquals(100, HttpStatusCode.of(100).value)
        assertEquals(599, HttpStatusCode.of(599).value)

        assertFails<IllegalArgumentException> {
            HttpStatusCode.of(600)
        }
    }

    @Test
    fun canonicalizesEveryKnownHttpCode() {
        val knownStatuses =
            listOf(
                200 to HttpStatusCode.OK,
                201 to HttpStatusCode.CREATED,
                202 to HttpStatusCode.ACCEPTED,
                204 to HttpStatusCode.NO_CONTENT,
                400 to HttpStatusCode.BAD_REQUEST,
                401 to HttpStatusCode.UNAUTHORIZED,
                403 to HttpStatusCode.FORBIDDEN,
                404 to HttpStatusCode.NOT_FOUND,
                409 to HttpStatusCode.CONFLICT,
                412 to HttpStatusCode.PRECONDITION_FAILED,
                413 to HttpStatusCode.PAYLOAD_TOO_LARGE,
                429 to HttpStatusCode.TOO_MANY_REQUESTS,
                500 to HttpStatusCode.INTERNAL_SERVER_ERROR,
                501 to HttpStatusCode.NOT_IMPLEMENTED,
                503 to HttpStatusCode.SERVICE_UNAVAILABLE,
                504 to HttpStatusCode.GATEWAY_TIMEOUT,
            )

        knownStatuses.forEach { (value, expected) ->
            assertSame(expected, HttpStatusCode.of(value))
        }
    }

    @Test
    fun createsValidNonCanonicalHttpCode() {
        val status = HttpStatusCode.of(418)

        assertEquals(418, status.value)
        assertNotSame(status, HttpStatusCode.of(418))
    }

    @Test
    fun exposesHttpStatusFamilies() {
        val informational = HttpStatusCode.of(100)
        val successful = HttpStatusCode.OK
        val redirection = HttpStatusCode.of(302)
        val clientError = HttpStatusCode.BAD_REQUEST
        val serverError = HttpStatusCode.INTERNAL_SERVER_ERROR

        assertTrue(informational.isInformational)
        assertFalse(informational.isSuccessful)
        assertFalse(informational.isRedirection)
        assertFalse(informational.isClientError)
        assertFalse(informational.isServerError)

        assertFalse(successful.isInformational)
        assertTrue(successful.isSuccessful)

        assertFalse(redirection.isSuccessful)
        assertTrue(redirection.isRedirection)

        assertFalse(clientError.isRedirection)
        assertTrue(clientError.isClientError)

        assertFalse(serverError.isClientError)
        assertTrue(serverError.isServerError)
    }

    @Test
    fun implementsValueEqualityComparisonHashCodeAndString() {
        val status = HttpStatusCode.of(418)
        val equal = HttpStatusCode.of(418)
        val different = HttpStatusCode.of(419)

        assertTrue(status.equals(status))
        assertEquals(status, equal)
        assertFalse(status.equals(different))
        assertFalse(status.equals("418"))
        assertFalse(status.equals(null))

        assertEquals(418, status.hashCode())
        assertTrue(HttpStatusCode.OK < HttpStatusCode.BAD_REQUEST)
        assertEquals("418", status.toString())
    }

    @Test fun standardMapsOnlyGenericSuccess() {
        val mapper = HttpOutcomeMapper.standard()
        assertEquals(HttpStatusCode.OK, mapper.map(StandardOutcomes.OK).orNull())
    }

    @Test fun standardMapsAuthenticationCorrectly() {
        val mapper = HttpOutcomeMapper.standard()
        assertEquals(HttpStatusCode.UNAUTHORIZED, mapper.map(StandardOutcomes.UNAUTHENTICATED).orNull())
        assertEquals(HttpStatusCode.FORBIDDEN, mapper.map(StandardOutcomes.PERMISSION_DENIED).orNull())
    }

    @Test fun standardMapsCommonFailures() {
        val mapper = HttpOutcomeMapper.standard()
        assertEquals(HttpStatusCode.BAD_REQUEST, mapper.map(StandardOutcomes.INVALID_ARGUMENT).orNull())
        assertEquals(HttpStatusCode.NOT_FOUND, mapper.map(StandardOutcomes.NOT_FOUND).orNull())
        assertEquals(HttpStatusCode.CONFLICT, mapper.map(StandardOutcomes.ALREADY_EXISTS).orNull())
        assertEquals(HttpStatusCode.TOO_MANY_REQUESTS, mapper.map(StandardOutcomes.RATE_LIMITED).orNull())
        assertEquals(
            HttpStatusCode.BAD_REQUEST,
            mapper.map(StandardOutcomes.OUT_OF_RANGE).orNull(),
        )
    }

    @Test fun standardMapsServerFailures() {
        val mapper = HttpOutcomeMapper.standard()
        assertEquals(HttpStatusCode.NOT_IMPLEMENTED, mapper.map(StandardOutcomes.UNIMPLEMENTED).orNull())
        assertEquals(HttpStatusCode.SERVICE_UNAVAILABLE, mapper.map(StandardOutcomes.UNAVAILABLE).orNull())
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR, mapper.map(StandardOutcomes.INTERNAL).orNull())
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR, mapper.map(StandardOutcomes.DATA_LOSS).orNull())
    }

    @Test fun standardContainsOnlyExplicitMappings() {
        assertEquals(12, HttpOutcomeMapper.standard().size)
    }

    @Test fun ambiguousFailuresAreDeliberatelyUnmapped() {
        val mapper = HttpOutcomeMapper.standard()
        listOf(
            StandardOutcomes.FAILED_PRECONDITION,
            StandardOutcomes.CANCELLED,
            StandardOutcomes.DEADLINE_EXCEEDED,
            StandardOutcomes.ABORTED,
            StandardOutcomes.RESOURCE_EXHAUSTED,
        ).forEach { assertNull(mapper.map(it).orNull()) }
    }

    @Test fun mapsOutcomeThroughValidatedDefinition() {
        assertEquals(HttpStatusCode.NOT_FOUND, HttpOutcomeMapper.standard().map(Outcome.of(StandardOutcomes.NOT_FOUND)).orNull())
    }

    @Test fun emptyMapperIsEmpty() {
        val mapper = HttpOutcomeMapper.empty()
        assertEquals(0, mapper.size)
        assertFalse(mapper.contains(StandardOutcomes.OK.code))
        assertNull(mapper.map(StandardOutcomes.OK).orNull())
    }

    @Test fun addsCustomMappingWithoutChangingOriginal() {
        val custom = OutcomeDefinition.custom("com.example", "CUSTOM", OutcomeState.FAILED, "Custom.")
        val empty = HttpOutcomeMapper.empty()
        val mapper = empty.withMapping(custom, HttpStatusCode.of(422))
        assertEquals(0, empty.size)
        assertEquals(HttpStatusCode.of(422), mapper.map(custom).orNull())
        assertTrue(mapper.contains(custom.code))
    }

    @Test fun rejectsDuplicateMapping() {
        assertFails<IllegalArgumentException> {
            HttpOutcomeMapper.standard().withMapping(StandardOutcomes.OK, HttpStatusCode.CREATED)
        }
    }

    @Test fun overrideChangesOnlySpecifiedMapping() {
        val mapper = HttpOutcomeMapper.standard().withOverride(StandardOutcomes.NOT_FOUND, HttpStatusCode.of(410))
        assertEquals(HttpStatusCode.of(410), mapper.map(StandardOutcomes.NOT_FOUND).orNull())
        assertEquals(HttpStatusCode.UNAUTHORIZED, mapper.map(StandardOutcomes.UNAUTHENTICATED).orNull())
    }

    @Test fun rejectsOverrideOfMissingMapping() {
        val custom = OutcomeDefinition.custom("com.example", "CUSTOM", OutcomeState.FAILED, "Custom.")
        assertFails<IllegalArgumentException> {
            HttpOutcomeMapper.standard().withOverride(custom, HttpStatusCode.BAD_REQUEST)
        }
    }

    @Test fun mappingsSnapshotIsUnmodifiable() {
        @Suppress("UNCHECKED_CAST")
        val mappings = HttpOutcomeMapper.standard().mappings() as MutableMap<Any, Any>
        assertFails<UnsupportedOperationException> { mappings.clear() }
    }
}
