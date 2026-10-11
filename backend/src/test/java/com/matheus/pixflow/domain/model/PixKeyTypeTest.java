package com.matheus.pixflow.domain.model;

import com.matheus.pixflow.domain.exception.BusinessValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PixKeyTypeTest {

    @Test
    @DisplayName("Should validate and sanitize valid CPF")
    void shouldValidateAndSanitizeCpf() {
        PixKeyType type = PixKeyType.CPF;
        assertDoesNotThrow(() -> type.validate("123.456.789-09"));
        assertEquals("12345678909", type.sanitizeKey("123.456.789-09"));
    }

    @Test
    @DisplayName("Should fail validation on invalid CPF format")
    void shouldFailOnInvalidCpf() {
        PixKeyType type = PixKeyType.CPF;
        assertThrows(BusinessValidationException.class, () -> type.validate("12345"));
    }

    @Test
    @DisplayName("Should validate and sanitize valid CNPJ")
    void shouldValidateAndSanitizeCnpj() {
        PixKeyType type = PixKeyType.CNPJ;
        assertDoesNotThrow(() -> type.validate("12.345.678/0001-90"));
        assertEquals("12345678000190", type.sanitizeKey("12.345.678/0001-90"));
    }

    @Test
    @DisplayName("Should validate and sanitize valid EMAIL")
    void shouldValidateAndSanitizeEmail() {
        PixKeyType type = PixKeyType.EMAIL;
        assertDoesNotThrow(() -> type.validate("developer@pixflow.com.br"));
        assertEquals("developer@pixflow.com.br", type.sanitizeKey("  developer@pixflow.com.br "));
    }

    @Test
    @DisplayName("Should validate and sanitize valid PHONE")
    void shouldValidateAndSanitizePhone() {
        PixKeyType type = PixKeyType.PHONE;
        assertDoesNotThrow(() -> type.validate("11987654321"));
        assertEquals("+5511987654321", type.sanitizeKey("11987654321"));

        assertDoesNotThrow(() -> type.validate("+5511987654321"));
        assertEquals("+5511987654321", type.sanitizeKey("+5511987654321"));
    }

    @Test
    @DisplayName("Should validate valid RANDOM EVP key")
    void shouldValidateRandomEvp() {
        PixKeyType type = PixKeyType.RANDOM;
        String evp = "123e4567-e89b-12d3-a456-426614174000";
        assertDoesNotThrow(() -> type.validate(evp));
        assertEquals(evp, type.sanitizeKey(evp));
    }

    @ParameterizedTest
    @CsvSource({
            "'', CPF",
            "'   ', EMAIL",
            ", PHONE"
    })
    @DisplayName("Should reject null or empty keys")
    void shouldRejectNullOrEmptyKeys(String key, PixKeyType type) {
        assertThrows(BusinessValidationException.class, () -> type.validate(key));
    }
}

