import React, { useEffect, useState } from 'react';
import { X, Clock, CheckCircle2, AlertTriangle, ArrowRight, Copy, Check, ShieldCheck, Terminal } from 'lucide-react';
import StatusBadge from './StatusBadge';
import { api } from '../services/api';

export default function TransactionDrawer({ chargeId, onClose }) {
  const [charge, setCharge] = useState(null);
  const [timeline, setTimeline] = useState([]);
  const [loading, setLoading] = useState(true);
  const [copied, setCopied] = useState(false);
  const [activeTab, setActiveTab] = useState('timeline'); // 'timeline' | 'json'

  useEffect(() => {
    if (!chargeId) return;

    let isMounted = true;
    async function loadData() {
      try {
        setLoading(true);
        const [chargeData, timelineData] = await Promise.all([
          api.getChargeById(chargeId),
          api.getChargeTimeline(chargeId).catch(() => []),
        ]);
        if (isMounted) {
          setCharge(chargeData);
          setTimeline(timelineData || []);
        }
      } catch (err) {
        console.error('Failed to load transaction details', err);
      } finally {
        if (isMounted) setLoading(false);
      }
    }

    loadData();
    return () => { isMounted = false; };
  }, [chargeId]);

  if (!chargeId) return null;

  const handleCopyJson = () => {
    navigator.clipboard.writeText(JSON.stringify(charge, null, 2));
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="fixed inset-0 z-50 overflow-hidden bg-black/60 backdrop-blur-sm flex justify-end">
      <div className="w-full max-w-xl bg-neutral-900 border-l border-neutral-800 h-full flex flex-col shadow-2xl animate-in slide-in-from-right duration-200">
        {/* Header */}
        <div className="px-6 py-5 border-b border-neutral-800 flex items-center justify-between bg-neutral-950">
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xs font-mono font-medium text-neutral-400">INSPECTOR</span>
              <span className="text-neutral-600">/</span>
              <span className="text-xs font-mono text-neutral-300">TX AUDIT</span>
            </div>
            <h2 className="text-base font-semibold text-neutral-100 mt-1">
              Detalhes da Transação
            </h2>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-neutral-400 hover:text-neutral-100 hover:bg-neutral-800 rounded-md transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        {loading ? (
          <div className="flex-1 flex items-center justify-center text-neutral-400 text-sm">
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 border-2 border-neutral-600 border-t-neutral-200 rounded-full animate-spin" />
              Carregando auditoria...
            </div>
          </div>
        ) : !charge ? (
          <div className="flex-1 flex items-center justify-center text-neutral-500 text-sm">
            Transação não encontrada
          </div>
        ) : (
          <div className="flex-1 overflow-y-auto p-6 space-y-6">
            {/* Quick Metrics Bar */}
            <div className="bg-neutral-950 border border-neutral-800 rounded-lg p-4">
              <div className="flex items-center justify-between mb-3">
                <span className="text-xs font-mono text-neutral-400 uppercase">Status do Motor</span>
                <StatusBadge status={charge.status} />
              </div>
              <div className="grid grid-cols-2 gap-4 pt-3 border-t border-neutral-800/80">
                <div>
                  <span className="text-xs text-neutral-500 block">Valor Liquidado</span>
                  <span className="text-xl font-mono font-bold text-neutral-100">
                    R$ {Number(charge.amount).toFixed(2).replace('.', ',')}
                  </span>
                </div>
                <div>
                  <span className="text-xs text-neutral-500 block">Tempo Total</span>
                  <span className="text-xl font-mono font-bold text-emerald-400">
                    {charge.settlementDurationMs ? `${charge.settlementDurationMs}ms` : 'Em processamento'}
                  </span>
                </div>
              </div>
            </div>

            {/* Details Table */}
            <div className="space-y-3">
              <h3 className="text-xs font-mono uppercase tracking-wider text-neutral-400">Identificadores SPB / DICT</h3>
              <div className="bg-neutral-950 border border-neutral-800 rounded-lg divide-y divide-neutral-800 text-xs font-mono">
                <div className="p-3 flex justify-between items-center">
                  <span className="text-neutral-500">ID Interno</span>
                  <span className="text-neutral-200 select-all">{charge.id}</span>
                </div>
                <div className="p-3 flex justify-between items-center">
                  <span className="text-neutral-500">EndToEndId (BACEN)</span>
                  <span className="text-neutral-200 select-all truncate max-w-[280px]">
                    {charge.endToEndId || 'Gerado na liquidação'}
                  </span>
                </div>
                <div className="p-3 flex justify-between items-center">
                  <span className="text-neutral-500">Chave PIX ({charge.pixKeyType})</span>
                  <span className="text-neutral-200 font-semibold">{charge.pixKey}</span>
                </div>
                <div className="p-3 flex justify-between items-center">
                  <span className="text-neutral-500">Recebedor</span>
                  <span className="text-neutral-200">{charge.recipientName || 'PixFlow Engine'}</span>
                </div>
                <div className="p-3 flex justify-between items-center">
                  <span className="text-neutral-500">Data de Emissão</span>
                  <span className="text-neutral-200">
                    {new Date(charge.createdAt).toLocaleString('pt-BR')}
                  </span>
                </div>
                {charge.settledAt && (
                  <div className="p-3 flex justify-between items-center">
                    <span className="text-neutral-500">Data de Liquidação</span>
                    <span className="text-emerald-400">
                      {new Date(charge.settledAt).toLocaleString('pt-BR')}
                    </span>
                  </div>
                )}
                {charge.webhookUrl && (
                  <div className="p-3 flex justify-between items-center">
                    <span className="text-neutral-500">Webhook Destino</span>
                    <span className="text-neutral-300 truncate max-w-[280px]">{charge.webhookUrl}</span>
                  </div>
                )}
              </div>
            </div>

            {/* Tabs: Timeline vs Raw JSON */}
            <div className="space-y-3">
              <div className="flex items-center gap-2 border-b border-neutral-800 pb-2">
                <button
                  onClick={() => setActiveTab('timeline')}
                  className={`text-xs font-mono px-3 py-1.5 rounded transition-colors ${
                    activeTab === 'timeline'
                      ? 'bg-neutral-800 text-neutral-100 font-medium'
                      : 'text-neutral-400 hover:text-neutral-200'
                  }`}
                >
                  Timeline de Auditoria ({timeline.length})
                </button>
                <button
                  onClick={() => setActiveTab('json')}
                  className={`text-xs font-mono px-3 py-1.5 rounded transition-colors ${
                    activeTab === 'json'
                      ? 'bg-neutral-800 text-neutral-100 font-medium'
                      : 'text-neutral-400 hover:text-neutral-200'
                  }`}
                >
                  Payload JSON
                </button>
              </div>

              {activeTab === 'timeline' ? (
                <div className="relative pl-6 space-y-6 before:absolute before:left-2 before:top-2 before:bottom-2 before:w-0.5 before:bg-neutral-800">
                  {timeline.length === 0 ? (
                    <div className="text-xs text-neutral-500 py-4">Nenhum evento auditado ainda.</div>
                  ) : (
                    timeline.map((item, idx) => (
                      <div key={idx} className="relative group">
                        {/* Bullet */}
                        <div className={`absolute -left-[27px] top-1 w-3.5 h-3.5 rounded-full border-2 bg-neutral-900 ${
                          item.status === 'SETTLED'
                            ? 'border-emerald-500 text-emerald-400'
                            : item.status === 'FAILED'
                            ? 'border-rose-500 text-rose-400'
                            : item.status === 'PROCESSING'
                            ? 'border-amber-500 text-amber-400'
                            : 'border-neutral-500 text-neutral-400'
                        }`} />

                        <div className="bg-neutral-950 border border-neutral-800/80 rounded-md p-3">
                          <div className="flex items-center justify-between text-xs">
                            <span className="font-mono font-semibold text-neutral-200">{item.action}</span>
                            <span className="font-mono text-neutral-500">
                              {new Date(item.timestamp).toLocaleTimeString('pt-BR')}
                            </span>
                          </div>
                          <p className="text-xs text-neutral-400 mt-1">{item.details}</p>
                          {item.workerId && (
                            <div className="mt-2 text-[11px] font-mono text-neutral-500 flex items-center gap-1.5">
                              <Terminal className="w-3 h-3 text-neutral-600" />
                              <span>Worker: {item.workerId}</span>
                            </div>
                          )}
                        </div>
                      </div>
                    ))
                  )}
                </div>
              ) : (
                <div className="relative">
                  <button
                    onClick={handleCopyJson}
                    className="absolute top-2 right-2 text-xs font-mono bg-neutral-800 hover:bg-neutral-700 text-neutral-300 px-2.5 py-1 rounded flex items-center gap-1 transition-colors"
                  >
                    {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                    {copied ? 'Copiado' : 'Copiar'}
                  </button>
                  <pre className="bg-neutral-950 border border-neutral-800 rounded-md p-4 text-xs font-mono text-neutral-300 overflow-x-auto max-h-80">
                    {JSON.stringify(charge, null, 2)}
                  </pre>
                </div>
              )}
            </div>
          </div>
        )}

        {/* Footer */}
        <div className="px-6 py-4 border-t border-neutral-800 bg-neutral-950 flex justify-between items-center text-xs text-neutral-500">
          <span className="font-mono">PixFlow Event-Driven Engine v1.0</span>
          <button
            onClick={onClose}
            className="px-4 py-2 bg-neutral-800 hover:bg-neutral-700 text-neutral-200 font-medium rounded transition-colors"
          >
            Fechar
          </button>
        </div>
      </div>
    </div>
  );
}

