import React, { useState, useEffect, useMemo } from 'react';
import { 
  Search, RefreshCw, Download, CheckCircle, XCircle, Clock, 
  ExternalLink, ChevronRight, Filter, AlertCircle 
} from 'lucide-react';
import StatusBadge from '../components/StatusBadge';
import { api } from '../services/api';

export default function LedgerView({ onInspectCharge, refreshTrigger }) {
  const [charges, setCharges] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [actionLoadingId, setActionLoadingId] = useState(null);

  const fetchCharges = async () => {
    try {
      setError(null);
      const data = await api.listCharges(0, 50);
      const items = Array.isArray(data) ? data : (data?.content || []);
      setCharges(items);
    } catch (err) {
      setError(err.message || 'Falha ao sincronizar livro-razão');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCharges();
  }, [refreshTrigger]);

  // Polling for real-time ledger updates
  useEffect(() => {
    if (!autoRefresh) return;
    const interval = setInterval(() => {
      fetchCharges();
    }, 3500);
    return () => clearInterval(interval);
  }, [autoRefresh]);

  const handleSettle = async (e, id) => {
    e.stopPropagation();
    try {
      setActionLoadingId(id);
      await api.settleCharge(id);
      await fetchCharges();
    } catch (err) {
      alert(`Erro ao liquidar: ${err.message}`);
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleCancel = async (e, id) => {
    e.stopPropagation();
    if (!confirm('Deseja realmente cancelar esta cobrança?')) return;
    try {
      setActionLoadingId(id);
      await api.cancelCharge(id);
      await fetchCharges();
    } catch (err) {
      alert(`Erro ao cancelar: ${err.message}`);
    } finally {
      setActionLoadingId(null);
    }
  };

  const filteredCharges = useMemo(() => {
    return charges.filter((c) => {
      const matchesStatus = statusFilter === 'ALL' || c.status === statusFilter;
      const term = search.toLowerCase().trim();
      const matchesSearch =
        !term ||
        (c.id && c.id.toLowerCase().includes(term)) ||
        (c.endToEndId && c.endToEndId.toLowerCase().includes(term)) ||
        (c.pixKey && c.pixKey.toLowerCase().includes(term)) ||
        (c.recipientName && c.recipientName.toLowerCase().includes(term));
      return matchesStatus && matchesSearch;
    });
  }, [charges, statusFilter, search]);

  const handleExportCsv = () => {
    window.open(api.getExportCsvUrl(), '_blank');
  };

  return (
    <div className="space-y-6">
      {/* View Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-xs font-mono text-neutral-400 mb-1">
            <span>SETTLEMENT LEDGER</span>
            <span>/</span>
            <span className="text-neutral-300">TRANSACTIONS AUDIT</span>
          </div>
          <h1 className="text-xl font-semibold text-neutral-100 tracking-tight">
            Livro-Razão & Auditoria de Transações
          </h1>
          <p className="text-xs text-neutral-400 mt-1">
            Registro imutável de transações com rastreamento completo de ciclo de vida e latência de liquidação.
          </p>
        </div>

        {/* Toolbar */}
        <div className="flex items-center gap-2">
          <label className="flex items-center gap-2 text-xs font-mono text-neutral-400 cursor-pointer bg-neutral-900 border border-neutral-800 px-3 py-2 rounded-md">
            <input
              type="checkbox"
              checked={autoRefresh}
              onChange={(e) => setAutoRefresh(e.target.checked)}
              className="rounded bg-neutral-950 border-neutral-700 text-neutral-100 focus:ring-0"
            />
            <span>Auto-Sync (3s)</span>
          </label>

          <button
            onClick={fetchCharges}
            disabled={loading}
            className="p-2 bg-neutral-900 hover:bg-neutral-800 border border-neutral-800 text-neutral-300 rounded-md transition-colors"
            title="Atualizar Livro-Razão"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>

          <button
            onClick={handleExportCsv}
            className="px-3 py-2 bg-neutral-900 hover:bg-neutral-800 border border-neutral-800 text-neutral-200 text-xs font-mono rounded-md flex items-center gap-1.5 transition-colors"
            title="Exportar registros em CSV"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Exportar CSV</span>
          </button>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="bg-neutral-900 border border-neutral-800 rounded-lg p-3 flex flex-col md:flex-row gap-3 items-center justify-between">
        {/* Search Input */}
        <div className="relative w-full md:w-96">
          <Search className="w-4 h-4 text-neutral-500 absolute left-3 top-2.5" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar por TxID, EndToEndId ou Chave PIX..."
            className="w-full bg-neutral-950 border border-neutral-800 focus:border-neutral-600 text-neutral-100 text-xs font-mono rounded-md pl-9 pr-3 py-2 outline-none transition-colors"
          />
        </div>

        {/* Status Filters */}
        <div className="flex items-center gap-1 overflow-x-auto w-full md:w-auto">
          {['ALL', 'PENDING', 'PROCESSING', 'SETTLED', 'FAILED', 'CANCELLED'].map((st) => (
            <button
              key={st}
              onClick={() => setStatusFilter(st)}
              className={`text-[11px] font-mono px-2.5 py-1.5 rounded transition-colors whitespace-nowrap ${
                statusFilter === st
                  ? 'bg-neutral-800 text-neutral-100 font-semibold'
                  : 'text-neutral-400 hover:text-neutral-200 hover:bg-neutral-950'
              }`}
            >
              {st}
            </button>
          ))}
        </div>
      </div>

      {/* Error notification */}
      {error && (
        <div className="p-3 bg-rose-950/40 border border-rose-800 text-rose-300 text-xs rounded-md flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* High-density Table */}
      <div className="bg-neutral-900 border border-neutral-800 rounded-lg overflow-hidden shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-neutral-800 bg-neutral-950 text-[11px] font-mono uppercase tracking-wider text-neutral-400">
                <th className="py-3 px-4">TxID / EndToEndId</th>
                <th className="py-3 px-4">Chave & Recebedor</th>
                <th className="py-3 px-4 text-right">Valor</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4 text-center">Latência</th>
                <th className="py-3 px-4">Data / Hora</th>
                <th className="py-3 px-4 text-right">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-neutral-800/80 text-xs font-mono">
              {loading && charges.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-neutral-500">
                    <div className="flex items-center justify-center gap-2">
                      <div className="w-4 h-4 border-2 border-neutral-600 border-t-neutral-200 rounded-full animate-spin" />
                      Carregando livro-razão...
                    </div>
                  </td>
                </tr>
              ) : filteredCharges.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-neutral-500">
                    Nenhuma transação encontrada com os filtros aplicados.
                  </td>
                </tr>
              ) : (
                filteredCharges.map((c) => (
                  <tr
                    key={c.id}
                    onClick={() => onInspectCharge(c.id)}
                    className="hover:bg-neutral-800/50 cursor-pointer transition-colors group"
                  >
                    {/* TxID & EndToEndId */}
                    <td className="py-3 px-4">
                      <div className="font-semibold text-neutral-200 group-hover:text-white transition-colors truncate max-w-[190px]">
                        {c.id}
                      </div>
                      <div className="text-[11px] text-neutral-500 truncate max-w-[190px]">
                        {c.endToEndId || 'EndToEndId pendente'}
                      </div>
                    </td>

                    {/* Key & Recipient */}
                    <td className="py-3 px-4">
                      <div className="text-neutral-200">
                        {c.pixKey}
                        <span className="ml-1 text-[10px] text-neutral-500">({c.pixKeyType})</span>
                      </div>
                      <div className="text-[11px] text-neutral-500">
                        {c.recipientName || 'PixFlow Engine'}
                      </div>
                    </td>

                    {/* Amount */}
                    <td className="py-3 px-4 text-right font-bold text-neutral-100">
                      R$ {Number(c.amount).toFixed(2).replace('.', ',')}
                    </td>

                    {/* Status Badge */}
                    <td className="py-3 px-4">
                      <StatusBadge status={c.status} />
                    </td>

                    {/* Latency */}
                    <td className="py-3 px-4 text-center">
                      {c.settlementDurationMs ? (
                        <span className="text-[11px] text-emerald-400 bg-emerald-950/40 px-1.5 py-0.5 rounded border border-emerald-800/40">
                          {c.settlementDurationMs}ms
                        </span>
                      ) : (
                        <span className="text-[11px] text-neutral-600">—</span>
                      )}
                    </td>

                    {/* Timestamp */}
                    <td className="py-3 px-4 text-[11px] text-neutral-400 whitespace-nowrap">
                      {new Date(c.createdAt).toLocaleTimeString('pt-BR')} - {new Date(c.createdAt).toLocaleDateString('pt-BR')}
                    </td>

                    {/* Quick Actions */}
                    <td className="py-3 px-4 text-right whitespace-nowrap" onClick={(e) => e.stopPropagation()}>
                      <div className="flex items-center justify-end gap-1.5">
                        {c.status === 'PENDING' && (
                          <>
                            <button
                              onClick={(e) => handleSettle(e, c.id)}
                              disabled={actionLoadingId === c.id}
                              className="p-1 text-emerald-400 hover:text-emerald-300 hover:bg-neutral-800 rounded transition-colors"
                              title="Liquidar Imediatamente"
                            >
                              <CheckCircle className="w-4 h-4" />
                            </button>
                            <button
                              onClick={(e) => handleCancel(e, c.id)}
                              disabled={actionLoadingId === c.id}
                              className="p-1 text-rose-400 hover:text-rose-300 hover:bg-neutral-800 rounded transition-colors"
                              title="Cancelar Cobrança"
                            >
                              <XCircle className="w-4 h-4" />
                            </button>
                          </>
                        )}
                        <button
                          onClick={() => onInspectCharge(c.id)}
                          className="p-1 text-neutral-400 hover:text-neutral-200 hover:bg-neutral-800 rounded transition-colors"
                          title="Inspecionar Auditoria"
                        >
                          <ChevronRight className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Footer Summary */}
        <div className="p-3 bg-neutral-950 border-t border-neutral-800 text-xs font-mono text-neutral-500 flex justify-between items-center">
          <span>Exibindo {filteredCharges.length} de {charges.length} transações registradas</span>
          <span>Motor de Liquidação Concorrente</span>
        </div>
      </div>
    </div>
  );
}

