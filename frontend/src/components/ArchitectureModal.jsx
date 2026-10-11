import React from 'react';
import { X, Layers, Cpu, Database, Cloud, ShieldCheck, Terminal, GitBranch } from 'lucide-react';

export default function ArchitectureModal({ isOpen, onClose }) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm overflow-y-auto">
      <div className="relative w-full max-w-3xl bg-neutral-900 border border-neutral-800 rounded-lg shadow-2xl overflow-hidden p-6 sm:p-8 text-neutral-100 my-8">
        
        {/* Header */}
        <div className="flex items-center justify-between pb-5 border-b border-neutral-800">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded bg-neutral-800 text-neutral-200">
              <Layers className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-mono text-neutral-400 uppercase">REFERENCE ARCHITECTURE</div>
              <h3 className="font-semibold text-base text-neutral-100">PixFlow: Hexagonal Event-Driven Engine</h3>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded text-neutral-400 hover:text-neutral-100 hover:bg-neutral-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Visual Pipeline Flow */}
        <div className="py-6 space-y-6">
          <div className="bg-neutral-950 p-4 sm:p-5 rounded border border-neutral-800 font-mono text-xs overflow-x-auto text-neutral-300">
            <div className="text-[11px] uppercase tracking-wider text-neutral-400 font-semibold mb-3 flex items-center gap-2">
              <GitBranch className="w-4 h-4 text-emerald-400" />
              Fluxo Transacional de Ponta a Ponta (End-to-End Event Stream)
            </div>
            <pre className="text-neutral-300 leading-relaxed font-mono">
{`[ React 18 SPA (Vite) ]
       │  POST /api/v1/charges (EMVCo BR Code + EndToEndId)
       ▼
[ Spring Boot 3.3.4 (Java 21) ] ── (Hexagonal Inbound Adapter)
       ├── 1. Domain Validation (CPF/CNPJ Módulo 11 + Invariantes)
       ├── 2. Persiste Estado PENDING ──► [ PostgreSQL 18.6 (Neon / Flyway) ]
       └── 3. Despacha Mensagem      ──► [ AWS SQS / Settlement Poller ]
                                                   │
                                                   ▼
                                        [ Async Settlement Worker ]
                                                   ├── Consome Fila SQS / Poller
                                                   ├── PENDING ─► PROCESSING
                                                   ├── Simula Clearing BACEN (SPI)
                                                   ├── PROCESSING ─► SETTLED / FAILED
                                                   └── Despacha Webhook (HMAC-SHA256)
                                                   │
                                                   ▼
                                        [ Audit Trail & Métricas ]`}
            </pre>
          </div>

          {/* Pillars Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="p-4 rounded bg-neutral-950 border border-neutral-800">
              <div className="flex items-center gap-2 text-neutral-200 font-semibold text-xs font-mono uppercase tracking-wider mb-2">
                <Cpu className="w-4 h-4 text-emerald-400" />
                Java 21 & Clean Architecture
              </div>
              <p className="text-xs text-neutral-400 leading-relaxed font-sans">
                Isolamento estrito entre <strong className="text-neutral-200">Domain</strong> (regras de negócio e invariantes do PIX), <strong className="text-neutral-200">Application</strong> (casos de uso) e <strong className="text-neutral-200">Infrastructure</strong> (adaptadores Web, PostgreSQL JPA e AWS SQS).
              </p>
            </div>

            <div className="p-4 rounded bg-neutral-950 border border-neutral-800">
              <div className="flex items-center gap-2 text-neutral-200 font-semibold text-xs font-mono uppercase tracking-wider mb-2">
                <Cloud className="w-4 h-4 text-amber-400" />
                AWS SQS & Event-Driven Workers
              </div>
              <p className="text-xs text-neutral-400 leading-relaxed font-sans">
                Desacoplamento temporal completo. O endpoint da API responde em milissegundos enquanto workers em background realizam a compensação financeira com garantia de idempotência.
              </p>
            </div>

            <div className="p-4 rounded bg-neutral-950 border border-neutral-800">
              <div className="flex items-center gap-2 text-neutral-200 font-semibold text-xs font-mono uppercase tracking-wider mb-2">
                <Database className="w-4 h-4 text-cyan-400" />
                PostgreSQL & Migrations Flyway
              </div>
              <p className="text-xs text-neutral-400 leading-relaxed font-sans">
                Esquema relacional versionado por código (<code className="font-mono text-neutral-300">V1__init_schema.sql</code>, <code className="font-mono text-neutral-300">V2__webhook_deliveries.sql</code>), com chaves UUID e trilha de auditoria completa em tabela separada.
              </p>
            </div>

            <div className="p-4 rounded bg-neutral-950 border border-neutral-800">
              <div className="flex items-center gap-2 text-neutral-200 font-semibold text-xs font-mono uppercase tracking-wider mb-2">
                <ShieldCheck className="w-4 h-4 text-emerald-400" />
                Cloud DevOps & IaC
              </div>
              <p className="text-xs text-neutral-400 leading-relaxed font-sans">
                Pipeline de CI/CD com GitHub Actions e cobertura &gt;=80% (JaCoCo), Dockerfile multi-stage otimizado para JVM Alpine e arquivos Terraform para provisionamento na AWS/LocalStack.
              </p>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="pt-4 border-t border-neutral-800 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 rounded bg-neutral-100 hover:bg-white text-neutral-950 font-semibold text-xs font-mono transition-colors"
          >
            Fechar
          </button>
        </div>

      </div>
    </div>
  );
}
