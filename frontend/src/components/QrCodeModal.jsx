import React, { useState } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import { X, Copy, Check, Zap, ExternalLink, ShieldCheck } from 'lucide-react';
import StatusBadge from './StatusBadge';

export default function QrCodeModal({ charge, onClose, onSettle, settling }) {
  const [copied, setCopied] = useState(false);

  if (!charge) return null;

  const handleCopy = () => {
    if (charge.qrCodePayload) {
      navigator.clipboard.writeText(charge.qrCodePayload);
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
      <div className="relative w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden p-6 text-slate-100">
        
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-800">
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-lg bg-emerald-500/10 text-emerald-400">
              <Zap className="w-4 h-4" />
            </div>
            <h3 className="font-bold text-base text-white">Cobrança PIX Gerada</h3>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="py-4 space-y-4">
          
          {/* Amount & Status Banner */}
          <div className="flex items-center justify-between p-3.5 rounded-xl bg-slate-950/80 border border-slate-800">
            <div>
              <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider block">Valor</span>
              <span className="text-xl font-extrabold text-emerald-400 font-mono">
                {formatBRL(charge.amount)}
              </span>
            </div>
            <StatusBadge status={charge.status} />
          </div>

          {/* QR Code Container */}
          <div className="flex flex-col items-center justify-center p-4 rounded-xl bg-white shadow-inner">
            <QRCodeSVG
              value={charge.qrCodePayload || 'pixflow'}
              size={180}
              level="M"
              includeMargin={false}
            />
            <span className="text-[10px] text-slate-600 font-mono mt-2 flex items-center gap-1 font-semibold">
              <ShieldCheck className="w-3 h-3 text-emerald-600" />
              Padrão EMVCo / Banco Central do Brasil
            </span>
          </div>

          {/* Pix Copia e Cola */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center justify-between">
              <span>PIX Copia e Cola</span>
              {copied && <span className="text-emerald-400 text-[11px] font-bold">Copiado para a área de transferência!</span>}
            </label>
            <div className="flex items-center gap-2">
              <input
                type="text"
                readOnly
                value={charge.qrCodePayload || ''}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-300 font-mono select-all focus:outline-none"
              />
              <button
                type="button"
                onClick={handleCopy}
                className="px-3 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 flex items-center gap-1 text-xs font-semibold shrink-0 transition-colors"
                title="Copiar código PIX"
              >
                {copied ? <Check className="w-4 h-4 text-emerald-400" /> : <Copy className="w-4 h-4" />}
                <span>{copied ? 'Copiado' : 'Copiar'}</span>
              </button>
            </div>
          </div>

          {/* Metadata details */}
          <div className="space-y-1.5 text-xs font-mono text-slate-400 bg-slate-950/40 p-3 rounded-xl border border-slate-800/60">
            <div className="flex justify-between">
              <span className="text-slate-500">TxID:</span>
              <span className="text-slate-300 truncate max-w-[220px]">{charge.txId}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-slate-500">EndToEndId:</span>
              <span className="text-slate-300 truncate max-w-[220px]">{charge.endToEndId}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-slate-500">Chave:</span>
              <span className="text-slate-300">{charge.pixKey} ({charge.pixKeyType})</span>
            </div>
          </div>

        </div>

        {/* Footer Actions */}
        <div className="pt-3 border-t border-slate-800 flex items-center gap-2">
          {charge.status === 'PENDING' && (
            <button
              onClick={() => onSettle(charge.id)}
              disabled={settling}
              className="flex-1 py-2.5 px-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-xs flex items-center justify-center gap-1.5 transition-colors disabled:opacity-50"
            >
              {settling ? (
                <span>Liquidando no BACEN...</span>
              ) : (
                <>
                  <Zap className="w-3.5 h-3.5" />
                  Simular Liquidação Agora
                </>
              )}
            </button>
          )}

          <button
            onClick={onClose}
            className="flex-1 py-2.5 px-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 font-semibold text-xs transition-colors text-center"
          >
            Fechar
          </button>
        </div>

      </div>
    </div>
  );
}
