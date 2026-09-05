import { Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import AppShellLayout from "./components/shell/AppShellLayout";
import Footer from "./components/shell/Footer";
import ProtectedRoute from "./components/routing/ProtectedRoute";
import RoleGuard from "./components/routing/RoleGuard";
import Home from "./pages/Home";
import LoginPage from "./pages/auth/LoginPage";
import RegisterPage from "./pages/auth/RegisterPage";
import ForgotPasswordPage from "./pages/auth/ForgotPasswordPage";
import ResetPasswordPage from "./pages/auth/ResetPasswordPage";
import NotAuthorizedPage from "./pages/NotAuthorizedPage";
import AdminDashboard from "./pages/dashboards/AdminDashboard";
import PharmacistDashboard from "./pages/dashboards/PharmacistDashboard";
import TechnicianDashboard from "./pages/dashboards/TechnicianDashboard";
import ProcurementDashboard from "./pages/dashboards/ProcurementDashboard";
import AuditorDashboard from "./pages/dashboards/AuditorDashboard";
import DoctorDashboard from "./pages/dashboards/DoctorDashboard";
import PatientDashboard from "./pages/dashboards/PatientDashboard";
import PrescriptionSubmitPage from "./pages/prescriptions/PrescriptionSubmitPage";
import PrescriptionQueuePage from "./pages/prescriptions/PrescriptionQueuePage";
import PrescriptionDetailPage from "./pages/prescriptions/PrescriptionDetailPage";
import MyPrescriptionsPage from "./pages/prescriptions/MyPrescriptionsPage";
import DispensingWorkbench from "./pages/dispensing/DispensingWorkbench";
import BalanceOrdersPage from "./pages/dispensing/BalanceOrdersPage";
import PatientListPage from "./pages/patients/PatientListPage";
import PatientDetailPage from "./pages/patients/PatientDetailPage";
import MyProfilePage from "./pages/patients/MyProfilePage";
import DoctorListPage from "./pages/doctors/DoctorListPage";
import DoctorDetailPage from "./pages/doctors/DoctorDetailPage";
import MyDoctorProfilePage from "./pages/doctors/MyDoctorProfilePage";
import DrugCatalogPage from "./pages/DrugCatalogPage";
import DrugListPage from "./pages/drugs/DrugListPage";
import DrugInteractionsPage from "./pages/drugs/DrugInteractionsPage";
import SupplierListPage from "./pages/suppliers/SupplierListPage";
import InventoryDashboard from "./pages/inventory/InventoryDashboard";
import BatchDetailPage from "./pages/inventory/BatchDetailPage";
import LocationsPage from "./pages/inventory/LocationsPage";
import AdjustmentApprovalPage from "./pages/inventory/AdjustmentApprovalPage";
import ControlledSubstanceRegisterPage from "./pages/controlled-substances/ControlledSubstanceRegisterPage";
import DiscrepanciesPage from "./pages/controlled-substances/DiscrepanciesPage";
import ReorderSuggestionsPage from "./pages/purchase-orders/ReorderSuggestionsPage";
import PurchaseOrderFormPage from "./pages/purchase-orders/PurchaseOrderFormPage";
import PurchaseOrderListPage from "./pages/purchase-orders/PurchaseOrderListPage";
import PurchaseOrderDetailPage from "./pages/purchase-orders/PurchaseOrderDetailPage";
import GrnFormPage from "./pages/grn/GrnFormPage";
import GrnListPage from "./pages/grn/GrnListPage";
import GrnDetailPage from "./pages/grn/GrnDetailPage";
import GrnDiscrepanciesPage from "./pages/grn/GrnDiscrepanciesPage";
import BillGenerationPage from "./pages/billing/BillGenerationPage";
import BillDetailPage from "./pages/billing/BillDetailPage";
import OutstandingBillsPage from "./pages/billing/OutstandingBillsPage";
import MyBillsPage from "./pages/billing/MyBillsPage";
import NotificationCenterPage from "./pages/notifications/NotificationCenterPage";
import ReportsHubPage from "./pages/reports/ReportsHubPage";
import InventorySummaryReportPage from "./pages/reports/InventorySummaryReportPage";
import DeadStockReportPage from "./pages/reports/DeadStockReportPage";
import SlowMovingReportPage from "./pages/reports/SlowMovingReportPage";
import StockTurnoverReportPage from "./pages/reports/StockTurnoverReportPage";
import PrescriptionVolumeReportPage from "./pages/reports/PrescriptionVolumeReportPage";
import DispensingTurnaroundReportPage from "./pages/reports/DispensingTurnaroundReportPage";
import TechnicianActivityReportPage from "./pages/reports/TechnicianActivityReportPage";
import PharmacistActivityReportPage from "./pages/reports/PharmacistActivityReportPage";
import DrugUtilizationReportPage from "./pages/reports/DrugUtilizationReportPage";
import ProcurementSpendingReportPage from "./pages/reports/ProcurementSpendingReportPage";
import RevenueReportPage from "./pages/reports/RevenueReportPage";
import OutstandingReportPage from "./pages/reports/OutstandingReportPage";
import AuditLogSearchPage from "./pages/admin/AuditLogSearchPage";
import UserManagementPage from "./pages/admin/UserManagementPage";
import SystemConfigPage from "./pages/admin/SystemConfigPage";
import ShiftManagementPage from "./pages/admin/ShiftManagementPage";
import HolidaysPage from "./pages/admin/HolidaysPage";
import OperatingHoursPage from "./pages/admin/OperatingHoursPage";
import SystemHealthPage from "./pages/admin/SystemHealthPage";
import CompliancePage from "./pages/admin/CompliancePage";
import "./App.css";
import SkipLink from "./components/common/SkipLink";
import ErrorBoundary from "./components/common/ErrorBoundary";

export default function App() {
  return <AuthProvider><SkipLink /><div id="main-content"><Routes>
    <Route path="/" element={<Home />} />
    <Route path="/login" element={<LoginPage />} />
    <Route path="/register" element={<RegisterPage />} />
    <Route path="/forgot-password" element={<ForgotPasswordPage />} />
    <Route path="/reset-password" element={<ResetPasswordPage />} />
    <Route path="/not-authorized" element={<NotAuthorizedPage />} />
    <Route path="/catalog" element={<DrugCatalogPage />} />
    <Route element={<ErrorBoundary><AppShellLayout /></ErrorBoundary>}>
    <Route element={<ProtectedRoute />}>
      <Route path="/notifications" element={<NotificationCenterPage />} />
      <Route path="/dashboard/admin" element={<RoleGuard allow={["ROLE_ADMIN"]}><AdminDashboard /></RoleGuard>} />
      <Route path="/dashboard/pharmacist" element={<RoleGuard allow={["ROLE_PHARMACIST"]}><PharmacistDashboard /></RoleGuard>} />
      <Route path="/dashboard/technician" element={<RoleGuard allow={["ROLE_TECHNICIAN"]}><TechnicianDashboard /></RoleGuard>} />
      <Route path="/dashboard/procurement" element={<RoleGuard allow={["ROLE_PROCUREMENT_OFFICER"]}><ProcurementDashboard /></RoleGuard>} />
      <Route path="/dashboard/auditor" element={<RoleGuard allow={["ROLE_AUDITOR"]}><AuditorDashboard /></RoleGuard>} />
      <Route path="/dashboard/doctor" element={<RoleGuard allow={["ROLE_DOCTOR"]}><DoctorDashboard /></RoleGuard>} />
      <Route path="/dashboard/patient" element={<RoleGuard allow={["ROLE_PATIENT"]}><PatientDashboard /></RoleGuard>} />
      <Route path="/patients" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PHARMACIST", "ROLE_TECHNICIAN"]}><PatientListPage /></RoleGuard>} />
      <Route path="/patients/me" element={<RoleGuard allow={["ROLE_PATIENT"]}><MyProfilePage /></RoleGuard>} />
      <Route path="/patients/:id" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PHARMACIST", "ROLE_TECHNICIAN", "ROLE_DOCTOR"]}><PatientDetailPage /></RoleGuard>} />
      <Route path="/doctors" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PHARMACIST"]}><DoctorListPage /></RoleGuard>} />
      <Route path="/doctors/me" element={<RoleGuard allow={["ROLE_DOCTOR"]}><MyDoctorProfilePage /></RoleGuard>} />
      <Route path="/doctors/:id" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PHARMACIST"]}><DoctorDetailPage /></RoleGuard>} />
      <Route path="/drugs" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PHARMACIST"]}><DrugListPage /></RoleGuard>} />
      <Route path="/drugs/interactions" element={<RoleGuard allow={["ROLE_ADMIN"]}><DrugInteractionsPage /></RoleGuard>} />
      <Route path="/suppliers" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PROCUREMENT_OFFICER"]}><SupplierListPage /></RoleGuard>} />
      <Route path="/inventory" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PHARMACIST", "ROLE_TECHNICIAN", "ROLE_PROCUREMENT_OFFICER", "ROLE_AUDITOR"]}><InventoryDashboard /></RoleGuard>} />
      <Route path="/inventory/batches/:id" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PHARMACIST", "ROLE_TECHNICIAN", "ROLE_PROCUREMENT_OFFICER", "ROLE_AUDITOR"]}><BatchDetailPage /></RoleGuard>} />
      <Route path="/inventory/locations" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PHARMACIST", "ROLE_TECHNICIAN"]}><LocationsPage /></RoleGuard>} />
      <Route path="/inventory/adjustments/approvals" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PHARMACIST"]}><AdjustmentApprovalPage /></RoleGuard>} />
      <Route path="/controlled-substances/register" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PHARMACIST"]}><ControlledSubstanceRegisterPage /></RoleGuard>} />
      <Route path="/controlled-substances/discrepancies" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PHARMACIST"]}><DiscrepanciesPage /></RoleGuard>} />
      <Route path="/purchase-orders" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PROCUREMENT_OFFICER"]}><PurchaseOrderListPage /></RoleGuard>} />
      <Route path="/purchase-orders/new" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PROCUREMENT_OFFICER"]}><PurchaseOrderFormPage /></RoleGuard>} />
      <Route path="/purchase-orders/reorder-suggestions" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PROCUREMENT_OFFICER"]}><ReorderSuggestionsPage /></RoleGuard>} />
      <Route path="/purchase-orders/:id" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PROCUREMENT_OFFICER"]}><PurchaseOrderDetailPage /></RoleGuard>} />
      <Route path="/grn" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PROCUREMENT_OFFICER", "ROLE_PHARMACIST"]}><GrnListPage /></RoleGuard>} />
      <Route path="/grn/new" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PROCUREMENT_OFFICER"]}><GrnFormPage /></RoleGuard>} />
      <Route path="/grn/discrepancies" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PROCUREMENT_OFFICER"]}><GrnDiscrepanciesPage /></RoleGuard>} />
      <Route path="/grn/:id" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PROCUREMENT_OFFICER", "ROLE_PHARMACIST"]}><GrnDetailPage /></RoleGuard>} />
      <Route path="/bills/new" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PHARMACIST"]}><BillGenerationPage /></RoleGuard>} />
      <Route path="/bills/outstanding" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PHARMACIST"]}><OutstandingBillsPage /></RoleGuard>} />
      <Route path="/bills/my" element={<RoleGuard allow={["ROLE_PATIENT"]}><MyBillsPage /></RoleGuard>} />
      <Route path="/bills/:id" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PHARMACIST", "ROLE_PATIENT"]}><BillDetailPage /></RoleGuard>} />
      <Route path="/reports" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_AUDITOR"]}><ReportsHubPage /></RoleGuard>} />
      <Route path="/reports/inventory-summary" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_AUDITOR"]}><InventorySummaryReportPage /></RoleGuard>} />
      <Route path="/reports/dead-stock" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_AUDITOR"]}><DeadStockReportPage /></RoleGuard>} />
      <Route path="/reports/slow-moving" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_AUDITOR"]}><SlowMovingReportPage /></RoleGuard>} />
      <Route path="/reports/stock-turnover" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_AUDITOR"]}><StockTurnoverReportPage /></RoleGuard>} />
      <Route path="/reports/prescription-volume" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_AUDITOR"]}><PrescriptionVolumeReportPage /></RoleGuard>} />
      <Route path="/reports/dispensing-turnaround" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_AUDITOR"]}><DispensingTurnaroundReportPage /></RoleGuard>} />
      <Route path="/reports/technician-activity" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_AUDITOR"]}><TechnicianActivityReportPage /></RoleGuard>} />
      <Route path="/reports/pharmacist-activity" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_AUDITOR"]}><PharmacistActivityReportPage /></RoleGuard>} />
      <Route path="/reports/drug-utilization" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_AUDITOR"]}><DrugUtilizationReportPage /></RoleGuard>} />
      <Route path="/reports/procurement-spending" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_AUDITOR"]}><ProcurementSpendingReportPage /></RoleGuard>} />
      <Route path="/reports/revenue" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_AUDITOR"]}><RevenueReportPage /></RoleGuard>} />
      <Route path="/reports/outstanding" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_AUDITOR"]}><OutstandingReportPage /></RoleGuard>} />
      <Route path="/admin/audit-logs" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_AUDITOR"]}><AuditLogSearchPage /></RoleGuard>} />
      <Route path="/admin/users" element={<RoleGuard allow={["ROLE_ADMIN"]}><UserManagementPage /></RoleGuard>} />
      <Route path="/admin/config" element={<RoleGuard allow={["ROLE_ADMIN"]}><SystemConfigPage /></RoleGuard>} />
      <Route path="/admin/shifts" element={<RoleGuard allow={["ROLE_ADMIN"]}><ShiftManagementPage /></RoleGuard>} />
      <Route path="/admin/holidays" element={<RoleGuard allow={["ROLE_ADMIN"]}><HolidaysPage /></RoleGuard>} />
      <Route path="/admin/operating-hours" element={<RoleGuard allow={["ROLE_ADMIN"]}><OperatingHoursPage /></RoleGuard>} />
      <Route path="/admin/system-health" element={<RoleGuard allow={["ROLE_ADMIN"]}><SystemHealthPage /></RoleGuard>} />
      <Route path="/admin/compliance" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_AUDITOR"]}><CompliancePage /></RoleGuard>} />
      <Route path="/prescriptions/new" element={<RoleGuard allow={["ROLE_DOCTOR"]}><PrescriptionSubmitPage /></RoleGuard>} />
      <Route path="/prescriptions/queue" element={<RoleGuard allow={["ROLE_TECHNICIAN", "ROLE_PHARMACIST"]}><PrescriptionQueuePage /></RoleGuard>} />
      <Route path="/prescriptions/my" element={<RoleGuard allow={["ROLE_PATIENT", "ROLE_DOCTOR"]}><MyPrescriptionsPage /></RoleGuard>} />
      <Route path="/prescriptions/:id" element={<RoleGuard allow={["ROLE_ADMIN", "ROLE_PHARMACIST", "ROLE_TECHNICIAN", "ROLE_DOCTOR", "ROLE_PATIENT"]}><PrescriptionDetailPage /></RoleGuard>} />
      <Route path="/dispensing" element={<RoleGuard allow={["ROLE_PHARMACIST", "ROLE_TECHNICIAN"]}><DispensingWorkbench /></RoleGuard>} />
      <Route path="/dispensing/balance-orders" element={<RoleGuard allow={["ROLE_PHARMACIST", "ROLE_ADMIN"]}><BalanceOrdersPage /></RoleGuard>} />
    </Route>
    </Route>
  </Routes></div><Footer /></AuthProvider>;
}
