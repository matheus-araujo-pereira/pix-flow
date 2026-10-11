import React, { useState } from 'react';
import { Send, Sparkles, AlertCircle } from 'lucide-react';

const KEY_TYPES = [
  { value: 'CPF', label: 'CPF', placeholder: '123.456.789-09', defaultKey: '12345678909' },
  { value: 'CNPJ', label: 'CNPJ', placeholder: '12.345.678/0001-90', defaultKey: '12345678000190' },
  { value: 'EMAIL', label: 'E-mail', placeholder: 'dev@pixflow.com.br', defaultKey: 'dev@pixflow.com.br' },
  { value: 'PHONE', label: 'Celular', placeholder: '+55 11 98765-4321', defaultKey: '+5511987654321' },
  { value: 'RANDOM', label: 'Chave EVP', placeholder: 'UUID aleatório', defaultKey: '3d8f8a12-7b4c-4e89-a291-5627a92bcdef' },
];

const PRESET_AMOUNTS = [
  { label: 'R$ 25', value: '25.00' },
  { label: 'R$ 100', value: '100.00' },
  { label: 'R$ 500', value: '500.00' },
  { label: 'R$ 60.000 (Simular Rejeição)', value: '60000.00' },
];

export default function PixForm({ onSubmit, loading }) {
  const [keyType, setKeyType] = useState('CPF');
  const [pixKey, setPixKey] = useState('12345678909');
  const [amount, setAmount] = useState('100.00');
  const [description, setDescription] = useState('Serviços Cloud & DevOps');
  const [error, setError] = useState(null);

  const handleKeyTypeChange = (newType) => {
    setKeyType(newType);
    const preset = KEY_TYPES.find(k => k.value === newType);
    if (preset) {
      setPixKey(preset.defaultKey);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    const parsedAmount = parseFloat(amount);
    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      setError('Por favor, informe um valor válido maior que zero.');
      return;
    }

    if (!pixKey.trim()) {
      setError('A chave PIX não pode ser vazia.');
      return;
    }

    try {
      await onSubmit({
        pixKey: pixKey.trim(),
        pixKeyType: keyType,
        amount: parsedAmount,
        description: description.trim() || undefined,
      });
    } catch (err) {
      setError(err.message || 'Falha ao gerar cobrança PIX');
    }
  };

  const selectedPreset = KEY_TYPES.find(k => k.value === keyType);

  return (
    <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-5 sm:p-6 backdrop-blur-sm shadow-xl">
      <div className="flex items-center gap-2 mb-5">
        <div className="p-2 rounded-lg bg-emerald-500/10 text-emerald-400">
          <Sparkles className="w-5 h-5" />
        </div>
        <div>
          <h2 className="text-base font-bold text-white">Disparar Pagamento Instantâneo</h2>
          <p className="text-xs text-slate-400">Geração de cobrança em tempo real com QR Code EMVCo</p>
        </div>
      </div>

      {error && (
        <div className="mb-4 p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0 text-rose-400" />
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        
        {/* Tipo de Chave */}
        <div>
          <label className="block text-xs font-semibold text-slate-300 mb-1.5 uppercase tracking-wider">
            Tipo de Chave PIX
          </label>
          <div className="grid grid-cols-3 sm:grid-cols-5 gap-1.5">
            {KEY_TYPES.map(k => (
              <button
                key={k.value}
                type="button"
                onClick={() => handleKeyTypeChange(k.value)}
                className={`py-1.5 px-2 text-xs font-semibold rounded-lg border transition-all text-center ${
                  keyType === k.value
                    ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/40 shadow-sm'
                    : 'bg-slate-800/60 text-slate-400 border-slate-700/60 hover:bg-slate-800 hover:text-slate-200'
                }`}
              >
                {k.label}
              </button>
            ))}
          </div>
        </div>

        {/* Chave PIX Input */}
        <div>
          <label className="block text-xs font-semibold text-slate-300 mb-1.5">
            Chave do Destinatário
          </label>
          <input
            type="text"
            value={pixKey}
            onChange={(e) => setPixKey(e.target.value)}
            placeholder={selectedPreset?.placeholder}
            className="w-full bg-slate-950/80 border border-slate-800 rounded-xl px-3.5 py-2.5 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition-colors font-mono"
            required
          />
        </div>

        {/* Valor da Cobrança */}
        <div>
          <label className="block text-xs font-semibold text-slate-300 mb-1.5">
            Valor da Transação (R$)
          </label>
          <div className="relative">
            <span className="absolute left-3.5 top-2.5 text-sm font-semibold text-slate-500">
              R$
            </span>
            <input
              type="number"
              step="0.01"
              min="0.01"
              max="99999999.99"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder="0,00"
              className="w-full bg-slate-950/80 border border-slate-800 rounded-xl pl-10 pr-3.5 py-2.5 text-sm text-white font-mono font-bold focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition-colors"
              required
            />
          </div>

          {/* Quick presets */}
          <div className="flex flex-wrap gap-1.5 mt-2">
            {PRESET_AMOUNTS.map(p => (
              <button
                key={p.value}
                type="button"
                onClick={() => setAmount(p.value)}
                className={`text-[11px] font-medium px-2 py-1 rounded-md border transition-colors ${
                  amount === p.value
                    ? 'bg-indigo-500/10 text-indigo-300 border-indigo-500/30'
                    : 'bg-slate-800/40 text-slate-400 border-slate-800 hover:text-slate-300'
                }`}
              >
                {p.label}
              </button>
            ))}
          </div>
        </div>

        {/* Descrição */}
        <div>
          <label className="block text-xs font-semibold text-slate-300 mb-1.5">
            Descrição / Mensagem (Opcional)
          </label>
          <input
            type="text"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Ex: Liquidação de faturas"
            className="w-full bg-slate-950/80 border border-slate-800 rounded-xl px-3.5 py-2.5 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition-colors"
            maxLength={100}
          />
        </div>

        {/* Submit Button */}
        <button
          type="submit"
          disabled={loading}
          className="w-full mt-2 py-3 px-4 rounded-xl font-bold text-sm bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-slate-950 shadow-lg shadow-emerald-500/20 flex items-center justify-center gap-2 transition-all transform active:scale-[0.99] disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {loading ? (
            <span className="flex items-center gap-2">
              <span className="animate-spin rounded-full h-4 w-4 border-2 border-slate-950 border-t-transparent"></span>
              Emitindo PIX...
            </span>
          ) : (
            <>
              <Send className="w-4 h-4" />
              Emitir Cobrança PIX
            </>
          )}
        </button>

      </form>
    </div>
  );
}
