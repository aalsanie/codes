package io.github.aalsanie.codes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ApiSnapshotTest {
    @Test
    void publicApiMatchesSnapshot() throws IOException {
        String expected = Files.readString(Path.of("api", "codes.api")).replace("\r\n", "\n").stripTrailing();
        assertEquals(expected, ApiSnapshot.create());
    }
}
