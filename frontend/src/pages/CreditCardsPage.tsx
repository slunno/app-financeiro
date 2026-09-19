import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { CreditCard, CreditCardInvoice } from '../types';
import { CreditCard as CardIcon, Plus, CheckCircle, AlertCircle, Trash2, Calendar } from 'lucide-react';

export const CreditCardsPage: React.FC = () => {
  const [cards, setCards] = useState<CreditCard[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [selectedCardInvoices, setSelectedCardInvoices] = useState<{ [cardId: string]: CreditCardInvoice[] }>({});

  const [name, setName] = useState('');
  const [creditLimit, setCreditLimit] = useState('');
  const [closingDay, setClosingDay] = useState('5');
  const [dueDay, setDueDay] = useState('15');
  const [cardBrand, setCardBrand] = useState('Mastercard');
  const [color, setColor] = useState('#3B82F6');

  const fetchCards = async () => {
    setLoading(true);
    try {
      const res = await api.get<CreditCard[]>('/credit-cards');
      setCards(res.data);

      for (const card of res.data) {
        const invRes = await api.get<CreditCardInvoice[]>(`/credit-cards/${card.id}/invoices`);
        setSelectedCardInvoices(prev => ({ ...prev, [card.id]: invRes.data }));
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCards();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/credit-cards', {
        name,
        creditLimit: parseFloat(creditLimit),
        closingDay: parseInt(closingDay, 10),
        dueDay: parseInt(dueDay, 10),
        cardBrand,
        color
      });
      setShowModal(false);
      setName('');
      setCreditLimit('');
      fetchCards();
    } catch (err) {
      alert('Erro ao cadastrar cartão');
    }
  };

  const handlePayInvoice = async (invoiceId: string) => {
    try {
      await api.post(`/credit-cards/invoices/${invoiceId}/pay`);
      alert('Fatura paga com sucesso!');
      fetchCards();
    } catch (err) {
      alert('Erro ao pagar fatura.');
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Deseja excluir este cartão?')) return;
    try {
      await api.delete(`/credit-cards/${id}`);
      fetchCards();
    } catch (err) {
      alert('Erro ao excluir cartão.');
    }
  };

  const formatBRL = (val: number) => {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(val);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">Cartões de Crédito</h1>
          <p className="text-sm text-slate-400 mt-1">Acompanhe seu limite utilizado, melhor dia de compra e faturas abertas.</p>
        </div>
        <button
          onClick={() => setShowModal(true)}
          className="flex items-center gap-2 bg-emerald-600 hover:bg-emerald-500 text-white px-4 py-2.5 rounded-lg text-sm font-semibold transition-colors"
        >
          <Plus className="h-4 w-4" />
          <span>Novo Cartão</span>
        </button>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500"></div>
        </div>
      ) : cards.length === 0 ? (
        <div className="bg-[#151D2F] border border-slate-800 rounded-xl p-12 text-center text-slate-400">
          <CardIcon className="h-12 w-12 mx-auto mb-3 opacity-40 text-blue-400" />
          <p className="text-base font-semibold text-white">Nenhum cartão cadastrado</p>
          <p className="text-sm mt-1">Cadastre seus cartões para controlar limite e evitar atrasos de fatura.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {cards.map((card) => {
            const invoices = selectedCardInvoices[card.id] || [];
            return (
              <div key={card.id} className="bg-[#151D2F] border border-slate-800 rounded-xl p-5 space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="p-3 rounded-xl text-white" style={{ backgroundColor: card.color || '#3B82F6' }}>
                      <CardIcon className="h-6 w-6" />
                    </div>
                    <div>
                      <h3 className="font-bold text-white text-base">{card.name}</h3>
                      <span className="text-xs text-slate-400">{card.cardBrand} • Vence dia {card.dueDay}</span>
                    </div>
                  </div>
                  <button onClick={() => handleDelete(card.id)} className="text-slate-500 hover:text-rose-400 p-1">
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>

                {/* Limit Progress */}
                <div className="bg-[#0B0F19] p-4 border border-slate-800/80 rounded-lg space-y-2">
                  <div className="flex justify-between text-xs font-semibold">
                    <span className="text-slate-400">Limite Utilizado: {formatBRL(card.usedLimit)}</span>
                    <span className="text-slate-300">Disponível: {formatBRL(card.availableLimit)}</span>
                  </div>
                  <div className="w-full bg-slate-800 rounded-full h-2.5 overflow-hidden">
                    <div
                      className={`h-2.5 rounded-full transition-all ${card.limitUsagePercentage > 80 ? 'bg-rose-500' : 'bg-blue-500'}`}
                      style={{ width: `${Math.min(card.limitUsagePercentage, 100)}%` }}
                    ></div>
                  </div>
                  <div className="flex justify-between text-[11px] text-slate-500">
                    <span>{card.limitUsagePercentage.toFixed(1)}% do limite total</span>
                    <span>Total: {formatBRL(card.creditLimit)}</span>
                  </div>
                </div>

                {/* Invoices List */}
                <div className="space-y-2 pt-2">
                  <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400">Faturas Recentes</h4>
                  {invoices.length === 0 ? (
                    <p className="text-xs text-slate-500 py-2">Nenhuma fatura gerada ainda.</p>
                  ) : (
                    invoices.map((inv) => (
                      <div key={inv.id} className="flex items-center justify-between p-3 bg-[#0B0F19] rounded-lg text-xs">
                        <div>
                          <span className="font-semibold text-white block">Fatura {inv.referenceMonth}/{inv.referenceYear}</span>
                          <span className="text-slate-500">Vencimento: {inv.dueDate}</span>
                        </div>
                        <div className="flex items-center gap-3">
                          <span className="font-bold text-slate-200">{formatBRL(inv.totalAmount)}</span>
                          {inv.status === 'PAID' ? (
                            <span className="px-2 py-0.5 bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded font-semibold text-[10px] flex items-center gap-1">
                              <CheckCircle className="h-3 w-3" /> Paga
                            </span>
                          ) : (
                            <button
                              onClick={() => handlePayInvoice(inv.id)}
                              className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-500 text-white rounded font-semibold text-[11px]"
                            >
                              Pagar Fatura
                            </button>
                          )}
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Modal Novo Cartão */}
      {showModal && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-[#151D2F] border border-slate-800 rounded-xl p-6 w-full max-w-md shadow-2xl space-y-4">
            <h3 className="text-lg font-bold text-white">Cadastrar Cartão de Crédito</h3>
            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Nome do Cartão</label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Ex: Nubank Black, Inter Mastercard"
                  className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Limite Total de Crédito (R$)</label>
                <input
                  type="number"
                  step="0.01"
                  required
                  value={creditLimit}
                  onChange={(e) => setCreditLimit(e.target.value)}
                  placeholder="5000.00"
                  className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Dia Fechamento</label>
                  <input
                    type="number"
                    min="1" max="31"
                    required
                    value={closingDay}
                    onChange={(e) => setClosingDay(e.target.value)}
                    className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Dia Vencimento</label>
                  <input
                    type="number"
                    min="1" max="31"
                    required
                    value={dueDay}
                    onChange={(e) => setDueDay(e.target.value)}
                    className="w-full bg-[#0B0F19] border border-slate-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Bandeira</label>
                <input
                  type="text"
                  value={cardBrand}
                  onChange={(e) => setCardBrand(e.target.value)}
                  placeholder="Mastercard, Visa, Elo"
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
                  Salvar Cartão
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
