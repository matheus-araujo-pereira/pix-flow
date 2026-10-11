import React from 'react';
import { DollarSign, ShieldCheck, Timer, Layers } from 'lucide-react';

export default function LatencyMonitor({ metrics, latency }) {
  const formatBRL = (value) => {
    if (value === null || value === undefined) return 'R$ 0,00';
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(value);
  };

  const volume = metrics?.totalSettledVolume || 0;
  const successRate = metrics?.successRatePercentage !== undefined ? metrics.successRatePercentage : 100;
  const avgLatency = metrics?.averageSettlementTimeMs || 0;
  const activeQueue = (metrics?.pendingCount || 0) + (metrics?.processingCount || 0);

  return (
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4 mb-6">
      {/* Volume Settled */}
      <div className="bg-neutral-900 border border-neutral-800 rounded-lg p-4">
        <div className="flex items-center justify-between mb-2">
          <span className="text-[11px] font-mono uppercase tracking-wider text-neutral-400">Volume Liquidado</span>
          <DollarSign className="w-4 h-4 text-emerald-400" />
        </div>
        <div className="text-xl font-bold font-mono text-neutral-100">
          {formatBRL(volume)}
        </div>
        <div className="text-[11px] font-mono text-neutral-500 mt-1">
          {metrics?.settledCount || 0} liquidações confirmadas
        </div>
      </div>

      {/* Success Rate */}
      <div className="bg-neutral-900 border border-neutral-800 rounded-lg p-4">
        <div className="flex items-center justify-between mb-2">
          <span className="text-[11px] font-mono uppercase tracking-wider text-neutral-400">Taxa de Sucesso</span>
          <ShieldCheck className="w-4 h-4 text-neutral-300" />
        </div>
        <div className="text-xl font-bold font-mono text-neutral-100">
          {successRate}%
        </div>
        <div className="w-full bg-neutral-950 border border-neutral-800 rounded-full h-1.5 mt-2 overflow-hidden">
          <div 
            className="bg-emerald-500 h-1.5 rounded-full transition-all duration-300"
            style={{ width: `${Math.min(100, Math.max(0, successRate))}%` }}
          />
        </div>
      </div>

      {/* Latency / Avg Settlement */}
      <div className="bg-neutral-900 border border-neutral-800 rounded-lg p-4">
        <div className="flex items-center justify-between mb-2">
          <span className="text-[11px] font-mono uppercase tracking-wider text-neutral-400">Tempo de Liquidação</span>
          <Timer className="w-4 h-4 text-amber-400" />
        </div>
        <div className="text-xl font-bold font-mono text-neutral-100">
          {avgLatency > 0 ? `${avgLatency}ms` : (latency ? `${latency}ms` : '---')}
        </div>
        <div className="text-[11px] font-mono text-neutral-500 mt-1">
          Latência HTTP: <span className="text-neutral-300">{latency ? `${latency}ms` : '---'}</span>
        </div>
      </div>

      {/* Active Queue / Workers */}
      <div className="bg-neutral-900 border border-neutral-800 rounded-lg p-4">
        <div className="flex items-center justify-between mb-2">
          <span className="text-[11px] font-mono uppercase tracking-wider text-neutral-400">Fila Transacional</span>
          <Layers className="w-4 h-4 text-neutral-400" />
        </div>
        <div className="text-xl font-bold font-mono text-neutral-100 flex items-center gap-2">
          <span>{activeQueue}</span>
          {activeQueue > 0 && (
            <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-amber-950 text-amber-400 border border-amber-800">
              Processando
            </span>
          )}
        </div>
        <div className="text-[11px] font-mono text-neutral-500 mt-1">
          Mensageria Assíncrona
        </div>
      </div>
    </div>
  );
}
