package io.github.aalsanie.codes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MappingResultTest {
    @Test
    void mappedCarriesValueAndFolds() {
        MappingResult<String> result = MappingResult.mapped("ok");
        assertTrue(result.isMapped());
        assertFalse(result.isUnmapped());
        assertEquals("ok", result.orNull());
        assertEquals(2, result.fold(String::length, () -> -1));
        assertEquals(MappingResult.mapped("ok"), result);
        assertEquals(result.hashCode(), MappingResult.mapped("ok").hashCode());
        assertEquals("Mapped(value=ok)", result.toString());
        assertThrows(NullPointerException.class, () -> MappingResult.mapped(null));
        assertThrows(NullPointerException.class, () -> result.fold(null, () -> -1));
        assertThrows(NullPointerException.class, () -> result.fold(String::length, null));
        assertEquals(result, result);
        assertNotEquals("ok", result);
        assertNotEquals(result, MappingResult.mapped("different"));
    }

    @Test
    void unmappedIsSingletonLikeAndFolds() {
        MappingResult<String> result = MappingResult.unmapped();
        assertFalse(result.isMapped());
        assertTrue(result.isUnmapped());
        assertNull(result.orNull());
        assertEquals(-1, result.fold(String::length, () -> -1));
        assertEquals(MappingResult.unmapped(), result);
        assertEquals(0, result.hashCode());
        assertEquals("Unmapped", result.toString());
        assertNotEquals("Unmapped", result);
    }
}
