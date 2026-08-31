package io.github.aalsanie.codes.protocol.http;

import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.OutcomeDefinition;
import io.github.aalsanie.codes.OutcomeState;
import io.github.aalsanie.codes.StandardOutcomes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpOutcomeMapperTest {
    @Test
    void standardMappingsAreConservative() {
        HttpOutcomeMapper mapper = HttpOutcomeMapper.standard();
        assertEquals(12, mapper.getSize());
        assertSame(HttpStatusCode.OK, mapper.map(StandardOutcomes.OK).orNull());
        assertSame(HttpStatusCode.BAD_REQUEST, mapper.map(StandardOutcomes.INVALID_ARGUMENT).orNull());
        assertSame(HttpStatusCode.UNAUTHORIZED, mapper.map(StandardOutcomes.UNAUTHENTICATED).orNull());
        assertSame(HttpStatusCode.FORBIDDEN, mapper.map(StandardOutcomes.PERMISSION_DENIED).orNull());
        assertSame(HttpStatusCode.NOT_FOUND, mapper.map(Outcome.of(StandardOutcomes.NOT_FOUND)).orNull());
        assertSame(HttpStatusCode.CONFLICT, mapper.map(StandardOutcomes.ALREADY_EXISTS).orNull());
        assertSame(HttpStatusCode.BAD_REQUEST, mapper.map(StandardOutcomes.OUT_OF_RANGE).orNull());
        assertSame(HttpStatusCode.TOO_MANY_REQUESTS, mapper.map(StandardOutcomes.RATE_LIMITED).orNull());
        assertSame(HttpStatusCode.NOT_IMPLEMENTED, mapper.map(StandardOutcomes.UNIMPLEMENTED).orNull());
        assertSame(HttpStatusCode.SERVICE_UNAVAILABLE, mapper.map(StandardOutcomes.UNAVAILABLE).orNull());
        assertSame(HttpStatusCode.INTERNAL_SERVER_ERROR, mapper.map(StandardOutcomes.INTERNAL).orNull());
        assertSame(HttpStatusCode.INTERNAL_SERVER_ERROR, mapper.map(StandardOutcomes.DATA_LOSS).orNull());
        assertTrue(mapper.map(StandardOutcomes.FAILED_PRECONDITION).isUnmapped());
        assertTrue(mapper.map(StandardOutcomes.CANCELLED).isUnmapped());
        assertTrue(mapper.map(StandardOutcomes.DEADLINE_EXCEEDED).isUnmapped());
        assertTrue(mapper.map(StandardOutcomes.ABORTED).isUnmapped());
        assertTrue(mapper.map(StandardOutcomes.RESOURCE_EXHAUSTED).isUnmapped());
        assertSame(HttpStatusCode.OK, HttpStatusCode.of(200));
        assertSame(HttpStatusCode.CREATED, HttpStatusCode.of(201));
        assertSame(HttpStatusCode.ACCEPTED, HttpStatusCode.of(202));
        assertSame(HttpStatusCode.NO_CONTENT, HttpStatusCode.of(204));
        assertSame(HttpStatusCode.BAD_REQUEST, HttpStatusCode.of(400));
        assertSame(HttpStatusCode.UNAUTHORIZED, HttpStatusCode.of(401));
        assertSame(HttpStatusCode.FORBIDDEN, HttpStatusCode.of(403));
        assertSame(HttpStatusCode.NOT_FOUND, HttpStatusCode.of(404));
        assertSame(HttpStatusCode.CONFLICT, HttpStatusCode.of(409));
        assertSame(HttpStatusCode.PRECONDITION_FAILED, HttpStatusCode.of(412));
        assertSame(HttpStatusCode.PAYLOAD_TOO_LARGE, HttpStatusCode.of(413));
        assertSame(HttpStatusCode.TOO_MANY_REQUESTS, HttpStatusCode.of(429));
        assertSame(HttpStatusCode.INTERNAL_SERVER_ERROR, HttpStatusCode.of(500));
        assertSame(HttpStatusCode.NOT_IMPLEMENTED, HttpStatusCode.of(501));
        assertSame(HttpStatusCode.SERVICE_UNAVAILABLE, HttpStatusCode.of(503));
        assertSame(HttpStatusCode.GATEWAY_TIMEOUT, HttpStatusCode.of(504));
        assertFalse(HttpStatusCode.OK.isInformational());
        assertFalse(HttpStatusCode.BAD_REQUEST.isSuccessful());
        assertFalse(HttpStatusCode.OK.isRedirection());
        assertFalse(HttpStatusCode.OK.isClientError());
        assertFalse(HttpStatusCode.BAD_REQUEST.isServerError());
        assertEquals(HttpStatusCode.OK, HttpStatusCode.OK);
        assertNotEquals(HttpStatusCode.OK, HttpStatusCode.CREATED);
        assertEquals(404, HttpStatusCode.NOT_FOUND.hashCode());
    }

    @Test
    void customMappingsAreImmutableAndExplicit() {
        OutcomeDefinition custom = OutcomeDefinition.custom(
            "com.example.payments", "PAYMENT_DECLINED", OutcomeState.FAILED, "Declined."
        );
        HttpOutcomeMapper base = HttpOutcomeMapper.standard();
        HttpOutcomeMapper extended = base.withMapping(custom, HttpStatusCode.of(422));
        assertFalse(base.contains(custom.getCode()));
        assertTrue(extended.contains(custom.getCode()));
        assertEquals(422, extended.map(custom).orNull().getValue());
        assertEquals(HttpStatusCode.of(410), base.withOverride(StandardOutcomes.NOT_FOUND, HttpStatusCode.of(410)).map(StandardOutcomes.NOT_FOUND).orNull());
        assertThrows(IllegalArgumentException.class, () -> base.withMapping(StandardOutcomes.NOT_FOUND, HttpStatusCode.NOT_FOUND));
        assertThrows(IllegalArgumentException.class, () -> base.withOverride(custom, HttpStatusCode.BAD_REQUEST));
        assertThrows(UnsupportedOperationException.class, () -> base.mappings().clear());
    }

    @Test
    void statusCodeValidatesAndClassifies() {
        assertTrue(HttpStatusCode.of(199).isInformational());
        assertTrue(HttpStatusCode.OK.isSuccessful());
        assertTrue(HttpStatusCode.of(302).isRedirection());
        assertTrue(HttpStatusCode.BAD_REQUEST.isClientError());
        assertTrue(HttpStatusCode.INTERNAL_SERVER_ERROR.isServerError());
        assertEquals(4, HttpStatusCode.NOT_FOUND.getFamily());
        assertEquals("404", HttpStatusCode.NOT_FOUND.toString());
        assertEquals(HttpStatusCode.of(418), HttpStatusCode.of(418));
        assertTrue(HttpStatusCode.OK.compareTo(HttpStatusCode.BAD_REQUEST) < 0);
        assertThrows(IllegalArgumentException.class, () -> HttpStatusCode.of(99));
        assertThrows(IllegalArgumentException.class, () -> HttpStatusCode.of(600));
        assertThrows(NullPointerException.class, () -> HttpStatusCode.OK.compareTo(null));
    }
}
