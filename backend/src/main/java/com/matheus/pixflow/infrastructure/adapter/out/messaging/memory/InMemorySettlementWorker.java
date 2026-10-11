package com.matheus.pixflow.infrastructure.adapter.out.messaging.memory;

import com.matheus.pixflow.application.dto.SettlementMessage;
import com.matheus.pixflow.application.service.SettlementEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@ConditionalOnProperty(name = "pixflow.messaging.provider", havingValue = "in-memory", matchIfMissing = true)
public class InMemorySettlementWorker implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(InMemorySettlementWorker.class);

    private final InMemoryChargeEventPublisher publisher;
    private final SettlementEngineService settlementEngineService;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService workerExecutor;

    public InMemorySettlementWorker(
            InMemoryChargeEventPublisher publisher,
            SettlementEngineService settlementEngineService
    ) {
        this.publisher = publisher;
        this.settlementEngineService = settlementEngineService;
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            log.info("Starting In-Memory Settlement Worker engine...");
            workerExecutor = Executors.newFixedThreadPool(3, r -> {
                Thread t = new Thread(r, "inmem-settlement-worker");
                t.setDaemon(true);
                return t;
            });

            for (int i = 0; i < 3; i++) {
                final int workerIdx = i + 1;
                workerExecutor.submit(() -> workerLoop("WORKER-LOCAL-0" + workerIdx));
            }
        }
    }

    private void workerLoop(String workerId) {
        log.info("Worker thread {} initialized and awaiting settlement events", workerId);
        while (running.get()) {
            try {
                SettlementMessage msg = publisher.getQueue().poll(100, TimeUnit.MILLISECONDS);
                if (msg != null) {
                    log.info("[{}] Dequeued settlement message for charge {}", workerId, msg.chargeId());
                    settlementEngineService.processSettlement(msg.chargeId(), workerId);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("[{}] Error processing transaction: {}", workerId, e.getMessage(), e);
            }
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            log.info("Stopping In-Memory Settlement Worker engine...");
            workerExecutor.shutdownNow();
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }
}

