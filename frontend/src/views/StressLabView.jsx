import React, { useState } from 'react';
import { Cpu, Zap, Play, CheckCircle2, AlertTriangle, Clock, RefreshCw, BarChart2 } from 'lucide-react';
import { api } from '../services/api';
import StatusBadge from '../components/StatusBadge';

export default function StressLabView({ onInspectCharge }) {
  const [batchSize, setBatchSize] = useState(10);
  const [baseAmount, setBaseAmount] = useState('25.50');
  const [webhookUrl, setWebhookUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [batchResult, setBatchResult] = useState(null);
  const [error, setError] = useState(null);

  // Pool of mathematically valid CPFs and CNPJs
  const validTestKeys = [
    { type: 'CPF', key: '529.982.247-25', name: 'Investidor Alfa' },
    { type: 'CPF', key: '111.444.777-35', name: 'Investidor Beta' },
    { type: 'CPF', key: '000.000.001-91', name: 'Investidor Gama' },
    { type: 'CNPJ', key: '04.252.011/0001-10', name: 'Fintech Hub LTDA' },
    { type: 'CNPJ', key: '11.222.333/0001-81', name: 'Gateway Express S.A.' },
    { type: 'EMAIL', key: 'treasury@pixflow.io', name: 'Tesouraria PixFlow' },
    { type: 'PHONE', key: '+5511987654321', name: 'Operações SP' },
    { type: 'RANDOM', key: 'a8b5e9d2-3c4f-4a1e-8b9a-1c2d3e4f5a6b', name: 'Custodiante Central' },
  ];

  const handleRunBatch = async () => {
    setError(null);
    setLoading(true);
    setBatchResult(null);

    try {
      const items = [];
      const parsedAmount = parseFloat(baseAmount) || 10.0;

      for (let i = 0; i < batchSize; i++) {
        const keyObj = validTestKeys[i % validTestKeys.length];
        items.push({
          pixKey: keyObj.key,
          pixKeyType: keyObj.type,
          amount: parseFloat((parsedAmount + (i * 1.5)).toFixed(2)),
          description: `Stress Test Concorrente #${i + 1}`,
          recipientName: keyObj.name,
          webhookUrl: webhookUrl.trim() || undefined,
        });
      }

      const startTime = performance.now();
      const response = await api.createBatchCharges({ charges: items });
      const clientTotalTime = Math.round(performance.now() - startTime);

      setBatchResult({
        ...response,
        clientTotalTime,
        throughput: response.durationMs > 0 ? ((response.total / (response.durationMs / 1000))).toFixed(1) : 'N/A'
      });
    } catch (err) {
      setError(err.message || 'Falha ao executar teste de estresse concorrente');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      {/* Header */}
      <div>
        <div className="flex items-center gap-2 text-xs font-mono text-neutral-400 mb-1">
          <span>STRESS LAB</span>
          <span>/</span>
          <span className="text-neutral-300">CONCURRENT BATCH DISPATCHER</span>
        </div>
        <h1 className="text-xl font-semibold text-neutral-100 tracking-tight">
          Laboratório de Carga & Concorrência
        </h1>
        <p className="text-xs text-neutral-400 mt-1">
          Dispare lotes paralelos de cobranças PIX simultâneas para avaliar o pool de conexões do PostgreSQL e a taxa de processamento assíncrono dos workers.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Controls Card */}
        <div className="lg:col-span-5 bg-neutral-900 border border-neutral-800 rounded-lg p-5 space-y-5">
          <h2 className="text-xs font-mono uppercase tracking-wider text-neutral-400">
            Parâmetros de Carga
          </h2>

          <div className="space-y-4">
            {/* Batch Size Selector */}
            <div>
              <label className="block text-xs font-mono text-neutral-400 uppercase mb-2">
                Volume de Transações Simultâneas
              </label>
              <div className="grid grid-cols-4 gap-2">
                {[5, 10, 15, 20].map((size) => (
                  <button
                    key={size}
                    type="button"
                    onClick={() => setBatchSize(size)}
                    className={`py-2 text-xs font-mono rounded border transition-colors ${
                      batchSize === size
                        ? 'bg-neutral-800 border-neutral-600 text-neutral-100 font-bold'
                        : 'bg-neutral-950 border-neutral-800 text-neutral-400 hover:text-neutral-200'
                    }`}
                  >
                    {size} ops
                  </button>
                ))}
              </div>
            </div>

            {/* Base Amount */}
            <div>
              <label className="block text-xs font-mono text-neutral-400 uppercase mb-1.5">
                Valor Base por Cobrança (R$)
              </label>
              <input
                type="number"
                step="0.01"
                min="0.01"
                value={baseAmount}
                onChange={(e) => setBaseAmount(e.target.value)}
                className="w-full bg-neutral-950 border border-neutral-800 focus:border-neutral-600 text-neutral-100 text-xs font-mono rounded-md px-3 py-2 outline-none"
              />
            </div>

            {/* Optional Webhook */}
            <div>
              <label className="block text-xs font-mono text-neutral-400 uppercase mb-1.5">
                Webhook de Notificação (Opcional)
              </label>
              <input
                type="url"
                value={webhookUrl}
                onChange={(e) => setWebhookUrl(e.target.value)}
                placeholder="https://webhook.site/..."
                className="w-full bg-neutral-950 border border-neutral-800 focus:border-neutral-600 text-neutral-100 text-xs font-mono rounded-md px-3 py-2 outline-none"
              />
            </div>

            {/* Info notice */}
            <div className="p-3 bg-neutral-950 border border-neutral-800 rounded-md text-[11px] font-mono text-neutral-500 space-y-1">
              <div>• Transações paralelas executadas em pool assíncrono.</div>
              <div>• Cada cobrança gera hash EMV e registro no livro-razão.</div>
              <div>• Respeita limites do plano Free Tier da nuvem.</div>
            </div>

            {/* Execute Button */}
            <button
              onClick={handleRunBatch}
              disabled={loading}
              className="w-full py-2.5 px-4 bg-neutral-100 hover:bg-white text-neutral-950 font-semibold text-xs font-mono uppercase tracking-wider rounded-md flex items-center justify-center gap-2 transition-all disabled:opacity-50"
            >
              {loading ? (
                <>
                  <RefreshCw className="w-4 h-4 animate-spin" />
                  Disparando Lote Concorrente...
                </>
              ) : (
                <>
                  <Zap className="w-4 h-4" />
                  Disparar {batchSize} Cobranças em Paralelo
                </>
              )}
            </button>
          </div>
        </div>

        {/* Results Card */}
        <div className="lg:col-span-7 bg-neutral-900 border border-neutral-800 rounded-lg p-5 space-y-5">
          <div className="flex items-center justify-between">
            <h2 className="text-xs font-mono uppercase tracking-wider text-neutral-400">
              Telemetria do Lote
            </h2>
            {batchResult && (
              <span className="text-[11px] font-mono text-emerald-400 bg-emerald-950/60 border border-emerald-800/60 px-2 py-0.5 rounded">
                LOTE CONCLUÍDO
              </span>
            )}
          </div>

          {error && (
            <div className="p-3 bg-rose-950/40 border border-rose-800 text-rose-300 text-xs rounded-md flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {batchResult ? (
            <div className="space-y-4">
              {/* Metrics Grid */}
              <div className="grid grid-cols-3 gap-3">
                <div className="bg-neutral-950 border border-neutral-800 rounded-md p-3">
                  <span className="text-[11px] font-mono text-neutral-500 block">Total Processado</span>
                  <span className="text-lg font-mono font-bold text-neutral-100">
                    {batchResult.successful} / {batchResult.total}
                  </span>
                </div>
                <div className="bg-neutral-950 border border-neutral-800 rounded-md p-3">
                  <span className="text-[11px] font-mono text-neutral-500 block">Tempo do Motor</span>
                  <span className="text-lg font-mono font-bold text-neutral-100">
                    {batchResult.durationMs}ms
                  </span>
                </div>
                <div className="bg-neutral-950 border border-neutral-800 rounded-md p-3">
                  <span className="text-[11px] font-mono text-neutral-500 block">Throughput (TPS)</span>
                  <span className="text-lg font-mono font-bold text-emerald-400">
                    {batchResult.throughput} tx/s
                  </span>
                </div>
              </div>

              {/* Items List */}
              <div className="border border-neutral-800 rounded-md overflow-hidden">
                <div className="bg-neutral-950 px-3 py-2 text-[11px] font-mono uppercase text-neutral-400 border-b border-neutral-800 flex justify-between">
                  <span>Itens do Lote #{batchResult.batchId?.substring(0, 8)}</span>
                  <span>Status</span>
                </div>
                <div className="max-h-64 overflow-y-auto divide-y divide-neutral-800/80 text-xs font-mono">
                  {batchResult.results?.map((item, idx) => (
                    <div
                      key={idx}
                      onClick={() => item.chargeId && onInspectCharge(item.chargeId)}
                      className="p-2.5 bg-neutral-900/60 hover:bg-neutral-800 cursor-pointer flex items-center justify-between transition-colors"
                    >
                      <div className="truncate max-w-[280px]">
                        <span className="text-neutral-500 mr-2">#{idx + 1}</span>
                        <span className="text-neutral-200">{item.chargeId || 'Falha na emissão'}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        {item.status ? (
                          <StatusBadge status={item.status} />
                        ) : (
                          <span className="text-rose-400 text-[11px]">{item.error}</span>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          ) : (
            <div className="p-12 text-center border border-dashed border-neutral-800 rounded-md text-neutral-500 space-y-2">
              <Cpu className="w-8 h-8 mx-auto text-neutral-600" />
              <p className="text-xs">Nenhum lote concorrente disparado.</p>
              <p className="text-[11px] text-neutral-600">
                Selecione o tamanho do lote e clique em Disparar para iniciar o teste de estresse.
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

