package com.matheus.pixflow.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionStatusTest {

    @Test
    @DisplayName("Should validate permitted state transitions from PENDING")
    void shouldValidatePendingTransitions() {
        TransactionStatus status = TransactionStatus.PENDING;
        assertTrue(status.canTransitionTo(TransactionStatus.PROCESSING));
        assertTrue(status.canTransitionTo(TransactionStatus.CANCELLED));
        assertFalse(status.canTransitionTo(TransactionStatus.SETTLED));
        assertFalse(status.canTransitionTo(TransactionStatus.FAILED));
        assertFalse(status.canTransitionTo(null));
        assertFalse(status.isTerminal());
    }

    @Test
    @DisplayName("Should validate permitted state transitions from PROCESSING")
    void shouldValidateProcessingTransitions() {
        TransactionStatus status = TransactionStatus.PROCESSING;
        assertTrue(status.canTransitionTo(TransactionStatus.SETTLED));
        assertTrue(status.canTransitionTo(TransactionStatus.FAILED));
        assertFalse(status.canTransitionTo(TransactionStatus.PENDING));
        assertFalse(status.canTransitionTo(TransactionStatus.CANCELLED));
        assertFalse(status.isTerminal());
    }

    @Test
    @DisplayName("Should recognize terminal states")
    void shouldRecognizeTerminalStates() {
        assertTrue(TransactionStatus.SETTLED.isTerminal());
        assertTrue(TransactionStatus.FAILED.isTerminal());
        assertTrue(TransactionStatus.CANCELLED.isTerminal());

        assertFalse(TransactionStatus.SETTLED.canTransitionTo(TransactionStatus.PROCESSING));
        assertFalse(TransactionStatus.FAILED.canTransitionTo(TransactionStatus.SETTLED));
        assertFalse(TransactionStatus.CANCELLED.canTransitionTo(TransactionStatus.PROCESSING));
    }
}

