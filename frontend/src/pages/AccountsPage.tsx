import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { Account, AccountType } from '../types';
import { Wallet, Plus, ArrowRightLeft, Trash2, Edit2, Landmark, Building2, CreditCard } from 'lucide-react';

export const AccountsPage: React.FC = () => {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [showTransferModal, setShowTransferModal] = useState(false);

  // Form State
  const [name, setName] = useState('');
  const [type, setType] = useState<AccountType>('CHECKING');
  const [initialBalance, setInitialBalance] = useState('0');
  const [bankName, setBankName] = useState('');
  const [color, setColor] = useState('#10B981');

  // Transfer State
  const [sourceId, setSourceId] = useState('');
  const [destId, setDestId] = useState('');
  const [transferAmount, setTransferAmount] = useState('');

  const fetchAccounts = () => {
    setLoading(true);
    api.get<Account[]>('/accounts')
      .then((res) => setAccounts(res.data))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchAccounts();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/accounts', {
        name,
        type,
        initialBalance: parseFloat(initialBalance),
        bankName,
        color
      });
      setShowModal(false);
      setName('');
      setInitialBalance('0');
      fetchAccounts();
    } catch (err) {
      alert('Erro ao criar conta bancária');
    }
  };

  const handleTransfer = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/accounts/transfer', {
        sourceAccountId: sourceId,
        destinationAccountId: destId,
        amount: parseFloat(transferAmount),
        description: 'Transferência entre contas'
      });
      setShowTransferModal(false);
      setTransferAmount('');
      fetchAccounts();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Erro ao realizar transferência');
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Deseja realmente excluir esta conta?')) return;
    try {
      await api.delete(`/accounts/${id}`);
      fetchAccounts();
    } catch (err) {
      alert('Erro ao excluir conta.');
    }
  };

  const formatBRL = (val: number) => {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(val);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">Contas & Carteiras</h1>
          <p className="text-sm text-slate-400 mt-1">Gerencie suas contas correntes, investimentos, poupança e dinheiro físico.</p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={() => setShowTransferModal(true)}
            className="flex items-center gap-2 bg-slate-800 hover:bg-slate-700 text-white px-4 py-2.5 rounded-lg text-sm font-semibold transition-colors"
          >
            <ArrowRightLeft className="h-4 w-4" />
            <span>Transferir</span>
          </button>
          <button
            onClick={() => setShowModal(true)}
            className="flex items-center gap-2 bg-emerald-600 hover:bg-emerald-500 text-white px-4 py-2.5 rounded-lg text-sm font-semibold transition-colors"
          >
            <Plus className="h-4 w-4" />
            <span>Nova Conta</span>
          </button>
        </div>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500"></div>
        </div>
      ) : accounts.length === 0 ? (
        <div className="bg-[#151D2F] border border-slate-800 rounded-xl p-12 text-center text-slate-400">
          <Wallet className="h-12 w-12 mx-auto mb-3 opacity-40 text-emerald-400" />
          <p className="text-base font-semibold text-white">Nenhuma conta cadastrada</p>
          <p className="text-sm mt-1">Adicione sua primeira conta corrente ou carteira digital para gerenciar seu saldo.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {accounts.map((acc) => (
            <div key={acc.id} className="bg-[#151D2F] border border-slate-800 rounded-xl p-5 flex flex-col justify-between hover:border-slate-700 transition-colors">
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-3">
                  <div className="p-3 rounded-xl text-white" style={{ backgroundColor: acc.color || '#10B981' }}>
                    <Landmark className="h-6 w-6" />
                  </div>
                  <div>
                    <h3 className="font-bold text-white text-base">{acc.name}</h3>
                    <span className="text-xs text-slate-400">{acc.bankName || 'Instituição Financeira'}</span>
                  </div>
                </div>
                <button onClick={() => handleDelete(acc.id)} className="text-slate-500 hover:text-rose-400 p-1">
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>

              <div className="mt-6 pt-4 border-t border-slate-800/80 flex items-end justify-between">
                <div>
                  <span className="text-xs text-slate-400 block font-medium">Saldo Atual</span>
                  <p className="text-2xl font-bold text-white mt-0.5">{formatBRL(acc.currentBalance)}</p>
                </div>
                <span className="text-xs px-2.5 py-1 bg-slate-800 text-slate-300 rounded-md font-semibold">
                  {acc.type}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal Nova Conta */}
      {showModal && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-[#151D2F] border border-slate-800 rounded-xl p-6 w-full max-w-md shadow-2xl space-y-4">
            <h3 className="text-lg font-bold text-white">Cadastrar Nova Conta</h3>
            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Nome da Conta</label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Ex: Nubank Principal, Carteira"
                  className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Tipo de Conta</label>
                <select
                  value={type}
                  onChange={(e) => setType(e.target.value as AccountType)}
                  className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                >
                  <option value="CHECKING">Conta Corrente</option>
                  <option value="SAVINGS">Poupança</option>
                  <option value="INVESTMENT">Investimento</option>
                  <option value="DIGITAL_WALLET">Carteira Digital</option>
                  <option value="CASH">Dinheiro em Espécie</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Instituição Bancária</label>
                <input
                  type="text"
                  value={bankName}
                  onChange={(e) => setBankName(e.target.value)}
                  placeholder="Ex: Itaú, Nubank, Bradesco"
                  className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Saldo Inicial (R$)</label>
                <input
                  type="number"
                  step="0.01"
                  required
                  value={initialBalance}
                  onChange={(e) => setInitialBalance(e.target.value)}
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
                  Salvar Conta
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Transferência */}
      {showTransferModal && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-[#151D2F] border border-slate-800 rounded-xl p-6 w-full max-w-md shadow-2xl space-y-4">
            <h3 className="text-lg font-bold text-white">Transferência Entre Contas</h3>
            <form onSubmit={handleTransfer} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Conta Origem</label>
                <select
                  required
                  value={sourceId}
                  onChange={(e) => setSourceId(e.target.value)}
                  className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                >
                  <option value="">Selecione a conta de origem</option>
                  {accounts.map(a => <option key={a.id} value={a.id}>{a.name} ({formatBRL(a.currentBalance)})</option>)}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Conta Destino</label>
                <select
                  required
                  value={destId}
                  onChange={(e) => setDestId(e.target.value)}
                  className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                >
                  <option value="">Selecione a conta de destino</option>
                  {accounts.map(a => <option key={a.id} value={a.id}>{a.name} ({formatBRL(a.currentBalance)})</option>)}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Valor da Transferência (R$)</label>
                <input
                  type="number"
                  step="0.01"
                  required
                  value={transferAmount}
                  onChange={(e) => setTransferAmount(e.target.value)}
                  placeholder="0.00"
                  className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowTransferModal(false)}
                  className="px-4 py-2 rounded-lg text-sm text-slate-400 hover:bg-slate-800"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg text-sm font-semibold bg-emerald-600 hover:bg-emerald-500 text-white"
                >
                  Confirmar Transferência
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
