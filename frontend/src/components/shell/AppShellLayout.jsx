import { useState } from "react";
import { Outlet } from "react-router-dom";
import Sidebar from "./Sidebar";
import TopBar from "./TopBar";

export default function AppShellLayout() {
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  return <div className="app-shell"><Sidebar isOpen={isSidebarOpen} onNavigate={() => setIsSidebarOpen(false)} /><div className="app-shell__main"><TopBar onMenuToggle={() => setIsSidebarOpen((open) => !open)} /><div className="app-shell__content"><Outlet /></div></div></div>;
}
