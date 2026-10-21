import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { Payment } from '../types';
import { CreditCard, RotateCcw, CheckCircle2, AlertCircle } from 'lucide-react';

export const Payments: React.FC = () => {
  const queryClient = useQueryClient();
  const [selectedPayment, setSelectedPayment] = useState<Payment | null>(null);
  const [refundReason, setRefundReason] = useState('Agent cancelled task before license activation');

  const { data: payments, isLoading } = useQuery<Payment[]>({
    queryKey: ['payments'],
    queryFn: async () => {
      const res = await apiClient.get('/payments');
      return res.data;
    }
  });

  const refundMutation = useMutation({
    mutationFn: async (payment: Payment) => {
      await apiClient.post(`/payments/${payment.id}/refund`, {
        amount: payment.amount,
        reason: refundReason
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['payments'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-stats'] });
      setSelectedPayment(null);
    }
  });

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight text-white">Settlements & Payment Orchestration</h2>
        <p className="text-sm text-slate-400">Multi-provider gateway captures, transaction references, and reverse clearing refunds.</p>
      </div>

      <div className="glass-card rounded-xl overflow-hidden">
        <table className="w-full text-left text-sm">
          <thead className="bg-surfaceBorder/40 text-slate-400 text-xs uppercase font-mono border-b border-surfaceBorder">
            <tr>
              <th className="px-6 py-4">Transaction Ref</th>
              <th className="px-6 py-4">Purchase Intent</th>
              <th className="px-6 py-4">Gateway Provider</th>
              <th className="px-6 py-4">Amount</th>
              <th className="px-6 py-4">Status</th>
              <th className="px-6 py-4 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-surfaceBorder">
            {isLoading ? (
              <tr>
                <td colSpan={6} className="text-center py-8 text-slate-400">Loading transactions...</td>
              </tr>
            ) : payments && payments.length > 0 ? (
              payments.map((p) => (
                <tr key={p.id} className="hover:bg-surfaceBorder/20 transition">
                  <td className="px-6 py-4">
                    <p className="font-mono text-xs text-white">{p.providerTransactionId || p.id}</p>
                    <p className="text-[11px] text-slate-400 font-mono">{p.id}</p>
                  </td>
                  <td className="px-6 py-4 font-mono text-xs text-slate-300">{p.purchaseIntentId}</td>
                  <td className="px-6 py-4 font-mono text-xs text-primary">{p.provider}</td>
                  <td className="px-6 py-4 font-bold text-white">${p.amount.toFixed(2)} {p.currency}</td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex items-center gap-1 text-xs font-medium px-2.5 py-0.5 rounded-full ${
                      p.status === 'CAPTURED'
                        ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                        : p.status === 'REFUNDED'
                        ? 'bg-purple-500/10 text-purple-400 border border-purple-500/20'
                        : 'bg-red-500/10 text-red-400 border border-red-500/20'
                    }`}>
                      {p.status === 'CAPTURED' ? <CheckCircle2 className="w-3 h-3" /> : <RotateCcw className="w-3 h-3" />}
                      {p.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-right">
                    {p.status === 'CAPTURED' && (
                      <button
                        onClick={() => setSelectedPayment(p)}
                        className="text-xs text-slate-400 hover:text-white font-medium transition"
                      >
                        Refund
                      </button>
                    )}
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={6} className="text-center py-8 text-slate-400">No payment records found.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {selectedPayment && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="glass-card max-w-md w-full p-6 rounded-2xl border border-surfaceBorder space-y-4">
            <h3 className="text-lg font-bold text-white">Execute Reverse Clearing Refund</h3>
            <p className="text-xs text-slate-400">This will submit a refund to the payment gateway and post debit/credit reversal entries to the double-entry ledger.</p>
            
            <div className="space-y-3 text-sm">
              <div>
                <label className="text-xs text-slate-400">Refund Amount</label>
                <input
                  type="text"
                  disabled
                  value={`$${selectedPayment.amount.toFixed(2)} ${selectedPayment.currency}`}
                  className="w-full bg-background/50 border border-surfaceBorder rounded-lg px-3 py-2 text-white mt-1 cursor-not-allowed"
                />
              </div>
              <div>
                <label className="text-xs text-slate-400">Audit Justification</label>
                <input
                  type="text"
                  value={refundReason}
                  onChange={e => setRefundReason(e.target.value)}
                  className="w-full bg-background border border-surfaceBorder rounded-lg px-3 py-2 text-white mt-1"
                />
              </div>
            </div>

            <div className="flex justify-end gap-3 pt-3">
              <button
                onClick={() => setSelectedPayment(null)}
                className="px-4 py-2 text-sm text-slate-400 hover:text-white"
              >
                Cancel
              </button>
              <button
                onClick={() => refundMutation.mutate(selectedPayment)}
                className="bg-purple-600 hover:bg-purple-500 text-white px-5 py-2 rounded-lg text-sm font-semibold transition"
              >
                Confirm Refund
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
