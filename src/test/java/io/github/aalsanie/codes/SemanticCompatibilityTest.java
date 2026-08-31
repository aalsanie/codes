package io.github.aalsanie.codes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.aalsanie.codes.protocol.grpc.GrpcOutcomeMapper;
import io.github.aalsanie.codes.protocol.http.HttpOutcomeMapper;
import io.github.aalsanie.codes.protocol.grpc.GrpcStatusCode;
import io.github.aalsanie.codes.protocol.http.HttpStatusCode;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class SemanticCompatibilityTest {
    @Test
    void standardOutcomeSemanticsMatchSnapshot() throws IOException {
        String actual = StandardOutcomes.all.stream()
            .sorted(Comparator.comparing(definition -> definition.getCode().getValue()))
            .map(definition -> definition.getCode().getValue() + "|" + definition.getState().name())
            .collect(Collectors.joining("\n"));
        assertEquals(readSnapshot("standard-outcomes.snapshot"), actual);
    }

    @Test
    void standardHttpMappingsMatchSnapshot() throws IOException {
        String actual = HttpOutcomeMapper.standard().mappings().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> entry.getKey().getValue() + "|" + entry.getValue().getValue())
            .collect(Collectors.joining("\n"));
        assertEquals(readSnapshot("http-mappings.snapshot"), actual);
    }

    @Test
    void standardGrpcMappingsMatchSnapshot() throws IOException {
        String actual = GrpcOutcomeMapper.standard().mappings().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> entry.getKey().getValue() + "|" + entry.getValue().name())
            .collect(Collectors.joining("\n"));
        assertEquals(readSnapshot("grpc-mappings.snapshot"), actual);
    }

    @Test
    void publicHttpStatusCodesMatchSnapshot() throws IOException {
        String actual = Arrays.stream(HttpStatusCode.class.getFields())
            .filter(field -> Modifier.isStatic(field.getModifiers()))
            .filter(field -> field.getType() == HttpStatusCode.class)
            .sorted(Comparator.comparing(Field::getName))
            .map(field -> field.getName() + "|" + readHttpStatusValue(field))
            .collect(Collectors.joining("\n"));

        assertEquals(readSnapshot("http-status-codes.snapshot"), actual);
    }

    @Test
    void grpcStatusCodesMatchSnapshot() throws IOException {
        String actual = Arrays.stream(GrpcStatusCode.values())
            .map(status -> status.name() + "|" + status.getValue())
            .collect(Collectors.joining("\n"));

        assertEquals(readSnapshot("grpc-status-codes.snapshot"), actual);
    }

    @Test
    void standardNamespaceMatchesSnapshot() throws IOException {
        assertEquals(
            StandardOutcomes.NAMESPACE,
            readSnapshot("standard-namespace.snapshot")
        );
    }

    private static String readSnapshot(String name) throws IOException {
        return Files.readString(Path.of("compatibility", name)).replace("\r\n", "\n").stripTrailing();
    }

    private static int readHttpStatusValue(Field field) {
        try {
            return ((HttpStatusCode) field.get(null)).getValue();
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
