package io.github.aalsanie.codes;

import io.github.aalsanie.codes.protocol.grpc.GrpcStatusCode;
import io.github.aalsanie.codes.protocol.http.HttpStatusCode;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NullabilityContractTest {

    @Test
    void publicPackagesAreNullMarked() {
        assertTrue(
            Outcome.class.getPackage().isAnnotationPresent(NullMarked.class)
        );
        assertTrue(
            HttpStatusCode.class.getPackage().isAnnotationPresent(NullMarked.class)
        );
        assertTrue(
            GrpcStatusCode.class.getPackage().isAnnotationPresent(NullMarked.class)
        );
    }

    @Test
    void nullableReturnTypesRemainAnnotated() throws NoSuchMethodException {
        assertNullableReturn(
            Outcome.class.getMethod("getDetail")
        );
        assertNullableReturn(
            Issue.class.getMethod("getCode")
        );
        assertNullableReturn(
            Issue.class.getMethod("getPath")
        );
        assertNullableReturn(
            OutcomeCode.class.getMethod("parseOrNull", String.class)
        );
        assertNullableReturn(
            OutcomeRegistry.class.getMethod("find", OutcomeCode.class)
        );
        assertNullableReturn(
            OutcomeRegistry.class.getMethod("find", String.class)
        );
        assertNullableReturn(
            MappingResult.class.getMethod("orNull")
        );
        assertNullableReturn(
            GrpcStatusCode.class.getMethod("fromValue", int.class)
        );
    }

    private static void assertNullableReturn(Method method) {
        assertTrue(
            method.getAnnotatedReturnType().isAnnotationPresent(Nullable.class),
            () -> method + " must retain @Nullable on its return type"
        );
    }
}
