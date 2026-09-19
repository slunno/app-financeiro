import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { Budget, Category } from '../types';
import { PieChart, Plus, AlertTriangle, ShieldCheck, Trash2 } from 'lucide-react';

export const BudgetsPage: React.FC = () => {
  const [budgets, setBudgets] = useState<Budget[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);

  const [categoryId, setCategoryId] = useState('');
  const [maxAmount, setMaxAmount] = useState('');

  const fetchBudgets = async () => {
    setLoading(true);
    try {
      const [bRes, cRes] = await Promise.all([
        api.get<Budget[]>('/budgets'),
        api.get<Category[]>('/categories?type=EXPENSE')
      ]);
      setBudgets(bRes.data);
      setCategories(cRes.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBudgets();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const now = new Date();
      await api.post('/budgets', {
        categoryId,
        maxAmount: parseFloat(maxAmount),
        month: now.getMonth() + 1,
        year: now.getFullYear()
      });
      setShowModal(false);
      setMaxAmount('');
      fetchBudgets();
    } catch (err) {
      alert('Erro ao definir orçamento.');
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Excluir este orçamento?')) return;
    try {
      await api.delete(`/budgets/${id}`);
      fetchBudgets();
    } catch (err) {
      alert('Erro ao excluir orçamento.');
    }
  };

  const formatBRL = (val: number) => {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(val);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">Orçamento Mensal</h1>
          <p className="text-sm text-slate-400 mt-1">Defina metas de limite por categoria de despesa e acompanhe seu consumo.</p>
        </div>
        <button
          onClick={() => setShowModal(true)}
          className="flex items-center gap-2 bg-emerald-600 hover:bg-emerald-500 text-white px-4 py-2.5 rounded-lg text-sm font-semibold transition-colors"
        >
          <Plus className="h-4 w-4" />
          <span>Definir Teto</span>
        </button>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500"></div>
        </div>
      ) : budgets.length === 0 ? (
        <div className="bg-[#151D2F] border border-slate-800 rounded-xl p-12 text-center text-slate-400">
          <PieChart className="h-12 w-12 mx-auto mb-3 opacity-40 text-amber-400" />
          <p className="text-base font-semibold text-white">Nenhum orçamento planejado para este mês</p>
          <p className="text-sm mt-1">Defina limites para categorias como Alimentação e Lazer para ganhar +300 XP no final do mês!</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {budgets.map((b) => {
            const percentage = b.maxAmount > 0 ? (b.spentAmount / b.maxAmount) * 100 : 0;
            const isExceeded = percentage >= 100;
            const isWarning = percentage >= 80 && !isExceeded;

            return (
              <div key={b.id} className="bg-[#151D2F] border border-slate-800 rounded-xl p-5 space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="p-2.5 rounded-lg text-white" style={{ backgroundColor: b.categoryColor || '#10B981' }}>
                      <PieChart className="h-5 w-5" />
                    </div>
                    <div>
                      <h3 className="font-bold text-white text-base">{b.categoryName}</h3>
                      <span className="text-xs text-slate-400">Teto: {formatBRL(b.maxAmount)}</span>
                    </div>
                  </div>
                  <button onClick={() => handleDelete(b.id)} className="text-slate-500 hover:text-rose-400 p-1">
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>

                <div className="space-y-1">
                  <div className="flex justify-between text-xs font-semibold">
                    <span className="text-slate-300">Gasto: {formatBRL(b.spentAmount)}</span>
                    <span className={isExceeded ? 'text-rose-400 font-bold' : isWarning ? 'text-amber-400' : 'text-emerald-400'}>
                      {percentage.toFixed(1)}%
                    </span>
                  </div>
                  <div className="w-full bg-slate-800 rounded-full h-2.5 overflow-hidden">
                    <div
                      className={`h-2.5 rounded-full transition-all ${isExceeded ? 'bg-rose-500' : isWarning ? 'bg-amber-400' : 'bg-emerald-500'}`}
                      style={{ width: `${Math.min(percentage, 100)}%` }}
                    ></div>
                  </div>
                </div>

                <div className="flex items-center justify-between text-xs pt-2 border-t border-slate-800">
                  {isExceeded ? (
                    <div className="flex items-center gap-1.5 text-rose-400 font-medium">
                      <AlertTriangle className="h-4 w-4" />
                      <span>Orçamento excedido!</span>
                    </div>
                  ) : isWarning ? (
                    <div className="flex items-center gap-1.5 text-amber-400 font-medium">
                      <AlertTriangle className="h-4 w-4" />
                      <span>Atenção: limite próximo!</span>
                    </div>
                  ) : (
                    <div className="flex items-center gap-1.5 text-emerald-400 font-medium">
                      <ShieldCheck className="h-4 w-4" />
                      <span>Dentro do planejado</span>
                    </div>
                  )}
                  <span className="text-slate-500">Restante: {formatBRL(Math.max(b.maxAmount - b.spentAmount, 0))}</span>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Modal Definir Teto */}
      {showModal && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-[#151D2F] border border-slate-800 rounded-xl p-6 w-full max-w-md shadow-2xl space-y-4">
            <h3 className="text-lg font-bold text-white">Definir Orçamento de Categoria</h3>
            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Categoria de Despesa</label>
                <select
                  required
                  value={categoryId}
                  onChange={(e) => setCategoryId(e.target.value)}
                  className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                >
                  <option value="">Selecione a categoria</option>
                  {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Limite Máximo Mensal (R$)</label>
                <input
                  type="number"
                  step="0.01"
                  required
                  value={maxAmount}
                  onChange={(e) => setMaxAmount(e.target.value)}
                  placeholder="1000.00"
                  className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
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
                  Salvar Teto
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
