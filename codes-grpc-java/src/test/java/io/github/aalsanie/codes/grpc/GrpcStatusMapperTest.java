package io.github.aalsanie.codes.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.aalsanie.codes.OutcomeDefinition;
import io.github.aalsanie.codes.OutcomeState;
import io.github.aalsanie.codes.StandardOutcomes;
import io.github.aalsanie.codes.protocol.grpc.GrpcOutcomeMapper;
import io.github.aalsanie.codes.protocol.grpc.GrpcStatusCode;
import io.grpc.Status;
import org.junit.jupiter.api.Test;

class GrpcStatusMapperTest {
    @Test
    void mapsCoreGrpcStatusToGrpcJava() {
        assertEquals(Status.Code.NOT_FOUND, GrpcStatusMapper.standard().map(StandardOutcomes.NOT_FOUND).orNull().getCode());
    }

    @Test
    void preservesApplicationMappingPolicy() {
        OutcomeDefinition rejected = OutcomeDefinition.custom(
            "com.example.orders", "ORDER_REJECTED", OutcomeState.FAILED, "Rejected."
        );
        GrpcOutcomeMapper core = GrpcOutcomeMapper.standard().withMapping(rejected, GrpcStatusCode.FAILED_PRECONDITION);
        assertEquals(Status.Code.FAILED_PRECONDITION, new GrpcStatusMapper(core).map(rejected).orNull().getCode());
        assertTrue(GrpcStatusMapper.standard().map(rejected).isUnmapped());
    }
}
