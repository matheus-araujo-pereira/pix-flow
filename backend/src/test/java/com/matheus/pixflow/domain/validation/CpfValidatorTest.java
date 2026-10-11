package com.matheus.pixflow.domain.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CpfValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "52998224725",
            "11144477735",
            "00000000191",
            "529.982.247-25"
    })
    @DisplayName("Deve validar CPFs com dígitos verificadores matematicamente válidos")
    void shouldValidateValidCpfs(String validCpf) {
        assertTrue(CpfValidator.isValid(validCpf));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "00000000000",
            "11111111111",
            "22222222222",
            "99999999999",
            "12345678900", // Dígitos verificadores incorretos
            "12345678911", // Dígitos verificadores incorretos
            "12345",       // Curto
            "",            // Vazio
            "abcdefghijk"
    })
    @DisplayName("Deve rejeitar CPFs inválidos ou com sequências repetidas")
    void shouldRejectInvalidCpfs(String invalidCpf) {
        assertFalse(CpfValidator.isValid(invalidCpf));
    }

    @Test
    @DisplayName("Deve rejeitar CPF nulo")
    void shouldRejectNullCpf() {
        assertFalse(CpfValidator.isValid(null));
    }
}
