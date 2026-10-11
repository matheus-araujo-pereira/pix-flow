/**
 * Canonical Brazilian Payment & Identifier Validators (Modulo 11 algorithms)
 * Matches backend validation rules exactly.
 */

export function sanitizeDigits(value) {
  return (value || '').replace(/\D/g, '');
}

export function validateCpf(rawCpf) {
  if (!rawCpf) return { valid: false, error: 'CPF é obrigatório', formatted: '' };

  const digits = sanitizeDigits(rawCpf);
  if (digits.length !== 11) {
    return { valid: false, error: 'CPF deve conter exatamente 11 dígitos', formatted: digits };
  }

  // Reject sequence of identical digits
  if (/^(\d)\1{10}$/.test(digits)) {
    return { valid: false, error: 'CPF inválido (todos os dígitos são idênticos)', formatted: digits };
  }

  // First check digit (weights 10 to 2)
  let sum1 = 0;
  for (let i = 0; i < 9; i++) {
    sum1 += parseInt(digits.charAt(i), 10) * (10 - i);
  }
  const rem1 = sum1 % 11;
  const digit1 = rem1 < 2 ? 0 : 11 - rem1;
  if (digit1 !== parseInt(digits.charAt(9), 10)) {
    return { valid: false, error: '1º Dígito verificador incorreto (Módulo 11)', formatted: digits };
  }

  // Second check digit (weights 11 to 2)
  let sum2 = 0;
  for (let i = 0; i < 10; i++) {
    sum2 += parseInt(digits.charAt(i), 10) * (11 - i);
  }
  const rem2 = sum2 % 11;
  const digit2 = rem2 < 2 ? 0 : 11 - rem2;
  if (digit2 !== parseInt(digits.charAt(10), 10)) {
    return { valid: false, error: '2º Dígito verificador incorreto (Módulo 11)', formatted: digits };
  }

  const formatted = digits.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  return { valid: true, error: null, formatted };
}

export function validateCnpj(rawCnpj) {
  if (!rawCnpj) return { valid: false, error: 'CNPJ é obrigatório', formatted: '' };

  const digits = sanitizeDigits(rawCnpj);
  if (digits.length !== 14) {
    return { valid: false, error: 'CNPJ deve conter exatamente 14 dígitos', formatted: digits };
  }

  // Reject sequence of identical digits
  if (/^(\d)\1{13}$/.test(digits)) {
    return { valid: false, error: 'CNPJ inválido (todos os dígitos são idênticos)', formatted: digits };
  }

  // Weights for CNPJ
  const w1 = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
  let sum1 = 0;
  for (let i = 0; i < 12; i++) {
    sum1 += parseInt(digits.charAt(i), 10) * w1[i];
  }
  const rem1 = sum1 % 11;
  const digit1 = rem1 < 2 ? 0 : 11 - rem1;
  if (digit1 !== parseInt(digits.charAt(12), 10)) {
    return { valid: false, error: '1º Dígito verificador incorreto (Módulo 11)', formatted: digits };
  }

  const w2 = [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
  let sum2 = 0;
  for (let i = 0; i < 13; i++) {
    sum2 += parseInt(digits.charAt(i), 10) * w2[i];
  }
  const rem2 = sum2 % 11;
  const digit2 = rem2 < 2 ? 0 : 11 - rem2;
  if (digit2 !== parseInt(digits.charAt(13), 10)) {
    return { valid: false, error: '2º Dígito verificador incorreto (Módulo 11)', formatted: digits };
  }

  const formatted = digits.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5');
  return { valid: true, error: null, formatted };
}

export function validateEmail(email) {
  if (!email || !email.trim()) return { valid: false, error: 'E-mail é obrigatório' };
  const trimmed = email.trim();
  const emailRegex = /^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\.[A-Za-z]{2,})$/;
  if (!emailRegex.test(trimmed)) {
    return { valid: false, error: 'Formato de e-mail inválido (ex: nome@dominio.com)' };
  }
  return { valid: true, error: null, formatted: trimmed };
}

export function validatePhone(phone) {
  if (!phone || !phone.trim()) return { valid: false, error: 'Telefone é obrigatório' };
  const trimmed = phone.trim();
  const digits = sanitizeDigits(trimmed);

  // E.164 Brazilian format: 10 or 11 digits (national) or 12/13 with country code 55
  if (digits.length === 10 || digits.length === 11) {
    const formatted = `+55${digits}`;
    return { valid: true, error: null, formatted };
  }
  if (digits.length === 12 || digits.length === 13) {
    if (digits.startsWith('55')) {
      return { valid: true, error: null, formatted: `+${digits}` };
    }
  }

  return { valid: false, error: 'Telefone deve ter DDD + 8 ou 9 dígitos (ex: 11987654321 ou +5511987654321)' };
}

export function validateEvp(evp) {
  if (!evp || !evp.trim()) return { valid: false, error: 'Chave Aleatória (EVP) é obrigatória' };
  const trimmed = evp.trim();
  const uuidRegex = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/;
  if (!uuidRegex.test(trimmed)) {
    return { valid: false, error: 'Chave Aleatória deve seguir padrão UUID v4 (ex: 123e4567-e89b-12d3-a456-426614174000)' };
  }
  return { valid: true, error: null, formatted: trimmed.toLowerCase() };
}

export function validatePixKey(type, value) {
  switch (type) {
    case 'CPF':
      return validateCpf(value);
    case 'CNPJ':
      return validateCnpj(value);
    case 'EMAIL':
      return validateEmail(value);
    case 'PHONE':
      return validatePhone(value);
    case 'RANDOM':
      return validateEvp(value);
    default:
      return { valid: false, error: 'Tipo de chave desconhecido' };
  }
}

/**
 * Mathematically valid sample keys for instant 1-click testing
 */
export const SAMPLE_KEYS = {
  CPF: [
    { label: 'CPF Válido A', value: '529.982.247-25' },
    { label: 'CPF Válido B', value: '111.444.777-35' },
    { label: 'CPF Inválido (DV Errado)', value: '123.456.789-11' },
  ],
  CNPJ: [
    { label: 'CNPJ Válido A', value: '04.252.011/0001-10' },
    { label: 'CNPJ Válido B', value: '11.222.333/0001-81' },
    { label: 'CNPJ Inválido (DV Errado)', value: '12.345.678/0001-90' },
  ],
  EMAIL: [
    { label: 'Tech Lead', value: 'matheus.lead@pixflow.io' },
    { label: 'Financeiro', value: 'financeiro@empresa.com.br' },
    { label: 'E-mail Inválido', value: 'invalido@sem-dominio' },
  ],
  PHONE: [
    { label: 'Celular SP', value: '+5511987654321' },
    { label: 'Celular RJ', value: '+5521998877665' },
  ],
  RANDOM: [
    { label: 'Chave EVP Alpha', value: 'a8b5e9d2-3c4f-4a1e-8b9a-1c2d3e4f5a6b' },
    { label: 'Chave EVP Beta', value: 'e1d2c3b4-a5f6-4789-90ab-cdef12345678' },
  ],
};

