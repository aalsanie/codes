package io.github.aalsanie.codes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.aalsanie.codes.protocol.grpc.GrpcOutcomeMapper;
import io.github.aalsanie.codes.protocol.http.HttpOutcomeMapper;
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

    private static String readSnapshot(String name) throws IOException {
        return Files.readString(Path.of("compatibility", name)).replace("\r\n", "\n").stripTrailing();
    }
}
