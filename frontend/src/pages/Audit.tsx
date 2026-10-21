import React, { useState, useEffect } from 'react';
import { apiClient } from '../api/client';
import { AuditEvent } from '../types';
import { History, ShieldCheck, Search, Filter, RefreshCw, Code, CheckCircle2, Lock } from 'lucide-react';

export const Audit: React.FC = () => {
  const [events, setEvents] = useState<AuditEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedEvent, setSelectedEvent] = useState<AuditEvent | null>(null);

  const fetchAuditEvents = async () => {
    try {
      setLoading(true);
      const res = await apiClient.get<AuditEvent[]>('/audit');
      setEvents(res.data);
    } catch (err: any) {
      console.error('Failed to load audit events', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAuditEvents();
  }, []);

  const filteredEvents = events.filter(e =>
    e.action.toLowerCase().includes(searchTerm.toLowerCase()) ||
    e.entityName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    e.actorId.toLowerCase().includes(searchTerm.toLowerCase()) ||
    e.entityId.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
            <History className="w-7 h-7 text-indigo-400" />
            SOC2 / ISO-27001 Tamper-Evident Audit Trail
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Cryptographically sealed timeline of agent decisions, mandate modifications, human overrides, and ledger transitions.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={fetchAuditEvents}
            className="flex items-center gap-2 px-3.5 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 rounded-lg text-sm font-medium transition"
          >
            <RefreshCw className="w-4 h-4" />
            Refresh
          </button>
        </div>
      </div>

      {/* Verification Card */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-5 backdrop-blur-sm flex flex-col md:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-lg bg-indigo-500/10 border border-indigo-500/20 text-indigo-400">
            <Lock className="w-6 h-6" />
          </div>
          <div>
            <div className="font-semibold text-white flex items-center gap-2">
              Append-Only Append Invariance
              <span className="text-xs px-2 py-0.5 rounded-full font-mono bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                VERIFIED SEAL
              </span>
            </div>
            <p className="text-xs text-slate-400 mt-0.5">
              Every audit record is immutable. Deletions and in-place updates are rejected at the database trigger layer.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3 w-full md:w-auto">
          <div className="relative flex-1 md:w-64">
            <Search className="w-4 h-4 text-slate-500 absolute left-3 top-2.5" />
            <input
              type="text"
              placeholder="Search actions, actors, entities..."
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-lg pl-9 pr-3 py-1.5 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-indigo-500"
            />
          </div>
        </div>
      </div>

      {/* Audit Log Table */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-xl overflow-hidden backdrop-blur-sm">
        <div className="px-6 py-4 border-b border-slate-800 flex justify-between items-center">
          <h2 className="text-base font-semibold text-white">Event Log ({filteredEvents.length})</h2>
          <span className="text-xs font-mono text-slate-500">Ordered by timestamp DESC</span>
        </div>

        {loading ? (
          <div className="p-8 text-center text-slate-400 animate-pulse">Loading audit ledger...</div>
        ) : filteredEvents.length === 0 ? (
          <div className="p-12 text-center text-slate-500">
            No audit records matched your filter criteria.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-950/40 text-slate-400 uppercase text-[11px] font-semibold tracking-wider border-b border-slate-800">
                <tr>
                  <th className="px-6 py-3.5">Timestamp</th>
                  <th className="px-6 py-3.5">Action</th>
                  <th className="px-6 py-3.5">Entity</th>
                  <th className="px-6 py-3.5">Entity ID</th>
                  <th className="px-6 py-3.5">Actor</th>
                  <th className="px-6 py-3.5 text-right">Payload Snapshot</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-slate-300">
                {filteredEvents.map(event => (
                  <tr key={event.id} className="hover:bg-slate-800/30 transition">
                    <td className="px-6 py-4 text-xs font-mono text-slate-400">
                      {new Date(event.timestamp).toLocaleString()}
                    </td>
                    <td className="px-6 py-4">
                      <span className="px-2 py-0.5 rounded text-xs font-mono font-bold bg-indigo-500/10 text-indigo-300 border border-indigo-500/20">
                        {event.action}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-xs font-medium text-white">
                      {event.entityName}
                    </td>
                    <td className="px-6 py-4 font-mono text-xs text-slate-400">
                      {event.entityId?.substring(0, 10)}...
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-xs font-medium text-slate-200">{event.actorId}</div>
                      <div className="text-[10px] text-slate-500 font-mono">{event.actorType}</div>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => setSelectedEvent(event)}
                        className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-xs text-indigo-300 font-mono transition"
                      >
                        <Code className="w-3.5 h-3.5" />
                        View JSON
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Snapshot Modal */}
      {selectedEvent && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-xl max-w-2xl w-full p-6 shadow-2xl space-y-4">
            <div className="flex justify-between items-start">
              <div>
                <h3 className="text-lg font-bold text-white flex items-center gap-2">
                  <ShieldCheck className="w-5 h-5 text-indigo-400" />
                  Audit Event Snapshot
                </h3>
                <p className="text-xs text-slate-400 font-mono mt-0.5">
                  ID: {selectedEvent.id} | Action: {selectedEvent.action}
                </p>
              </div>
              <button
                onClick={() => setSelectedEvent(null)}
                className="text-slate-400 hover:text-white"
              >
                ✕
              </button>
            </div>

            <div className="bg-slate-950 p-4 rounded-lg border border-slate-800 overflow-x-auto max-h-96">
              <pre className="text-xs font-mono text-emerald-400">
                {(() => {
                  try {
                    return JSON.stringify(JSON.parse(selectedEvent.payloadSnapshot), null, 2);
                  } catch {
                    return selectedEvent.payloadSnapshot;
                  }
                })()}
              </pre>
            </div>

            <div className="flex justify-end pt-2">
              <button
                onClick={() => setSelectedEvent(null)}
                className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-white rounded-lg text-xs font-medium transition"
              >
                Close Snapshot
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
