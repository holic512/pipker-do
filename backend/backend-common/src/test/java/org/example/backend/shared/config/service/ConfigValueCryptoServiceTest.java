package org.example.backend.shared.config.service;

import org.example.backend.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigValueCryptoServiceTest {

    @Test
    void shouldEncryptAndDecryptSensitiveValue() {
        ConfigValueCryptoService service = new ConfigValueCryptoService("test-secret-for-llm-config");

        String encrypted = service.encrypt("sk-test-value");

        assertNotEquals("sk-test-value", encrypted);
        assertEquals("sk-test-value", service.decrypt(encrypted));
    }

    @Test
    void shouldRequireSecretWhenEncryptingNonEmptyValue() {
        ConfigValueCryptoService service = new ConfigValueCryptoService("");

        assertThrows(BusinessException.class, () -> service.encrypt("sk-test-value"));
    }
}
