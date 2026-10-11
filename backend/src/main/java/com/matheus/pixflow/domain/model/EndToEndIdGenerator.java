package com.matheus.pixflow.domain.model;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class EndToEndIdGenerator {

    private static final String DEFAULT_ISPB = "00000000"; // Central Bank default mock
    private static final String ALPHANUMERIC = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
            .withZone(ZoneOffset.UTC);

    private EndToEndIdGenerator() {}

    /**
     * Generates standard BACEN PIX EndToEndId (32 chars):
     * Format: 'E' + ISPB (8) + YYYYMMDDHHmm (12) + Random Alphanumeric (11)
     */
    public static String generate(Instant timestamp) {
        String formattedDate = DATE_FORMATTER.format(timestamp != null ? timestamp : Instant.now());
        StringBuilder sb = new StringBuilder(32);
        sb.append('E');
        sb.append(DEFAULT_ISPB);
        sb.append(formattedDate);

        for (int i = 0; i < 11; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }
}
