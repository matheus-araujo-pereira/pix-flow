import React from 'react';
import { DollarSign, ShieldCheck, Timer, Layers, ArrowUpRight } from 'lucide-react';

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
      <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-4 relative overflow-hidden backdrop-blur-sm group hover:border-slate-700 transition-all">
        <div className="flex items-center justify-between mb-2">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Volume Liquidado</span>
          <div className="p-2 rounded-lg bg-emerald-500/10 text-emerald-400">
            <DollarSign className="w-4 h-4" />
          </div>
        </div>
        <div className="text-xl sm:text-2xl font-extrabold text-white tracking-tight">
          {formatBRL(volume)}
        </div>
        <p className="text-[11px] text-emerald-400 flex items-center gap-1 mt-1 font-medium">
          <ArrowUpRight className="w-3 h-3" />
          {metrics?.settledCount || 0} transações confirmadas
        </p>
      </div>

      {/* Success Rate */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-4 relative overflow-hidden backdrop-blur-sm group hover:border-slate-700 transition-all">
        <div className="flex items-center justify-between mb-2">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Taxa de Sucesso</span>
          <div className="p-2 rounded-lg bg-indigo-500/10 text-indigo-400">
            <ShieldCheck className="w-4 h-4" />
          </div>
        </div>
        <div className="text-xl sm:text-2xl font-extrabold text-white tracking-tight">
          {successRate}%
        </div>
        <div className="w-full bg-slate-800 rounded-full h-1.5 mt-2 overflow-hidden">
          <div 
            className="bg-gradient-to-r from-emerald-500 to-indigo-500 h-1.5 rounded-full transition-all duration-500"
            style={{ width: `${Math.min(100, Math.max(0, successRate))}%` }}
          />
        </div>
      </div>

      {/* Latency / Avg Settlement */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-4 relative overflow-hidden backdrop-blur-sm group hover:border-slate-700 transition-all">
        <div className="flex items-center justify-between mb-2">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Tempo Médio Liquidação</span>
          <div className="p-2 rounded-lg bg-amber-500/10 text-amber-400">
            <Timer className="w-4 h-4" />
          </div>
        </div>
        <div className="text-xl sm:text-2xl font-extrabold text-white tracking-tight font-mono">
          {avgLatency > 0 ? `${avgLatency} ms` : (latency ? `${latency} ms` : '---')}
        </div>
        <p className="text-[11px] text-slate-400 mt-1">
          Ping API: <span className="text-emerald-400 font-mono">{latency ? `${latency}ms` : '---'}</span>
        </p>
      </div>

      {/* Active Queue / Workers */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-4 relative overflow-hidden backdrop-blur-sm group hover:border-slate-700 transition-all">
        <div className="flex items-center justify-between mb-2">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Fila Assíncrona</span>
          <div className="p-2 rounded-lg bg-purple-500/10 text-purple-400">
            <Layers className="w-4 h-4" />
          </div>
        </div>
        <div className="text-xl sm:text-2xl font-extrabold text-white tracking-tight flex items-center gap-2">
          <span>{activeQueue}</span>
          {activeQueue > 0 && (
            <span className="text-xs px-2 py-0.5 rounded-full bg-indigo-500/20 text-indigo-400 animate-pulse font-medium">
              Processando
            </span>
          )}
        </div>
        <p className="text-[11px] text-slate-400 mt-1">
          AWS SQS / Workers Ativos
        </p>
      </div>

    </div>
  );
}

