package com.matheus.pixflow.domain.validation;

public final class CnpjValidator {

    private static final int[] WEIGHTS_FIRST = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] WEIGHTS_SECOND = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private CnpjValidator() {}

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

        // Rejeita sequências repetidas (ex: 00000000000000, 11111111111111)
        if (cnpj.chars().distinct().count() == 1) {
            return false;
        }

        try {
            // 1º Dígito Verificador
            int sum1 = 0;
            for (int i = 0; i < 12; i++) {
                sum1 += (cnpj.charAt(i) - '0') * WEIGHTS_FIRST[i];
            }
            int remainder1 = sum1 % 11;
            int digit1 = (remainder1 < 2) ? 0 : 11 - remainder1;

            if (digit1 != (cnpj.charAt(12) - '0')) {
                return false;
            }

            // 2º Dígito Verificador
            int sum2 = 0;
            for (int i = 0; i < 13; i++) {
                sum2 += (cnpj.charAt(i) - '0') * WEIGHTS_SECOND[i];
            }
            int remainder2 = sum2 % 11;
            int digit2 = (remainder2 < 2) ? 0 : 11 - remainder2;

            return digit2 == (cnpj.charAt(13) - '0');
        } catch (Exception e) {
            return false;
        }
    }
}
