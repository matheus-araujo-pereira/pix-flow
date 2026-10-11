package com.matheus.pixflow.domain.exception;

import java.util.UUID;

public class ChargeNotFoundException extends DomainException {
    public ChargeNotFoundException(UUID id) {
        super(String.format("Charge with ID '%s' was not found.", id));
    }

    public ChargeNotFoundException(String txId) {
        super(String.format("Charge with TxID '%s' was not found.", txId));
    }
}
