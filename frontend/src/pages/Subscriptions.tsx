import React, { useState, useEffect } from 'react';
import { apiClient } from '../api/client';
import { Subscription } from '../types';
import { Repeat, ShieldCheck, AlertCircle, RefreshCw, Plus, CheckCircle2, XCircle } from 'lucide-react';

export const Subscriptions: React.FC = () => {
  const [subscriptions, setSubscriptions] = useState<Subscription[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [sweepLoading, setSweepLoading] = useState(false);
  const [sweepResult, setSweepResult] = useState<string | null>(null);

  const [newSub, setNewSub] = useState({
    agentId: 'agent-procurement-001',
    planId: 'plan-enterprise-tier1',
    billingCycle: 'MONTHLY'
  });

  const fetchSubscriptions = async () => {
    try {
      setLoading(true);
      const res = await apiClient.get<Subscription[]>('/subscriptions');
      setSubscriptions(res.data);
      setError(null);
    } catch (err: any) {
      setError(err.response?.data?.detail || 'Failed to load subscriptions');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSubscriptions();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await apiClient.post('/subscriptions', newSub);
      setIsModalOpen(false);
      fetchSubscriptions();
    } catch (err: any) {
      alert(err.response?.data?.detail || 'Failed to create subscription');
    }
  };

  const handleCancel = async (id: string) => {
    if (!window.confirm('Cancel this agent subscription recurring mandate?')) return;
    try {
      await apiClient.post(`/subscriptions/${id}/cancel`);
      fetchSubscriptions();
    } catch (err: any) {
      alert(err.response?.data?.detail || 'Failed to cancel subscription');
    }
  };

  const handleDunningSweep = async () => {
    try {
      setSweepLoading(true);
      setSweepResult(null);
      const res = await apiClient.post('/subscriptions/dunning/sweep');
      setSweepResult(`Dunning sweep completed: ${JSON.stringify(res.data)}`);
      fetchSubscriptions();
    } catch (err: any) {
      setSweepResult(`Dunning sweep failed: ${err.response?.data?.detail || err.message}`);
    } finally {
      setSweepLoading(false);
    }
  };

  const activeCount = subscriptions.filter(s => s.status === 'ACTIVE').length;
  const pastDueCount = subscriptions.filter(s => s.status === 'PAST_DUE').length;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
            <Repeat className="w-7 h-7 text-indigo-400" />
            Agent Subscriptions & Recurring Mandates
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Automated SaaS billing cycles, dunning orchestration, and policy-governed agent usage quotas.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={handleDunningSweep}
            disabled={sweepLoading}
            className="flex items-center gap-2 px-3.5 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 rounded-lg text-sm font-medium transition disabled:opacity-50"
          >
            <RefreshCw className={`w-4 h-4 ${sweepLoading ? 'animate-spin text-indigo-400' : ''}`} />
            Run Dunning Sweep
          </button>
          <button
            onClick={() => setIsModalOpen(true)}
            className="flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg text-sm font-medium shadow-lg shadow-indigo-500/20 transition"
          >
            <Plus className="w-4 h-4" />
            New Subscription
          </button>
        </div>
      </div>

      {sweepResult && (
        <div className="p-3 bg-indigo-950/40 border border-indigo-500/30 rounded-lg text-indigo-300 text-xs font-mono">
          {sweepResult}
        </div>
      )}

      {/* Metrics Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        <div className="bg-slate-900/60 border border-slate-800 p-5 rounded-xl">
          <div className="flex items-center justify-between">
            <span className="text-slate-400 text-xs font-semibold uppercase tracking-wider">Active Agent Subscriptions</span>
            <ShieldCheck className="w-5 h-5 text-emerald-400" />
          </div>
          <div className="mt-3 text-3xl font-bold text-white">{activeCount}</div>
          <div className="mt-1 text-xs text-slate-500">Autonomous spend limits auto-renewing</div>
        </div>
        <div className="bg-slate-900/60 border border-slate-800 p-5 rounded-xl">
          <div className="flex items-center justify-between">
            <span className="text-slate-400 text-xs font-semibold uppercase tracking-wider">Past Due / Dunning</span>
            <AlertCircle className="w-5 h-5 text-amber-400" />
          </div>
          <div className="mt-3 text-3xl font-bold text-amber-400">{pastDueCount}</div>
          <div className="mt-1 text-xs text-slate-500">In dunning recovery retry cadence</div>
        </div>
        <div className="bg-slate-900/60 border border-slate-800 p-5 rounded-xl">
          <div className="flex items-center justify-between">
            <span className="text-slate-400 text-xs font-semibold uppercase tracking-wider">Total Managed</span>
            <Repeat className="w-5 h-5 text-indigo-400" />
          </div>
          <div className="mt-3 text-3xl font-bold text-indigo-400">{subscriptions.length}</div>
          <div className="mt-1 text-xs text-slate-500">Aggregated tenant subscriptions</div>
        </div>
      </div>

      {/* Subscriptions Table */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-xl overflow-hidden backdrop-blur-sm">
        <div className="px-6 py-4 border-b border-slate-800 flex items-center justify-between">
          <h2 className="text-base font-semibold text-white">Active & Recurring Billing Schedules</h2>
          <button onClick={fetchSubscriptions} className="text-slate-400 hover:text-white transition">
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>

        {loading ? (
          <div className="p-8 text-center text-slate-400 animate-pulse">Loading subscriptions...</div>
        ) : error ? (
          <div className="p-6 text-center text-rose-400 flex items-center justify-center gap-2">
            <AlertCircle className="w-5 h-5" />
            {error}
          </div>
        ) : subscriptions.length === 0 ? (
          <div className="p-12 text-center text-slate-500">
            No subscriptions found. Create an autonomous recurring mandate to begin billing.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-950/40 text-slate-400 uppercase text-[11px] font-semibold tracking-wider border-b border-slate-800">
                <tr>
                  <th className="px-6 py-3.5">Subscription ID</th>
                  <th className="px-6 py-3.5">Agent Identifier</th>
                  <th className="px-6 py-3.5">Plan</th>
                  <th className="px-6 py-3.5">Status</th>
                  <th className="px-6 py-3.5">Current Period</th>
                  <th className="px-6 py-3.5">Dunning Step</th>
                  <th className="px-6 py-3.5 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-slate-300">
                {subscriptions.map(sub => (
                  <tr key={sub.id} className="hover:bg-slate-800/30 transition">
                    <td className="px-6 py-4 font-mono text-xs text-indigo-300 font-medium">
                      {sub.id.substring(0, 8)}...
                    </td>
                    <td className="px-6 py-4 font-mono text-xs text-slate-200">
                      {sub.agentId}
                    </td>
                    <td className="px-6 py-4 font-medium text-white">
                      {sub.planId}
                    </td>
                    <td className="px-6 py-4">
                      <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${
                        sub.status === 'ACTIVE'
                          ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                          : sub.status === 'PAST_DUE'
                          ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                          : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                      }`}>
                        {sub.status === 'ACTIVE' && <CheckCircle2 className="w-3.5 h-3.5" />}
                        {sub.status === 'PAST_DUE' && <AlertCircle className="w-3.5 h-3.5" />}
                        {sub.status === 'CANCELLED' && <XCircle className="w-3.5 h-3.5" />}
                        {sub.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-xs text-slate-400">
                      {sub.currentPeriodStart ? new Date(sub.currentPeriodStart).toLocaleDateString() : 'N/A'} -{' '}
                      {sub.currentPeriodEnd ? new Date(sub.currentPeriodEnd).toLocaleDateString() : 'N/A'}
                    </td>
                    <td className="px-6 py-4">
                      <span className={`px-2 py-0.5 rounded text-xs font-mono ${
                        sub.dunningStep > 0 ? 'bg-amber-500/20 text-amber-300' : 'bg-slate-800 text-slate-400'
                      }`}>
                        Step {sub.dunningStep || 0}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      {sub.status !== 'CANCELLED' ? (
                        <button
                          onClick={() => handleCancel(sub.id)}
                          className="text-xs text-rose-400 hover:text-rose-300 transition font-medium hover:underline"
                        >
                          Cancel
                        </button>
                      ) : (
                        <span className="text-xs text-slate-500">Terminated</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Create Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-xl max-w-md w-full p-6 shadow-2xl">
            <h3 className="text-lg font-bold text-white mb-2">Create Agent Subscription</h3>
            <p className="text-slate-400 text-xs mb-5">
              Instantiate recurring spending authorization and billing cycle for an autonomous agent.
            </p>
            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">Agent ID</label>
                <input
                  type="text"
                  value={newSub.agentId}
                  onChange={e => setNewSub({ ...newSub, agentId: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-indigo-500"
                  required
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">Subscription Plan</label>
                <select
                  value={newSub.planId}
                  onChange={e => setNewSub({ ...newSub, planId: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-indigo-500"
                >
                  <option value="plan-enterprise-tier1">Enterprise Tier 1 ($5,000/mo limit)</option>
                  <option value="plan-pro-tier2">Pro Tier 2 ($2,000/mo limit)</option>
                  <option value="plan-starter-tier3">Starter Tier 3 ($500/mo limit)</option>
                </select>
              </div>
              <div className="flex justify-end gap-3 mt-6 pt-3 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 text-sm text-slate-400 hover:text-white transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg text-sm font-medium transition shadow-lg shadow-indigo-500/20"
                >
                  Confirm Subscription
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
