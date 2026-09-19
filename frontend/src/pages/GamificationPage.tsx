import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { Achievement, Mission, UserXP } from '../types';
import { Trophy, Flame, Target, Award, ShieldCheck, Sparkles, CheckCircle2, Lock } from 'lucide-react';

interface GamificationSummaryData {
  userXp: UserXP;
  activeMissions: Mission[];
  achievements: Achievement[];
  unlockedAchievementsCount: number;
  totalAchievementsCount: number;
}

export const GamificationPage: React.FC = () => {
  const [data, setData] = useState<GamificationSummaryData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get<GamificationSummaryData>('/gamification/summary')
      .then((res) => setData(res.data))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500"></div>
      </div>
    );
  }

  const level = data?.userXp.currentLevel || 1;
  const currentXp = data?.userXp.currentXp || 0;
  const xpNeeded = level * 200;
  const progress = Math.min((currentXp / xpNeeded) * 100, 100);

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">Gamificação & Níveis</h1>
        <p className="text-sm text-slate-400 mt-1">
          Sua evolução é recompensada por bons hábitos de organização e nunca por gastos excessivos.
        </p>
      </div>

      {/* Level & Stats Dashboard */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Level Card (2 cols) */}
        <div className="lg:col-span-2 bg-[#151D2F] border border-slate-800 rounded-xl p-6 relative overflow-hidden flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <div>
              <span className="text-xs font-bold uppercase tracking-wider text-emerald-400">Nível Atual</span>
              <h2 className="text-3xl font-extrabold text-white mt-1">Nível {level} — Mestre da Disciplina</h2>
            </div>
            <div className="p-3 bg-amber-500/10 border border-amber-500/20 rounded-xl text-amber-400">
              <Trophy className="h-8 w-8" />
            </div>
          </div>

          <div className="my-6 space-y-2">
            <div className="flex justify-between text-sm font-semibold">
              <span className="text-slate-300">Progresso do Nível {level}</span>
              <span className="text-emerald-400">{currentXp} / {xpNeeded} XP</span>
            </div>
            <div className="w-full bg-slate-800 rounded-full h-3 overflow-hidden">
              <div
                className="bg-emerald-500 h-3 rounded-full transition-all duration-300"
                style={{ width: `${progress}%` }}
              ></div>
            </div>
            <span className="text-xs text-slate-400 block text-right">Total Acumulado: {data?.userXp.totalXp} XP</span>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 pt-4 border-t border-slate-800">
            <div className="p-3 bg-[#0B0F19] rounded-lg">
              <span className="text-[11px] text-slate-400 font-medium block">Streak de Hábito</span>
              <span className="text-lg font-bold text-amber-400 flex items-center gap-1 mt-0.5">
                <Flame className="h-4 w-4 fill-amber-400" /> {data?.userXp.streakDays || 1} Dias
              </span>
            </div>
            <div className="p-3 bg-[#0B0F19] rounded-lg">
              <span className="text-[11px] text-slate-400 font-medium block">Saúde Financeira</span>
              <span className="text-lg font-bold text-emerald-400 mt-0.5 block">
                {data?.userXp.financialHealthScore || 70} / 100
              </span>
            </div>
            <div className="p-3 bg-[#0B0F19] rounded-lg col-span-2 sm:col-span-1">
              <span className="text-[11px] text-slate-400 font-medium block">Conquistas</span>
              <span className="text-lg font-bold text-white mt-0.5 block">
                {data?.unlockedAchievementsCount} / {data?.totalAchievementsCount}
              </span>
            </div>
          </div>
        </div>

        {/* Rule Explanation Card */}
        <div className="bg-[#151D2F] border border-slate-800 rounded-xl p-6 flex flex-col justify-between">
          <div className="space-y-3">
            <div className="flex items-center gap-2 text-emerald-400 font-bold text-sm">
              <ShieldCheck className="h-5 w-5" />
              <span>Regras Éticas do Sistema</span>
            </div>
            <h3 className="text-base font-bold text-white">Como Ganhar XP?</h3>
            <ul className="text-xs text-slate-400 space-y-2">
              <li className="flex items-start gap-2">
                <span className="text-emerald-400 font-bold">+10 XP</span>
                <span>Ao registrar qualquer lançamento financeiro no dia.</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-emerald-400 font-bold">+20 XP</span>
                <span>Ao pagar uma fatura ou conta antes do vencimento.</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-emerald-400 font-bold">+200 XP</span>
                <span>Ao alcançar 100% de uma meta financeira planejada.</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-emerald-400 font-bold">+300 XP</span>
                <span>Ao fechar o mês com todas as categorias dentro do orçamento.</span>
              </li>
            </ul>
          </div>
        </div>
      </div>

      {/* Active Missions Section */}
      <div className="space-y-4">
        <div className="flex items-center gap-2">
          <Sparkles className="h-5 w-5 text-amber-400" />
          <h2 className="text-xl font-bold text-white">Missões de Organização</h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {data?.activeMissions.map((m) => (
            <div key={m.id} className="bg-[#151D2F] border border-slate-800 rounded-xl p-5 space-y-3">
              <div className="flex items-center justify-between">
                <h3 className="font-bold text-white text-base">{m.title}</h3>
                <span className="text-xs font-bold text-amber-400 bg-amber-500/10 border border-amber-500/20 px-2.5 py-1 rounded-full">
                  +{m.xpReward} XP
                </span>
              </div>
              <p className="text-xs text-slate-400">{m.description}</p>
              <div className="space-y-1">
                <div className="flex justify-between text-xs font-semibold">
                  <span className="text-slate-300">Progresso</span>
                  <span className="text-emerald-400">{m.currentCount} / {m.targetCount}</span>
                </div>
                <div className="w-full bg-slate-800 rounded-full h-2 overflow-hidden">
                  <div className="bg-amber-400 h-2 rounded-full" style={{ width: `${m.progressPercentage}%` }}></div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Achievements Section */}
      <div className="space-y-4">
        <div className="flex items-center gap-2">
          <Award className="h-5 w-5 text-emerald-400" />
          <h2 className="text-xl font-bold text-white">Conquistas Globais</h2>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {data?.achievements.map((a) => (
            <div
              key={a.id}
              className={`p-5 rounded-xl border flex items-start gap-4 ${
                a.unlocked
                  ? 'bg-[#151D2F] border-amber-500/30'
                  : 'bg-[#151D2F]/40 border-slate-800/80 opacity-60'
              }`}
            >
              <div className={`p-3 rounded-xl ${a.unlocked ? 'bg-amber-500/20 text-amber-400' : 'bg-slate-800 text-slate-500'}`}>
                {a.unlocked ? <CheckCircle2 className="h-6 w-6" /> : <Lock className="h-6 w-6" />}
              </div>
              <div>
                <h3 className="font-bold text-white text-sm">{a.title}</h3>
                <p className="text-xs text-slate-400 mt-0.5">{a.description}</p>
                <span className="text-[11px] font-bold text-amber-400 mt-2 block">+{a.xpReward} XP</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
