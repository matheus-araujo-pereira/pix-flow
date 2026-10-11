package com.matheus.pixflow.domain.model;

import com.matheus.pixflow.domain.exception.BusinessValidationException;

import java.util.regex.Pattern;

public enum PixKeyType {
    CPF("^[0-9]{11}$"),
    CNPJ("^[0-9]{14}$"),
    EMAIL("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"),
    PHONE("^\\+?[1-9][0-9]{10,13}$"),
    RANDOM("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private final Pattern pattern;

    PixKeyType(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public void validate(String key) {
        if (key == null || key.isBlank()) {
            throw new BusinessValidationException("PIX key cannot be null or empty.");
        }
        String sanitizedKey = sanitizeKey(key);
        if (!pattern.matcher(sanitizedKey).matches()) {
            throw new BusinessValidationException(
                String.format("Invalid PIX key format '%s' for type %s.", key, this.name())
            );
        }
    }

    public String sanitizeKey(String key) {
        if (key == null) return null;
        if (this == CPF || this == CNPJ) {
            return key.replaceAll("[^0-9]", "");
        }
        if (this == PHONE) {
            String digits = key.replaceAll("[^0-9+]", "");
            return digits.startsWith("+") ? digits : "+55" + digits;
        }
        return key.trim();
    }
}

