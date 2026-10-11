package com.matheus.pixflow.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DomainExceptionTest {

    @Test
    @DisplayName("Deve testar construtores de DomainException e subclasses")
    void shouldTestDomainExceptions() {
        Throwable cause = new RuntimeException("Root cause");
        DomainException exWithCause = new DomainException("Error with cause", cause) {};

        assertEquals("Error with cause", exWithCause.getMessage());
        assertEquals(cause, exWithCause.getCause());

        DomainException exWithoutCause = new DomainException("Simple error") {};
        assertEquals("Simple error", exWithoutCause.getMessage());
        assertNull(exWithoutCause.getCause());

        UUID chargeId = UUID.randomUUID();
        ChargeNotFoundException notFound = new ChargeNotFoundException(chargeId);
        assertTrue(notFound.getMessage().contains(chargeId.toString()));

        ChargeNotFoundException notFoundTx = new ChargeNotFoundException("tx12345");
        assertTrue(notFoundTx.getMessage().contains("tx12345"));
    }
}
