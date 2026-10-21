import React from 'react';
import { NavLink } from 'react-router-dom';
import { 
  ShieldCheck, 
  LayoutDashboard, 
  FileCheck2, 
  ShoppingCart, 
  Inbox, 
  CreditCard, 
  Repeat, 
  BookOpen, 
  AlertTriangle, 
  History
} from 'lucide-react';

const navItems = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/mandates', label: 'Mandates', icon: FileCheck2 },
  { to: '/purchase-intents', label: 'Purchase Intents', icon: ShoppingCart },
  { to: '/approvals', label: 'Approvals Inbox', icon: Inbox },
  { to: '/payments', label: 'Payments & Refunds', icon: CreditCard },
  { to: '/subscriptions', label: 'Subscriptions', icon: Repeat },
  { to: '/ledger', label: 'Double-Entry Ledger', icon: BookOpen },
  { to: '/risk-rules', label: 'Risk & Fraud Rules', icon: AlertTriangle },
  { to: '/audit', label: 'Audit Trail', icon: History },
];

export const Sidebar: React.FC = () => {
  return (
    <aside className="w-64 bg-surface border-r border-surfaceBorder flex flex-col h-screen fixed left-0 top-0 z-20">
      <div className="h-16 flex items-center px-6 gap-3 border-b border-surfaceBorder">
        <div className="w-9 h-9 rounded-lg bg-primary/20 flex items-center justify-center border border-primary/40">
          <ShieldCheck className="w-5 h-5 text-primary" />
        </div>
        <div>
          <h1 className="font-bold text-base tracking-tight text-white">AgentGuard</h1>
          <p className="text-[10px] text-slate-400 font-mono">FINTECH & TRUST PLATFORM</p>
        </div>
      </div>

      <nav className="flex-1 py-4 px-3 space-y-1 overflow-y-auto">
        {navItems.map(item => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-all ${
                  isActive
                    ? 'bg-primary text-white shadow-lg shadow-primary/25 font-semibold'
                    : 'text-slate-400 hover:text-slate-100 hover:bg-surfaceBorder/40'
                }`
              }
            >
              <Icon className="w-4 h-4 shrink-0" />
              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </nav>

      <div className="p-4 border-t border-surfaceBorder bg-black/20">
        <div className="flex items-center gap-2">
          <div className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse" />
          <span className="text-xs font-mono text-slate-400">Ledger Invariant: Balanced</span>
        </div>
      </div>
    </aside>
  );
};
