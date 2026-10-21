import React, { useState, useEffect } from 'react';
import { apiClient } from '../api/client';
import { RiskRule } from '../types';
import { ShieldAlert, Sliders, Plus, CheckCircle2, XCircle, RefreshCw, AlertTriangle, Cpu } from 'lucide-react';

export const RiskRules: React.FC = () => {
  const [rules, setRules] = useState<RiskRule[]>([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const [newRule, setNewRule] = useState({
    name: 'Velocity Surge Protection',
    ruleType: 'VELOCITY_BURST',
    weight: 35,
    thresholdValue: 10,
    active: true
  });

  const fetchRules = async () => {
    try {
      setLoading(true);
      const res = await apiClient.get<RiskRule[]>('/risk-rules');
      setRules(res.data);
    } catch (err: any) {
      console.error('Failed to load risk rules', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRules();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await apiClient.post('/risk-rules', newRule);
      setIsModalOpen(false);
      fetchRules();
    } catch (err: any) {
      alert(err.response?.data?.detail || 'Failed to create risk rule');
    }
  };

  const handleToggle = async (rule: RiskRule) => {
    try {
      await apiClient.put(`/risk-rules/${rule.id}`, {
        ...rule,
        active: !rule.active
      });
      fetchRules();
    } catch (err: any) {
      alert(err.response?.data?.detail || 'Failed to update rule');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
            <ShieldAlert className="w-7 h-7 text-indigo-400" />
            Autonomous Risk Policy Engine
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Dynamic heuristic weights, velocity limits, and LLM hallucination containment thresholds.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={fetchRules}
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
            Configure Rule
          </button>
        </div>
      </div>

      {/* Logic Breakdown Card */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
        <div className="lg:col-span-2 bg-slate-900/60 border border-slate-800 rounded-xl p-5 backdrop-blur-sm">
          <h2 className="text-sm font-semibold text-white flex items-center gap-2 mb-3">
            <Sliders className="w-4 h-4 text-indigo-400" />
            Policy Scoring Evaluation Spectrum
          </h2>
          <div className="space-y-4">
            <div>
              <div className="flex justify-between text-xs font-mono mb-1">
                <span className="text-emerald-400 font-semibold">0 - 29: AUTO-APPROVE</span>
                <span className="text-amber-400 font-semibold">30 - 69: HUMAN-IN-THE-LOOP</span>
                <span className="text-rose-400 font-semibold">70 - 100: HARD REJECT</span>
              </div>
              <div className="h-3 w-full rounded-full bg-slate-800 overflow-hidden flex">
                <div className="w-[30%] bg-emerald-500"></div>
                <div className="w-[40%] bg-amber-500"></div>
                <div className="w-[30%] bg-rose-500"></div>
              </div>
            </div>
            <p className="text-xs text-slate-400 leading-relaxed">
              Every purchase intent dispatched by an AI agent evaluates all active rules concurrently.
              Weights are aggregated into a composite risk index from 0 to 100. If any single critical rule
              is breached, execution trips fail-safe circuit breakers.
            </p>
          </div>
        </div>

        <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-5 backdrop-blur-sm flex flex-col justify-between">
          <div>
            <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Engine Architecture</div>
            <div className="flex items-center gap-2 text-white font-semibold text-base mb-1">
              <Cpu className="w-5 h-5 text-indigo-400" />
              Real-time Sliding Window
            </div>
            <p className="text-xs text-slate-400">
              Redis token buckets calculate moving velocity averages in sub-5ms latency before database locks.
            </p>
          </div>
          <div className="text-xs text-emerald-400 font-mono bg-emerald-500/10 border border-emerald-500/20 p-2.5 rounded-lg mt-3">
            Status: Engine Operational (p99 &lt; 3.8ms)
          </div>
        </div>
      </div>

      {/* Rules Table */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-xl overflow-hidden backdrop-blur-sm">
        <div className="px-6 py-4 border-b border-slate-800">
          <h2 className="text-base font-semibold text-white">Active Heuristics & Weights</h2>
        </div>

        {loading ? (
          <div className="p-8 text-center text-slate-400 animate-pulse">Loading risk rules...</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-950/40 text-slate-400 uppercase text-[11px] font-semibold tracking-wider border-b border-slate-800">
                <tr>
                  <th className="px-6 py-3.5">Rule Name</th>
                  <th className="px-6 py-3.5">Category</th>
                  <th className="px-6 py-3.5">Weight / Severity</th>
                  <th className="px-6 py-3.5">Threshold</th>
                  <th className="px-6 py-3.5">Active Status</th>
                  <th className="px-6 py-3.5 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-slate-300">
                {rules.map(rule => (
                  <tr key={rule.id} className="hover:bg-slate-800/30 transition">
                    <td className="px-6 py-4 font-medium text-white">
                      {rule.name}
                    </td>
                    <td className="px-6 py-4 font-mono text-xs text-indigo-300">
                      {rule.ruleType}
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        <span className="font-mono text-xs font-bold text-white">+{rule.weight}</span>
                        <div className="w-16 bg-slate-800 h-1.5 rounded-full overflow-hidden">
                          <div
                            className={`h-full ${rule.weight > 50 ? 'bg-rose-500' : rule.weight > 25 ? 'bg-amber-500' : 'bg-indigo-500'}`}
                            style={{ width: `${Math.min(rule.weight, 100)}%` }}
                          ></div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 font-mono text-xs text-slate-400">
                      {rule.thresholdValue !== undefined ? rule.thresholdValue : 'N/A'}
                    </td>
                    <td className="px-6 py-4">
                      <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${
                        rule.active
                          ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                          : 'bg-slate-800 text-slate-400'
                      }`}>
                        {rule.active ? <CheckCircle2 className="w-3.5 h-3.5" /> : <XCircle className="w-3.5 h-3.5" />}
                        {rule.active ? 'ENFORCED' : 'MUTED'}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => handleToggle(rule)}
                        className={`text-xs font-medium transition ${
                          rule.active ? 'text-amber-400 hover:text-amber-300' : 'text-emerald-400 hover:text-emerald-300'
                        }`}
                      >
                        {rule.active ? 'Mute Rule' : 'Enforce Rule'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-xl max-w-md w-full p-6 shadow-2xl">
            <h3 className="text-lg font-bold text-white mb-2">Configure Risk Rule</h3>
            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">Rule Name</label>
                <input
                  type="text"
                  value={newRule.name}
                  onChange={e => setNewRule({ ...newRule, name: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-indigo-500"
                  required
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">Rule Type</label>
                <select
                  value={newRule.ruleType}
                  onChange={e => setNewRule({ ...newRule, ruleType: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-indigo-500 font-mono"
                >
                  <option value="VELOCITY_BURST">VELOCITY_BURST (Rapid fire transactions)</option>
                  <option value="NEW_RECIPIENT">NEW_RECIPIENT (First time vendor)</option>
                  <option value="AMOUNT_ANOMALY">AMOUNT_ANOMALY (Deviation from baseline)</option>
                  <option value="PROMPT_INJECTION_RISK">PROMPT_INJECTION_RISK (Unverified origin)</option>
                  <option value="GEO_MISMATCH">GEO_MISMATCH (Cross-region anomaly)</option>
                </select>
              </div>
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">Score Weight (0 - 100)</label>
                <input
                  type="number"
                  min="1"
                  max="100"
                  value={newRule.weight}
                  onChange={e => setNewRule({ ...newRule, weight: parseInt(e.target.value) || 10 })}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-indigo-500 font-mono"
                  required
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">Threshold Parameter</label>
                <input
                  type="number"
                  value={newRule.thresholdValue}
                  onChange={e => setNewRule({ ...newRule, thresholdValue: parseFloat(e.target.value) || 0 })}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-indigo-500 font-mono"
                />
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
                  Save Policy
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
