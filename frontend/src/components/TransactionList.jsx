import React, { useState } from 'react';
import { RefreshCw, QrCode, Ban, Zap, ExternalLink, Filter } from 'lucide-react';
import StatusBadge from './StatusBadge';

export default function TransactionList({
  transactions,
  loading,
  autoRefresh,
  onToggleAutoRefresh,
  onRefresh,
  onSelectCharge,
  onSettle,
  onCancel,
  settlingId,
  cancellingId,
}) {
  const [filter, setFilter] = useState('ALL');

  const filtered = transactions.filter(t => {
    if (filter === 'ALL') return true;
    return t.status === filter;
  });

  const formatBRL = (val) => new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(val || 0);

  const formatDate = (dateStr) => {
    if (!dateStr) return '---';
    const date = new Date(dateStr);
    return date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  };

  return (
    <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-5 sm:p-6 backdrop-blur-sm shadow-xl">
      
      {/* Header & Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-5">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-base font-bold text-white">Ledger de Transações em Tempo Real</h2>
            <span className="text-xs px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 font-mono">
              {filtered.length}
            </span>
          </div>
          <p className="text-xs text-slate-400">Stream assíncrono sincronizado com PostgreSQL e Workers</p>
        </div>

        <div className="flex items-center gap-2 self-start sm:self-auto">
          {/* Auto Refresh Toggle */}
          <button
            onClick={onToggleAutoRefresh}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold border transition-all ${
              autoRefresh
                ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                : 'bg-slate-800 text-slate-400 border-slate-700'
            }`}
            title="Alternar atualização automática a cada 2.5s"
          >
            <span className={`w-2 h-2 rounded-full ${autoRefresh ? 'bg-emerald-400 animate-ping' : 'bg-slate-500'}`} />
            <span>Auto-Sync</span>
          </button>

          {/* Manual Refresh */}
          <button
            onClick={onRefresh}
            disabled={loading}
            className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700 transition-colors disabled:opacity-50"
            title="Atualizar lista agora"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin text-emerald-400' : ''}`} />
          </button>
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center gap-1.5 overflow-x-auto pb-3 mb-3 border-b border-slate-800 scrollbar-none text-xs">
        <Filter className="w-3.5 h-3.5 text-slate-500 mr-1 shrink-0" />
        {['ALL', 'PENDING', 'PROCESSING', 'SETTLED', 'FAILED', 'CANCELLED'].map((st) => (
          <button
            key={st}
            onClick={() => setFilter(st)}
            className={`px-2.5 py-1 rounded-lg font-medium whitespace-nowrap transition-colors ${
              filter === st
                ? 'bg-slate-800 text-white font-semibold'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
            }`}
          >
            {st === 'ALL' ? 'Todos' : st}
          </button>
        ))}
      </div>

      {/* Transactions Table / Cards */}
      {filtered.length === 0 ? (
        <div className="text-center py-12 px-4">
          <div className="h-12 w-12 rounded-2xl bg-slate-800/50 flex items-center justify-center mx-auto mb-3 text-slate-500">
            <Zap className="w-6 h-6" />
          </div>
          <p className="text-sm font-semibold text-slate-300">Nenhuma transação encontrada</p>
          <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
            Dispare um pagamento através do formulário ao lado para observar o ciclo assíncrono de liquidação em ação.
          </p>
        </div>
      ) : (
        <div className="overflow-x-auto -mx-5 sm:mx-0">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950/40 text-slate-400 uppercase tracking-wider font-semibold border-b border-slate-800">
              <tr>
                <th className="py-3 px-4">Identificador / TxID</th>
                <th className="py-3 px-4">Chave PIX</th>
                <th className="py-3 px-4">Valor</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4">Horário</th>
                <th className="py-3 px-4 text-right">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-sans">
              {filtered.map((t) => (
                <tr key={t.id} className="hover:bg-slate-800/30 transition-colors group">
                  
                  {/* TxID & EndToEnd */}
                  <td className="py-3.5 px-4">
                    <div className="font-mono font-medium text-slate-200 truncate max-w-[140px] sm:max-w-[180px]">
                      {t.txId}
                    </div>
                    <div className="font-mono text-[10px] text-slate-500 truncate max-w-[140px] sm:max-w-[180px]">
                      {t.endToEndId}
                    </div>
                  </td>

                  {/* PIX Key */}
                  <td className="py-3.5 px-4">
                    <div className="font-mono text-slate-300 truncate max-w-[130px]">
                      {t.pixKey}
                    </div>
                    <div className="text-[10px] text-slate-500 font-semibold">
                      {t.pixKeyType}
                    </div>
                  </td>

                  {/* Amount */}
                  <td className="py-3.5 px-4 font-mono font-bold text-white whitespace-nowrap">
                    {formatBRL(t.amount)}
                  </td>

                  {/* Status Badge */}
                  <td className="py-3.5 px-4">
                    <StatusBadge status={t.status} />
                    {t.failureReason && (
                      <div className="text-[10px] text-rose-400 mt-0.5 truncate max-w-[140px]" title={t.failureReason}>
                        {t.failureReason}
                      </div>
                    )}
                  </td>

                  {/* Timestamp */}
                  <td className="py-3.5 px-4 font-mono text-slate-400 whitespace-nowrap">
                    {formatDate(t.createdAt)}
                  </td>

                  {/* Row Actions */}
                  <td className="py-3.5 px-4 text-right whitespace-nowrap">
                    <div className="flex items-center justify-end gap-1.5">
                      
                      {/* View QR Code */}
                      <button
                        onClick={() => onSelectCharge(t)}
                        className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 transition-colors"
                        title="Ver QR Code & Copia e Cola"
                      >
                        <QrCode className="w-3.5 h-3.5" />
                      </button>

                      {/* Manual Settle Button (if pending) */}
                      {t.status === 'PENDING' && (
                        <button
                          onClick={() => onSettle(t.id)}
                          disabled={settlingId === t.id}
                          className="p-1.5 rounded-lg bg-indigo-600/30 hover:bg-indigo-600/50 text-indigo-300 border border-indigo-500/30 transition-colors disabled:opacity-50"
                          title="Simular Liquidação Imediata"
                        >
                          <Zap className={`w-3.5 h-3.5 ${settlingId === t.id ? 'animate-spin' : ''}`} />
                        </button>
                      )}

                      {/* Cancel Button (if pending) */}
                      {t.status === 'PENDING' && (
                        <button
                          onClick={() => onCancel(t.id)}
                          disabled={cancellingId === t.id}
                          className="p-1.5 rounded-lg bg-rose-600/20 hover:bg-rose-600/40 text-rose-300 border border-rose-500/20 transition-colors disabled:opacity-50"
                          title="Cancelar Cobrança"
                        >
                          <Ban className={`w-3.5 h-3.5 ${cancellingId === t.id ? 'animate-spin' : ''}`} />
                        </button>
                      )}

                    </div>
                  </td>

                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

    </div>
  );
}
