package com.matheus.pixflow.domain.model;

import com.matheus.pixflow.domain.exception.BusinessValidationException;
import com.matheus.pixflow.domain.exception.InvalidStatusTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ChargeTest {

    @Test
    @DisplayName("Should successfully create a charge with PENDING status and valid BR Code")
    void shouldCreateChargeSuccessfully() {
        Charge charge = Charge.create("52998224725", PixKeyType.CPF, new BigDecimal("150.00"), "Software License");

        assertNotNull(charge.getId());
        assertNotNull(charge.getTxId());
        assertNotNull(charge.getEndToEndId());
        assertEquals("52998224725", charge.getPixKey());
        assertEquals(PixKeyType.CPF, charge.getPixKeyType());
        assertEquals(new BigDecimal("150.00"), charge.getAmount());
        assertEquals("Software License", charge.getDescription());
        assertEquals(TransactionStatus.PENDING, charge.getStatus());
        assertNotNull(charge.getQrCodePayload());
        assertTrue(charge.getQrCodePayload().contains("br.gov.bcb.pix"));
        assertTrue(charge.getQrCodePayload().contains("PixFlow Engine"));
        assertNull(charge.getFailureReason());
        assertNull(charge.getSettledAt());
        assertNotNull(charge.getCreatedAt());
        assertNotNull(charge.getUpdatedAt());
    }

    @Test
    @DisplayName("Should use default description when null or blank")
    void shouldUseDefaultDescription() {
        Charge charge = Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("10.00"), null);
        assertEquals("PixFlow Instant Transfer", charge.getDescription());

        Charge charge2 = Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("10.00"), "   ");
        assertEquals("PixFlow Instant Transfer", charge2.getDescription());
    }

    @Test
    @DisplayName("Should reject invalid amounts")
    void shouldRejectInvalidAmounts() {
        assertThrows(BusinessValidationException.class, () ->
                Charge.create("dev@pixflow.com", PixKeyType.EMAIL, null, "desc"));

        assertThrows(BusinessValidationException.class, () ->
                Charge.create("dev@pixflow.com", PixKeyType.EMAIL, BigDecimal.ZERO, "desc"));

        assertThrows(BusinessValidationException.class, () ->
                Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("-5.00"), "desc"));

        assertThrows(BusinessValidationException.class, () ->
                Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("100000000.00"), "desc"));
    }

    @Test
    @DisplayName("Should reject null key type")
    void shouldRejectNullKeyType() {
        assertThrows(BusinessValidationException.class, () ->
                Charge.create("dev@pixflow.com", null, new BigDecimal("10.00"), "desc"));
    }

    @Test
    @DisplayName("Should follow valid lifecycle transitions: PENDING -> PROCESSING -> SETTLED")
    void shouldFollowValidLifecycleToSettled() {
        Charge charge = Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("50.00"), "Test");

        charge.transitionToProcessing();
        assertEquals(TransactionStatus.PROCESSING, charge.getStatus());

        charge.settle();
        assertEquals(TransactionStatus.SETTLED, charge.getStatus());
        assertNotNull(charge.getSettledAt());
    }

    @Test
    @DisplayName("Should follow valid lifecycle transitions: PENDING -> PROCESSING -> FAILED")
    void shouldFollowValidLifecycleToFailed() {
        Charge charge = Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("50.00"), "Test");

        charge.transitionToProcessing();
        charge.fail("Rejected by mock risk policy");

        assertEquals(TransactionStatus.FAILED, charge.getStatus());
        assertEquals("Rejected by mock risk policy", charge.getFailureReason());

        Charge chargeDefaultReason = Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("50.00"), "Test");
        chargeDefaultReason.transitionToProcessing();
        chargeDefaultReason.fail(null);
        Charge chargeBlankReason = Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("50.00"), "Test");
        chargeBlankReason.transitionToProcessing();
        chargeBlankReason.fail("   ");
        assertEquals("Settlement rejected by clearing network.", chargeBlankReason.getFailureReason());
    }

    @Test
    @DisplayName("Should cancel pending charge")
    void shouldCancelPendingCharge() {
        Charge charge = Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("50.00"), "Test");
        charge.cancel();
        assertEquals(TransactionStatus.CANCELLED, charge.getStatus());
    }

    @Test
    @DisplayName("Should reject invalid state transitions")
    void shouldRejectInvalidStateTransitions() {
        Charge charge = Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("50.00"), "Test");

        // Cannot settle directly from PENDING
        assertThrows(InvalidStatusTransitionException.class, charge::settle);

        charge.transitionToProcessing();
        // Cannot cancel from PROCESSING
        assertThrows(InvalidStatusTransitionException.class, charge::cancel);

        charge.settle();
        // Cannot transition from terminal SETTLED
        assertThrows(InvalidStatusTransitionException.class, charge::transitionToProcessing);
    }

    @Test
    @DisplayName("Should restore charge from persistent state")
    void shouldRestoreCharge() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        Charge restored = Charge.restore(
                id,
                "tx12345",
                "E00000000202610111200abc123456789",
                "52998224725",
                PixKeyType.CPF,
                new BigDecimal("99.90"),
                "Restored",
                TransactionStatus.SETTLED,
                "mock-payload",
                null,
                now,
                now,
                now
        );

        assertEquals(id, restored.getId());
        assertEquals("tx12345", restored.getTxId());
        assertEquals(TransactionStatus.SETTLED, restored.getStatus());
        assertEquals(now, restored.getSettledAt());
    }
}

