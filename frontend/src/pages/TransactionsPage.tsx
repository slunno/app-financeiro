import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { Transaction, Account, Category, CreditCard, TransactionType, PaymentMethod } from '../types';
import { Plus, Trash2, Filter, Receipt, ArrowUpRight, ArrowDownLeft } from 'lucide-react';

export const TransactionsPage: React.FC = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [creditCards, setCreditCards] = useState<CreditCard[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);

  // Form State
  const [description, setDescription] = useState('');
  const [amount, setAmount] = useState('');
  const [type, setType] = useState<TransactionType>('EXPENSE');
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('PIX');
  const [categoryId, setCategoryId] = useState('');
  const [accountId, setAccountId] = useState('');
  const [creditCardId, setCreditCardId] = useState('');
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [installmentsCount, setInstallmentsCount] = useState('1');

  const fetchData = async () => {
    setLoading(true);
    try {
      const [tRes, aRes, cRes, ccRes] = await Promise.all([
        api.get<Transaction[]>('/transactions'),
        api.get<Account[]>('/accounts'),
        api.get<Category[]>('/categories'),
        api.get<CreditCard[]>('/credit-cards'),
      ]);
      setTransactions(tRes.data);
      setAccounts(aRes.data);
      setCategories(cRes.data);
      setCreditCards(ccRes.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/transactions', {
        description,
        amount: parseFloat(amount),
        type,
        paymentMethod,
        categoryId,
        accountId: accountId || null,
        creditCardId: creditCardId || null,
        date,
        installmentsCount: parseInt(installmentsCount, 10)
      });
      setShowModal(false);
      setDescription('');
      setAmount('');
      fetchData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Erro ao registrar transação');
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Deseja excluir esta transação?')) return;
    try {
      await api.delete(`/transactions/${id}`);
      fetchData();
    } catch (err) {
      alert('Erro ao excluir.');
    }
  };

  const formatBRL = (val: number) => {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(val);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">Lançamentos Financeiros</h1>
          <p className="text-sm text-slate-400 mt-1">Registre receitas, despesas e compras parceladas no cartão.</p>
        </div>
        <button
          onClick={() => setShowModal(true)}
          className="flex items-center gap-2 bg-emerald-600 hover:bg-emerald-500 text-white px-4 py-2.5 rounded-lg text-sm font-semibold transition-colors"
        >
          <Plus className="h-4 w-4" />
          <span>Novo Lançamento</span>
        </button>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500"></div>
        </div>
      ) : transactions.length === 0 ? (
        <div className="bg-[#151D2F] border border-slate-800 rounded-xl p-12 text-center text-slate-400">
          <Receipt className="h-12 w-12 mx-auto mb-3 opacity-40 text-emerald-400" />
          <p className="text-base font-semibold text-white">Nenhum lançamento registrado</p>
          <p className="text-sm mt-1">Cadastre receitas e despesas para ganhar XP e manter o hábito ativo.</p>
        </div>
      ) : (
        <div className="bg-[#151D2F] border border-slate-800 rounded-xl overflow-hidden shadow-xl">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-[#0B0F19] text-slate-400 border-b border-slate-800 uppercase text-[11px] tracking-wider font-semibold">
                <tr>
                  <th className="px-5 py-3.5">Descrição</th>
                  <th className="px-5 py-3.5">Categoria</th>
                  <th className="px-5 py-3.5">Origem / Método</th>
                  <th className="px-5 py-3.5">Data</th>
                  <th className="px-5 py-3.5">Valor</th>
                  <th className="px-5 py-3.5 text-right">Ação</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800 text-slate-200">
                {transactions.map((t) => (
                  <tr key={t.id} className="hover:bg-slate-800/40 transition-colors">
                    <td className="px-5 py-3.5 font-semibold text-white flex items-center gap-3">
                      <div className={`p-2 rounded-lg ${t.type === 'INCOME' ? 'bg-emerald-500/10 text-emerald-400' : 'bg-rose-500/10 text-rose-400'}`}>
                        {t.type === 'INCOME' ? <ArrowUpRight className="h-4 w-4" /> : <ArrowDownLeft className="h-4 w-4" />}
                      </div>
                      <span>{t.description}</span>
                    </td>
                    <td className="px-5 py-3.5">
                      <span className="px-2.5 py-1 bg-slate-800 rounded-md text-xs text-slate-300 font-medium">
                        {t.categoryName}
                      </span>
                    </td>
                    <td className="px-5 py-3.5 text-xs text-slate-400">
                      {t.creditCardName ? `Cartão: ${t.creditCardName}` : t.accountName || t.paymentMethod}
                    </td>
                    <td className="px-5 py-3.5 text-xs text-slate-400">{t.date}</td>
                    <td className={`px-5 py-3.5 font-bold ${t.type === 'INCOME' ? 'text-emerald-400' : 'text-slate-200'}`}>
                      {t.type === 'INCOME' ? '+' : '-'} {formatBRL(t.amount)}
                    </td>
                    <td className="px-5 py-3.5 text-right">
                      <button onClick={() => handleDelete(t.id)} className="text-slate-500 hover:text-rose-400 p-1">
                        <Trash2 className="h-4 w-4" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Modal Lançamento */}
      {showModal && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-[#151D2F] border border-slate-800 rounded-xl p-6 w-full max-w-lg shadow-2xl space-y-4">
            <h3 className="text-lg font-bold text-white">Novo Lançamento Financeiro</h3>
            <form onSubmit={handleCreate} className="space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <button
                  type="button"
                  onClick={() => setType('EXPENSE')}
                  className={`py-2 rounded-lg text-sm font-semibold border ${type === 'EXPENSE' ? 'bg-rose-600/20 border-rose-500 text-rose-400' : 'bg-[#0B0F19] border-slate-700 text-slate-400'}`}
                >
                  Despesa
                </button>
                <button
                  type="button"
                  onClick={() => setType('INCOME')}
                  className={`py-2 rounded-lg text-sm font-semibold border ${type === 'INCOME' ? 'bg-emerald-600/20 border-emerald-500 text-emerald-400' : 'bg-[#0B0F19] border-slate-700 text-slate-400'}`}
                >
                  Receita
                </button>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Descrição</label>
                <input
                  type="text"
                  required
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Ex: Supermercado, Salário Mensal"
                  className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Valor (R$)</label>
                  <input
                    type="number"
                    step="0.01"
                    required
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                    placeholder="0.00"
                    className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Data</label>
                  <input
                    type="date"
                    required
                    value={date}
                    onChange={(e) => setDate(e.target.value)}
                    className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Categoria</label>
                <select
                  required
                  value={categoryId}
                  onChange={(e) => setCategoryId(e.target.value)}
                  className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                >
                  <option value="">Selecione uma categoria</option>
                  {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Conta Bancária</label>
                  <select
                    value={accountId}
                    onChange={(e) => { setAccountId(e.target.value); setCreditCardId(''); }}
                    className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  >
                    <option value="">Nenhuma (ou Cartão)</option>
                    {accounts.map((a) => <option key={a.id} value={a.id}>{a.name}</option>)}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Cartão de Crédito</label>
                  <select
                    value={creditCardId}
                    onChange={(e) => { setCreditCardId(e.target.value); setAccountId(''); setPaymentMethod('CREDIT_CARD'); }}
                    className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  >
                    <option value="">Nenhum (ou Conta)</option>
                    {creditCards.map((cc) => <option key={cc.id} value={cc.id}>{cc.name}</option>)}
                  </select>
                </div>
              </div>

              {creditCardId && (
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Parcelas (1x à vista)</label>
                  <select
                    value={installmentsCount}
                    onChange={(e) => setInstallmentsCount(e.target.value)}
                    className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  >
                    {[1,2,3,4,5,6,10,12].map(n => <option key={n} value={n}>{n}x</option>)}
                  </select>
                </div>
              )}

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
                  Salvar Lançamento (+10 XP)
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
