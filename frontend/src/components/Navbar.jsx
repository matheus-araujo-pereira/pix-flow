import React from 'react';
import { 
  Send, Layers, Zap, Radio, BookOpen, GitBranch, 
  Github, Activity, WifiOff, FileText 
} from 'lucide-react';

export default function Navbar({ 
  currentTab, 
  onSelectTab, 
  latency, 
  isOnline, 
  onOpenArchitecture 
}) {
  const swaggerUrl = (import.meta.env.VITE_API_URL || 'https://pix-flow-backend.onrender.com').replace(/\/$/, '') + '/swagger-ui.html';

  const navItems = [
    { id: 'terminal', label: 'Terminal', icon: Send },
    { id: 'ledger', label: 'Livro-Razão', icon: Layers },
    { id: 'stress', label: 'Stress Lab', icon: Zap },
    { id: 'webhooks', label: 'Webhooks', icon: Radio },
    { id: 'tutorial', label: 'Roteiro & Testes', icon: BookOpen },
  ];

  return (
    <header className="sticky top-0 z-40 w-full bg-neutral-950/90 backdrop-blur-md border-b border-neutral-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="h-14 flex items-center justify-between">
          {/* Brand */}
          <div className="flex items-center gap-3">
            <button
              onClick={() => onSelectTab('terminal')}
              className="flex items-center gap-2 text-left group"
            >
              <div className="w-7 h-7 rounded bg-neutral-100 flex items-center justify-center text-neutral-950 font-black font-mono text-xs shadow-sm">
                PX
              </div>
              <div>
                <span className="font-mono font-bold text-sm tracking-tight text-neutral-100">
                  PIXFLOW
                </span>
                <span className="ml-1.5 text-[10px] font-mono px-1 py-0.5 rounded bg-neutral-900 border border-neutral-800 text-neutral-400">
                  ENGINE v1.0
                </span>
              </div>
            </button>
          </div>

          {/* Center Navigation Tabs */}
          <nav className="hidden md:flex items-center gap-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = currentTab === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => onSelectTab(item.id)}
                  className={`flex items-center gap-1.5 px-3 py-1.5 rounded text-xs font-mono transition-colors ${
                    isActive
                      ? 'bg-neutral-800 text-neutral-100 font-semibold'
                      : 'text-neutral-400 hover:text-neutral-200 hover:bg-neutral-900'
                  }`}
                >
                  <Icon className="w-3.5 h-3.5" />
                  <span>{item.label}</span>
                </button>
              );
            })}
          </nav>

          {/* Right Controls & Telemetry */}
          <div className="flex items-center gap-2">
            {/* Latency Pill */}
            <div className={`flex items-center gap-1.5 px-2.5 py-1 rounded border text-xs font-mono ${
              isOnline
                ? 'bg-emerald-950/40 border-emerald-800/50 text-emerald-400'
                : 'bg-rose-950/40 border-rose-800/50 text-rose-400'
            }`}>
              <span className={`w-1.5 h-1.5 rounded-full ${isOnline ? 'bg-emerald-400 animate-pulse' : 'bg-rose-400'}`} />
              <span>{isOnline ? `${latency || 0}ms` : 'Offline'}</span>
            </div>

            {/* Architecture Modal Button */}
            <button
              onClick={onOpenArchitecture}
              className="hidden sm:flex items-center gap-1 px-2.5 py-1 rounded bg-neutral-900 hover:bg-neutral-800 border border-neutral-800 text-neutral-300 text-xs font-mono transition-colors"
              title="Diagrama de Arquitetura Hexagonal"
            >
              <GitBranch className="w-3.5 h-3.5 text-neutral-400" />
              <span>Arquitetura</span>
            </button>

            {/* Swagger OpenAPI */}
            <a
              href={swaggerUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="hidden sm:flex items-center gap-1 px-2.5 py-1 rounded bg-neutral-900 hover:bg-neutral-800 border border-neutral-800 text-neutral-300 text-xs font-mono transition-colors"
              title="Documentação OpenAPI"
            >
              <FileText className="w-3.5 h-3.5 text-neutral-400" />
              <span>Swagger</span>
            </a>

            {/* GitHub Link */}
            <a
              href="https://github.com/matheus-araujo-pereira/pix-flow"
              target="_blank"
              rel="noopener noreferrer"
              className="p-1.5 rounded bg-neutral-900 hover:bg-neutral-800 border border-neutral-800 text-neutral-400 hover:text-neutral-200 transition-colors"
              title="Repositório GitHub"
            >
              <Github className="w-4 h-4" />
            </a>
          </div>
        </div>

        {/* Mobile Navigation Tabs */}
        <div className="flex md:hidden border-t border-neutral-800/80 py-2 gap-1 overflow-x-auto">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = currentTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => onSelectTab(item.id)}
                className={`flex items-center gap-1 px-2.5 py-1 rounded text-[11px] font-mono whitespace-nowrap transition-colors ${
                  isActive
                    ? 'bg-neutral-800 text-neutral-100 font-semibold'
                    : 'text-neutral-400 hover:text-neutral-200'
                }`}
              >
                <Icon className="w-3 h-3" />
                <span>{item.label}</span>
              </button>
            );
          })}
        </div>
      </div>
    </header>
  );
}
