package com.matheus.pixflow.application.port.in;

import com.matheus.pixflow.application.dto.ChargeResponse;
import com.matheus.pixflow.application.dto.CreateChargeRequest;

public interface CreateChargeUseCase {
    ChargeResponse create(CreateChargeRequest request);
}
