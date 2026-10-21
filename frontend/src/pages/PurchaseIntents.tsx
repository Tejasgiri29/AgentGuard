import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { PurchaseIntent } from '../types';
import { ShoppingCart, Send, ShieldAlert, CheckCircle, Clock } from 'lucide-react';

export const PurchaseIntents: React.FC = () => {
  const queryClient = useQueryClient();
  const [showModal, setShowModal] = useState(false);
  const [merchantName, setMerchantName] = useState('Notion');
  const [amount, setAmount] = useState('49.00');
  const [merchantCategory, setMerchantCategory] = useState('SAAS_SUBSCRIPTION');
  const [description, setDescription] = useState('Monthly AI Research Workspace Seat');

  const { data: intents, isLoading } = useQuery<PurchaseIntent[]>({
    queryKey: ['purchase-intents'],
    queryFn: async () => {
      const res = await apiClient.get('/purchase-intents');
      return res.data;
    }
  });

  const submitMutation = useMutation({
    mutationFn: async () => {
      await apiClient.post('/purchase-intents', {
        mandateId: 'mnd-research-standard',
        agentId: 'agent-research-01',
        merchantName,
        merchantCategory,
        amount: parseFloat(amount),
        currency: 'USD',
        description
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['purchase-intents'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-stats'] });
      setShowModal(false);
    }
  });

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'COMPLETED':
      case 'APPROVED':
        return <span className="inline-flex items-center gap-1 text-emerald-400 font-medium text-xs bg-emerald-500/10 px-2.5 py-1 rounded-full border border-emerald-500/20"><CheckCircle className="w-3 h-3" /> {status}</span>;
      case 'AWAITING_HUMAN_APPROVAL':
        return <span className="inline-flex items-center gap-1 text-amber-400 font-medium text-xs bg-amber-500/10 px-2.5 py-1 rounded-full border border-amber-500/20"><Clock className="w-3 h-3" /> PENDING REVIEW</span>;
      default:
        return <span className="inline-flex items-center gap-1 text-red-400 font-medium text-xs bg-red-500/10 px-2.5 py-1 rounded-full border border-red-500/20"><ShieldAlert className="w-3 h-3" /> {status}</span>;
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-white">Purchase Intents & Saga Execution</h2>
          <p className="text-sm text-slate-400">Real-time spend delegation stream, risk scoring, and order fulfillment states.</p>
        </div>
        <button
          onClick={() => setShowModal(true)}
          className="flex items-center gap-2 bg-primary hover:bg-primaryHover text-white px-4 py-2 rounded-lg text-sm font-semibold transition shadow-lg shadow-primary/20"
        >
          <Send className="w-4 h-4" />
          <span>Submit Intent</span>
        </button>
      </div>

      <div className="glass-card rounded-xl overflow-hidden">
        <table className="w-full text-left text-sm">
          <thead className="bg-surfaceBorder/40 text-slate-400 text-xs uppercase font-mono border-b border-surfaceBorder">
            <tr>
              <th className="px-6 py-4">Intent ID</th>
              <th className="px-6 py-4">Agent</th>
              <th className="px-6 py-4">Merchant & Category</th>
              <th className="px-6 py-4">Amount</th>
              <th className="px-6 py-4">Status & Saga State</th>
              <th className="px-6 py-4 text-right">Timestamp</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-surfaceBorder">
            {isLoading ? (
              <tr>
                <td colSpan={6} className="text-center py-8 text-slate-400">Loading purchase intents...</td>
              </tr>
            ) : intents && intents.length > 0 ? (
              intents.map((intent) => (
                <tr key={intent.id} className="hover:bg-surfaceBorder/20 transition">
                  <td className="px-6 py-4 font-mono text-xs text-white">{intent.id}</td>
                  <td className="px-6 py-4 font-mono text-xs text-slate-300">{intent.agentId}</td>
                  <td className="px-6 py-4">
                    <p className="font-semibold text-white">{intent.merchantName}</p>
                    <span className="text-[11px] font-mono text-slate-400">{intent.merchantCategory}</span>
                  </td>
                  <td className="px-6 py-4 font-semibold text-white">
                    ${intent.amount.toFixed(2)} {intent.currency}
                  </td>
                  <td className="px-6 py-4">
                    {getStatusBadge(intent.status)}
                  </td>
                  <td className="px-6 py-4 text-right text-xs text-slate-400 font-mono">
                    {new Date(intent.createdAt).toLocaleTimeString()}
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={6} className="text-center py-8 text-slate-400">No purchase intents logged.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="glass-card max-w-md w-full p-6 rounded-2xl border border-surfaceBorder space-y-4">
            <h3 className="text-lg font-bold text-white">Simulate Agent Purchase Intent</h3>
            <div className="space-y-3 text-sm">
              <div>
                <label className="text-xs text-slate-400">Merchant Name</label>
                <input
                  type="text"
                  value={merchantName}
                  onChange={e => setMerchantName(e.target.value)}
                  className="w-full bg-background border border-surfaceBorder rounded-lg px-3 py-2 text-white mt-1"
                />
              </div>
              <div>
                <label className="text-xs text-slate-400">Category</label>
                <input
                  type="text"
                  value={merchantCategory}
                  onChange={e => setMerchantCategory(e.target.value)}
                  className="w-full bg-background border border-surfaceBorder rounded-lg px-3 py-2 text-white mt-1"
                />
              </div>
              <div>
                <label className="text-xs text-slate-400">Amount ($)</label>
                <input
                  type="number"
                  value={amount}
                  onChange={e => setAmount(e.target.value)}
                  className="w-full bg-background border border-surfaceBorder rounded-lg px-3 py-2 text-white mt-1"
                />
              </div>
              <div>
                <label className="text-xs text-slate-400">Purpose Description</label>
                <input
                  type="text"
                  value={description}
                  onChange={e => setDescription(e.target.value)}
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
                onClick={() => submitMutation.mutate()}
                className="bg-primary hover:bg-primaryHover text-white px-5 py-2 rounded-lg text-sm font-semibold transition"
              >
                Submit Intent
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
