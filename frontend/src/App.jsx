import React, { useState, useEffect, useCallback } from 'react';
import Navbar from './components/Navbar';
import LatencyMonitor from './components/LatencyMonitor';
import TerminalView from './views/TerminalView';
import LedgerView from './views/LedgerView';
import StressLabView from './views/StressLabView';
import WebhooksView from './views/WebhooksView';
import TutorialView from './views/TutorialView';
import TransactionDrawer from './components/TransactionDrawer';
import QrCodeModal from './components/QrCodeModal';
import ArchitectureModal from './components/ArchitectureModal';
import { api } from './services/api';
import { AlertCircle, CheckCircle2, ServerCrash } from 'lucide-react';

export default function App() {
  const getInitialTab = () => {
    const hash = window.location.hash.replace('#', '');
    return ['terminal', 'ledger', 'stress', 'webhooks', 'tutorial'].includes(hash) ? hash : 'terminal';
  };

  const [currentTab, setCurrentTab] = useState(getInitialTab);
  const [metrics, setMetrics] = useState(null);
  const [latency, setLatency] = useState(null);
  const [isOnline, setIsOnline] = useState(true);
  const [selectedCharge, setSelectedCharge] = useState(null);
  const [selectedInspectId, setSelectedInspectId] = useState(null);
  const [isArchOpen, setIsArchOpen] = useState(false);
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [toast, setToast] = useState(null);

  const handleSelectTab = (tab) => {
    setCurrentTab(tab);
    window.location.hash = tab;
  };

  useEffect(() => {
    const onHashChange = () => {
      const hash = window.location.hash.replace('#', '');
      if (['terminal', 'ledger', 'stress', 'webhooks', 'tutorial'].includes(hash)) {
        setCurrentTab(hash);
      }
    };
    window.addEventListener('hashchange', onHashChange);
    return () => window.removeEventListener('hashchange', onHashChange);
  }, []);

  const showToast = (message, type = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000);
  };

  const fetchMetrics = useCallback(async () => {
    try {
      const data = await api.getMetrics();
      setMetrics(data);
    } catch (err) {
      // Backend may be sleeping or restarting
    }
  }, []);

  const checkPing = useCallback(async () => {
    const res = await api.pingLatency();
    setIsOnline(res.online);
    setLatency(res.latency);
  }, []);

  useEffect(() => {
    fetchMetrics();
    checkPing();

    const pingInterval = setInterval(checkPing, 5000);
    const metricsInterval = setInterval(fetchMetrics, 4000);

    return () => {
      clearInterval(pingInterval);
      clearInterval(metricsInterval);
    };
  }, [checkPing, fetchMetrics]);

  const handleChargeCreated = (charge) => {
    showToast(`Cobrança PIX ${charge.id?.substring(0, 8)} gerada com sucesso!`, 'success');
    setRefreshTrigger((prev) => prev + 1);
    fetchMetrics();
  };

  const handleManualSettle = async (id) => {
    try {
      await api.settleCharge(id);
      showToast('Transação liquidada manualmente!', 'success');
      setRefreshTrigger((prev) => prev + 1);
      fetchMetrics();
      if (selectedCharge && selectedCharge.id === id) {
        setSelectedCharge((prev) => ({ ...prev, status: 'SETTLED' }));
      }
    } catch (err) {
      showToast(err.message, 'error');
    }
  };

  return (
    <div className="min-h-screen bg-[#09090b] text-neutral-100 flex flex-col justify-between selection:bg-neutral-800 selection:text-white">
      {/* Header and Nav */}
      <div>
        <Navbar
          currentTab={currentTab}
          onSelectTab={handleSelectTab}
          latency={latency}
          isOnline={isOnline}
          onOpenArchitecture={() => setIsArchOpen(true)}
        />

        {/* Cold Start / Offline Banner */}
        {!isOnline && (
          <div className="bg-amber-950/60 border-b border-amber-800/80 px-4 py-2 text-center text-xs font-mono text-amber-300 flex items-center justify-center gap-2">
            <ServerCrash className="w-4 h-4 text-amber-400 shrink-0" />
            <span>
              Conectando ao backend... No plano gratuito do Render, o servidor pode demorar até 45s para reiniciar após inatividade (cold start).
            </span>
          </div>
        )}

        {/* Main Workspace */}
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* Top KPI Metrics Strip */}
          <LatencyMonitor metrics={metrics} latency={latency} />

          {/* Active View Container */}
          <div className="animate-in fade-in duration-200">
            {currentTab === 'terminal' && (
              <TerminalView
                onChargeCreated={handleChargeCreated}
                onOpenQrCode={(c) => setSelectedCharge(c)}
                onInspectCharge={(id) => setSelectedInspectId(id)}
              />
            )}

            {currentTab === 'ledger' && (
              <LedgerView
                onInspectCharge={(id) => setSelectedInspectId(id)}
                refreshTrigger={refreshTrigger}
              />
            )}

            {currentTab === 'stress' && (
              <StressLabView
                onInspectCharge={(id) => setSelectedInspectId(id)}
              />
            )}

            {currentTab === 'webhooks' && (
              <WebhooksView />
            )}

            {currentTab === 'tutorial' && (
              <TutorialView
                onNavigate={handleSelectTab}
                onInspectCharge={(id) => setSelectedInspectId(id)}
              />
            )}
          </div>
        </main>
      </div>

      {/* Footer */}
      <footer className="border-t border-neutral-800/80 bg-neutral-950 py-5 text-xs font-mono text-neutral-500">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-3">
          <div>
            PixFlow: Event-Driven Payment & Settlement Engine (Java 21, AWS SQS, Neon PostgreSQL, React)
          </div>
          <div className="text-neutral-400 text-[11px]">
            100% Free Tier Cloud Architecture • MIT License
          </div>
        </div>
      </footer>

      {/* Floating Notification Toast */}
      {toast && (
        <div className="fixed bottom-5 right-5 z-50 animate-in slide-in-from-bottom duration-150">
          <div className={`flex items-center gap-2 px-3.5 py-2.5 rounded-lg shadow-xl text-xs font-mono border ${
            toast.type === 'success'
              ? 'bg-neutral-900 text-emerald-400 border-emerald-800'
              : 'bg-neutral-900 text-rose-400 border-rose-800'
          }`}>
            {toast.type === 'success' ? (
              <CheckCircle2 className="w-4 h-4 shrink-0 text-emerald-400" />
            ) : (
              <AlertCircle className="w-4 h-4 shrink-0 text-rose-400" />
            )}
            <span>{toast.message}</span>
          </div>
        </div>
      )}

      {/* Transaction Details Drawer */}
      <TransactionDrawer
        chargeId={selectedInspectId}
        onClose={() => setSelectedInspectId(null)}
      />

      {/* QR Code Modal */}
      <QrCodeModal
        charge={selectedCharge}
        onClose={() => setSelectedCharge(null)}
        onSettle={handleManualSettle}
      />

      {/* Architecture Visual Modal */}
      <ArchitectureModal
        isOpen={isArchOpen}
        onClose={() => setIsArchOpen(false)}
      />
    </div>
  );
}
