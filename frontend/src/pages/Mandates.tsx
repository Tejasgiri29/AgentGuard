import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { Mandate } from '../types';
import { Shield, Plus, Ban, CheckCircle, AlertOctagon } from 'lucide-react';

export const Mandates: React.FC = () => {
  const queryClient = useQueryClient();
  const [showModal, setShowModal] = useState(false);
  const [name, setName] = useState('');
  const [singleLimit, setSingleLimit] = useState('250.00');
  const [dailyLimit, setDailyLimit] = useState('500.00');
  const [monthlyLimit, setMonthlyLimit] = useState('3000.00');
  const [allowedCategories, setAllowedCategories] = useState('SAAS_SUBSCRIPTION,DEVELOPER_TOOLS');
  const [allowedMerchants, setAllowedMerchants] = useState('Notion,GitHub,AWS,ArXiv');

  const { data: mandates, isLoading } = useQuery<Mandate[]>({
    queryKey: ['mandates'],
    queryFn: async () => {
      const res = await apiClient.get('/mandates');
      return res.data;
    }
  });

  const createMutation = useMutation({
    mutationFn: async () => {
      await apiClient.post('/mandates', {
        userId: 'usr-alpha-dev',
        agentId: 'agent-research-01',
        name,
        description: 'Operator configured policy via UI',
        maxSingleTransactionAmount: parseFloat(singleLimit),
        maxDailyAmount: parseFloat(dailyLimit),
        maxMonthlyAmount: parseFloat(monthlyLimit),
        allowedCategories,
        allowedMerchants,
        blockedMerchants: 'CryptoShady,UntrustedGambling',
        requireApprovalAboveAmount: parseFloat(singleLimit) * 0.5,
        allowRecurringSubscriptions: true
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['mandates'] });
      setShowModal(false);
      setName('');
    }
  });

  const revokeMutation = useMutation({
    mutationFn: async (id: string) => {
      await apiClient.post(`/mandates/${id}/revoke`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['mandates'] });
    }
  });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-white">Delegated Spending Mandates</h2>
          <p className="text-sm text-slate-400">Cryptographically auditable authority envelopes bounding AI agent transactions.</p>
        </div>
        <button
          onClick={() => setShowModal(true)}
          className="flex items-center gap-2 bg-primary hover:bg-primaryHover text-white px-4 py-2 rounded-lg text-sm font-semibold transition shadow-lg shadow-primary/20"
        >
          <Plus className="w-4 h-4" />
          <span>Create Mandate</span>
        </button>
      </div>

      <div className="glass-card rounded-xl overflow-hidden">
        <table className="w-full text-left text-sm">
          <thead className="bg-surfaceBorder/40 text-slate-400 text-xs uppercase font-mono border-b border-surfaceBorder">
            <tr>
              <th className="px-6 py-4">Mandate Name</th>
              <th className="px-6 py-4">Agent Principal</th>
              <th className="px-6 py-4">Single Limit</th>
              <th className="px-6 py-4">Daily Budget</th>
              <th className="px-6 py-4">Categories</th>
              <th className="px-6 py-4">Status</th>
              <th className="px-6 py-4 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-surfaceBorder">
            {isLoading ? (
              <tr>
                <td colSpan={7} className="text-center py-8 text-slate-400">Loading mandates...</td>
              </tr>
            ) : mandates && mandates.length > 0 ? (
              mandates.map((m) => (
                <tr key={m.id} className="hover:bg-surfaceBorder/20 transition">
                  <td className="px-6 py-4">
                    <p className="font-semibold text-white">{m.name}</p>
                    <p className="text-xs text-slate-400 font-mono">{m.id}</p>
                  </td>
                  <td className="px-6 py-4 font-mono text-xs text-slate-300">{m.agentId}</td>
                  <td className="px-6 py-4 font-semibold text-white">${m.maxSingleTransactionAmount}</td>
                  <td className="px-6 py-4 text-slate-300">${m.maxDailyAmount}</td>
                  <td className="px-6 py-4">
                    <span className="inline-block px-2 py-0.5 rounded text-[11px] font-mono bg-surfaceBorder text-slate-300">
                      {m.allowedCategories || 'ALL'}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${
                      m.status === 'ACTIVE' 
                        ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' 
                        : 'bg-red-500/10 text-red-400 border border-red-500/20'
                    }`}>
                      {m.status === 'ACTIVE' ? <CheckCircle className="w-3 h-3" /> : <Ban className="w-3 h-3" />}
                      {m.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-right">
                    {m.status === 'ACTIVE' && (
                      <button
                        onClick={() => revokeMutation.mutate(m.id)}
                        className="text-xs text-red-400 hover:text-red-300 font-medium transition"
                      >
                        Revoke
                      </button>
                    )}
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={7} className="text-center py-8 text-slate-400">No active mandates found.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="glass-card max-w-lg w-full p-6 rounded-2xl border border-surfaceBorder space-y-4">
            <h3 className="text-lg font-bold text-white">Create Spending Authority Mandate</h3>
            <div className="space-y-3 text-sm">
              <div>
                <label className="text-xs text-slate-400">Mandate Name</label>
                <input
                  type="text"
                  value={name}
                  onChange={e => setName(e.target.value)}
                  placeholder="e.g. AI Research Fast-Track"
                  className="w-full bg-background border border-surfaceBorder rounded-lg px-3 py-2 text-white mt-1 focus:outline-none focus:border-primary"
                />
              </div>
              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="text-xs text-slate-400">Max Single ($)</label>
                  <input
                    type="number"
                    value={singleLimit}
                    onChange={e => setSingleLimit(e.target.value)}
                    className="w-full bg-background border border-surfaceBorder rounded-lg px-3 py-2 text-white mt-1"
                  />
                </div>
                <div>
                  <label className="text-xs text-slate-400">Max Daily ($)</label>
                  <input
                    type="number"
                    value={dailyLimit}
                    onChange={e => setDailyLimit(e.target.value)}
                    className="w-full bg-background border border-surfaceBorder rounded-lg px-3 py-2 text-white mt-1"
                  />
                </div>
                <div>
                  <label className="text-xs text-slate-400">Monthly ($)</label>
                  <input
                    type="number"
                    value={monthlyLimit}
                    onChange={e => setMonthlyLimit(e.target.value)}
                    className="w-full bg-background border border-surfaceBorder rounded-lg px-3 py-2 text-white mt-1"
                  />
                </div>
              </div>
              <div>
                <label className="text-xs text-slate-400">Allowed Categories (comma-separated)</label>
                <input
                  type="text"
                  value={allowedCategories}
                  onChange={e => setAllowedCategories(e.target.value)}
                  className="w-full bg-background border border-surfaceBorder rounded-lg px-3 py-2 text-white mt-1"
                />
              </div>
            </div>

            <div className="flex justify-end gap-3 pt-3">
              <button
                onClick={() => setShowModal(false)}
                className="px-4 py-2 text-sm text-slate-400 hover:text-white"
              >
                Cancel
              </button>
              <button
                onClick={() => createMutation.mutate()}
                disabled={!name}
                className="bg-primary hover:bg-primaryHover text-white px-5 py-2 rounded-lg text-sm font-semibold transition"
              >
                Save Mandate
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
