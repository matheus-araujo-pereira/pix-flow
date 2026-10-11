package com.matheus.pixflow.infrastructure.adapter.out.messaging.memory;

import com.matheus.pixflow.application.dto.SettlementMessage;
import com.matheus.pixflow.application.port.out.ChargeEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@ConditionalOnProperty(name = "pixflow.messaging.provider", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryChargeEventPublisher implements ChargeEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(InMemoryChargeEventPublisher.class);

    private final BlockingQueue<SettlementMessage> queue = new LinkedBlockingQueue<>();

    @Override
    public void publish(SettlementMessage message) {
        log.info("Dispatched settlement message to In-Memory Event Queue for charge: {}", message.chargeId());
        queue.offer(message);
    }

    public BlockingQueue<SettlementMessage> getQueue() {
        return queue;
    }
}
