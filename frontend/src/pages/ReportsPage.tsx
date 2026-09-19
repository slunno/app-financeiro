import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, Legend } from 'recharts';
import { BarChart3, TrendingUp, TrendingDown, Calendar } from 'lucide-react';

interface MonthlyComparison {
  monthLabel: string;
  income: number;
  expense: number;
  result: number;
}

export const ReportsPage: React.FC = () => {
  const [data, setData] = useState<MonthlyComparison[]>([]);
  const [loading, setLoading] = useState(true);
  const [months, setMonths] = useState(6);

  useEffect(() => {
    setLoading(true);
    api.get<MonthlyComparison[]>(`/reports/monthly-comparison?months=${months}`)
      .then((res) => setData(res.data))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, [months]);

  const formatBRL = (val: number) => {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(val);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">Relatórios & Gráficos</h1>
          <p className="text-sm text-slate-400 mt-1">Análise comparativa de Receitas x Despesas e fluxo de caixa por período.</p>
        </div>
        <div className="flex items-center gap-2 bg-[#151D2F] border border-slate-800 p-1.5 rounded-lg text-xs font-semibold">
          <button
            onClick={() => setMonths(3)}
            className={`px-3 py-1.5 rounded-md ${months === 3 ? 'bg-emerald-600 text-white' : 'text-slate-400 hover:text-white'}`}
          >
            3 Meses
          </button>
          <button
            onClick={() => setMonths(6)}
            className={`px-3 py-1.5 rounded-md ${months === 6 ? 'bg-emerald-600 text-white' : 'text-slate-400 hover:text-white'}`}
          >
            6 Meses
          </button>
          <button
            onClick={() => setMonths(12)}
            className={`px-3 py-1.5 rounded-md ${months === 12 ? 'bg-emerald-600 text-white' : 'text-slate-400 hover:text-white'}`}
          >
            12 Meses
          </button>
        </div>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500"></div>
        </div>
      ) : (
        <div className="space-y-6">
          {/* Main Chart */}
          <div className="bg-[#151D2F] border border-slate-800 rounded-xl p-5 space-y-4">
            <h3 className="font-bold text-white text-base flex items-center gap-2">
              <BarChart3 className="h-5 w-5 text-emerald-400" />
              <span>Evolução de Receitas x Despesas</span>
            </h3>

            <div className="h-80 w-full pt-4">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={data} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#334155" vertical={false} />
                  <XAxis dataKey="monthLabel" stroke="#94A3B8" fontSize={12} tickLine={false} />
                  <YAxis stroke="#94A3B8" fontSize={12} tickLine={false} />
                  <Tooltip
                    contentStyle={{ backgroundColor: '#0B0F19', borderColor: '#334155', borderRadius: '8px', color: '#fff' }}
                    formatter={(value: any) => [formatBRL(Number(value)), '']}
                  />
                  <Legend wrapperStyle={{ paddingTop: '10px' }} />
                  <Bar dataKey="income" name="Receitas" fill="#10B981" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="expense" name="Despesas" fill="#EF4444" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Monthly Comparison Table */}
          <div className="bg-[#151D2F] border border-slate-800 rounded-xl overflow-hidden shadow-xl">
            <div className="px-5 py-4 border-b border-slate-800">
              <h3 className="font-bold text-white text-sm uppercase tracking-wider">Detalhamento Mensal</h3>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-[#0B0F19] text-slate-400 border-b border-slate-800 uppercase text-[11px] tracking-wider font-semibold">
                  <tr>
                    <th className="px-5 py-3.5">Mês / Ano</th>
                    <th className="px-5 py-3.5">Receitas</th>
                    <th className="px-5 py-3.5">Despesas</th>
                    <th className="px-5 py-3.5 text-right">Resultado</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800 text-slate-200">
                  {data.map((row, idx) => (
                    <tr key={idx} className="hover:bg-slate-800/40 transition-colors">
                      <td className="px-5 py-3.5 font-semibold text-white flex items-center gap-2">
                        <Calendar className="h-4 w-4 text-slate-400" />
                        <span>{row.monthLabel}</span>
                      </td>
                      <td className="px-5 py-3.5 text-emerald-400 font-semibold">{formatBRL(row.income)}</td>
                      <td className="px-5 py-3.5 text-rose-400 font-semibold">{formatBRL(row.expense)}</td>
                      <td className={`px-5 py-3.5 font-bold text-right ${row.result >= 0 ? 'text-emerald-400' : 'text-rose-400'}`}>
                        {formatBRL(row.result)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
