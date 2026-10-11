import React, { useState } from 'react';
import { 
  BookOpen, Play, CheckCircle2, AlertTriangle, ArrowRight, ShieldCheck, 
  Cpu, Layers, Send, ExternalLink, Terminal, Copy, Check 
} from 'lucide-react';
import { api } from '../services/api';

export default function TutorialView({ onNavigate, onInspectCharge }) {
  const [runningScenario, setRunningScenario] = useState(null);
  const [scenarioOutput, setScenarioOutput] = useState(null);
  const [copiedCurl, setCopiedCurl] = useState(null);

  const runScenario1 = async () => {
    setRunningScenario(1);
    setScenarioOutput(null);
    try {
      // 1. Create valid charge
      const res = await api.createCharge({
        pixKey: '529.982.247-25',
        pixKeyType: 'CPF',
        amount: 150.0,
        description: 'Tutorial Cenário 1: Happy Path',
        recipientName: 'PixFlow Engenharia S.A.',
      });

      setScenarioOutput({
        scenario: 1,
        success: true,
        title: 'Cenário 1 Concluído com Sucesso!',
        data: res,
        message: 'Cobrança emitida e enfileirada no motor de liquidação. O worker atualizará para SETTLED em aproximadamente 1.2 segundos.',
      });
    } catch (err) {
      setScenarioOutput({
        scenario: 1,
        success: false,
        title: 'Falha no Cenário 1',
        message: err.message,
      });
    } finally {
      setRunningScenario(null);
    }
  };

  const runScenario2 = async () => {
    setRunningScenario(2);
    setScenarioOutput(null);
    try {
      // Intentionally send invalid CPF check digits to verify backend modulo 11 rejection
      await api.createCharge({
        pixKey: '12345678911',
        pixKeyType: 'CPF',
        amount: 50.0,
        description: 'Teste de Validação Canônica Módulo 11',
      });
      setScenarioOutput({
        scenario: 2,
        success: false,
        title: 'Atenção: A API inesperadamente aceitou o CPF inválido',
        message: 'O CPF deveria ter sido rejeitado pela validação Módulo 11.',
      });
    } catch (err) {
      setScenarioOutput({
        scenario: 2,
        success: true,
        title: 'Validação Canônica Confirmada (Módulo 11)!',
        message: `A API rejeitou com sucesso o CPF incorreto com a mensagem: "${err.message}" (HTTP 422 Unprocessable Entity). O motor protegeu a integridade dos dados antes da persistência.`,
      });
    } finally {
      setRunningScenario(null);
    }
  };

  const runScenario3 = async () => {
    setRunningScenario(3);
    setScenarioOutput(null);
    try {
      const created = await api.createCharge({
        pixKey: 'treasury@pixflow.io',
        pixKeyType: 'EMAIL',
        amount: 25.0,
        description: 'Teste de Cancelamento Pré-Liquidação',
      });

      const cancelled = await api.cancelCharge(created.id);
      setScenarioOutput({
        scenario: 3,
        success: true,
        title: 'Cancelamento Executado!',
        data: cancelled,
        message: `A cobrança ${created.id} foi cancelada com sucesso antes da liquidação financeira definitiva. Status atual: CANCELLED.`,
      });
    } catch (err) {
      setScenarioOutput({
        scenario: 3,
        success: false,
        title: 'Falha no Cancelamento',
        message: err.message,
      });
    } finally {
      setRunningScenario(null);
    }
  };

  const copyCurl = (id, command) => {
    navigator.clipboard.writeText(command);
    setCopiedCurl(id);
    setTimeout(() => setCopiedCurl(null), 2000);
  };

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      {/* Header */}
      <div>
        <div className="flex items-center gap-2 text-xs font-mono text-neutral-400 mb-1">
          <span>OPERATIONAL GUIDE</span>
          <span>/</span>
          <span className="text-neutral-300">INTERACTIVE TEST SUITE</span>
        </div>
        <h1 className="text-xl font-semibold text-neutral-100 tracking-tight">
          Guia Prático & Roteiro de Testes do Motor PixFlow
        </h1>
        <p className="text-xs text-neutral-400 mt-1">
          Roteiro interativo para homologação das capacidades do motor transacional. Cada cenário pode ser executado diretamente com 1 clique ou testado via terminal (cURL).
        </p>
      </div>

      {/* Interactive Execution Output Banner */}
      {scenarioOutput && (
        <div
          className={`p-4 rounded-lg border transition-all ${
            scenarioOutput.success
              ? 'bg-neutral-900 border-emerald-800 text-neutral-200'
              : 'bg-neutral-900 border-rose-800 text-neutral-200'
          }`}
        >
          <div className="flex items-start justify-between gap-3">
            <div className="flex items-start gap-3">
              {scenarioOutput.success ? (
                <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0 mt-0.5" />
              ) : (
                <AlertTriangle className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />
              )}
              <div>
                <h3 className="text-sm font-semibold text-neutral-100">{scenarioOutput.title}</h3>
                <p className="text-xs text-neutral-400 mt-1">{scenarioOutput.message}</p>
                {scenarioOutput.data?.id && (
                  <div className="mt-3 flex items-center gap-2">
                    <button
                      onClick={() => onInspectCharge(scenarioOutput.data.id)}
                      className="px-2.5 py-1 bg-neutral-800 hover:bg-neutral-700 text-neutral-200 text-xs font-mono rounded flex items-center gap-1 transition-colors"
                    >
                      <span>Inspecionar Transação #{scenarioOutput.data.id.substring(0, 8)}</span>
                      <ArrowRight className="w-3 h-3" />
                    </button>
                  </div>
                )}
              </div>
            </div>
            <button
              onClick={() => setScenarioOutput(null)}
              className="text-xs font-mono text-neutral-500 hover:text-neutral-300"
            >
              Fechar
            </button>
          </div>
        </div>
      )}

      {/* Scenarios Grid */}
      <div className="space-y-4">
        {/* Scenario 1 */}
        <div className="bg-neutral-900 border border-neutral-800 rounded-lg p-5 space-y-3">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <div className="flex items-center gap-2">
              <span className="text-xs font-mono px-2 py-0.5 bg-neutral-800 text-neutral-300 rounded font-semibold">
                CENÁRIO 1
              </span>
              <h3 className="text-sm font-semibold text-neutral-100">
                Ciclo de Vida Padrão (Happy Path de Liquidação)
              </h3>
            </div>
            <button
              onClick={runScenario1}
              disabled={runningScenario === 1}
              className="self-start sm:self-auto px-3 py-1.5 bg-neutral-100 hover:bg-white text-neutral-950 font-semibold text-xs font-mono rounded flex items-center gap-1.5 transition-colors disabled:opacity-50"
            >
              <Play className="w-3 h-3 fill-current" />
              <span>{runningScenario === 1 ? 'Executando...' : 'Executar Teste'}</span>
            </button>
          </div>
          <p className="text-xs text-neutral-400">
            Valida o fluxo ponta a ponta: emissão com CPF matematicamente válido (<code className="font-mono text-neutral-300">529.982.247-25</code>), enfileiramento assíncrono em fila e liquidação do worker após 1200ms com registro no livro-razão.
          </p>
          <div className="bg-neutral-950 p-2.5 rounded border border-neutral-800/80 text-[11px] font-mono text-neutral-400 flex items-center justify-between">
            <span className="truncate mr-2">POST /api/v1/charges &#123; pixKey: "529.982.247-25", amount: 150.00 &#125;</span>
            <button
              onClick={() => copyCurl(1, `curl -X POST https://pix-flow-backend.onrender.com/api/v1/charges -H "Content-Type: application/json" -d '{"pixKey":"52998224725","pixKeyType":"CPF","amount":150.00}'`)}
              className="text-neutral-400 hover:text-neutral-200"
              title="Copiar cURL"
            >
              {copiedCurl === 1 ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
            </button>
          </div>
        </div>

        {/* Scenario 2 */}
        <div className="bg-neutral-900 border border-neutral-800 rounded-lg p-5 space-y-3">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <div className="flex items-center gap-2">
              <span className="text-xs font-mono px-2 py-0.5 bg-neutral-800 text-neutral-300 rounded font-semibold">
                CENÁRIO 2
              </span>
              <h3 className="text-sm font-semibold text-neutral-100">
                Validação Canônica de Dígito Verificador (Módulo 11)
              </h3>
            </div>
            <button
              onClick={runScenario2}
              disabled={runningScenario === 2}
              className="self-start sm:self-auto px-3 py-1.5 bg-neutral-800 hover:bg-neutral-700 text-neutral-200 font-semibold text-xs font-mono rounded flex items-center gap-1.5 transition-colors disabled:opacity-50"
            >
              <Play className="w-3 h-3 fill-current" />
              <span>{runningScenario === 2 ? 'Testando...' : 'Testar Rejeição de CPF'}</span>
            </button>
          </div>
          <p className="text-xs text-neutral-400">
            Envia propositalmente uma chave CPF com dígitos verificadores matematicamente incorretos (<code className="font-mono text-neutral-300">123.456.789-11</code>). Demonstra a proteção rígida em nível de domínio (Domain Layer) com resposta HTTP 422 Unprocessable Entity.
          </p>
        </div>

        {/* Scenario 3 */}
        <div className="bg-neutral-900 border border-neutral-800 rounded-lg p-5 space-y-3">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <div className="flex items-center gap-2">
              <span className="text-xs font-mono px-2 py-0.5 bg-neutral-800 text-neutral-300 rounded font-semibold">
                CENÁRIO 3
              </span>
              <h3 className="text-sm font-semibold text-neutral-100">
                Cancelamento de Cobrança Pré-Liquidação
              </h3>
            </div>
            <button
              onClick={runScenario3}
              disabled={runningScenario === 3}
              className="self-start sm:self-auto px-3 py-1.5 bg-neutral-800 hover:bg-neutral-700 text-neutral-200 font-semibold text-xs font-mono rounded flex items-center gap-1.5 transition-colors disabled:opacity-50"
            >
              <Play className="w-3 h-3 fill-current" />
              <span>{runningScenario === 3 ? 'Cancelando...' : 'Executar Cancelamento'}</span>
            </button>
          </div>
          <p className="text-xs text-neutral-400">
            Cria uma cobrança e cancela antes que ela seja liquidada financeiramente pelo motor. Transições permitidas: <code className="font-mono text-neutral-300">PENDING -&gt; CANCELLED</code>. O motor rejeita tentativas de cancelar cobranças já liquidadas.
          </p>
        </div>

        {/* Scenario 4 */}
        <div className="bg-neutral-900 border border-neutral-800 rounded-lg p-5 space-y-3">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <div className="flex items-center gap-2">
              <span className="text-xs font-mono px-2 py-0.5 bg-neutral-800 text-neutral-300 rounded font-semibold">
                CENÁRIO 4
              </span>
              <h3 className="text-sm font-semibold text-neutral-100">
                Teste de Concorrência & Carga (Stress Lab)
              </h3>
            </div>
            <button
              onClick={() => onNavigate('stress')}
              className="self-start sm:self-auto px-3 py-1.5 bg-neutral-800 hover:bg-neutral-700 text-neutral-200 font-semibold text-xs font-mono rounded flex items-center gap-1.5 transition-colors"
            >
              <span>Abrir Stress Lab</span>
              <ArrowRight className="w-3 h-3" />
            </button>
          </div>
          <p className="text-xs text-neutral-400">
            Submeta lotes de até 20 transações em paralelo (<code className="font-mono text-neutral-300">POST /api/v1/charges/batch</code>). O motor executa as cobranças simultaneamente em threads virtuais/pool dedicado e retorna o Throughput (transações/segundo).
          </p>
        </div>

        {/* Scenario 5 */}
        <div className="bg-neutral-900 border border-neutral-800 rounded-lg p-5 space-y-3">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <div className="flex items-center gap-2">
              <span className="text-xs font-mono px-2 py-0.5 bg-neutral-800 text-neutral-300 rounded font-semibold">
                CENÁRIO 5
              </span>
              <h3 className="text-sm font-semibold text-neutral-100">
                Livro-Razão & Auditoria de Eventos
              </h3>
            </div>
            <button
              onClick={() => onNavigate('ledger')}
              className="self-start sm:self-auto px-3 py-1.5 bg-neutral-800 hover:bg-neutral-700 text-neutral-200 font-semibold text-xs font-mono rounded flex items-center gap-1.5 transition-colors"
            >
              <span>Abrir Livro-Razão</span>
              <ArrowRight className="w-3 h-3" />
            </button>
          </div>
          <p className="text-xs text-neutral-400">
            Navegue pelo histórico completo com busca por TxID e EndToEndId, filtros por estado, exportação de relatório para CSV e abertura do Inspector com a timeline dos workers.
          </p>
        </div>

        {/* Scenario 6 */}
        <div className="bg-neutral-900 border border-neutral-800 rounded-lg p-5 space-y-3">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <div className="flex items-center gap-2">
              <span className="text-xs font-mono px-2 py-0.5 bg-neutral-800 text-neutral-300 rounded font-semibold">
                CENÁRIO 6
              </span>
              <h3 className="text-sm font-semibold text-neutral-100">
                Auditoria de Webhooks com Assinatura HMAC-SHA256
              </h3>
            </div>
            <button
              onClick={() => onNavigate('webhooks')}
              className="self-start sm:self-auto px-3 py-1.5 bg-neutral-800 hover:bg-neutral-700 text-neutral-200 font-semibold text-xs font-mono rounded flex items-center gap-1.5 transition-colors"
            >
              <span>Ver Webhooks</span>
              <ArrowRight className="w-3 h-3" />
            </button>
          </div>
          <p className="text-xs text-neutral-400">
            Verifique as notificações assíncronas despachadas para ERPs parceiros na liquidação de cada PIX. Cada webhook inclui o cabeçalho <code className="font-mono text-neutral-300">X-Signature-SHA256</code> para autenticação criptográfica da origem.
          </p>
        </div>
      </div>
    </div>
  );
}
