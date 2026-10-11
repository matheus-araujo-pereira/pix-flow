package com.matheus.pixflow.infrastructure.adapter.out.messaging.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.pixflow.application.dto.SettlementMessage;
import com.matheus.pixflow.application.service.SettlementEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@ConditionalOnProperty(name = "pixflow.messaging.provider", havingValue = "sqs")
public class SqsSettlementConsumer implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(SqsSettlementConsumer.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final SettlementEngineService settlementEngineService;
    private final String queueUrl;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService pollerExecutor;
    long backoffMillis = 1000;

    public SqsSettlementConsumer(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            SettlementEngineService settlementEngineService,
            @Value("${pixflow.messaging.queue-url}") String queueUrl
    ) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.settlementEngineService = settlementEngineService;
        this.queueUrl = queueUrl;
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            log.info("Starting SQS Settlement Consumer poller for queue: {}", queueUrl);
            pollerExecutor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "sqs-settlement-poller");
                t.setDaemon(true);
                return t;
            });
            pollerExecutor.submit(this::pollLoop);
        }
    }

    private void pollLoop() {
        while (running.get()) {
            try {
                ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .maxNumberOfMessages(5)
                        .waitTimeSeconds(5) // SQS Long Polling
                        .build();

                List<Message> messages = sqsClient.receiveMessage(request).messages();
                for (Message msg : messages) {
                    processMessage(msg);
                }
            } catch (Exception e) {
                log.error("Error polling SQS queue: {}. Backing off...", e.getMessage());
                try {
                    Thread.sleep(backoffMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void processMessage(Message msg) {
        try {
            SettlementMessage settlementMessage = objectMapper.readValue(msg.body(), SettlementMessage.class);
            log.info("Processing SQS settlement message for charge: {}", settlementMessage.chargeId());

            settlementEngineService.processSettlement(settlementMessage.chargeId(), "WORKER-AWS-SQS");

            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(msg.receiptHandle())
                    .build());
            log.info("Deleted processed message from SQS for charge: {}", settlementMessage.chargeId());
        } catch (Exception e) {
            log.error("Failed to process message from SQS: {}", msg.body(), e);
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            log.info("Stopping SQS Settlement Consumer poller...");
            pollerExecutor.shutdownNow();
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }
}
