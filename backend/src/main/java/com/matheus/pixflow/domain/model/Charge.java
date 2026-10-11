package com.matheus.pixflow.domain.model;

import com.matheus.pixflow.domain.exception.BusinessValidationException;
import com.matheus.pixflow.domain.exception.InvalidStatusTransitionException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Charge {

    private final UUID id;
    private final String txId;
    private final String endToEndId;
    private final String pixKey;
    private final PixKeyType pixKeyType;
    private final BigDecimal amount;
    private final String description;
    private TransactionStatus status;
    private final String qrCodePayload;
    private String failureReason;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant settledAt;

    private Charge(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "ID cannot be null");
        this.txId = Objects.requireNonNull(builder.txId, "TxID cannot be null");
        this.endToEndId = Objects.requireNonNull(builder.endToEndId, "EndToEndId cannot be null");
        this.pixKey = Objects.requireNonNull(builder.pixKey, "PixKey cannot be null");
        this.pixKeyType = Objects.requireNonNull(builder.pixKeyType, "PixKeyType cannot be null");
        this.amount = Objects.requireNonNull(builder.amount, "Amount cannot be null");
        this.description = builder.description;
        this.status = Objects.requireNonNull(builder.status, "Status cannot be null");
        this.qrCodePayload = builder.qrCodePayload;
        this.failureReason = builder.failureReason;
        this.createdAt = Objects.requireNonNull(builder.createdAt, "CreatedAt cannot be null");
        this.updatedAt = Objects.requireNonNull(builder.updatedAt, "UpdatedAt cannot be null");
        this.settledAt = builder.settledAt;
    }

    public static Charge create(String rawPixKey, PixKeyType keyType, BigDecimal rawAmount, String rawDescription) {
        if (keyType == null) {
            throw new BusinessValidationException("PIX key type is required.");
        }
        if (rawAmount == null || rawAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("Transaction amount must be strictly greater than zero.");
        }
        if (rawAmount.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new BusinessValidationException("Transaction amount exceeds maximum permissible limit.");
        }

        keyType.validate(rawPixKey);
        String sanitizedKey = keyType.sanitizeKey(rawPixKey);
        BigDecimal standardizedAmount = rawAmount.setScale(2, RoundingMode.HALF_UP);

        UUID generatedId = UUID.randomUUID();
        Instant now = Instant.now();
        String generatedTxId = "tx" + generatedId.toString().replace("-", "").substring(0, 24);
        String generatedEndToEndId = EndToEndIdGenerator.generate(now);

        String description = (rawDescription != null && !rawDescription.isBlank())
                ? rawDescription.trim()
                : "PixFlow Instant Transfer";

        String brCode = generateBrCode(sanitizedKey, standardizedAmount, generatedTxId, description);

        return new Builder()
                .id(generatedId)
                .txId(generatedTxId)
                .endToEndId(generatedEndToEndId)
                .pixKey(sanitizedKey)
                .pixKeyType(keyType)
                .amount(standardizedAmount)
                .description(description)
                .status(TransactionStatus.PENDING)
                .qrCodePayload(brCode)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static Charge restore(
            UUID id,
            String txId,
            String endToEndId,
            String pixKey,
            PixKeyType pixKeyType,
            BigDecimal amount,
            String description,
            TransactionStatus status,
            String qrCodePayload,
            String failureReason,
            Instant createdAt,
            Instant updatedAt,
            Instant settledAt
    ) {
        return new Builder()
                .id(id)
                .txId(txId)
                .endToEndId(endToEndId)
                .pixKey(pixKey)
                .pixKeyType(pixKeyType)
                .amount(amount)
                .description(description)
                .status(status)
                .qrCodePayload(qrCodePayload)
                .failureReason(failureReason)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .settledAt(settledAt)
                .build();
    }

    public void transitionToProcessing() {
        validateTransition(TransactionStatus.PROCESSING);
        this.status = TransactionStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }

    public void settle() {
        validateTransition(TransactionStatus.SETTLED);
        this.status = TransactionStatus.SETTLED;
        Instant now = Instant.now();
        this.settledAt = now;
        this.updatedAt = now;
    }

    public void fail(String reason) {
        validateTransition(TransactionStatus.FAILED);
        this.status = TransactionStatus.FAILED;
        this.failureReason = (reason != null && !reason.isBlank()) ? reason : "Settlement rejected by clearing network.";
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        validateTransition(TransactionStatus.CANCELLED);
        this.status = TransactionStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    private void validateTransition(TransactionStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new InvalidStatusTransitionException(this.status, target);
        }
    }

    /**
     * Standard EMVCo / BR Code TLV (Tag-Length-Value) Generator for Central Bank of Brazil PIX
     */
    public static String generateBrCode(String pixKey, BigDecimal amount, String txId, String description) {
        String merchantAccount = formatTlv("00", "br.gov.bcb.pix") + formatTlv("01", pixKey);
        String additionalData = formatTlv("05", txId);

        StringBuilder sb = new StringBuilder();
        sb.append(formatTlv("00", "01")); // Format indicator
        sb.append(formatTlv("26", merchantAccount)); // Pix Merchant Account Info
        sb.append(formatTlv("52", "0000")); // Merchant Category Code
        sb.append(formatTlv("53", "986")); // BRL Currency Code (ISO 4217)
        sb.append(formatTlv("54", amount.toPlainString())); // Amount
        sb.append(formatTlv("58", "BR")); // Country code
        sb.append(formatTlv("59", "PixFlow Engine")); // Merchant Name
        sb.append(formatTlv("60", "BRASILIA")); // Merchant City
        sb.append(formatTlv("62", additionalData)); // Additional Data (TxID)
        sb.append("6304"); // Tag 63 with length 4 for CRC16

        String crc = calculateCrc16Ccitt(sb.toString());
        return sb.toString() + crc;
    }

    private static String formatTlv(String tag, String value) {
        String length = String.format("%02d", value.length());
        return tag + length + value;
    }

    private static String calculateCrc16Ccitt(String payload) {
        int crc = 0xFFFF;
        int polynomial = 0x1021;
        byte[] bytes = payload.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }
        crc &= 0xFFFF;
        return String.format("%04X", crc);
    }

    // Getters
    public UUID getId() { return id; }
    public String getTxId() { return txId; }
    public String getEndToEndId() { return endToEndId; }
    public String getPixKey() { return pixKey; }
    public PixKeyType getPixKeyType() { return pixKeyType; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public TransactionStatus getStatus() { return status; }
    public String getQrCodePayload() { return qrCodePayload; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getSettledAt() { return settledAt; }

    public static class Builder {
        private UUID id;
        private String txId;
        private String endToEndId;
        private String pixKey;
        private PixKeyType pixKeyType;
        private BigDecimal amount;
        private String description;
        private TransactionStatus status;
        private String qrCodePayload;
        private String failureReason;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant settledAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder txId(String txId) { this.txId = txId; return this; }
        public Builder endToEndId(String endToEndId) { this.endToEndId = endToEndId; return this; }
        public Builder pixKey(String pixKey) { this.pixKey = pixKey; return this; }
        public Builder pixKeyType(PixKeyType pixKeyType) { this.pixKeyType = pixKeyType; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder status(TransactionStatus status) { this.status = status; return this; }
        public Builder qrCodePayload(String qrCodePayload) { this.qrCodePayload = qrCodePayload; return this; }
        public Builder failureReason(String failureReason) { this.failureReason = failureReason; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder settledAt(Instant settledAt) { this.settledAt = settledAt; return this; }

        public Charge build() {
            return new Charge(this);
        }
    }
}
