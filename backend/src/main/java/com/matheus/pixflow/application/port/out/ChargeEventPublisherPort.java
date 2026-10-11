package com.matheus.pixflow.application.port.out;

import com.matheus.pixflow.application.dto.SettlementMessage;

public interface ChargeEventPublisherPort {
    void publish(SettlementMessage message);
}

