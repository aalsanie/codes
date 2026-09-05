package io.github.aalsanie.codes.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.aalsanie.codes.testing.PublicApiSnapshot;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ApiSnapshotTest {
    @Test
    void publicApiMatchesSnapshot() throws IOException {
        String expected = Files.readString(Path.of(System.getProperty("codes.apiSnapshot")))
            .replace("\r\n", "\n")
            .stripTrailing();

        assertEquals(
            expected,
            PublicApiSnapshot.create(GoogleRpcOutcomeMapper.class, "io.github.aalsanie.codes.grpc")
        );
    }
}
