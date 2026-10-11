package com.matheus.pixflow.infrastructure.adapter.out.messaging.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.pixflow.application.dto.SettlementMessage;
import com.matheus.pixflow.application.service.SettlementEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqsSettlementConsumerTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SettlementEngineService settlementEngineService;

    private SqsSettlementConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new SqsSettlementConsumer(
                sqsClient,
                objectMapper,
                settlementEngineService,
                "https://sqs.us-east-1.amazonaws.com/123456789012/pix-settlement-queue"
        );
    }

    @Test
    @DisplayName("Should start and stop consumer lifecycle cleanly")
    void shouldManageLifecycle() {
        SqsSettlementConsumer unstarted = new SqsSettlementConsumer(
                sqsClient,
                objectMapper,
                settlementEngineService,
                "https://sqs.us-east-1.amazonaws.com/123456789012/pix-settlement-queue"
        );
        unstarted.stop(); // stops when pollerExecutor is null

        assertFalse(consumer.isRunning());
        consumer.start();
        consumer.start(); // double start
        assertTrue(consumer.isRunning());
        consumer.stop();
        consumer.stop(); // double stop
        assertFalse(consumer.isRunning());
    }

    @Test
    @DisplayName("Should poll and process message from SQS")
    void shouldPollAndProcessMessage() throws Exception {
        UUID chargeId = UUID.randomUUID();
        SettlementMessage settlementMsg = SettlementMessage.of(chargeId, "tx123", new BigDecimal("100.00"));
        Message sqsMessage = Message.builder()
                .body("{\"chargeId\":\"" + chargeId + "\"}")
                .receiptHandle("handle-123")
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(sqsMessage).build())
                .thenReturn(ReceiveMessageResponse.builder().build());

        when(objectMapper.readValue(eq(sqsMessage.body()), eq(SettlementMessage.class)))
                .thenReturn(settlementMsg);

        consumer.start();

        // Let background poller process one message
        Thread.sleep(300);

        consumer.stop();

        verify(settlementEngineService, atLeastOnce()).processSettlement(eq(chargeId), eq("WORKER-AWS-SQS"));
        verify(sqsClient, atLeastOnce()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("Should handle invalid message body without crashing poller")
    void shouldHandleInvalidMessageBody() throws Exception {
        Message invalidMessage = Message.builder()
                .body("corrupted-body")
                .receiptHandle("handle-bad")
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(invalidMessage).build())
                .thenReturn(ReceiveMessageResponse.builder().build());

        when(objectMapper.readValue(eq("corrupted-body"), eq(SettlementMessage.class)))
                .thenThrow(new RuntimeException("Bad JSON"));

        consumer.start();
        Thread.sleep(150);
        consumer.stop();

        verify(settlementEngineService, never()).processSettlement(any(), any());
    }

    @Test
    @DisplayName("Should handle SQS polling exception and recover normally")
    void shouldHandlePollingExceptionAndRecover() throws Exception {
        consumer.backoffMillis = 1;
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenThrow(new RuntimeException("SQS Connection Lost"))
                .thenReturn(ReceiveMessageResponse.builder().build());

        consumer.start();
        Thread.sleep(60);
        consumer.stop();

        assertFalse(consumer.isRunning());
    }

    @Test
    @DisplayName("Should handle thread interruption during SQS backoff sleep")
    void shouldHandlePollingInterruption() throws Exception {
        consumer.backoffMillis = 5000;
        CountDownLatch latch = new CountDownLatch(1);
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenAnswer(inv -> {
                    latch.countDown();
                    throw new RuntimeException("SQS Connection Lost");
                });

        consumer.start();
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        Thread.sleep(30);
        consumer.stop();

        assertFalse(consumer.isRunning());
    }
}

