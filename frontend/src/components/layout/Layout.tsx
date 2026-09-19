import React, { useState } from 'react';
import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  Wallet,
  LayoutDashboard,
  CreditCard,
  Receipt,
  PieChart,
  Target,
  Trophy,
  BarChart3,
  LogOut,
  Menu,
  X,
  Bell,
  Flame,
  User as UserIcon,
  ShieldAlert
} from 'lucide-react';

export const Layout: React.FC = () => {
  const { user, userXp, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);

  const navItems = [
    { label: 'Dashboard', path: '/', icon: LayoutDashboard },
    { label: 'Contas & Carteiras', path: '/accounts', icon: Wallet },
    { label: 'Transações', path: '/transactions', icon: Receipt },
    { label: 'Cartões de Crédito', path: '/credit-cards', icon: CreditCard },
    { label: 'Orçamentos', path: '/budgets', icon: PieChart },
    { label: 'Metas Financeiras', path: '/goals', icon: Target },
    { label: 'Gamificação & XP', path: '/gamification', icon: Trophy },
    { label: 'Relatórios', path: '/reports', icon: BarChart3 },
  ];

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const level = userXp?.currentLevel || 1;
  const currentXp = userXp?.currentXp || 0;
  const xpNeeded = level * 200;
  const xpPercentage = Math.min((currentXp / xpNeeded) * 100, 100);

  return (
    <div className="min-h-screen bg-[#0B0F19] text-slate-100 flex flex-col md:flex-row">
      {/* Mobile Top Bar */}
      <div className="md:hidden flex items-center justify-between px-4 py-3 bg-[#151D2F] border-b border-slate-800">
        <div className="flex items-center gap-2">
          <div className="p-2 bg-emerald-500/10 border border-emerald-500/30 rounded-lg text-emerald-400">
            <Wallet className="h-5 w-5" />
          </div>
          <span className="font-bold text-lg text-white">Finanzas</span>
        </div>

        <div className="flex items-center gap-3">
          <div className="flex items-center gap-1 text-amber-400 bg-amber-500/10 border border-amber-500/20 px-2 py-1 rounded-md text-xs font-semibold">
            <Flame className="h-4 w-4" />
            <span>{userXp?.streakDays || 1}d</span>
          </div>
          <button
            onClick={() => setMobileOpen(!mobileOpen)}
            className="p-2 rounded-lg bg-slate-800 text-slate-300"
          >
            {mobileOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
          </button>
        </div>
      </div>

      {/* Sidebar Navigation */}
      <aside
        className={`fixed inset-y-0 left-0 z-50 w-64 bg-[#151D2F] border-r border-slate-800 flex flex-col justify-between transform transition-transform duration-200 ease-in-out md:translate-x-0 ${
          mobileOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div>
          {/* Logo Header */}
          <div className="p-5 border-b border-slate-800 flex items-center gap-3">
            <div className="p-2.5 bg-emerald-500/10 border border-emerald-500/30 rounded-xl text-emerald-400">
              <Wallet className="h-6 w-6" />
            </div>
            <div>
              <h1 className="font-bold text-lg text-white tracking-wide">Finanzas</h1>
              <span className="text-xs text-slate-400 font-medium">SaaS & Gamificação</span>
            </div>
          </div>

          {/* Gamification Level Mini Widget */}
          <div className="mx-4 mt-4 p-3.5 bg-[#0B0F19] border border-slate-800 rounded-xl">
            <div className="flex items-center justify-between mb-1.5">
              <span className="text-xs font-semibold text-emerald-400 uppercase tracking-wider">
                Nível {level}
              </span>
              <div className="flex items-center gap-1 text-amber-400 text-xs font-bold">
                <Flame className="h-3.5 w-3.5 fill-amber-400" />
                <span>{userXp?.streakDays || 1} Dias</span>
              </div>
            </div>
            <div className="w-full bg-slate-800 rounded-full h-2 mb-1 overflow-hidden">
              <div
                className="bg-emerald-500 h-2 rounded-full transition-all duration-300"
                style={{ width: `${xpPercentage}%` }}
              ></div>
            </div>
            <div className="flex justify-between text-[10px] text-slate-400">
              <span>{currentXp} XP</span>
              <span>{xpNeeded} XP</span>
            </div>
          </div>

          {/* Nav Items */}
          <nav className="mt-4 px-3 space-y-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = location.pathname === item.path;
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  onClick={() => setMobileOpen(false)}
                  className={`flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-emerald-600/10 text-emerald-400 border border-emerald-500/20'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                  }`}
                >
                  <Icon className="h-4 w-4" />
                  <span>{item.label}</span>
                </Link>
              );
            })}
          </nav>
        </div>

        {/* User Footer */}
        <div className="p-4 border-t border-slate-800 flex items-center justify-between">
          <div className="flex items-center gap-2.5 overflow-hidden">
            <div className="p-2 bg-slate-800 rounded-lg text-slate-300 flex-shrink-0">
              <UserIcon className="h-4 w-4" />
            </div>
            <div className="truncate">
              <p className="text-xs font-semibold text-white truncate">{user?.name}</p>
              <p className="text-[11px] text-slate-400 truncate">{user?.email}</p>
            </div>
          </div>
          <button
            onClick={handleLogout}
            title="Sair do sistema"
            className="p-2 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition-colors"
          >
            <LogOut className="h-4 w-4" />
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="flex-1 md:pl-64 min-h-screen flex flex-col">
        <div className="p-4 sm:p-6 md:p-8 flex-1 max-w-7xl mx-auto w-full">
          <Outlet />
        </div>
      </main>
    </div>
  );
};
