import React, { useState, useEffect } from 'react';
import { Radio, RefreshCw, ShieldCheck, Check, Copy, AlertCircle, Play, ChevronRight, Terminal } from 'lucide-react';
import { api } from '../services/api';

export default function WebhooksView() {
  const [deliveries, setDeliveries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedWebhook, setSelectedWebhook] = useState(null);
  const [replayingId, setReplayingId] = useState(null);
  const [copiedSig, setCopiedSig] = useState(false);

  const fetchWebhooks = async () => {
    try {
      setError(null);
      const data = await api.listWebhooks(0, 50);
      setDeliveries(data || []);
      if (data && data.length > 0 && !selectedWebhook) {
        setSelectedWebhook(data[0]);
      }
    } catch (err) {
      setError(err.message || 'Falha ao sincronizar entregas de webhooks');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWebhooks();
  }, []);

  const handleReplay = async (id) => {
    try {
      setReplayingId(id);
      await api.replayWebhook(id);
      await fetchWebhooks();
      alert('Webhook reenviado com sucesso!');
    } catch (err) {
      alert(`Falha no reenvio: ${err.message}`);
    } finally {
      setReplayingId(null);
    }
  };

  const handleCopySignature = (sig) => {
    navigator.clipboard.writeText(sig);
    setCopiedSig(true);
    setTimeout(() => setCopiedSig(false), 2000);
  };

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-xs font-mono text-neutral-400 mb-1">
            <span>OBSERVABILITY</span>
            <span>/</span>
            <span className="text-neutral-300">WEBHOOK DISPATCHER & HMAC-SHA256</span>
          </div>
          <h1 className="text-xl font-semibold text-neutral-100 tracking-tight">
            Auditoria de Webhooks & Assinaturas
          </h1>
          <p className="text-xs text-neutral-400 mt-1">
            Rastreamento de notificações assíncronas enviadas aos parceiros bancários com autenticação criptográfica HMAC-SHA256.
          </p>
        </div>

        <button
          onClick={fetchWebhooks}
          disabled={loading}
          className="self-start sm:self-auto px-3 py-2 bg-neutral-900 hover:bg-neutral-800 border border-neutral-800 text-neutral-200 text-xs font-mono rounded-md flex items-center gap-1.5 transition-colors"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Atualizar Eventos</span>
        </button>
      </div>

      {error && (
        <div className="p-3 bg-rose-950/40 border border-rose-800 text-rose-300 text-xs rounded-md flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Two columns layout */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Deliveries List */}
        <div className="lg:col-span-5 bg-neutral-900 border border-neutral-800 rounded-lg overflow-hidden flex flex-col">
          <div className="p-3 bg-neutral-950 border-b border-neutral-800 flex justify-between items-center text-xs font-mono text-neutral-400">
            <span>Disparos Registrados ({deliveries.length})</span>
            <span>Status</span>
          </div>

          <div className="divide-y divide-neutral-800/80 max-h-[520px] overflow-y-auto">
            {loading && deliveries.length === 0 ? (
              <div className="p-8 text-center text-xs font-mono text-neutral-500">
                Carregando webhooks...
              </div>
            ) : deliveries.length === 0 ? (
              <div className="p-8 text-center text-xs font-mono text-neutral-500 space-y-2">
                <Radio className="w-6 h-6 mx-auto text-neutral-600" />
                <p>Nenhum webhook disparado ainda.</p>
                <p className="text-[11px] text-neutral-600">
                  Emita uma cobrança informando uma URL de webhook ou aguarde a liquidação de uma transação.
                </p>
              </div>
            ) : (
              deliveries.map((w) => (
                <div
                  key={w.id}
                  onClick={() => setSelectedWebhook(w)}
                  className={`p-3 text-xs font-mono cursor-pointer transition-colors ${
                    selectedWebhook?.id === w.id
                      ? 'bg-neutral-800/80 border-l-2 border-emerald-500'
                      : 'hover:bg-neutral-800/40 bg-neutral-900'
                  }`}
                >
                  <div className="flex items-center justify-between mb-1">
                    <span className="font-semibold text-neutral-200">{w.eventType}</span>
                    <span className={`text-[11px] px-1.5 py-0.5 rounded ${
                      w.responseStatus >= 200 && w.responseStatus < 300
                        ? 'bg-emerald-950/60 text-emerald-400 border border-emerald-800/60'
                        : 'bg-rose-950/60 text-rose-400 border border-rose-800/60'
                    }`}>
                      HTTP {w.responseStatus}
                    </span>
                  </div>
                  <div className="text-[11px] text-neutral-400 truncate mb-1">
                    {w.targetUrl}
                  </div>
                  <div className="flex items-center justify-between text-[10px] text-neutral-500">
                    <span>Latência: {w.latencyMs}ms</span>
                    <span>{new Date(w.dispatchedAt).toLocaleTimeString('pt-BR')}</span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Selected Webhook Inspector */}
        <div className="lg:col-span-7 bg-neutral-900 border border-neutral-800 rounded-lg p-5 space-y-5">
          <div className="flex items-center justify-between">
            <h2 className="text-xs font-mono uppercase tracking-wider text-neutral-400">
              Inspeção Criptográfica & Payload
            </h2>
            {selectedWebhook && (
              <button
                onClick={() => handleReplay(selectedWebhook.id)}
                disabled={replayingId === selectedWebhook.id}
                className="px-2.5 py-1 bg-neutral-800 hover:bg-neutral-700 text-neutral-200 text-xs font-mono rounded flex items-center gap-1.5 transition-colors"
                title="Simular reenvio deste webhook"
              >
                <Play className="w-3 h-3 text-emerald-400" />
                <span>{replayingId === selectedWebhook.id ? 'Reenviando...' : 'Replay Webhook'}</span>
              </button>
            )}
          </div>

          {selectedWebhook ? (
            <div className="space-y-4">
              {/* Security info */}
              <div className="p-3 bg-neutral-950 border border-neutral-800 rounded-md space-y-2">
                <div className="flex items-center gap-1.5 text-xs font-mono text-emerald-400">
                  <ShieldCheck className="w-4 h-4" />
                  <span className="font-semibold">Assinatura HMAC-SHA256 (Header X-Signature-SHA256)</span>
                </div>
                <div className="relative">
                  <input
                    type="text"
                    readOnly
                    value={selectedWebhook.signatureSha256 || 'Assinatura gerada no despacho'}
                    className="w-full bg-neutral-900 border border-neutral-800 text-neutral-300 font-mono text-xs rounded px-2.5 py-1.5 pr-8 outline-none select-all"
                  />
                  <button
                    onClick={() => handleCopySignature(selectedWebhook.signatureSha256)}
                    className="absolute right-2 top-2 text-neutral-400 hover:text-neutral-100"
                    title="Copiar Assinatura"
                  >
                    {copiedSig ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                  </button>
                </div>
              </div>

              {/* Delivery Meta Grid */}
              <div className="grid grid-cols-2 gap-3 text-xs font-mono">
                <div className="p-2.5 bg-neutral-950 border border-neutral-800 rounded">
                  <span className="text-[11px] text-neutral-500 block">ID de Notificação</span>
                  <span className="text-neutral-200 truncate block">{selectedWebhook.id}</span>
                </div>
                <div className="p-2.5 bg-neutral-950 border border-neutral-800 rounded">
                  <span className="text-[11px] text-neutral-500 block">ID da Cobrança Vinculada</span>
                  <span className="text-neutral-200 truncate block">{selectedWebhook.chargeId}</span>
                </div>
              </div>

              {/* Raw Payload Preview */}
              <div>
                <span className="text-xs font-mono text-neutral-400 block mb-1.5">
                  Payload JSON Enviado no POST:
                </span>
                <pre className="p-4 bg-neutral-950 border border-neutral-800 rounded-md text-xs font-mono text-neutral-300 overflow-x-auto max-h-72">
                  {(() => {
                    try {
                      return JSON.stringify(JSON.parse(selectedWebhook.payloadJson), null, 2);
                    } catch {
                      return selectedWebhook.payloadJson;
                    }
                  })()}
                </pre>
              </div>
            </div>
          ) : (
            <div className="p-12 text-center border border-dashed border-neutral-800 rounded-md text-neutral-500 space-y-2">
              <Terminal className="w-8 h-8 mx-auto text-neutral-600" />
              <p className="text-xs">Selecione um webhook na lista ao lado para inspecionar.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

