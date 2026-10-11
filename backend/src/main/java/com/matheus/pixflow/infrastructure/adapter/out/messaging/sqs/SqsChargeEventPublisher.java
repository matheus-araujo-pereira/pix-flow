package com.matheus.pixflow.infrastructure.adapter.out.messaging.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.pixflow.application.dto.SettlementMessage;
import com.matheus.pixflow.application.port.out.ChargeEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
@ConditionalOnProperty(name = "pixflow.messaging.provider", havingValue = "sqs")
public class SqsChargeEventPublisher implements ChargeEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(SqsChargeEventPublisher.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String queueUrl;

    public SqsChargeEventPublisher(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            @Value("${pixflow.messaging.queue-url}") String queueUrl
    ) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.queueUrl = queueUrl;
    }

    @Override
    public void publish(SettlementMessage message) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(message);
            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(jsonPayload)
                    .build();

            sqsClient.sendMessage(request);
            log.info("Published settlement message to AWS SQS for charge: {}", message.chargeId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize settlement message to JSON: {}", message, e);
            throw new RuntimeException("Could not serialize settlement message", e);
        } catch (Exception e) {
            log.error("Failed to send message to SQS queue {}: {}", queueUrl, e.getMessage(), e);
            throw new RuntimeException("SQS publish failure", e);
        }
    }
}
