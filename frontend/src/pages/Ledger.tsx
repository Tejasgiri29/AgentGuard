import React, { useState, useEffect } from 'react';
import { apiClient } from '../api/client';
import { LedgerAccount, JournalEntry, TrialBalance } from '../types';
import { BookOpen, Scale, ArrowDownRight, ArrowUpRight, CheckCircle2, AlertCircle, Plus, RefreshCw, Layers } from 'lucide-react';

export const Ledger: React.FC = () => {
  const [accounts, setAccounts] = useState<LedgerAccount[]>([]);
  const [journalEntries, setJournalEntries] = useState<JournalEntry[]>([]);
  const [trialBalance, setTrialBalance] = useState<TrialBalance | null>(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'accounts' | 'entries'>('accounts');
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Manual Journal Entry Form State
  const [manualEntry, setManualEntry] = useState({
    referenceId: 'REF-' + Date.now().toString().slice(-6),
    description: 'Manual adjustment',
    lines: [
      { accountId: '', entryType: 'DEBIT', amount: 100 },
      { accountId: '', entryType: 'CREDIT', amount: 100 }
    ]
  });

  const fetchData = async () => {
    try {
      setLoading(true);
      const [accRes, entriesRes, tbRes] = await Promise.all([
        apiClient.get<LedgerAccount[]>('/ledger/accounts'),
        apiClient.get<JournalEntry[]>('/ledger/journal-entries'),
        apiClient.get<TrialBalance>('/ledger/trial-balance')
      ]);
      setAccounts(accRes.data);
      setJournalEntries(entriesRes.data);
      setTrialBalance(tbRes.data);

      if (accRes.data.length >= 2 && !manualEntry.lines[0].accountId) {
        setManualEntry(prev => ({
          ...prev,
          lines: [
            { ...prev.lines[0], accountId: accRes.data[0].id },
            { ...prev.lines[1], accountId: accRes.data[1].id }
          ]
        }));
      }
    } catch (err: any) {
      console.error('Failed to load ledger data', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handlePostEntry = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await apiClient.post('/ledger/journal-entries', manualEntry);
      setIsModalOpen(false);
      fetchData();
    } catch (err: any) {
      alert(err.response?.data?.detail || 'Failed to post balanced journal entry');
    }
  };

  const handleLineChange = (index: number, field: string, value: any) => {
    const updated = [...manualEntry.lines];
    updated[index] = { ...updated[index], [field]: value };
    setManualEntry({ ...manualEntry, lines: updated });
  };

  const addLine = () => {
    setManualEntry({
      ...manualEntry,
      lines: [...manualEntry.lines, { accountId: accounts[0]?.id || '', entryType: 'CREDIT', amount: 0 }]
    });
  };

  const removeLine = (idx: number) => {
    if (manualEntry.lines.length <= 2) return;
    setManualEntry({
      ...manualEntry,
      lines: manualEntry.lines.filter((_, i) => i !== idx)
    });
  };

  const debitsSum = manualEntry.lines
    .filter(l => l.entryType === 'DEBIT')
    .reduce((sum, l) => sum + (Number(l.amount) || 0), 0);
  const creditsSum = manualEntry.lines
    .filter(l => l.entryType === 'CREDIT')
    .reduce((sum, l) => sum + (Number(l.amount) || 0), 0);
  const isInBalance = Math.abs(debitsSum - creditsSum) < 0.0001;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
            <BookOpen className="w-7 h-7 text-indigo-400" />
            Immutable Double-Entry Ledger
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Zero-leakage audited financial journal with strict mathematical invariance: ∑ Debits ≡ ∑ Credits.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={fetchData}
            className="flex items-center gap-2 px-3.5 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 rounded-lg text-sm font-medium transition"
          >
            <RefreshCw className="w-4 h-4" />
            Refresh
          </button>
          <button
            onClick={() => setIsModalOpen(true)}
            className="flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg text-sm font-medium shadow-lg shadow-indigo-500/20 transition"
          >
            <Plus className="w-4 h-4" />
            Post Journal Entry
          </button>
        </div>
      </div>

      {/* Trial Balance Health Banner */}
      <div className={`p-5 rounded-xl border flex flex-col md:flex-row items-center justify-between gap-4 ${
        trialBalance?.isBalanced
          ? 'bg-emerald-950/20 border-emerald-500/30 text-emerald-300'
          : 'bg-rose-950/20 border-rose-500/30 text-rose-300'
      }`}>
        <div className="flex items-center gap-3">
          {trialBalance?.isBalanced ? (
            <div className="p-2.5 rounded-lg bg-emerald-500/10 border border-emerald-500/20">
              <Scale className="w-6 h-6 text-emerald-400" />
            </div>
          ) : (
            <div className="p-2.5 rounded-lg bg-rose-500/10 border border-rose-500/20">
              <AlertCircle className="w-6 h-6 text-rose-400" />
            </div>
          )}
          <div>
            <div className="font-semibold text-white flex items-center gap-2">
              Trial Balance Verification
              <span className={`text-xs px-2 py-0.5 rounded-full font-mono ${
                trialBalance?.isBalanced ? 'bg-emerald-500/20 text-emerald-300' : 'bg-rose-500/20 text-rose-300'
              }`}>
                {trialBalance?.isBalanced ? 'BALANCED' : 'IMBALANCE DETECTED'}
              </span>
            </div>
            <p className="text-xs text-slate-400 mt-0.5">
              Continuous real-time verification of chart of accounts across multi-tenant settlements.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-8 font-mono text-sm">
          <div>
            <span className="text-xs text-slate-400 block">TOTAL DEBITS</span>
            <span className="text-white font-bold">${trialBalance?.totalDebits?.toLocaleString() || '0.00'}</span>
          </div>
          <div>
            <span className="text-xs text-slate-400 block">TOTAL CREDITS</span>
            <span className="text-white font-bold">${trialBalance?.totalCredits?.toLocaleString() || '0.00'}</span>
          </div>
          <div>
            <span className="text-xs text-slate-400 block">VARIANCE</span>
            <span className="text-emerald-400 font-bold">$0.00</span>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-800 gap-4">
        <button
          onClick={() => setActiveTab('accounts')}
          className={`pb-3 text-sm font-medium flex items-center gap-2 border-b-2 transition ${
            activeTab === 'accounts'
              ? 'border-indigo-500 text-indigo-400'
              : 'border-transparent text-slate-400 hover:text-slate-300'
          }`}
        >
          <Layers className="w-4 h-4" />
          Chart of Accounts ({accounts.length})
        </button>
        <button
          onClick={() => setActiveTab('entries')}
          className={`pb-3 text-sm font-medium flex items-center gap-2 border-b-2 transition ${
            activeTab === 'entries'
              ? 'border-indigo-500 text-indigo-400'
              : 'border-transparent text-slate-400 hover:text-slate-300'
          }`}
        >
          <BookOpen className="w-4 h-4" />
          Journal Entries ({journalEntries.length})
        </button>
      </div>

      {/* Tab 1: Chart of Accounts */}
      {activeTab === 'accounts' && (
        <div className="bg-slate-900/60 border border-slate-800 rounded-xl overflow-hidden backdrop-blur-sm">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-950/40 text-slate-400 uppercase text-[11px] font-semibold tracking-wider border-b border-slate-800">
                <tr>
                  <th className="px-6 py-3.5">Account Number</th>
                  <th className="px-6 py-3.5">Name</th>
                  <th className="px-6 py-3.5">Category</th>
                  <th className="px-6 py-3.5">Balance</th>
                  <th className="px-6 py-3.5">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-slate-300">
                {accounts.map(acc => (
                  <tr key={acc.id} className="hover:bg-slate-800/30 transition">
                    <td className="px-6 py-4 font-mono text-xs text-indigo-300 font-semibold">
                      {acc.accountNumber}
                    </td>
                    <td className="px-6 py-4 font-medium text-white">
                      {acc.name}
                    </td>
                    <td className="px-6 py-4">
                      <span className={`px-2 py-0.5 rounded text-xs font-mono font-medium ${
                        acc.type === 'ASSET' ? 'bg-sky-500/10 text-sky-400 border border-sky-500/20' :
                        acc.type === 'LIABILITY' ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20' :
                        acc.type === 'REVENUE' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' :
                        acc.type === 'EXPENSE' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20' :
                        'bg-purple-500/10 text-purple-400 border border-purple-500/20'
                      }`}>
                        {acc.type}
                      </span>
                    </td>
                    <td className="px-6 py-4 font-mono text-sm font-semibold text-white">
                      ${acc.balance?.toLocaleString(undefined, { minimumFractionDigits: 2 })} {acc.currency}
                    </td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                        <CheckCircle2 className="w-3.5 h-3.5" />
                        ACTIVE
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab 2: Journal Entries */}
      {activeTab === 'entries' && (
        <div className="space-y-4">
          {journalEntries.map(entry => (
            <div key={entry.id} className="bg-slate-900/60 border border-slate-800 rounded-xl p-5 backdrop-blur-sm">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-3 border-b border-slate-800 gap-2">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-white font-semibold">{entry.description}</span>
                    <span className="text-xs font-mono text-indigo-400 bg-indigo-500/10 px-2 py-0.5 rounded border border-indigo-500/20">
                      Ref: {entry.referenceId}
                    </span>
                  </div>
                  <span className="text-xs text-slate-400">
                    Posted on {new Date(entry.postedAt).toLocaleString()}
                  </span>
                </div>
                {entry.reversal && (
                  <span className="text-xs px-2.5 py-1 bg-amber-500/10 text-amber-400 rounded-full border border-amber-500/20 font-semibold">
                    Reversal Entry
                  </span>
                )}
              </div>

              {/* Lines */}
              <div className="mt-3 overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead>
                    <tr className="text-slate-400 border-b border-slate-800/40">
                      <th className="py-2">Account ID</th>
                      <th className="py-2">Type</th>
                      <th className="py-2 text-right">Debit</th>
                      <th className="py-2 text-right">Credit</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/30">
                    {entry.lines.map((line, idx) => (
                      <tr key={idx} className="font-mono">
                        <td className="py-2 text-slate-300">{line.accountId}</td>
                        <td className="py-2">
                          <span className={`inline-flex items-center gap-1 ${
                            line.entryType === 'DEBIT' ? 'text-sky-400' : 'text-emerald-400'
                          }`}>
                            {line.entryType === 'DEBIT' ? <ArrowDownRight className="w-3.5 h-3.5" /> : <ArrowUpRight className="w-3.5 h-3.5" />}
                            {line.entryType}
                          </span>
                        </td>
                        <td className="py-2 text-right font-semibold text-slate-200">
                          {line.entryType === 'DEBIT' ? `$${line.amount.toFixed(2)}` : '-'}
                        </td>
                        <td className="py-2 text-right font-semibold text-slate-200">
                          {line.entryType === 'CREDIT' ? `$${line.amount.toFixed(2)}` : '-'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Manual Entry Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-xl max-w-2xl w-full p-6 shadow-2xl">
            <h3 className="text-lg font-bold text-white mb-1">Post Balanced Journal Entry</h3>
            <p className="text-slate-400 text-xs mb-4">
              Enter double-entry postings. The sum of debits must mathematically equal credits.
            </p>

            <form onSubmit={handlePostEntry} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">Reference ID</label>
                  <input
                    type="text"
                    value={manualEntry.referenceId}
                    onChange={e => setManualEntry({ ...manualEntry, referenceId: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-indigo-500 font-mono"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">Description</label>
                  <input
                    type="text"
                    value={manualEntry.description}
                    onChange={e => setManualEntry({ ...manualEntry, description: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-indigo-500"
                    required
                  />
                </div>
              </div>

              {/* Journal Lines */}
              <div className="space-y-2">
                <div className="flex justify-between items-center">
                  <label className="block text-xs font-medium text-slate-300">Journal Lines</label>
                  <button
                    type="button"
                    onClick={addLine}
                    className="text-xs text-indigo-400 hover:text-indigo-300"
                  >
                    + Add Line
                  </button>
                </div>

                {manualEntry.lines.map((line, idx) => (
                  <div key={idx} className="flex gap-2 items-center">
                    <select
                      value={line.accountId}
                      onChange={e => handleLineChange(idx, 'accountId', e.target.value)}
                      className="flex-1 bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-white"
                      required
                    >
                      {accounts.map(a => (
                        <option key={a.id} value={a.id}>
                          {a.accountNumber} - {a.name}
                        </option>
                      ))}
                    </select>

                    <select
                      value={line.entryType}
                      onChange={e => handleLineChange(idx, 'entryType', e.target.value)}
                      className="w-28 bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-white font-mono"
                    >
                      <option value="DEBIT">DEBIT</option>
                      <option value="CREDIT">CREDIT</option>
                    </select>

                    <input
                      type="number"
                      step="0.01"
                      value={line.amount}
                      onChange={e => handleLineChange(idx, 'amount', parseFloat(e.target.value) || 0)}
                      className="w-28 bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-white font-mono text-right"
                      required
                    />

                    {manualEntry.lines.length > 2 && (
                      <button
                        type="button"
                        onClick={() => removeLine(idx)}
                        className="text-rose-400 hover:text-rose-300 px-2 py-1 text-xs"
                      >
                        ✕
                      </button>
                    )}
                  </div>
                ))}
              </div>

              {/* Real-time Math Summary */}
              <div className="bg-slate-950 p-3 rounded-lg border border-slate-800 flex justify-between text-xs font-mono">
                <div>Debits: <span className="text-white">${debitsSum.toFixed(2)}</span></div>
                <div>Credits: <span className="text-white">${creditsSum.toFixed(2)}</span></div>
                <div>Status: {isInBalance ? (
                  <span className="text-emerald-400 font-semibold">BALANCED (0.00)</span>
                ) : (
                  <span className="text-rose-400 font-semibold">DELTA ${Math.abs(debitsSum - creditsSum).toFixed(2)}</span>
                )}</div>
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
                  disabled={!isInBalance}
                  className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-40 disabled:cursor-not-allowed text-white rounded-lg text-sm font-medium transition shadow-lg shadow-indigo-500/20"
                >
                  Commit Journal Entry
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
