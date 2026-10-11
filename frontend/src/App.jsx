import React, { useState, useEffect, useCallback } from 'react';
import Navbar from './components/Navbar';
import LatencyMonitor from './components/LatencyMonitor';
import PixForm from './components/PixForm';
import TransactionList from './components/TransactionList';
import QrCodeModal from './components/QrCodeModal';
import ArchitectureModal from './components/ArchitectureModal';
import { api } from './services/api';
import { AlertCircle, CheckCircle2, CloudRain } from 'lucide-react';

export default function App() {
  const [transactions, setTransactions] = useState([]);
  const [metrics, setMetrics] = useState(null);
  const [latency, setLatency] = useState(null);
  const [isOnline, setIsOnline] = useState(true);
  const [loading, setLoading] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [settlingId, setSettlingId] = useState(null);
  const [cancellingId, setCancellingId] = useState(null);
  const [selectedCharge, setSelectedCharge] = useState(null);
  const [isArchOpen, setIsArchOpen] = useState(false);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [toast, setToast] = useState(null);

  const showToast = (message, type = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000);
  };

  const fetchTransactions = useCallback(async () => {
    try {
      const data = await api.listCharges(0, 30);
      if (data && data.content) {
        setTransactions(data.content);
      }
    } catch (err) {
      console.warn('Erro ao carregar transações:', err.message);
    }
  }, []);

  const fetchMetrics = useCallback(async () => {
    try {
      const data = await api.getMetrics();
      setMetrics(data);
    } catch (err) {
      console.warn('Erro ao carregar métricas:', err.message);
    }
  }, []);

  const checkPing = useCallback(async () => {
    const res = await api.pingLatency();
    setIsOnline(res.online);
    setLatency(res.latency);
  }, []);

  const handleRefreshAll = async () => {
    setLoading(true);
    await Promise.all([fetchTransactions(), fetchMetrics(), checkPing()]);
    setLoading(false);
  };

  // Initial load
  useEffect(() => {
    handleRefreshAll();
  }, []);

  // Polling loop for latency and live updates
  useEffect(() => {
    const pingInterval = setInterval(checkPing, 4000);

    let streamInterval = null;
    if (autoRefresh) {
      streamInterval = setInterval(() => {
        fetchTransactions();
        fetchMetrics();
      }, 2500);
    }

    return () => {
      clearInterval(pingInterval);
      if (streamInterval) clearInterval(streamInterval);
    };
  }, [autoRefresh, checkPing, fetchTransactions, fetchMetrics]);

  // Handle Create Charge
  const handleCreateCharge = async (formData) => {
    setFormLoading(true);
    try {
      const created = await api.createCharge(formData);
      showToast(`Cobrança PIX ${created.txId} gerada com sucesso!`, 'success');
      setSelectedCharge(created);
      await Promise.all([fetchTransactions(), fetchMetrics()]);
    } catch (err) {
      showToast(err.message, 'error');
      throw err;
    } finally {
      setFormLoading(false);
    }
  };

  // Handle Manual Settle
  const handleSettle = async (chargeId) => {
    setSettlingId(chargeId);
    try {
      const updated = await api.settleCharge(chargeId);
      showToast(`Transação ${updated.txId} liquidada com status ${updated.status}!`, 'success');
      if (selectedCharge && selectedCharge.id === chargeId) {
        setSelectedCharge(updated);
      }
      await Promise.all([fetchTransactions(), fetchMetrics()]);
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setSettlingId(null);
    }
  };

  // Handle Cancel
  const handleCancel = async (chargeId) => {
    setCancellingId(chargeId);
    try {
      const updated = await api.cancelCharge(chargeId);
      showToast(`Transação ${updated.txId} cancelada com sucesso!`, 'success');
      if (selectedCharge && selectedCharge.id === chargeId) {
        setSelectedCharge(updated);
      }
      await Promise.all([fetchTransactions(), fetchMetrics()]);
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setCancellingId(null);
    }
  };

  return (
    <div className="min-h-screen bg-[#0B0F19] text-slate-100 flex flex-col justify-between">
      
      {/* Top Navigation */}
      <div>
        <Navbar
          latency={latency}
          isOnline={isOnline}
          onOpenArchitecture={() => setIsArchOpen(true)}
        />

        {/* Cold Start / Offline Banner */}
        {!isOnline && (
          <div className="bg-amber-500/10 border-b border-amber-500/20 px-4 py-2.5 text-center text-xs text-amber-300 flex items-center justify-center gap-2">
            <CloudRain className="w-4 h-4 text-amber-400" />
            <span>
              Conectando ao backend... No plano gratuito (Render), o servidor pode demorar cerca de 40s para acordar (cold start).
            </span>
          </div>
        )}

        {/* Main Workspace */}
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          
          {/* Header Hero */}
          <div className="mb-6">
            <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white">
              Motor de Liquidação Transacional PIX
            </h1>
            <p className="text-sm text-slate-400 mt-1">
              Processamento assíncrono baseado em eventos com Java 21, Spring Boot 3, AWS SQS e PostgreSQL
            </p>
          </div>

          {/* KPI Latency and Metrics Bar */}
          <LatencyMonitor metrics={metrics} latency={latency} />

          {/* Operational Grid: Form & Ledger */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
            
            {/* Left: Trigger PIX Form (4 cols on large) */}
            <div className="lg:col-span-5 w-full">
              <PixForm onSubmit={handleCreateCharge} loading={formLoading} />
            </div>

            {/* Right: Real-time Transaction Ledger (7 cols on large) */}
            <div className="lg:col-span-7 w-full">
              <TransactionList
                transactions={transactions}
                loading={loading}
                autoRefresh={autoRefresh}
                onToggleAutoRefresh={() => setAutoRefresh(!autoRefresh)}
                onRefresh={handleRefreshAll}
                onSelectCharge={setSelectedCharge}
                onSettle={handleSettle}
                onCancel={handleCancel}
                settlingId={settlingId}
                cancellingId={cancellingId}
              />
            </div>

          </div>

        </main>
      </div>

      {/* Footer */}
      <footer className="border-t border-slate-800/80 bg-slate-950/40 py-6 text-center text-xs text-slate-500">
        <div className="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-3">
          <div>
            PixFlow — Arquitetura de Alto Nível para Portfólio Cloud DevOps (Java 21, AWS, Linux).
          </div>
          <div className="text-slate-400 font-mono text-[11px]">
            Desenvolvido por Matheus Araujo Pereira • 100% Free Tier Compliant
          </div>
        </div>
      </footer>

      {/* Floating Notification Toast */}
      {toast && (
        <div className="fixed bottom-5 right-5 z-50 animate-bounce-short">
          <div className={`flex items-center gap-2 px-4 py-3 rounded-2xl shadow-2xl text-xs font-semibold border ${
            toast.type === 'success'
              ? 'bg-emerald-950/90 text-emerald-300 border-emerald-500/40'
              : 'bg-rose-950/90 text-rose-300 border-rose-500/40'
          }`}>
            {toast.type === 'success' ? (
              <CheckCircle2 className="w-4 h-4 text-emerald-400" />
            ) : (
              <AlertCircle className="w-4 h-4 text-rose-400" />
            )}
            <span>{toast.message}</span>
          </div>
        </div>
      )}

      {/* QR Code Modal */}
      <QrCodeModal
        charge={selectedCharge}
        onClose={() => setSelectedCharge(null)}
        onSettle={handleSettle}
        settling={settlingId === selectedCharge?.id}
      />

      {/* Architecture Visual Modal */}
      <ArchitectureModal
        isOpen={isArchOpen}
        onClose={() => setIsArchOpen(false)}
      />

    </div>
  );
}
