package com.matheus.pixflow.domain.validation;

public final class CpfValidator {

    private CpfValidator() {}

    /**
     * Valida CPF com algoritmo canônico de dígitos verificadores (Módulo 11).
     * Rejeita números com todos os dígitos iguais e tamanhos diferentes de 11.
     */
    public static boolean isValid(String rawCpf) {
        if (rawCpf == null) {
            return false;
        }

        String cpf = rawCpf.replaceAll("[^0-9]", "");
        if (cpf.length() != 11) {
            return false;
        }

        // Rejeita CPFs com todos os dígitos iguais (ex: 00000000000, 11111111111)
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        // 1º Dígito Verificador (pesos de 10 a 2)
        int sum1 = 0;
        for (int i = 0; i < 9; i++) {
            sum1 += (cpf.charAt(i) - '0') * (10 - i);
        }
        int remainder1 = sum1 % 11;
        int digit1 = (remainder1 < 2) ? 0 : 11 - remainder1;

        if (digit1 != (cpf.charAt(9) - '0')) {
            return false;
        }

        // 2º Dígito Verificador (pesos de 11 a 2)
        int sum2 = 0;
        for (int i = 0; i < 10; i++) {
            sum2 += (cpf.charAt(i) - '0') * (11 - i);
        }
        int remainder2 = sum2 % 11;
        int digit2 = (remainder2 < 2) ? 0 : 11 - remainder2;

        return digit2 == (cpf.charAt(10) - '0');
    }
}
