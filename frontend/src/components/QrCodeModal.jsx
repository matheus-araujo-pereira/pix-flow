import React, { useState } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import { X, Copy, Check, ShieldCheck, Zap } from 'lucide-react';
import StatusBadge from './StatusBadge';

export default function QrCodeModal({ charge, onClose, onSettle, settling }) {
  const [copied, setCopied] = useState(false);

  if (!charge) return null;

  const payload = charge.emvPayload || charge.qrCodePayload || '';

  const handleCopy = () => {
    if (payload) {
      navigator.clipboard.writeText(payload);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const formatBRL = (val) => new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(val || 0);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-fade-in">
      <div className="relative w-full max-w-md bg-neutral-900 border border-neutral-800 rounded-lg shadow-2xl p-6 text-neutral-100">
        
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-neutral-800">
          <div>
            <span className="text-[11px] font-mono text-neutral-400 uppercase">EMVCO BR CODE</span>
            <h3 className="font-semibold text-base text-neutral-100">QR Code da Cobrança</h3>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded text-neutral-400 hover:text-neutral-100 hover:bg-neutral-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="py-4 space-y-4">
          {/* Amount & Status Banner */}
          <div className="flex items-center justify-between p-3.5 rounded bg-neutral-950 border border-neutral-800">
            <div>
              <span className="text-[11px] font-mono text-neutral-500 uppercase tracking-wider block">Valor Nominal</span>
              <span className="text-xl font-bold text-neutral-100 font-mono">
                {formatBRL(charge.amount)}
              </span>
            </div>
            <StatusBadge status={charge.status} />
          </div>

          {/* QR Code Container */}
          <div className="flex flex-col items-center justify-center p-5 rounded bg-white">
            <QRCodeSVG
              value={payload || 'pixflow'}
              size={175}
              level="M"
              includeMargin={false}
            />
            <span className="text-[10px] text-neutral-800 font-mono mt-3 flex items-center gap-1 font-semibold">
              <ShieldCheck className="w-3.5 h-3.5 text-emerald-600" />
              Padrão EMVCo / Banco Central do Brasil
            </span>
          </div>

          {/* Pix Copia e Cola */}
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="text-xs font-mono text-neutral-400 uppercase">
                PIX Copia e Cola
              </label>
              {copied && <span className="text-emerald-400 text-[11px] font-mono">Copiado!</span>}
            </div>
            <div className="flex items-center gap-2">
              <input
                type="text"
                readOnly
                value={payload}
                className="w-full bg-neutral-950 border border-neutral-800 rounded px-3 py-2 text-xs text-neutral-300 font-mono select-all outline-none"
              />
              <button
                type="button"
                onClick={handleCopy}
                className="px-3 py-2 rounded bg-neutral-800 hover:bg-neutral-700 text-neutral-200 border border-neutral-700 flex items-center gap-1 text-xs font-mono shrink-0 transition-colors"
                title="Copiar código PIX"
              >
                {copied ? <Check className="w-4 h-4 text-emerald-400" /> : <Copy className="w-4 h-4" />}
                <span>{copied ? 'Copiado' : 'Copiar'}</span>
              </button>
            </div>
          </div>

          {/* Metadata details */}
          <div className="space-y-1.5 text-xs font-mono text-neutral-400 bg-neutral-950 p-3 rounded border border-neutral-800">
            <div className="flex justify-between">
              <span className="text-neutral-500">TxID:</span>
              <span className="text-neutral-300 truncate max-w-[220px]">{charge.id}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-neutral-500">Chave:</span>
              <span className="text-neutral-300">{charge.pixKey} ({charge.pixKeyType})</span>
            </div>
            <div className="flex justify-between">
              <span className="text-neutral-500">Recebedor:</span>
              <span className="text-neutral-300">{charge.recipientName || 'PixFlow Engine'}</span>
            </div>
          </div>
        </div>

        {/* Footer Actions */}
        <div className="pt-3 border-t border-neutral-800 flex items-center gap-2">
          {charge.status === 'PENDING' && onSettle && (
            <button
              onClick={() => onSettle(charge.id)}
              disabled={settling}
              className="flex-1 py-2 px-3 rounded bg-neutral-100 hover:bg-white text-neutral-950 font-semibold text-xs font-mono flex items-center justify-center gap-1.5 transition-colors disabled:opacity-50"
            >
              {settling ? (
                <span>Liquidando...</span>
              ) : (
                <>
                  <Zap className="w-3.5 h-3.5" />
                  Liquidar Agora
                </>
              )}
            </button>
          )}

          <button
            onClick={onClose}
            className="flex-1 py-2 px-3 rounded bg-neutral-800 hover:bg-neutral-700 text-neutral-300 font-medium text-xs font-mono transition-colors text-center"
          >
            Fechar
          </button>
        </div>

      </div>
    </div>
  );
}
