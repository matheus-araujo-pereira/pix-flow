package com.matheus.pixflow.domain.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CnpjValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "04252011000110",
            "11222333000181",
            "04.252.011/0001-10",
            "11.222.333/0001-81"
    })
    @DisplayName("Deve validar CNPJs com dígitos verificadores matematicamente válidos")
    void shouldValidateValidCnpjs(String validCnpj) {
        assertTrue(CnpjValidator.isValid(validCnpj));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "00000000000000",
            "11111111111111",
            "12345678000190", // Dígitos verificadores incorretos
            "12345",          // Curto
            "",
            "abcdefghijklmn"
    })
    @DisplayName("Deve rejeitar CNPJs inválidos ou com sequências repetidas")
    void shouldRejectInvalidCnpjs(String invalidCnpj) {
        assertFalse(CnpjValidator.isValid(invalidCnpj));
    }

    @Test
    @DisplayName("Deve rejeitar CNPJ nulo")
    void shouldRejectNullCnpj() {
        assertFalse(CnpjValidator.isValid(null));
    }
}
