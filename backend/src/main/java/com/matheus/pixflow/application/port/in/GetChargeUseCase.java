package com.matheus.pixflow.application.port.in;

import com.matheus.pixflow.application.dto.ChargeResponse;
import com.matheus.pixflow.application.dto.PageResponse;

import java.util.UUID;

public interface GetChargeUseCase {
    ChargeResponse getById(UUID id);
    ChargeResponse getByTxId(String txId);
    PageResponse<ChargeResponse> listAll(int page, int size);
}
