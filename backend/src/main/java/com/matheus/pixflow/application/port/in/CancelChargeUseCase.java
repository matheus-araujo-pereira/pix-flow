package com.matheus.pixflow.application.port.in;

import com.matheus.pixflow.application.dto.ChargeResponse;

import java.util.UUID;

public interface CancelChargeUseCase {
    ChargeResponse cancel(UUID id);
}
