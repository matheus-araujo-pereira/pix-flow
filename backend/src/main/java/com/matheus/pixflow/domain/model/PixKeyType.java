package com.matheus.pixflow.domain.model;

import com.matheus.pixflow.domain.exception.BusinessValidationException;
import com.matheus.pixflow.domain.validation.CnpjValidator;
import com.matheus.pixflow.domain.validation.CpfValidator;

import java.util.regex.Pattern;

public enum PixKeyType {
    CPF("^[0-9]{11}$"),
    CNPJ("^[0-9]{14}$"),
    EMAIL("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"),
    PHONE("^\\+55[1-9]{2}9?[0-9]{8}$"),
    RANDOM("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private final Pattern pattern;

    PixKeyType(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public void validate(String key) {
        if (key == null || key.isBlank()) {
            throw new BusinessValidationException("A chave PIX não pode ser nula ou vazia.");
        }
        String sanitizedKey = sanitizeKey(key);

        if (!pattern.matcher(sanitizedKey).matches()) {
            throw new BusinessValidationException(
                String.format("Formato da chave PIX '%s' é inválido para o tipo %s.", key, this.name())
            );
        }

        // Validação matemática canônica de dígitos verificadores
        if (this == CPF && !CpfValidator.isValid(sanitizedKey)) {
            throw new BusinessValidationException(
                String.format("O CPF '%s' possui dígitos verificadores matematicamente inválidos.", key)
            );
        }

        if (this == CNPJ && !CnpjValidator.isValid(sanitizedKey)) {
            throw new BusinessValidationException(
                String.format("O CNPJ '%s' possui dígitos verificadores matematicamente inválidos.", key)
            );
        }
    }

    public String sanitizeKey(String key) {
        if (key == null) return null;
        if (this == CPF || this == CNPJ) {
            return key.replaceAll("[^0-9]", "");
        }
        if (this == PHONE) {
            String digits = key.replaceAll("[^0-9]", "");
            return digits.startsWith("55") ? "+" + digits : "+55" + digits;
        }
        return key.trim();
    }
}
