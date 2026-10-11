package com.matheus.pixflow.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class EndToEndIdGeneratorTest {

    @Test
    @DisplayName("Should generate BACEN compliant 32-character EndToEndId")
    void shouldGenerateValidEndToEndId() {
        Instant now = Instant.now();
        String endToEndId = EndToEndIdGenerator.generate(now);

        assertNotNull(endToEndId);
        assertEquals(32, endToEndId.length(), "EndToEndId must have exactly 32 characters");
        assertTrue(endToEndId.startsWith("E00000000"), "EndToEndId must start with 'E' followed by 8-digit ISPB");

        String nullDateId = EndToEndIdGenerator.generate(null);
        assertEquals(32, nullDateId.length());
        assertTrue(nullDateId.startsWith("E00000000"));
    }
}

