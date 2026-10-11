import React from 'react';
import { Zap, BookOpen, Layers, Github, Activity, WifiOff } from 'lucide-react';

export default function Navbar({ latency, isOnline, onOpenArchitecture }) {
  const swaggerUrl = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '') + '/swagger-ui.html';

  return (
    <header className="sticky top-0 z-40 w-full border-b border-slate-800 bg-[#0B0F19]/80 backdrop-blur-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        
        {/* Brand & Subtitle */}
        <div className="flex items-center gap-3">
          <div className="h-10 w-10 rounded-xl bg-gradient-to-tr from-emerald-500 to-indigo-600 flex items-center justify-center shadow-lg shadow-emerald-500/10">
            <Zap className="h-5 w-5 text-white fill-white" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-extrabold text-lg tracking-tight bg-gradient-to-r from-white via-slate-200 to-slate-400 bg-clip-text text-transparent">
                PixFlow
              </span>
              <span className="text-[10px] uppercase font-bold tracking-wider px-1.5 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                Engine
              </span>
            </div>
            <p className="text-xs text-slate-400 hidden sm:block">Event-Driven Payment & Settlement</p>
          </div>
        </div>

        {/* Action Controls & Telemetry */}
        <div className="flex items-center gap-2 sm:gap-4">
          
          {/* Latency / Online Status */}
          <div className={`flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-medium border ${
            isOnline 
              ? 'bg-emerald-950/30 border-emerald-500/30 text-emerald-400' 
              : 'bg-rose-950/30 border-rose-500/30 text-rose-400'
          }`}>
            {isOnline ? (
              <>
                <span className="relative flex h-2 w-2">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                  <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
                </span>
                <Activity className="w-3.5 h-3.5" />
                <span className="font-mono">{latency !== null ? `${latency} ms` : 'Live'}</span>
              </>
            ) : (
              <>
                <WifiOff className="w-3.5 h-3.5 text-rose-400" />
                <span>Desconectado</span>
              </>
            )}
          </div>

          {/* Architecture Modal Trigger */}
          <button
            onClick={onOpenArchitecture}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-slate-800/80 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors"
            title="Ver Arquitetura Hexagonal & Cloud"
          >
            <Layers className="w-3.5 h-3.5 text-indigo-400" />
            <span className="hidden md:inline">Arquitetura</span>
          </button>

          {/* Swagger OpenAPI */}
          <a
            href={swaggerUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-slate-800/80 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors"
            title="Abrir Swagger UI"
          >
            <BookOpen className="w-3.5 h-3.5 text-emerald-400" />
            <span className="hidden md:inline">OpenAPI</span>
          </a>

          {/* GitHub Repo */}
          <a
            href="https://github.com/matheus-araujo-pereira/pix-flow"
            target="_blank"
            rel="noopener noreferrer"
            className="p-2 rounded-lg bg-slate-800/80 hover:bg-slate-700 text-slate-300 border border-slate-700 transition-colors"
            title="Ver no GitHub"
          >
            <Github className="w-4 h-4" />
          </a>

        </div>

      </div>
    </header>
  );
}

