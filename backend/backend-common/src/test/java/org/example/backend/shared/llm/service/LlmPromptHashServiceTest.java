package org.example.backend.shared.llm.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class LlmPromptHashServiceTest {

    @Test
    void shouldHashPromptDeterministically() {
        LlmPromptHashService service = new LlmPromptHashService();

        assertEquals(service.sha256("hello"), service.sha256("hello"));
        assertNotEquals(service.sha256("hello"), service.sha256("world"));
        assertEquals(64, service.sha256("hello").length());
    }
}
