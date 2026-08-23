package io.github.aalsanie.codes.protocol.grpc

import io.github.aalsanie.codes.Outcome
import io.github.aalsanie.codes.OutcomeDefinition
import io.github.aalsanie.codes.OutcomeState
import io.github.aalsanie.codes.StandardOutcomes
import io.github.aalsanie.codes.assertEquals
import io.github.aalsanie.codes.assertFails
import io.github.aalsanie.codes.assertNull
import io.github.aalsanie.codes.assertTrue
import org.junit.jupiter.api.Test

class GrpcOutcomeMapperTest {
    @Test fun grpcValuesAreExactlyOfficialRange() {
        assertEquals((0..16).toList(), GrpcStatusCode.entries.map { it.value }.sorted())
    }

    @Test fun resolvesGrpcValue() {
        GrpcStatusCode.entries.forEach { assertEquals(it, GrpcStatusCode.fromValue(it.value)) }
        assertNull(GrpcStatusCode.fromValue(-1))
        assertNull(GrpcStatusCode.fromValue(17))
    }

    @Test fun standardMapsEveryStandardOutcome() {
        val mapper = GrpcOutcomeMapper.standard()
        assertEquals(StandardOutcomes.all.size, mapper.size)
        StandardOutcomes.all.forEach { assertTrue(mapper.map(it).isMapped) }
    }

    @Test fun standardMapsSuccessAndPendingToOk() {
        val mapper = GrpcOutcomeMapper.standard()
        assertEquals(GrpcStatusCode.OK, mapper.map(StandardOutcomes.OK).orNull())
        assertEquals(GrpcStatusCode.OK, mapper.map(StandardOutcomes.CREATED).orNull())
        assertEquals(GrpcStatusCode.OK, mapper.map(StandardOutcomes.ACCEPTED).orNull())
        assertEquals(GrpcStatusCode.OK, mapper.map(StandardOutcomes.NO_CONTENT).orNull())
    }

    @Test fun standardMapsFailureSemantics() {
        val mapper = GrpcOutcomeMapper.standard()
        assertEquals(GrpcStatusCode.INVALID_ARGUMENT, mapper.map(StandardOutcomes.INVALID_ARGUMENT).orNull())
        assertEquals(GrpcStatusCode.UNAUTHENTICATED, mapper.map(StandardOutcomes.UNAUTHENTICATED).orNull())
        assertEquals(GrpcStatusCode.PERMISSION_DENIED, mapper.map(StandardOutcomes.PERMISSION_DENIED).orNull())
        assertEquals(GrpcStatusCode.NOT_FOUND, mapper.map(StandardOutcomes.NOT_FOUND).orNull())
        assertEquals(GrpcStatusCode.ALREADY_EXISTS, mapper.map(StandardOutcomes.ALREADY_EXISTS).orNull())
        assertEquals(GrpcStatusCode.FAILED_PRECONDITION, mapper.map(StandardOutcomes.FAILED_PRECONDITION).orNull())
        assertEquals(GrpcStatusCode.OUT_OF_RANGE, mapper.map(StandardOutcomes.OUT_OF_RANGE).orNull())
        assertEquals(GrpcStatusCode.CANCELLED, mapper.map(StandardOutcomes.CANCELLED).orNull())
        assertEquals(GrpcStatusCode.DEADLINE_EXCEEDED, mapper.map(StandardOutcomes.DEADLINE_EXCEEDED).orNull())
        assertEquals(GrpcStatusCode.ABORTED, mapper.map(StandardOutcomes.ABORTED).orNull())
        assertEquals(GrpcStatusCode.UNIMPLEMENTED, mapper.map(StandardOutcomes.UNIMPLEMENTED).orNull())
        assertEquals(GrpcStatusCode.UNAVAILABLE, mapper.map(StandardOutcomes.UNAVAILABLE).orNull())
        assertEquals(GrpcStatusCode.INTERNAL, mapper.map(StandardOutcomes.INTERNAL).orNull())
        assertEquals(GrpcStatusCode.DATA_LOSS, mapper.map(StandardOutcomes.DATA_LOSS).orNull())
    }

    @Test fun resourceFailuresMapToResourceExhausted() {
        val mapper = GrpcOutcomeMapper.standard()
        assertEquals(GrpcStatusCode.RESOURCE_EXHAUSTED, mapper.map(StandardOutcomes.PAYLOAD_TOO_LARGE).orNull())
        assertEquals(GrpcStatusCode.RESOURCE_EXHAUSTED, mapper.map(StandardOutcomes.RATE_LIMITED).orNull())
        assertEquals(GrpcStatusCode.RESOURCE_EXHAUSTED, mapper.map(StandardOutcomes.RESOURCE_EXHAUSTED).orNull())
    }

    @Test fun mapsOutcome() {
        assertEquals(GrpcStatusCode.ABORTED, GrpcOutcomeMapper.standard().map(Outcome.of(StandardOutcomes.ABORTED)).orNull())
    }

    @Test fun addsAndOverridesCustomMapping() {
        val custom = OutcomeDefinition.custom("com.example", "CUSTOM", OutcomeState.FAILED, "Custom.")
        val added = GrpcOutcomeMapper.empty().withMapping(custom, GrpcStatusCode.UNKNOWN)
        assertEquals(GrpcStatusCode.UNKNOWN, added.map(custom).orNull())
        assertTrue(added.contains(custom.code))
        val overridden = added.withOverride(custom, GrpcStatusCode.INTERNAL)
        assertEquals(GrpcStatusCode.INTERNAL, overridden.map(custom).orNull())
    }

    @Test fun rejectsDuplicateAndMissingOverride() {
        assertFails<IllegalArgumentException> {
            GrpcOutcomeMapper.standard().withMapping(StandardOutcomes.OK, GrpcStatusCode.UNKNOWN)
        }
        val custom = OutcomeDefinition.custom("com.example", "CUSTOM", OutcomeState.FAILED, "Custom.")
        assertFails<IllegalArgumentException> {
            GrpcOutcomeMapper.standard().withOverride(custom, GrpcStatusCode.UNKNOWN)
        }
    }

    @Test fun mappingsSnapshotIsUnmodifiable() {
        @Suppress("UNCHECKED_CAST")
        val mappings = GrpcOutcomeMapper.standard().mappings() as MutableMap<Any, Any>
        assertFails<UnsupportedOperationException> { mappings.clear() }
    }
}
