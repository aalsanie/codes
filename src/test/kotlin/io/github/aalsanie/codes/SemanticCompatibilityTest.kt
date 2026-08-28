package io.github.aalsanie.codes

import io.github.aalsanie.codes.protocol.grpc.GrpcOutcomeMapper
import io.github.aalsanie.codes.protocol.http.HttpOutcomeMapper
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class SemanticCompatibilityTest {
    @Test fun standardOutcomeSemanticsMatchSnapshot() {
        val actual =
            StandardOutcomes.all
                .sortedBy { it.code.value }
                .joinToString("\n") { "${it.code.value}|${it.state.name}" }

        assertEquals(readSnapshot("standard-outcomes.snapshot"), actual)
    }

    @Test fun standardHttpMappingsMatchSnapshot() {
        val actual =
            HttpOutcomeMapper
                .standard()
                .mappings()
                .entries
                .sortedBy { it.key.value }
                .joinToString("\n") { "${it.key.value}|${it.value.value}" }

        assertEquals(readSnapshot("http-mappings.snapshot"), actual)
    }

    @Test fun standardGrpcMappingsMatchSnapshot() {
        val actual =
            GrpcOutcomeMapper
                .standard()
                .mappings()
                .entries
                .sortedBy { it.key.value }
                .joinToString("\n") { "${it.key.value}|${it.value.name}" }

        assertEquals(readSnapshot("grpc-mappings.snapshot"), actual)
    }

    private fun readSnapshot(name: String): String =
        Files
            .readString(Path.of("compatibility", name))
            .replace("\r\n", "\n")
            .trimEnd()
}
