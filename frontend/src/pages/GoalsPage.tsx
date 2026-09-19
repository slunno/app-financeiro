import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { Goal } from '../types';
import { Target, Plus, Trophy, DollarSign, CheckCircle2, Trash2 } from 'lucide-react';

export const GoalsPage: React.FC = () => {
  const [goals, setGoals] = useState<Goal[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [showAporteModal, setShowAporteModal] = useState(false);
  const [selectedGoalId, setSelectedGoalId] = useState('');

  // Form state
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [targetAmount, setTargetAmount] = useState('');
  const [currentAmount, setCurrentAmount] = useState('0');
  const [targetDate, setTargetDate] = useState('');

  // Aporte state
  const [contributionAmount, setContributionAmount] = useState('');

  const fetchGoals = () => {
    setLoading(true);
    api.get<Goal[]>('/goals')
      .then((res) => setGoals(res.data))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchGoals();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/goals', {
        title,
        description,
        targetAmount: parseFloat(targetAmount),
        currentAmount: parseFloat(currentAmount),
        targetDate: targetDate || null
      });
      setShowModal(false);
      setTitle('');
      setTargetAmount('');
      fetchGoals();
    } catch (err) {
      alert('Erro ao criar meta financeira');
    }
  };

  const handleAporte = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post(`/goals/${selectedGoalId}/contribute`, {
        amount: parseFloat(contributionAmount),
        notes: 'Aporte realizado via aplicativo'
      });
      setShowAporteModal(false);
      setContributionAmount('');
      alert('Aporte realizado com sucesso! Se a meta foi concluída, você ganhou +200 XP!');
      fetchGoals();
    } catch (err) {
      alert('Erro ao realizar aporte.');
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Excluir esta meta?')) return;
    try {
      await api.delete(`/goals/${id}`);
      fetchGoals();
    } catch (err) {
      alert('Erro ao excluir meta.');
    }
  };

  const formatBRL = (val: number) => {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(val);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">Metas Financeiras</h1>
          <p className="text-sm text-slate-400 mt-1">Crie objetivos de reserva, viagens ou compras e ganhe +200 XP ao atingir 100%.</p>
        </div>
        <button
          onClick={() => setShowModal(true)}
          className="flex items-center gap-2 bg-emerald-600 hover:bg-emerald-500 text-white px-4 py-2.5 rounded-lg text-sm font-semibold transition-colors"
        >
          <Plus className="h-4 w-4" />
          <span>Nova Meta</span>
        </button>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500"></div>
        </div>
      ) : goals.length === 0 ? (
        <div className="bg-[#151D2F] border border-slate-800 rounded-xl p-12 text-center text-slate-400">
          <Target className="h-12 w-12 mx-auto mb-3 opacity-40 text-emerald-400" />
          <p className="text-base font-semibold text-white">Nenhuma meta cadastrada</p>
          <p className="text-sm mt-1">Defina metas como Reserva de Emergência ou Viagem para desbloquear conquistas.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {goals.map((g) => {
            const isCompleted = g.status === 'COMPLETED' || g.progressPercentage >= 100;
            return (
              <div key={g.id} className="bg-[#151D2F] border border-slate-800 rounded-xl p-5 flex flex-col justify-between space-y-4">
                <div className="space-y-2">
                  <div className="flex items-start justify-between">
                    <div className="flex items-center gap-3">
                      <div className={`p-2.5 rounded-xl text-white ${isCompleted ? 'bg-amber-500' : 'bg-emerald-600'}`}>
                        {isCompleted ? <Trophy className="h-5 w-5" /> : <Target className="h-5 w-5" />}
                      </div>
                      <div>
                        <h3 className="font-bold text-white text-base">{g.title}</h3>
                        <span className="text-xs text-slate-400">Alvo: {formatBRL(g.targetAmount)}</span>
                      </div>
                    </div>
                    <button onClick={() => handleDelete(g.id)} className="text-slate-500 hover:text-rose-400 p-1">
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                  {g.description && <p className="text-xs text-slate-400">{g.description}</p>}
                </div>

                <div className="space-y-2">
                  <div className="flex justify-between text-xs font-semibold">
                    <span className="text-slate-300">Acumulado: {formatBRL(g.currentAmount)}</span>
                    <span className={isCompleted ? 'text-amber-400 font-bold' : 'text-emerald-400'}>
                      {g.progressPercentage.toFixed(1)}%
                    </span>
                  </div>
                  <div className="w-full bg-slate-800 rounded-full h-2.5 overflow-hidden">
                    <div
                      className={`h-2.5 rounded-full transition-all ${isCompleted ? 'bg-amber-400' : 'bg-emerald-500'}`}
                      style={{ width: `${g.progressPercentage}%` }}
                    ></div>
                  </div>

                  <div className="flex items-center justify-between pt-2">
                    {isCompleted ? (
                      <span className="text-xs text-amber-400 font-bold flex items-center gap-1">
                        <CheckCircle2 className="h-4 w-4" /> Meta Concluída (+200 XP)
                      </span>
                    ) : (
                      <button
                        onClick={() => { setSelectedGoalId(g.id); setShowAporteModal(true); }}
                        className="w-full py-2 bg-slate-800 hover:bg-slate-700 text-emerald-400 rounded-lg text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors"
                      >
                        <DollarSign className="h-4 w-4" />
                        <span>Fazer Aporte</span>
                      </button>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Modal Nova Meta */}
      {showModal && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-[#151D2F] border border-slate-800 rounded-xl p-6 w-full max-w-md shadow-2xl space-y-4">
            <h3 className="text-lg font-bold text-white">Criar Meta Financeira</h3>
            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Título da Meta</label>
                <input
                  type="text"
                  required
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="Ex: Reserva de Emergência, Viagem Europa"
                  className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Descrição (opcional)</label>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Detalhes sobre a meta..."
                  className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 h-20"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Valor Alvo (R$)</label>
                  <input
                    type="number"
                    step="0.01"
                    required
                    value={targetAmount}
                    onChange={(e) => setTargetAmount(e.target.value)}
                    placeholder="10000.00"
                    className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Valor Inicial (R$)</label>
                  <input
                    type="number"
                    step="0.01"
                    value={currentAmount}
                    onChange={(e) => setCurrentAmount(e.target.value)}
                    className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  />
                </div>
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 rounded-lg text-sm text-slate-400 hover:bg-slate-800"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg text-sm font-semibold bg-emerald-600 hover:bg-emerald-500 text-white"
                >
                  Salvar Meta
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Aporte */}
      {showAporteModal && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-[#151D2F] border border-slate-800 rounded-xl p-6 w-full max-w-md shadow-2xl space-y-4">
            <h3 className="text-lg font-bold text-white">Adicionar Aporte na Meta</h3>
            <form onSubmit={handleAporte} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Valor do Aporte (R$)</label>
                <input
                  type="number"
                  step="0.01"
                  required
                  value={contributionAmount}
                  onChange={(e) => setContributionAmount(e.target.value)}
                  placeholder="500.00"
                  className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowAporteModal(false)}
                  className="px-4 py-2 rounded-lg text-sm text-slate-400 hover:bg-slate-800"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg text-sm font-semibold bg-emerald-600 hover:bg-emerald-500 text-white"
                >
                  Confirmar Aporte
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
