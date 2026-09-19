import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { DashboardSummary } from '../types';
import {
  Wallet,
  TrendingUp,
  TrendingDown,
  DollarSign,
  Trophy,
  Target,
  PieChart,
  Plus,
  Flame,
  ShieldCheck,
  ArrowRight,
  Clock,
  Sparkles
} from 'lucide-react';
import { Link } from 'react-router-dom';

export const DashboardPage: React.FC = () => {
  const [data, setData] = useState<DashboardSummary | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get<DashboardSummary>('/dashboard')
      .then((res) => setData(res.data))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500"></div>
      </div>
    );
  }

  const formatBRL = (val?: number) => {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(val || 0);
  };

  const level = data?.userXp.currentLevel || 1;
  const currentXp = data?.userXp.currentXp || 0;
  const xpNeeded = level * 200;
  const xpProgress = Math.min((currentXp / xpNeeded) * 100, 100);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">Painel Financeiro</h1>
          <p className="text-sm text-slate-400 mt-1">Acompanhe seu saldo, orçamento mensal e evolução de nível.</p>
        </div>
        <div className="flex items-center gap-3">
          <Link
            to="/transactions"
            className="flex items-center gap-2 bg-emerald-600 hover:bg-emerald-500 text-white px-4 py-2.5 rounded-lg text-sm font-semibold transition-colors shadow-lg shadow-emerald-900/20"
          >
            <Plus className="h-4 w-4" />
            <span>Novo Lançamento</span>
          </Link>
        </div>
      </div>

      {/* Gamification Banner */}
      <div className="bg-[#151D2F] border border-slate-800 rounded-xl p-5 sm:p-6 relative overflow-hidden">
        <div className="absolute -right-10 -bottom-10 opacity-10 pointer-events-none">
          <Trophy className="h-64 w-64 text-emerald-400" />
        </div>

        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 relative z-10">
          <div className="space-y-2">
            <div className="flex items-center gap-3">
              <span className="px-3 py-1 bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 rounded-full text-xs font-bold uppercase tracking-wider">
                Nível {level} — Aprendiz Financeiro
              </span>
              <div className="flex items-center gap-1 text-amber-400 text-xs font-bold bg-amber-500/10 border border-amber-500/20 px-2.5 py-1 rounded-full">
                <Flame className="h-4 w-4 fill-amber-400" />
                <span>{data?.userXp.streakDays || 1} Dias Seguidos</span>
              </div>
            </div>
            <h2 className="text-xl font-bold text-white">Sua Saúde Financeira: {data?.userXp.financialHealthScore || 70}/100</h2>
            <p className="text-xs text-slate-400 max-w-xl">
              Métrica interna de organização baseada em regularidade de lançamentos e cumprimento de orçamento.
            </p>
          </div>

          <div className="w-full md:w-72 bg-[#0B0F19] p-4 border border-slate-800 rounded-xl space-y-2">
            <div className="flex justify-between text-xs font-medium">
              <span className="text-slate-300">Progresso de XP</span>
              <span className="text-emerald-400 font-bold">{currentXp} / {xpNeeded} XP</span>
            </div>
            <div className="w-full bg-slate-800 rounded-full h-2.5 overflow-hidden">
              <div className="bg-emerald-500 h-2.5 rounded-full transition-all duration-300" style={{ width: `${xpProgress}%` }}></div>
            </div>
            <p className="text-[11px] text-slate-500 text-right">Faltam {xpNeeded - currentXp} XP para o Nível {level + 1}</p>
          </div>
        </div>
      </div>

      {/* Financial Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Total Balance */}
        <div className="bg-[#151D2F] border border-slate-800 rounded-xl p-5">
          <div className="flex items-center justify-between text-slate-400 mb-3">
            <span className="text-xs font-semibold uppercase tracking-wider">Saldo Total</span>
            <div className="p-2 bg-blue-500/10 text-blue-400 rounded-lg">
              <Wallet className="h-5 w-5" />
            </div>
          </div>
          <p className="text-2xl font-bold text-white">{formatBRL(data?.totalBalance)}</p>
          <span className="text-xs text-slate-500 mt-1 block">Somatório de todas as contas</span>
        </div>

        {/* Monthly Income */}
        <div className="bg-[#151D2F] border border-slate-800 rounded-xl p-5">
          <div className="flex items-center justify-between text-slate-400 mb-3">
            <span className="text-xs font-semibold uppercase tracking-wider">Receitas do Mês</span>
            <div className="p-2 bg-emerald-500/10 text-emerald-400 rounded-lg">
              <TrendingUp className="h-5 w-5" />
            </div>
          </div>
          <p className="text-2xl font-bold text-emerald-400">{formatBRL(data?.monthlyIncome)}</p>
          <span className="text-xs text-slate-500 mt-1 block">Entradas registradas no mês</span>
        </div>

        {/* Monthly Expenses */}
        <div className="bg-[#151D2F] border border-slate-800 rounded-xl p-5">
          <div className="flex items-center justify-between text-slate-400 mb-3">
            <span className="text-xs font-semibold uppercase tracking-wider">Despesas do Mês</span>
            <div className="p-2 bg-rose-500/10 text-rose-400 rounded-lg">
              <TrendingDown className="h-5 w-5" />
            </div>
          </div>
          <p className="text-2xl font-bold text-rose-400">{formatBRL(data?.monthlyExpenses)}</p>
          <span className="text-xs text-slate-500 mt-1 block">Saídas registradas no mês</span>
        </div>

        {/* Monthly Result */}
        <div className="bg-[#151D2F] border border-slate-800 rounded-xl p-5">
          <div className="flex items-center justify-between text-slate-400 mb-3">
            <span className="text-xs font-semibold uppercase tracking-wider">Resultado do Mês</span>
            <div className="p-2 bg-amber-500/10 text-amber-400 rounded-lg">
              <DollarSign className="h-5 w-5" />
            </div>
          </div>
          <p className={`text-2xl font-bold ${(data?.monthlyResult || 0) >= 0 ? 'text-emerald-400' : 'text-rose-400'}`}>
            {formatBRL(data?.monthlyResult)}
          </p>
          <span className="text-xs text-slate-500 mt-1 block">Receitas (-) Despesas</span>
        </div>
      </div>

      {/* Main Grid: Recent Transactions & Active Missions */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Recent Transactions (2 cols) */}
        <div className="lg:col-span-2 bg-[#151D2F] border border-slate-800 rounded-xl p-5">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-bold text-lg text-white">Lançamentos Recentes</h3>
            <Link to="/transactions" className="text-xs font-semibold text-emerald-400 hover:text-emerald-300 flex items-center gap-1">
              Ver todos <ArrowRight className="h-3.5 w-3.5" />
            </Link>
          </div>

          {(!data?.recentTransactions || data.recentTransactions.length === 0) ? (
            <div className="text-center py-10 text-slate-500">
              <Clock className="h-8 w-8 mx-auto mb-2 opacity-50" />
              <p className="text-sm">Nenhuma transação cadastrada até o momento.</p>
              <p className="text-xs mt-1">Crie seu primeiro lançamento para somar +10 XP!</p>
            </div>
          ) : (
            <div className="divide-y divide-slate-800">
              {data.recentTransactions.map((t) => (
                <div key={t.id} className="py-3 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div
                      className="p-2.5 rounded-lg text-white text-xs font-bold"
                      style={{ backgroundColor: t.categoryColor || '#334155' }}
                    >
                      {t.type === 'INCOME' ? '+' : '-'}
                    </div>
                    <div>
                      <p className="text-sm font-semibold text-white">{t.description}</p>
                      <span className="text-xs text-slate-400">{t.categoryName} • {t.date}</span>
                    </div>
                  </div>
                  <span className={`text-sm font-bold ${t.type === 'INCOME' ? 'text-emerald-400' : 'text-slate-200'}`}>
                    {t.type === 'INCOME' ? '+' : '-'} {formatBRL(t.amount)}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Active Missions (1 col) */}
        <div className="bg-[#151D2F] border border-slate-800 rounded-xl p-5">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <Sparkles className="h-5 w-5 text-amber-400" />
              <h3 className="font-bold text-lg text-white">Missões Ativas</h3>
            </div>
            <Link to="/gamification" className="text-xs font-semibold text-emerald-400 hover:text-emerald-300">
              Painel Completo
            </Link>
          </div>

          {(!data?.activeMissions || data.activeMissions.length === 0) ? (
            <div className="text-center py-8 text-slate-500">
              <Trophy className="h-8 w-8 mx-auto mb-2 opacity-50" />
              <p className="text-sm">Todas as missões concluídas!</p>
            </div>
          ) : (
            <div className="space-y-4">
              {data.activeMissions.map((m) => (
                <div key={m.id} className="p-3.5 bg-[#0B0F19] border border-slate-800 rounded-lg space-y-2">
                  <div className="flex items-center justify-between">
                    <h4 className="text-xs font-bold text-white">{m.title}</h4>
                    <span className="text-[10px] font-bold text-amber-400 bg-amber-500/10 px-2 py-0.5 rounded border border-amber-500/20">
                      +{m.xpReward} XP
                    </span>
                  </div>
                  <p className="text-xs text-slate-400">{m.description}</p>
                  <div className="w-full bg-slate-800 rounded-full h-2 overflow-hidden">
                    <div className="bg-amber-400 h-2 rounded-full" style={{ width: `${m.progressPercentage}%` }}></div>
                  </div>
                  <div className="flex justify-between text-[10px] text-slate-500">
                    <span>Progresso: {m.currentCount} / {m.targetCount}</span>
                    <span>Vence em {m.endDate}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
