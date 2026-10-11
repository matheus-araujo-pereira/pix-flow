import React, { useState, useMemo } from 'react';
import { Send, CheckCircle2, AlertCircle, Copy, Check, QrCode, Sparkles, RefreshCw } from 'lucide-react';
import { api } from '../services/api';
import { validatePixKey, SAMPLE_KEYS } from '../utils/validators';

export default function TerminalView({ onChargeCreated, onOpenQrCode, onInspectCharge }) {
  const [keyType, setKeyType] = useState('CPF');
  const [pixKey, setPixKey] = useState('529.982.247-25');
  const [amount, setAmount] = useState('100.00');
  const [description, setDescription] = useState('Serviço de Consultoria Cloud DevOps');
  const [recipientName, setRecipientName] = useState('PixFlow Engenharia S.A.');
  const [webhookUrl, setWebhookUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [lastCreated, setLastCreated] = useState(null);
  const [copiedPayload, setCopiedPayload] = useState(false);

  // Real-time client-side validation
  const validationResult = useMemo(() => {
    return validatePixKey(keyType, pixKey);
  }, [keyType, pixKey]);

  const handleSelectSample = (sample) => {
    setPixKey(sample.value);
    setError(null);
  };

  const handleKeyTypeChange = (newType) => {
    setKeyType(newType);
    setError(null);
    if (SAMPLE_KEYS[newType] && SAMPLE_KEYS[newType][0]) {
      setPixKey(SAMPLE_KEYS[newType][0].value);
    }
  };

  const handleAmountIncrement = (inc) => {
    const current = parseFloat(amount) || 0;
    setAmount((current + inc).toFixed(2));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (!validationResult.valid) {
      setError(`Validação falhou: ${validationResult.error}`);
      return;
    }

    const parsedAmount = parseFloat(amount);
    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      setError('Valor deve ser estritamente maior que zero');
      return;
    }

    try {
      setLoading(true);
      const payload = {
        pixKey: validationResult.formatted || pixKey.trim(),
        pixKeyType: keyType,
        amount: parsedAmount,
        description: description.trim() || undefined,
        recipientName: recipientName.trim() || undefined,
        webhookUrl: webhookUrl.trim() || undefined,
      };

      const result = await api.createCharge(payload);
      setLastCreated(result);
      if (onChargeCreated) onChargeCreated(result);
    } catch (err) {
      setError(err.message || 'Erro ao registrar cobrança');
    } finally {
      setLoading(false);
    }
  };

  const handleCopyPayload = (text) => {
    navigator.clipboard.writeText(text);
    setCopiedPayload(true);
    setTimeout(() => setCopiedPayload(false), 2000);
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div>
        <div className="flex items-center gap-2 text-xs font-mono text-neutral-400 mb-1">
          <span>EMISSION ENGINE</span>
          <span>/</span>
          <span className="text-neutral-300">INSTANT PIX</span>
        </div>
        <h1 className="text-xl font-semibold text-neutral-100 tracking-tight">
          Terminal de Emissão de Cobrança PIX
        </h1>
        <p className="text-xs text-neutral-400 mt-1">
          Emissão direta com validação matemática estrita de chaves (Módulo 11 para CPF/CNPJ), geração de payload EMV e despacho assíncrono via mensageria.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Form Column */}
        <div className="lg:col-span-7 bg-neutral-900 border border-neutral-800 rounded-lg p-5 shadow-sm space-y-5">
          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Key Type Tabs */}
            <div>
              <label className="block text-xs font-mono text-neutral-400 uppercase mb-2">
                Tipo de Chave DICT
              </label>
              <div className="grid grid-cols-5 gap-1.5 p-1 bg-neutral-950 border border-neutral-800 rounded-md">
                {['CPF', 'CNPJ', 'EMAIL', 'PHONE', 'RANDOM'].map((type) => (
                  <button
                    key={type}
                    type="button"
                    onClick={() => handleKeyTypeChange(type)}
                    className={`text-xs font-mono py-1.5 rounded transition-all ${
                      keyType === type
                        ? 'bg-neutral-800 text-neutral-100 font-semibold shadow-sm'
                        : 'text-neutral-400 hover:text-neutral-200'
                    }`}
                  >
                    {type === 'RANDOM' ? 'EVP' : type}
                  </button>
                ))}
              </div>
            </div>

            {/* Quick Sample Selector */}
            {SAMPLE_KEYS[keyType] && (
              <div>
                <span className="text-[11px] font-mono text-neutral-500 block mb-1.5">
                  Preenchimento Rápido com Chaves de Teste:
                </span>
                <div className="flex flex-wrap gap-1.5">
                  {SAMPLE_KEYS[keyType].map((sample, idx) => (
                    <button
                      key={idx}
                      type="button"
                      onClick={() => handleSelectSample(sample)}
                      className={`text-[11px] font-mono px-2 py-0.5 rounded border transition-colors ${
                        sample.label.includes('Inválido')
                          ? 'border-rose-900/60 bg-rose-950/20 text-rose-400 hover:bg-rose-900/30'
                          : 'border-neutral-800 bg-neutral-950 text-neutral-300 hover:bg-neutral-800'
                      }`}
                    >
                      {sample.label}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* Pix Key Input with Live Validator Feedback */}
            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label className="text-xs font-mono text-neutral-400 uppercase">
                  Chave PIX
                </label>
                {/* Live Validation Pill */}
                {pixKey.length > 0 && (
                  <span
                    className={`inline-flex items-center gap-1 text-[11px] font-mono px-2 py-0.5 rounded border ${
                      validationResult.valid
                        ? 'border-emerald-800/80 bg-emerald-950/50 text-emerald-400'
                        : 'border-rose-800/80 bg-rose-950/50 text-rose-400'
                    }`}
                  >
                    {validationResult.valid ? (
                      <>
                        <CheckCircle2 className="w-3 h-3" />
                        {keyType === 'CPF' || keyType === 'CNPJ' ? 'Módulo 11 Válido' : 'Formato Válido'}
                      </>
                    ) : (
                      <>
                        <AlertCircle className="w-3 h-3" />
                        {validationResult.error}
                      </>
                    )}
                  </span>
                )}
              </div>
              <input
                type="text"
                value={pixKey}
                onChange={(e) => setPixKey(e.target.value)}
                placeholder="Insira a chave PIX..."
                className={`w-full bg-neutral-950 border text-neutral-100 text-sm font-mono rounded-md px-3 py-2 outline-none transition-colors ${
                  pixKey.length > 0 && !validationResult.valid
                    ? 'border-rose-800 focus:border-rose-600'
                    : 'border-neutral-800 focus:border-neutral-600'
                }`}
                required
              />
            </div>

            {/* Amount and Quick Increments */}
            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label className="text-xs font-mono text-neutral-400 uppercase">
                  Valor da Cobrança (BRL)
                </label>
                <span className="text-[11px] font-mono text-neutral-500">Mínimo R$ 0,01</span>
              </div>
              <div className="relative">
                <span className="absolute left-3 top-2 text-sm font-mono text-neutral-500">R$</span>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="w-full bg-neutral-950 border border-neutral-800 focus:border-neutral-600 text-neutral-100 text-sm font-mono rounded-md pl-10 pr-3 py-2 outline-none transition-colors"
                  required
                />
              </div>
              {/* Increments */}
              <div className="flex gap-1.5 mt-2">
                {[10, 50, 100, 500, 1000].map((inc) => (
                  <button
                    key={inc}
                    type="button"
                    onClick={() => handleAmountIncrement(inc)}
                    className="text-[11px] font-mono px-2 py-0.5 rounded bg-neutral-950 border border-neutral-800 hover:bg-neutral-800 text-neutral-400 hover:text-neutral-200 transition-colors"
                  >
                    +R$ {inc}
                  </button>
                ))}
              </div>
            </div>

            {/* Recipient and Description */}
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-mono text-neutral-400 uppercase mb-1.5">
                  Recebedor
                </label>
                <input
                  type="text"
                  value={recipientName}
                  onChange={(e) => setRecipientName(e.target.value)}
                  className="w-full bg-neutral-950 border border-neutral-800 focus:border-neutral-600 text-neutral-100 text-xs font-mono rounded-md px-3 py-2 outline-none transition-colors"
                />
              </div>
              <div>
                <label className="block text-xs font-mono text-neutral-400 uppercase mb-1.5">
                  Descrição
                </label>
                <input
                  type="text"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="w-full bg-neutral-950 border border-neutral-800 focus:border-neutral-600 text-neutral-100 text-xs font-mono rounded-md px-3 py-2 outline-none transition-colors"
                />
              </div>
            </div>

            {/* Webhook URL (Optional) */}
            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label className="text-xs font-mono text-neutral-400 uppercase">
                  Webhook de Notificação (Opcional)
                </label>
                <span className="text-[11px] font-mono text-neutral-500">HMAC-SHA256</span>
              </div>
              <input
                type="url"
                value={webhookUrl}
                onChange={(e) => setWebhookUrl(e.target.value)}
                placeholder="https://webhook.site/seu-id ou webhook do ERP"
                className="w-full bg-neutral-950 border border-neutral-800 focus:border-neutral-600 text-neutral-100 text-xs font-mono rounded-md px-3 py-2 outline-none transition-colors"
              />
            </div>

            {/* Error Message */}
            {error && (
              <div className="p-3 bg-rose-950/40 border border-rose-800 text-rose-300 text-xs rounded-md flex items-start gap-2">
                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                <span>{error}</span>
              </div>
            )}

            {/* Submit Button */}
            <button
              type="submit"
              disabled={loading || !validationResult.valid}
              className="w-full py-2.5 px-4 bg-neutral-100 hover:bg-white text-neutral-950 font-semibold text-xs font-mono uppercase tracking-wider rounded-md flex items-center justify-center gap-2 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? (
                <>
                  <RefreshCw className="w-4 h-4 animate-spin" />
                  Emitindo Cobrança...
                </>
              ) : (
                <>
                  <Send className="w-4 h-4" />
                  Emitir Cobrança PIX
                </>
              )}
            </button>
          </form>
        </div>

        {/* Feedback / Result Column */}
        <div className="lg:col-span-5 space-y-4">
          <div className="bg-neutral-900 border border-neutral-800 rounded-lg p-5">
            <h3 className="text-xs font-mono uppercase tracking-wider text-neutral-400 mb-3">
              Resultado da Emissão
            </h3>

            {lastCreated ? (
              <div className="space-y-4">
                <div className="p-3 bg-neutral-950 border border-neutral-800 rounded-md space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-mono text-neutral-400">TxID (ID da Transação)</span>
                    <span className="text-xs font-mono text-emerald-400 font-semibold">EMITIDO</span>
                  </div>
                  <div className="text-xs font-mono text-neutral-200 select-all break-all">
                    {lastCreated.id}
                  </div>
                </div>

                <div className="space-y-1.5">
                  <span className="text-xs font-mono text-neutral-400 block">Payload EMV (Copia e Cola):</span>
                  <div className="relative">
                    <textarea
                      readOnly
                      rows={3}
                      value={lastCreated.qrCodePayload || lastCreated.emvPayload || ''}
                      className="w-full bg-neutral-950 border border-neutral-800 rounded-md p-2 text-xs font-mono text-neutral-300 resize-none outline-none"
                    />
                    <button
                      onClick={() => handleCopyPayload(lastCreated.qrCodePayload || lastCreated.emvPayload)}
                      className="absolute right-2 bottom-3 p-1.5 bg-neutral-800 hover:bg-neutral-700 text-neutral-300 rounded text-xs flex items-center gap-1 transition-colors"
                      title="Copiar Payload"
                    >
                      {copiedPayload ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                    </button>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-2">
                  <button
                    onClick={() => onOpenQrCode(lastCreated)}
                    className="py-2 px-3 bg-neutral-800 hover:bg-neutral-700 text-neutral-200 rounded text-xs font-mono flex items-center justify-center gap-1.5 transition-colors"
                  >
                    <QrCode className="w-3.5 h-3.5" />
                    Exibir QR Code
                  </button>
                  <button
                    onClick={() => onInspectCharge(lastCreated.id)}
                    className="py-2 px-3 bg-neutral-800 hover:bg-neutral-700 text-neutral-200 rounded text-xs font-mono flex items-center justify-center gap-1.5 transition-colors"
                  >
                    <Sparkles className="w-3.5 h-3.5 text-amber-400" />
                    Inspecionar Transação
                  </button>
                </div>

                <div className="p-3 bg-neutral-950 border border-neutral-800/80 rounded-md text-[11px] font-mono text-neutral-500 space-y-1">
                  <div>Status: <span className="text-neutral-300 font-semibold">{lastCreated.status}</span></div>
                  <div>Valor: <span className="text-neutral-300 font-semibold">R$ {Number(lastCreated.amount).toFixed(2).replace('.', ',')}</span></div>
                  <div>Chave: <span className="text-neutral-300">{lastCreated.pixKey}</span></div>
                  <div className="text-neutral-600 pt-1">
                    * Liquidação assíncrona automática simulada em 1.2 segundos pelo worker.
                  </div>
                </div>
              </div>
            ) : (
              <div className="p-8 text-center border border-dashed border-neutral-800 rounded-md text-neutral-500 space-y-2">
                <QrCode className="w-8 h-8 mx-auto text-neutral-600" />
                <p className="text-xs">Nenhuma cobrança emitida nesta sessão.</p>
                <p className="text-[11px] text-neutral-600">
                  Preencha o formulário ao lado e clique em Emitir para inspecionar o payload EMV.
                </p>
              </div>
            )}
          </div>

          {/* Engine Info Box */}
          <div className="bg-neutral-900 border border-neutral-800 rounded-lg p-4 text-xs space-y-2">
            <span className="font-mono text-neutral-400 uppercase block">Garantias Arquiteturais</span>
            <ul className="space-y-1 text-neutral-400 text-[11px]">
              <li>• <strong>Idempotência:</strong> Transações com mesmo TxID rejeitam duplicidade.</li>
              <li>• <strong>Validação Restrita:</strong> Algoritmo Módulo 11 executado no cliente e no servidor.</li>
              <li>• <strong>Desacoplamento:</strong> Publicação em fila AWS SQS / poller não bloqueante.</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}

