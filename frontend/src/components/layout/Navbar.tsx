import React from 'react';
import { Building2, UserCircle, Bell } from 'lucide-react';

interface NavbarProps {
  currentTenant: string;
  onTenantChange: (tenant: string) => void;
}

export const Navbar: React.FC<NavbarProps> = ({ currentTenant, onTenantChange }) => {
  return (
    <header className="h-16 bg-surface/80 backdrop-blur-md border-b border-surfaceBorder fixed top-0 right-0 left-64 z-10 px-8 flex items-center justify-between">
      <div className="flex items-center gap-3">
        <Building2 className="w-4 h-4 text-slate-400" />
        <span className="text-xs text-slate-400 uppercase tracking-wider font-mono">Tenant Partition:</span>
        <select
          value={currentTenant}
          onChange={(e) => onTenantChange(e.target.value)}
          className="bg-background text-sm font-medium border border-surfaceBorder rounded-md px-3 py-1.5 text-white focus:outline-none focus:border-primary"
        >
          <option value="tenant-corp-alpha">AlphaCorp Global Innovations</option>
          <option value="tenant-fintech-beta">BetaScale Financial Technologies</option>
        </select>
      </div>

      <div className="flex items-center gap-4">
        <button className="p-2 text-slate-400 hover:text-white rounded-lg hover:bg-surfaceBorder/50 transition">
          <Bell className="w-4 h-4" />
        </button>
        <div className="h-4 w-[1px] bg-surfaceBorder" />
        <div className="flex items-center gap-2.5">
          <UserCircle className="w-5 h-5 text-primary" />
          <div className="text-left text-xs">
            <p className="font-semibold text-white">Alice Vance</p>
            <p className="text-[10px] text-slate-400 font-mono">ADMIN / OPERATOR</p>
          </div>
        </div>
      </div>
    </header>
  );
};
