package com.matheus.pixflow.domain.validation;

public final class CnpjValidator {

    private CnpjValidator() {}

    private static final int[] WEIGHTS_FIRST_DIGIT = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] WEIGHTS_SECOND_DIGIT = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    /**
     * Valida CNPJ com algoritmo canônico de dígitos verificadores (Módulo 11).
     * Rejeita números com todos os dígitos iguais e tamanhos diferentes de 14.
     */
    public static boolean isValid(String rawCnpj) {
        if (rawCnpj == null) {
            return false;
        }

        String cnpj = rawCnpj.replaceAll("[^0-9]", "");
        if (cnpj.length() != 14) {
            return false;
        }

        // Rejeita sequências com todos os dígitos iguais
        if (cnpj.chars().distinct().count() == 1) {
            return false;
        }

        // 1º Dígito Verificador
        int sum1 = 0;
        for (int i = 0; i < 12; i++) {
            sum1 += (cnpj.charAt(i) - '0') * WEIGHTS_FIRST_DIGIT[i];
        }
        int remainder1 = sum1 % 11;
        int digit1 = (remainder1 < 2) ? 0 : 11 - remainder1;

        if (digit1 != (cnpj.charAt(12) - '0')) {
            return false;
        }

        // 2º Dígito Verificador
        int sum2 = 0;
        for (int i = 0; i < 13; i++) {
            sum2 += (cnpj.charAt(i) - '0') * WEIGHTS_SECOND_DIGIT[i];
        }
        int remainder2 = sum2 % 11;
        int digit2 = (remainder2 < 2) ? 0 : 11 - remainder2;

        return digit2 == (cnpj.charAt(13) - '0');
    }
}
