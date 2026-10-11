package com.matheus.pixflow.infrastructure.adapter.out.messaging.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.pixflow.application.dto.SettlementMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqsChargeEventPublisherTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private ObjectMapper objectMapper;

    private SqsChargeEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new SqsChargeEventPublisher(
                sqsClient,
                objectMapper,
                "https://sqs.us-east-1.amazonaws.com/123456789012/pix-settlement-queue"
        );
    }

    @Test
    @DisplayName("Should successfully publish message to SQS queue")
    void shouldPublishMessageSuccessfully() throws Exception {
        SettlementMessage msg = SettlementMessage.of(UUID.randomUUID(), "tx123", new BigDecimal("100.00"));

        when(objectMapper.writeValueAsString(msg)).thenReturn("{\"chargeId\":\"test\"}");
        when(sqsClient.sendMessage(any(SendMessageRequest.class))).thenReturn(SendMessageResponse.builder().build());

        publisher.publish(msg);

        verify(sqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    @DisplayName("Should throw runtime exception when JSON serialization fails")
    void shouldThrowOnSerializationFailure() throws Exception {
        SettlementMessage msg = SettlementMessage.of(UUID.randomUUID(), "tx123", new BigDecimal("100.00"));

        when(objectMapper.writeValueAsString(msg)).thenThrow(new JsonProcessingException("Mock error") {});

        assertThrows(RuntimeException.class, () -> publisher.publish(msg));
    }

    @Test
    @DisplayName("Should throw runtime exception when SQS client send fails")
    void shouldThrowOnSqsClientFailure() throws Exception {
        SettlementMessage msg = SettlementMessage.of(UUID.randomUUID(), "tx123", new BigDecimal("100.00"));

        when(objectMapper.writeValueAsString(msg)).thenReturn("{\"chargeId\":\"test\"}");
        when(sqsClient.sendMessage(any(SendMessageRequest.class))).thenThrow(new RuntimeException("AWS Network Timeout"));

        assertThrows(RuntimeException.class, () -> publisher.publish(msg));
    }
}

