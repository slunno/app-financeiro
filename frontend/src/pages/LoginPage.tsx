import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { AuthResponse } from '../types';
import { Wallet, Lock, Mail, ArrowRight, ShieldCheck, Trophy, Sparkles } from 'lucide-react';

export const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await api.post<AuthResponse>('/auth/login', { email, password });
      login(response.data);
      navigate('/');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Falha ao autenticar. Verifique suas credenciais.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#0B0F19] flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="flex justify-center items-center gap-3">
          <div className="p-3 bg-emerald-500/10 border border-emerald-500/30 rounded-xl text-emerald-400">
            <Wallet className="h-8 w-8" />
          </div>
          <span className="text-2xl font-bold tracking-tight text-white">Finanzas</span>
        </div>
        <h2 className="mt-6 text-center text-3xl font-extrabold text-white">
          Acesse sua conta
        </h2>
        <p className="mt-2 text-center text-sm text-slate-400">
          Gerencie seu patrimônio e evolua seu nível financeiro todos os dias.
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-[#151D2F] py-8 px-4 shadow-xl border border-slate-800 sm:rounded-xl sm:px-10">
          {error && (
            <div className="mb-4 p-3 bg-rose-500/10 border border-rose-500/30 rounded-lg text-rose-400 text-sm">
              {error}
            </div>
          )}

          <form className="space-y-6" onSubmit={handleSubmit}>
            <div>
              <label className="block text-sm font-medium text-slate-300">
                E-mail
              </label>
              <div className="mt-1 relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                  <Mail className="h-5 w-5" />
                </div>
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="block w-full pl-10 pr-3 py-2.5 bg-[#0B0F19] border border-slate-700 rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent text-sm"
                  placeholder="seu.email@exemplo.com"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-300">
                Senha
              </label>
              <div className="mt-1 relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                  <Lock className="h-5 w-5" />
                </div>
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="block w-full pl-10 pr-3 py-2.5 bg-[#0B0F19] border border-slate-700 rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent text-sm"
                  placeholder="••••••••"
                />
              </div>
            </div>

            <div>
              <button
                type="submit"
                disabled={loading}
                className="w-full flex justify-center items-center gap-2 py-3 px-4 border border-transparent rounded-lg text-sm font-medium text-white bg-emerald-600 hover:bg-emerald-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-emerald-500 transition-colors disabled:opacity-50"
              >
                {loading ? 'Autenticando...' : 'Entrar no Sistema'}
                {!loading && <ArrowRight className="h-4 w-4" />}
              </button>
            </div>
          </form>

          <div className="mt-6 border-t border-slate-800 pt-6">
            <div className="flex items-center justify-between text-sm">
              <span className="text-slate-400">Ainda não tem conta?</span>
              <Link to="/register" className="font-medium text-emerald-400 hover:text-emerald-300">
                Criar conta gratuita
              </Link>
            </div>
          </div>
        </div>

        {/* Feature Badges */}
        <div className="mt-8 grid grid-cols-3 gap-3 text-center">
          <div className="p-3 bg-[#151D2F]/50 border border-slate-800/80 rounded-lg">
            <ShieldCheck className="h-5 w-5 text-emerald-400 mx-auto mb-1" />
            <span className="text-xs text-slate-400 block font-medium">Segurança JWT</span>
          </div>
          <div className="p-3 bg-[#151D2F]/50 border border-slate-800/80 rounded-lg">
            <Trophy className="h-5 w-5 text-amber-400 mx-auto mb-1" />
            <span className="text-xs text-slate-400 block font-medium">Gamificação XP</span>
          </div>
          <div className="p-3 bg-[#151D2F]/50 border border-slate-800/80 rounded-lg">
            <Sparkles className="h-5 w-5 text-blue-400 mx-auto mb-1" />
            <span className="text-xs text-slate-400 block font-medium">Dashboard SaaS</span>
          </div>
        </div>
      </div>
    </div>
  );
};
