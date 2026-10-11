package com.matheus.pixflow.infrastructure.adapter.in.web;

import com.matheus.pixflow.domain.exception.BusinessValidationException;
import com.matheus.pixflow.domain.exception.ChargeNotFoundException;
import com.matheus.pixflow.domain.exception.InvalidStatusTransitionException;
import com.matheus.pixflow.domain.model.TransactionStatus;
import com.matheus.pixflow.infrastructure.adapter.in.web.dto.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/charges");

    @Test
    @DisplayName("Deve tratar ChargeNotFoundException com 404 NOT_FOUND")
    void shouldHandleChargeNotFound() {
        ChargeNotFoundException ex = new ChargeNotFoundException(UUID.randomUUID());
        ResponseEntity<ErrorResponse> response = handler.handleChargeNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
    }

    @Test
    @DisplayName("Deve tratar BusinessValidationException com 422 UNPROCESSABLE_ENTITY")
    void shouldHandleBusinessValidation() {
        BusinessValidationException ex = new BusinessValidationException("CPF inválido");
        ResponseEntity<ErrorResponse> response = handler.handleBusinessValidation(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CPF inválido", response.getBody().message());
    }

    @Test
    @DisplayName("Deve tratar InvalidStatusTransitionException com 409 CONFLICT")
    void shouldHandleInvalidTransition() {
        InvalidStatusTransitionException ex = new InvalidStatusTransitionException(TransactionStatus.SETTLED, TransactionStatus.PROCESSING);
        ResponseEntity<ErrorResponse> response = handler.handleInvalidTransition(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentNotValidException com 400 BAD_REQUEST")
    void shouldHandleValidationErrors() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "amount", "must be positive"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("must be positive", response.getBody().fieldErrors().get("amount"));
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException com 400 BAD_REQUEST")
    void shouldHandleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Parâmetro inválido");
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Parâmetro inválido", response.getBody().message());
    }

    @Test
    @DisplayName("Deve tratar Exception genérica com 500 INTERNAL_SERVER_ERROR")
    void shouldHandleGeneralException() {
        RuntimeException ex = new RuntimeException("Erro inesperado no banco");
        ResponseEntity<ErrorResponse> response = handler.handleGeneralException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected internal error occurred. Please contact system support.", response.getBody().message());
    }
}
