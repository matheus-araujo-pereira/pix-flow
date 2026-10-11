package com.matheus.pixflow.domain.exception;

import com.matheus.pixflow.domain.model.TransactionStatus;

public class InvalidStatusTransitionException extends DomainException {
    public InvalidStatusTransitionException(TransactionStatus currentStatus, TransactionStatus targetStatus) {
        super(String.format("Cannot transition transaction from status %s to %s.", currentStatus, targetStatus));
    }
}
