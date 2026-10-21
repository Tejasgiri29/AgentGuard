import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Sidebar } from './components/layout/Sidebar';
import { Navbar } from './components/layout/Navbar';
import { Dashboard } from './pages/Dashboard';
import { Mandates } from './pages/Mandates';
import { PurchaseIntents } from './pages/PurchaseIntents';
import { Approvals } from './pages/Approvals';
import { Payments } from './pages/Payments';
import { Subscriptions } from './pages/Subscriptions';
import { Ledger } from './pages/Ledger';
import { RiskRules } from './pages/RiskRules';
import { Audit } from './pages/Audit';

export const App: React.FC = () => {
  const [tenantId, setTenantId] = useState(
    localStorage.getItem('agentguard_tenant_id') || 'tenant-corp-alpha'
  );

  const handleTenantChange = (newTenant: string) => {
    localStorage.setItem('agentguard_tenant_id', newTenant);
    setTenantId(newTenant);
    window.location.reload();
  };

  return (
    <BrowserRouter>
      <div className="flex h-screen bg-slate-950 text-slate-100 overflow-hidden font-sans">
        {/* Sidebar */}
        <Sidebar />

        {/* Main Area */}
        <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
          <Navbar currentTenant={tenantId} onTenantChange={handleTenantChange} />

          {/* Scrollable Page Body */}
          <main className="flex-1 overflow-y-auto p-6 lg:p-8 bg-gradient-to-b from-slate-950 via-slate-900 to-slate-950">
            <div className="max-w-7xl mx-auto">
              <Routes>
                <Route path="/" element={<Dashboard />} />
                <Route path="/mandates" element={<Mandates />} />
                <Route path="/intents" element={<PurchaseIntents />} />
                <Route path="/approvals" element={<Approvals />} />
                <Route path="/payments" element={<Payments />} />
                <Route path="/subscriptions" element={<Subscriptions />} />
                <Route path="/ledger" element={<Ledger />} />
                <Route path="/risk" element={<RiskRules />} />
                <Route path="/audit" element={<Audit />} />
                <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
            </div>
          </main>
        </div>
      </div>
    </BrowserRouter>
  );
};
