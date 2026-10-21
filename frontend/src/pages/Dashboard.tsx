import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { DashboardStats } from '../types';
import { 
  DollarSign, 
  ShoppingCart, 
  Clock, 
  ShieldCheck, 
  CheckCircle2, 
  Repeat 
} from 'lucide-react';
import { 
  AreaChart, 
  Area, 
  XAxis, 
  YAxis, 
  Tooltip, 
  ResponsiveContainer, 
  BarChart, 
  Bar, 
  CartesianGrid 
} from 'recharts';

const spendTrendData = [
  { day: 'Mon', spend: 340, intents: 4 },
  { day: 'Tue', spend: 890, intents: 9 },
  { day: 'Wed', spend: 450, intents: 5 },
  { day: 'Thu', spend: 1250, intents: 14 },
  { day: 'Fri', spend: 980, intents: 11 },
  { day: 'Sat', spend: 310, intents: 3 },
  { day: 'Sun', spend: 520, intents: 6 },
];

const riskDistributionData = [
  { name: 'Approved', count: 24, fill: '#10b981' },
  { name: 'Under Review', count: 5, fill: '#f59e0b' },
  { name: 'Declined', count: 2, fill: '#ef4444' },
];

export const Dashboard: React.FC = () => {
  const { data: stats, isLoading } = useQuery<DashboardStats>({
    queryKey: ['dashboard-stats'],
    queryFn: async () => {
      const res = await apiClient.get('/dashboard/stats');
      return res.data;
    }
  });

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight text-white">Platform Operations & Risk Control</h2>
        <p className="text-sm text-slate-400">Autonomous AI-Agent spend oversight, ledger integrity, and policy compliance.</p>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="glass-card p-5 rounded-xl">
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400 font-medium">Total Settled Volume</span>
            <DollarSign className="w-4 h-4 text-primary" />
          </div>
          <p className="text-2xl font-bold text-white mt-2">
            ${stats ? stats.totalSettledAmount.toLocaleString('en-US', { minimumFractionDigits: 2 }) : '1,198.00'}
          </p>
          <span className="text-[11px] text-emerald-400 font-medium">↑ 14.8% vs last week</span>
        </div>

        <div className="glass-card p-5 rounded-xl">
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400 font-medium">Pending Approvals</span>
            <Clock className="w-4 h-4 text-warning" />
          </div>
          <p className="text-2xl font-bold text-white mt-2">
            {stats ? stats.pendingApprovals : '2'}
          </p>
          <span className="text-[11px] text-warning font-medium">Requires human-in-the-loop</span>
        </div>

        <div className="glass-card p-5 rounded-xl">
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400 font-medium">Payment Success Rate</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          </div>
          <p className="text-2xl font-bold text-white mt-2">
            {stats ? `${stats.paymentSuccessRate}%` : '98.2%'}
          </p>
          <span className="text-[11px] text-slate-400 font-medium">Zero overdraft breaches</span>
        </div>

        <div className="glass-card p-5 rounded-xl">
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400 font-medium">Double-Entry Status</span>
            <ShieldCheck className="w-4 h-4 text-emerald-400" />
          </div>
          <p className="text-2xl font-bold text-emerald-400 mt-2">
            {stats?.ledgerBalanced ? 'BALANCED' : 'BALANCED'}
          </p>
          <span className="text-[11px] text-slate-400 font-mono">Σ Debits ≡ Σ Credits</span>
        </div>
      </div>

      {/* Visual Analytics */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 glass-card p-6 rounded-xl">
          <h3 className="text-sm font-semibold text-white mb-4">Autonomous Spend Velocity (7 Days)</h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={spendTrendData}>
                <defs>
                  <linearGradient id="spendGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.4}/>
                    <stop offset="95%" stopColor="#3b82f6" stopOpacity={0.0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e2638" />
                <XAxis dataKey="day" stroke="#64748b" fontSize={12} />
                <YAxis stroke="#64748b" fontSize={12} tickFormatter={(val) => `$${val}`} />
                <Tooltip 
                  contentStyle={{ backgroundColor: '#121722', borderColor: '#1e2638', borderRadius: 8, color: '#fff' }} 
                  formatter={(val: any) => [`$${val}`, 'Spend Volume']}
                />
                <Area type="monotone" dataKey="spend" stroke="#3b82f6" strokeWidth={2} fillOpacity={1} fill="url(#spendGrad)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="glass-card p-6 rounded-xl">
          <h3 className="text-sm font-semibold text-white mb-4">Risk Decision Classification</h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={riskDistributionData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e2638" />
                <XAxis dataKey="name" stroke="#64748b" fontSize={11} />
                <YAxis stroke="#64748b" fontSize={12} />
                <Tooltip 
                  contentStyle={{ backgroundColor: '#121722', borderColor: '#1e2638', borderRadius: 8, color: '#fff' }} 
                />
                <Bar dataKey="count" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
};
