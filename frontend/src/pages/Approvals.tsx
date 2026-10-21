import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { Approval } from '../types';
import { Check, X, AlertTriangle, UserCheck, Shield } from 'lucide-react';

export const Approvals: React.FC = () => {
  const queryClient = useQueryClient();
  const [notes, setNotes] = useState<{ [id: string]: string }>({});

  const { data: approvals, isLoading } = useQuery<Approval[]>({
    queryKey: ['pending-approvals'],
    queryFn: async () => {
      const res = await apiClient.get('/approvals/pending');
      return res.data;
    }
  });

  const resolveMutation = useMutation({
    mutationFn: async ({ id, approved }: { id: string; approved: boolean }) => {
      await apiClient.post(`/approvals/${id}/resolve`, {
        approverUserId: 'usr-alpha-admin',
        approved,
        notes: notes[id] || (approved ? 'Authorized by administrator' : 'Declined policy breach')
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pending-approvals'] });
      queryClient.invalidateQueries({ queryKey: ['purchase-intents'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-stats'] });
    }
  });

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight text-white">Human-in-the-Loop Approval Inbox</h2>
        <p className="text-sm text-slate-400">Escalated purchase intents flagged by the multi-vector risk and anomaly policy engine.</p>
      </div>

      <div className="grid grid-cols-1 gap-4">
        {isLoading ? (
          <div className="glass-card p-8 text-center text-slate-400 rounded-xl">Loading approval tasks...</div>
        ) : approvals && approvals.length > 0 ? (
          approvals.map((appr) => (
            <div key={appr.id} className="glass-card p-6 rounded-xl border border-surfaceBorder space-y-4">
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-lg bg-amber-500/10 border border-amber-500/30 flex items-center justify-center">
                    <AlertTriangle className="w-5 h-5 text-amber-400" />
                  </div>
                  <div>
                    <h3 className="text-base font-semibold text-white">Approval Task: {appr.id}</h3>
                    <p className="text-xs text-slate-400 font-mono">Referenced Intent: {appr.purchaseIntentId}</p>
                  </div>
                </div>
                <span className="text-xs font-mono text-slate-400">{new Date(appr.createdAt).toLocaleString()}</span>
              </div>

              <div className="bg-background/80 p-4 rounded-lg border border-surfaceBorder">
                <p className="text-xs text-slate-400 uppercase font-mono font-semibold">Risk & Policy Evaluation Assessment</p>
                <p className="text-sm text-amber-300 mt-1">{appr.notes}</p>
              </div>

              <div className="flex items-center gap-3 pt-2">
                <input
                  type="text"
                  placeholder="Approver justification notes..."
                  value={notes[appr.id] || ''}
                  onChange={e => setNotes({ ...notes, [appr.id]: e.target.value })}
                  className="flex-1 bg-background border border-surfaceBorder rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-primary"
                />
                <button
                  onClick={() => resolveMutation.mutate({ id: appr.id, approved: true })}
                  className="flex items-center gap-1.5 bg-emerald-600 hover:bg-emerald-500 text-white px-4 py-2 rounded-lg text-sm font-semibold transition"
                >
                  <Check className="w-4 h-4" />
                  <span>Approve & Settle</span>
                </button>
                <button
                  onClick={() => resolveMutation.mutate({ id: appr.id, approved: false })}
                  className="flex items-center gap-1.5 bg-red-600/80 hover:bg-red-600 text-white px-4 py-2 rounded-lg text-sm font-semibold transition"
                >
                  <X className="w-4 h-4" />
                  <span>Decline</span>
                </button>
              </div>
            </div>
          ))
        ) : (
          <div className="glass-card p-12 text-center rounded-xl space-y-3">
            <UserCheck className="w-10 h-10 text-emerald-400 mx-auto" />
            <h3 className="text-base font-semibold text-white">Inbox Zero</h3>
            <p className="text-xs text-slate-400 max-w-sm mx-auto">All delegated agent spending intents are within predefined mandate policies and autonomous safety thresholds.</p>
          </div>
        )}
      </div>
    </div>
  );
};
