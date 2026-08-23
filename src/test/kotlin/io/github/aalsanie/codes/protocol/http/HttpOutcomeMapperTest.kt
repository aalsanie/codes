package io.github.aalsanie.codes.protocol.http

import io.github.aalsanie.codes.Outcome
import io.github.aalsanie.codes.OutcomeDefinition
import io.github.aalsanie.codes.OutcomeState
import io.github.aalsanie.codes.StandardOutcomes
import io.github.aalsanie.codes.assertEquals
import io.github.aalsanie.codes.assertFails
import io.github.aalsanie.codes.assertFalse
import io.github.aalsanie.codes.assertNull
import io.github.aalsanie.codes.assertSame
import io.github.aalsanie.codes.assertTrue
import org.junit.jupiter.api.Test

class HttpOutcomeMapperTest {
    @Test fun validatesHttpCodeRange() {
        assertFails<IllegalArgumentException> { HttpStatusCode.of(99) }
        assertFails<IllegalArgumentException> { HttpStatusCode.of(600) }
    }

    @Test fun canonicalizesKnownHttpCodes() { assertSame(HttpStatusCode.OK, HttpStatusCode.of(200)) }
    @Test fun supportsValidUnknownHttpCode() { assertEquals(418, HttpStatusCode.of(418).value) }

    @Test fun exposesHttpFamilies() {
        assertTrue(HttpStatusCode.of(100).isInformational)
        assertTrue(HttpStatusCode.OK.isSuccessful)
        assertTrue(HttpStatusCode.of(302).isRedirection)
        assertTrue(HttpStatusCode.BAD_REQUEST.isClientError)
        assertTrue(HttpStatusCode.INTERNAL_SERVER_ERROR.isServerError)
    }

    @Test fun statusEqualityComparisonAndString() {
        assertEquals(HttpStatusCode.of(418), HttpStatusCode.of(418))
        assertEquals(HttpStatusCode.of(418).hashCode(), 418)
        assertTrue(HttpStatusCode.OK < HttpStatusCode.BAD_REQUEST)
        assertEquals("418", HttpStatusCode.of(418).toString())
    }

    @Test fun standardMapsUnambiguousSuccesses() {
        val mapper = HttpOutcomeMapper.standard()
        assertEquals(HttpStatusCode.OK, mapper.map(StandardOutcomes.OK).orNull())
        assertEquals(HttpStatusCode.CREATED, mapper.map(StandardOutcomes.CREATED).orNull())
        assertEquals(HttpStatusCode.ACCEPTED, mapper.map(StandardOutcomes.ACCEPTED).orNull())
        assertEquals(HttpStatusCode.NO_CONTENT, mapper.map(StandardOutcomes.NO_CONTENT).orNull())
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
        assertEquals(HttpStatusCode.PAYLOAD_TOO_LARGE, mapper.map(StandardOutcomes.PAYLOAD_TOO_LARGE).orNull())
        assertEquals(HttpStatusCode.TOO_MANY_REQUESTS, mapper.map(StandardOutcomes.RATE_LIMITED).orNull())
    }

    @Test fun standardMapsServerFailures() {
        val mapper = HttpOutcomeMapper.standard()
        assertEquals(HttpStatusCode.NOT_IMPLEMENTED, mapper.map(StandardOutcomes.UNIMPLEMENTED).orNull())
        assertEquals(HttpStatusCode.SERVICE_UNAVAILABLE, mapper.map(StandardOutcomes.UNAVAILABLE).orNull())
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR, mapper.map(StandardOutcomes.INTERNAL).orNull())
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR, mapper.map(StandardOutcomes.DATA_LOSS).orNull())
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
        assertEquals(HttpStatusCode.CREATED, mapper.map(StandardOutcomes.CREATED).orNull())
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
