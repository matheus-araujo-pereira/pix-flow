package com.matheus.pixflow.domain.model;

import com.matheus.pixflow.domain.exception.BusinessValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PixKeyTypeTest {

    @Test
    @DisplayName("Should validate and sanitize valid CPF with correct check digits")
    void shouldValidateAndSanitizeCpf() {
        PixKeyType type = PixKeyType.CPF;
        assertDoesNotThrow(() -> type.validate("529.982.247-25"));
        assertEquals("52998224725", type.sanitizeKey("529.982.247-25"));
    }

    @Test
    @DisplayName("Should fail validation on invalid CPF check digits")
    void shouldFailOnInvalidCpfCheckDigits() {
        PixKeyType type = PixKeyType.CPF;
        assertThrows(BusinessValidationException.class, () -> type.validate("123.456.789-11"));
    }

    @Test
    @DisplayName("Should fail validation on invalid CPF length")
    void shouldFailOnInvalidCpf() {
        PixKeyType type = PixKeyType.CPF;
        assertThrows(BusinessValidationException.class, () -> type.validate("12345"));
    }

    @Test
    @DisplayName("Should validate and sanitize valid CNPJ with correct check digits")
    void shouldValidateAndSanitizeCnpj() {
        PixKeyType type = PixKeyType.CNPJ;
        assertDoesNotThrow(() -> type.validate("04.252.011/0001-10"));
        assertEquals("04252011000110", type.sanitizeKey("04.252.011/0001-10"));
    }

    @Test
    @DisplayName("Should fail validation on invalid CNPJ check digits")
    void shouldFailOnInvalidCnpjCheckDigits() {
        PixKeyType type = PixKeyType.CNPJ;
        assertThrows(BusinessValidationException.class, () -> type.validate("12.345.678/0001-90"));
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
