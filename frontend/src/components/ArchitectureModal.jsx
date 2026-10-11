import React from 'react';
import { X, Layers, Cpu, Database, Cloud, ShieldCheck, Terminal, GitBranch } from 'lucide-react';

export default function ArchitectureModal({ isOpen, onClose }) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in overflow-y-auto">
      <div className="relative w-full max-w-3xl bg-slate-900 border border-slate-800 rounded-3xl shadow-2xl overflow-hidden p-6 sm:p-8 text-slate-100 my-8">
        
        {/* Header */}
        <div className="flex items-center justify-between pb-5 border-b border-slate-800">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-gradient-to-tr from-emerald-500 to-indigo-600 text-white shadow-lg">
              <Layers className="w-6 h-6" />
            </div>
            <div>
              <h3 className="font-extrabold text-lg text-white">Arquitetura de Referência: PixFlow</h3>
              <p className="text-xs text-slate-400">Clean Architecture, Event-Driven SQS & Cloud DevOps</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Visual Pipeline Flow */}
        <div className="py-6 space-y-6">
          
          <div className="bg-slate-950 p-4 sm:p-5 rounded-2xl border border-slate-800 font-mono text-xs overflow-x-auto text-slate-300">
            <div className="text-[11px] uppercase tracking-wider text-emerald-400 font-bold mb-3 flex items-center gap-2">
              <GitBranch className="w-4 h-4" />
              Fluxo Transacional de Ponta a Ponta (End-to-End Event Stream)
            </div>
            <div className="whitespace-pre text-slate-300 leading-relaxed">
{`[ React 18 SPA (Vite) ]
       │  POST /api/v1/charges (EMVCo BR Code + EndToEndId)
       ▼
[ Spring Boot 3.3.4 (Java 21) ] ── (Hexagonal Inbound Adapter)
       ├── 1. Domain Validation (CPF/CNPJ/Email/EVP Invariants)
       ├── 2. Persiste Estado PENDING ──► [ PostgreSQL 16 (Neon / Flyway) ]
       └── 3. Despacha Mensagem      ──► [ AWS SQS / LocalStack Queue ]
                                                   │
                                                   ▼
                                        [ Async Settlement Worker ]
                                                   ├── Consome Fila SQS
                                                   ├── PENDING ─► PROCESSING
                                                   ├── Simula Clearing BACEN (SPI)
                                                   └── PROCESSING ─► SETTLED / FAILED
                                                   │
                                                   ▼
                                        [ Audit Trail & Métricas ]`}
            </div>
          </div>

          {/* Pillars Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            
            <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800/80">
              <div className="flex items-center gap-2 text-emerald-400 font-bold text-xs uppercase tracking-wider mb-2">
                <Cpu className="w-4 h-4" />
                Java 21 & Clean Architecture
              </div>
              <p className="text-xs text-slate-400 leading-relaxed">
                Isolamento estrito entre <strong className="text-slate-200">Domain</strong> (regras de negócio e invariantes do PIX), <strong className="text-slate-200">Application</strong> (casos de uso) e <strong className="text-slate-200">Infrastructure</strong> (adaptadores Web, PostgreSQL JPA e AWS SQS).
              </p>
            </div>

            <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800/80">
              <div className="flex items-center gap-2 text-indigo-400 font-bold text-xs uppercase tracking-wider mb-2">
                <Cloud className="w-4 h-4" />
                AWS SQS & Event-Driven Workers
              </div>
              <p className="text-xs text-slate-400 leading-relaxed">
                Desacoplamento temporal completo. O endpoint da API responde em milissegundos (&lt;30ms) enquanto workers em background realizam a compensação financeira com garantia de idempotência.
              </p>
            </div>

            <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800/80">
              <div className="flex items-center gap-2 text-amber-400 font-bold text-xs uppercase tracking-wider mb-2">
                <Database className="w-4 h-4" />
                PostgreSQL & Migrations Flyway
              </div>
              <p className="text-xs text-slate-400 leading-relaxed">
                Esquema relacional versionado por código (<code className="text-amber-300">V1__init_schema.sql</code>), com chaves UUID, índices otimizados para busca por TxId/Status e trilha de auditoria completa em tabela separada.
              </p>
            </div>

            <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800/80">
              <div className="flex items-center gap-2 text-teal-400 font-bold text-xs uppercase tracking-wider mb-2">
                <ShieldCheck className="w-4 h-4" />
                Cloud DevOps & IaC
              </div>
              <p className="text-xs text-slate-400 leading-relaxed">
                Pipeline de CI/CD com GitHub Actions e cobertura mínima de 80% (JaCoCo), Dockerfile multi-stage otimizado para JVM Alpine e arquivos Terraform para provisionamento na AWS/LocalStack.
              </p>
            </div>

          </div>

          {/* Quick Terminal Testing */}
          <div className="p-4 rounded-xl bg-slate-950/40 border border-slate-800 text-xs">
            <div className="flex items-center gap-2 text-slate-300 font-bold mb-2">
              <Terminal className="w-4 h-4 text-emerald-400" />
              Comando cURL Rápido para Teste:
            </div>
            <pre className="bg-slate-950 p-3 rounded-lg overflow-x-auto text-[11px] text-emerald-300 font-mono">
{`curl -X POST http://localhost:8080/api/v1/charges \\
  -H "Content-Type: application/json" \\
  -d '{"pixKey":"12345678909","pixKeyType":"CPF","amount":150.00,"description":"Teste"}'`}
            </pre>
          </div>

        </div>

        {/* Footer */}
        <div className="pt-4 border-t border-slate-800 flex justify-end">
          <button
            onClick={onClose}
            className="px-5 py-2.5 rounded-xl bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold text-xs transition-colors"
          >
            Entendido
          </button>
        </div>

      </div>
    </div>
  );
}
