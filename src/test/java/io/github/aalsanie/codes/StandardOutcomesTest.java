package io.github.aalsanie.codes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import org.junit.jupiter.api.Test;

class StandardOutcomesTest {
    @Test
    void standardCatalogHasStableShape() {
        assertEquals(17, StandardOutcomes.all.size());
        assertEquals(17, new HashSet<>(StandardOutcomes.all.stream().map(OutcomeDefinition::getCode).toList()).size());
        assertSame(StandardOutcomes.OK, StandardOutcomes.all.get(0));
        assertEquals(OutcomeState.SUCCEEDED, StandardOutcomes.OK.getState());
        assertTrue(
            StandardOutcomes.all.stream()
                .filter(definition -> definition != StandardOutcomes.OK)
                .allMatch(definition -> definition.getState() == OutcomeState.FAILED)
        );
        assertTrue(StandardOutcomes.all.stream().allMatch(definition -> definition.getCode().getNamespace().equals(StandardOutcomes.NAMESPACE)));
    }
}
