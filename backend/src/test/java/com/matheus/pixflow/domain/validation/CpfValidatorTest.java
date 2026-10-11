package com.matheus.pixflow.domain.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class CpfValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "52998224725",
            "11144477735",
            "00000000191",
            "529.982.247-25",
            "10000000280", // remainder2 < 2
            "10000000604"  // remainder1 < 2
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
            "12345678900", // 1º DV incorreto
            "12345678911", // 1º DV incorreto
            "10000000109", // 2º DV incorreto (1º DV é 0, mas 2º DV deveria ser 8)
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

    @Test
    @DisplayName("Deve cobrir construtor privado")
    void shouldCoverPrivateConstructor() throws Exception {
        Constructor<CpfValidator> constructor = CpfValidator.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }
}
