package com.matheus.pixflow.application.port.in;

import com.matheus.pixflow.application.dto.ChargeResponse;

import java.util.UUID;

public interface SettleChargeUseCase {
    ChargeResponse triggerSettlement(UUID id);
}

