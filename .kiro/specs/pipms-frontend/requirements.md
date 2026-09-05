# Requirements Document

## Introduction

PIPMS (Pharmacy Inventory and Prescription Management System) React Frontend covers Sprints 21–30. The system is a React 19 single-page application that serves 7 authenticated roles — Admin, Pharmacist, Technician, Procurement Officer, Auditor, Doctor, and Patient — plus an unauthenticated Guest. It integrates with a Spring Boot 4.1.0 backend on port 8080 via 150+ REST endpoints, runs on port 8081, and covers the full pharmacy operational lifecycle: authentication, patient/doctor management, drug catalog, inventory with FEFO dispensing, prescription workflow, controlled-substance compliance, procurement, billing, notifications, reporting, and system administration. WCAG 2.1 AA accessibility is a first-class requirement built in from Sprint 21.

---

## Glossary

- **AccessDeniedPage**: A dedicated page component rendered when an authenticated user attempts to access a route outside their allowed roles. Distinct from NotFoundPage.
- **AccessToken**: A short-lived JWT bearer token held exclusively in React Context memory (AuthContext), never in localStorage, sessionStorage, or cookies.
- **AuthContext**: The React Context that stores `accessToken`, `refreshToken`, `user` profile, and auth actions (`login`, `logout`, `updateTokens`). The single source of authentication truth on the frontend.
- **Axios Instance**: The configured HTTP client (`src/api/index.js`) with `baseURL=http://localhost:8080`, 30s timeout, and request/response interceptors for token attachment and silent refresh.
- **Batch**: A physical lot of a drug identified by a batch number, with manufacturing date, expiry date, and quantity. Managed by `BatchResponse`.
- **BillResponse**: The data model representing a pharmacy bill — patient, dispensing record, line items, total amount, payment status, and optional insurance claim.
- **CsAuthContext**: The React Context holding the controlled-substance authorization state: `csAuthorized` (boolean), `authorizedAt` (Date | null), and `isExpired()` (returns true if authorization window > 30 minutes has elapsed).
- **CsReauthModal**: A modal dialog that prompts the user for their CS PIN when the CS authorization window has expired or was never established.
- **DataTable**: The shared paginated, sortable table component. Requires an `aria-label` prop; renders a skeleton/spinner on `loading=true` and `EmptyState` on empty data.
- **DispensingWorkbench**: The main page for the dispensing workflow (`src/pages/dispensing/DispensingWorkbench.jsx`). Supports barcode scan (html5-qrcode) and manual text fallback, FEFO batch display, prepare, authorize, label, acknowledgement, counselling, returns, and error reports.
- **Doctor**: An authenticated role that can submit prescriptions and manage their own patients.
- **EARS**: Easy Approach to Requirements Syntax. Six patterns — Ubiquitous, Event-driven, State-driven, Unwanted-event, Optional-feature, and Complex — used to express acceptance criteria.
- **EmptyState**: A shared component rendered instead of an empty table when a list returns `content = []` and `loading = false`.
- **ErrorBoundary**: A React class component that catches render errors and displays a generic fallback message without stack traces.
- **FEFO**: First-Expired, First-Out. Batch selection policy: batches with the earliest expiry date are allocated first. Ordering is performed by the backend; the frontend displays the received order.
- **Guest**: An unauthenticated visitor who can browse the public drug catalog only.
- **InventoryDashboard**: The 5-tab inventory management page (All Stock / Low Stock / Near Expiry / Expired / Quarantined).
- **LabelPreview**: A print-optimised React component that renders a dispensing label from `LabelResponse` data and triggers `window.print()`.
- **Modal**: The shared modal dialog component with focus trap, Escape-to-close, and `aria-modal="true"`.
- **NotificationContext**: The React Context that polls `GET /api/notifications/unread-count` every 30 seconds and exposes `unreadCount` and a manual `refresh()` function.
- **PageResponse\<T\>**: The paginated API envelope returned by list endpoints — `{ content[], pageNumber, pageSize, totalElements, totalPages, last }`.
- **Patient**: An authenticated role that can view their own prescriptions, bills, and medical profile.
- **PIPMS**: Pharmacy Inventory and Prescription Management System.
- **ProtectedRoute**: A route wrapper that redirects unauthenticated users to `/login` (preserving the intended destination in location state).
- **RefreshToken**: A longer-lived token stored in React Context memory used by the Axios interceptor to silently obtain a new `accessToken` on 401 responses.
- **RoleGuard**: A route wrapper that renders `AccessDeniedPage` when the authenticated user's roles do not include any of the route's `allowedRoles`.
- **RoleBasedSidebar**: The navigation sidebar that renders role-appropriate links and collapses to a hamburger menu below 768px viewport width.
- **SRS**: Software Requirements Specification — the authoritative backend specification document referenced throughout this design.
- **StatusBadge**: A shared component rendering a coloured status label with a screen-reader `aria-label`.
- **Toast**: A shared notification component rendered in an `aria-live` region (`polite` for success/info, `assertive` for errors).
- **useApi**: A custom hook returning `{ data, loading, error, refetch }` for any async API function, with double-invocation safety for React StrictMode.
- **useForm**: A custom hook managing form `values`, `errors`, `touched`, `handleChange`, `handleBlur`, `handleSubmit`, `isSubmitting`, and `reset`.
- **usePagination**: A custom hook managing `page` (0-indexed) and `pageSize` state; changing `pageSize` resets `page` to 0.
- **ValidatedInput**: A shared form input component that sets `aria-invalid="true"` and `aria-describedby` referencing the error element when validation fails.
- **WCAG 2.1 AA**: Web Content Accessibility Guidelines version 2.1 at the AA conformance level. Minimum colour contrast 4.5:1 for body text, 3:1 for large text.

---

## Requirements

---

### Requirement 1: Application Bootstrap and Routing

**User Story:** As any user, I want the application to load quickly and route me to the correct starting point, so that I can reach my role-specific dashboard without manual navigation.

#### Acceptance Criteria

1. WHEN the application starts, THE Frontend SHALL mount `AuthContext`, `NotificationContext`, and `CsAuthContext` providers wrapping all routes.
2. WHEN an unauthenticated user navigates to any protected route, THE `ProtectedRoute` SHALL redirect the user to `/login`, preserving the intended destination in `location.state.from`.
3. WHEN an authenticated user navigates to a route whose `allowedRoles` does not include their role, THE `RoleGuard` SHALL render `AccessDeniedPage` and SHALL NOT render the protected page content.
4. WHEN an authenticated user logs in successfully, THE Frontend SHALL navigate to the role-default route determined by `getPostLoginRoute(user)`.
5. THE Frontend SHALL apply `React.lazy` and `Suspense` code-splitting for at minimum the Auth, Inventory, Dispensing, and Admin route groups.
6. WHEN a route is not found, THE Frontend SHALL render `NotFoundPage` with a human-readable message and a link back to the home route.
7. WHEN an authenticated user navigates to `/login`, THE Frontend SHALL redirect them to their role-default route instead of showing the login form.

> *Correctness Properties: Property 2 (Role Enforcement — AC 3), Property 1 (Token Isolation — AC 1)*

---

### Requirement 2: Authentication Module

**User Story:** As any system user, I want secure login, registration, and session management, so that only I can access my account and my session persists seamlessly across API calls.

#### Acceptance Criteria

1. WHEN a user submits valid credentials on `LoginPage`, THE `AuthContext` SHALL store `accessToken` and `refreshToken` exclusively in React Context memory and SHALL NOT write either token to `localStorage`, `sessionStorage`, or any browser cookie.
2. WHEN a user submits valid credentials on `LoginPage`, THE `AuthContext` SHALL set the `user` profile (id, username, email, roles, permissions) from the backend `AuthResponse`.
3. WHEN the Axios interceptor receives a 401 response and `refreshToken` is available, THE Axios Instance SHALL silently call `POST /api/auth/refresh`, update tokens via `AuthContext.updateTokens`, and retry the original request without user interaction.
4. WHEN the `POST /api/auth/refresh` call also fails with 401, THE Axios Instance SHALL call `AuthContext.logout()` and navigate the user to `/login`.
5. WHEN multiple requests receive 401 simultaneously during a token refresh, THE Axios Instance SHALL queue all additional requests and replay them after the single in-flight refresh completes.
6. WHEN a user clicks "Logout", THE `AuthContext` SHALL clear `accessToken`, `refreshToken`, and `user` from memory and navigate to `/login`.
7. WHEN a user submits the `RegisterPage` form with valid data (patient-only registration), THE Frontend SHALL call `POST /api/auth/register` and redirect to `/login` on success.
8. WHEN a user submits the `ForgotPasswordPage`, THE Frontend SHALL call the forgot-password endpoint and display a confirmation message without revealing whether the email exists.
9. WHEN a user submits a valid reset token and new password on `ResetPasswordPage`, THE Frontend SHALL call the reset endpoint and redirect to `/login` on success.
10. WHEN a user submits the `ChangePasswordPage`, THE Frontend SHALL call the change-password endpoint and display a success toast on completion.
11. WHEN a user sets up a CS PIN on the `CsPinSetupPage`, THE Frontend SHALL call `POST /api/auth/controlled-substance-pin` and SHALL NOT persist the PIN in any React state after the API call completes.
12. WHEN a user submits the `LogoutAllPage`, THE Frontend SHALL call the logout-all endpoint, clear all auth state, and navigate to `/login`.
13. IF login credentials are invalid (401 from `POST /api/auth/login`), THEN THE `LoginPage` SHALL display an inline error message and SHALL NOT navigate away from the login page.

> *Correctness Properties: Property 1 (Token Isolation — AC 1, 11), Property 3 (Double-Submit Prevention — implicit on all form submissions)*

---

### Requirement 3: Patient and Doctor Management Module

**User Story:** As an Admin or Pharmacist, I want to manage patient and doctor records, so that accurate demographic and medical information is available during prescription and dispensing workflows.

#### Acceptance Criteria

1. THE `PatientListPage` SHALL display a paginated, searchable list of patients using `DataTable` with an `aria-label` of "Patient list".
2. WHEN an Admin or Pharmacist submits the `PatientFormPage` with valid data, THE Frontend SHALL call the patient create/update endpoint and redirect to the patient list on success.
3. WHEN a patient views their own `PatientProfilePage`, THE Frontend SHALL display allergies, active conditions, and current medications in separate accessible panels.
4. WHEN a Doctor views a patient's allergy panel, THE Frontend SHALL render each allergy entry with severity classification.
5. WHEN a Doctor submits a `DoctorVerificationPage` action, THE Frontend SHALL call the verification endpoint and update the doctor's verification status badge without full-page reload.
6. THE `DoctorListPage` SHALL display a paginated list of doctors accessible to Admin and Pharmacist roles only.
7. IF a patient or doctor record is not found (404), THEN THE Frontend SHALL render `NotFoundPage`.
8. WHEN an Admin updates a staff profile, THE Frontend SHALL validate the Staff ID field against the pattern `/^[A-Za-z0-9]{4,20}$/` on blur and on submit before calling the API.

> *Correctness Properties: Property 8 (Accessible Form Errors — AC 8), Property 7 (Pagination Bounds — AC 1, 6)*

---

### Requirement 4: Drug Catalog and Supplier Module

**User Story:** As a Guest, Pharmacist, or Admin, I want to browse and manage the drug catalog and supplier list, so that accurate drug information is available for prescriptions and procurement.

#### Acceptance Criteria

1. THE `PublicDrugCatalogPage` SHALL be accessible to unauthenticated Guests without requiring login.
2. WHEN a Pharmacist or Admin views `StaffDrugListPage`, THE Frontend SHALL display a paginated drug list with search and filter controls using `DataTable`.
3. WHEN a Pharmacist or Admin submits `DrugFormPage` with valid data, THE Frontend SHALL call the drug create/update endpoint and display a success toast on completion.
4. WHEN a Pharmacist or Admin views `SupplierListPage`, THE Frontend SHALL display a paginated list of suppliers.
5. WHEN an Admin submits a supplier approval action, THE Frontend SHALL call the supplier approval endpoint and update the supplier's `StatusBadge` immediately.
6. WHEN a Pharmacist views a drug's detail page with known interactions, THE Frontend SHALL render a drug interactions panel listing each interaction with severity level.
7. IF a drug search returns no results, THEN THE Frontend SHALL render `EmptyState` with the message "No drugs found matching your search."

> *Correctness Properties: Property 5 (Empty State Completeness — AC 7), Property 7 (Pagination Bounds — AC 2, 4)*

---

### Requirement 5: Inventory Module

**User Story:** As a Pharmacist or Technician, I want to manage stock levels, batch details, and inventory adjustments, so that drug availability is accurate and expiring stock is identified proactively.

#### Acceptance Criteria

1. THE `InventoryDashboard` SHALL display five tabs: All Stock, Low Stock, Near Expiry, Expired, and Quarantined — each rendering the corresponding filtered batch list via `DataTable`.
2. WHEN a tab is selected, THE `InventoryDashboard` SHALL fetch the corresponding filtered data from the backend and update the displayed table.
3. WHEN the backend returns batch records, THE `InventoryDashboard` SHALL mark any batch with `daysToExpiry ≤ 30` as CRITICAL and any batch with `31 ≤ daysToExpiry ≤ 90` as WARNING in the expiry column.
4. WHEN a Pharmacist or Technician submits a `BatchFormPage` with a valid batch number, THE Frontend SHALL validate the batch number against `/^[A-Za-z0-9]{3,50}$/` on blur and on submit before calling the create/update API.
5. WHEN a Pharmacist creates a new batch, THE Frontend SHALL call the batch creation endpoint and display the new batch in the All Stock tab on success.
6. WHEN a Pharmacist submits a stock count reconciliation, THE Frontend SHALL call the stock count endpoint and update affected batch quantities in the dashboard.
7. WHEN a Pharmacist submits an inventory adjustment, THE Frontend SHALL call the adjustment endpoint and display the updated running balance.
8. THE `ApprovalQueuePage` SHALL display pending adjustment/count approvals and allow Pharmacists to approve or reject each entry.
9. IF any batch list tab returns `content = []` and `loading = false`, THEN THE `InventoryDashboard` SHALL render `EmptyState` for that tab and SHALL NOT render an empty table element.
10. WHEN a Pharmacist assigns a batch to a storage location, THE Frontend SHALL call the location assignment endpoint with the selected `locationId`.

> *Correctness Properties: Property 5 (Empty State Completeness — AC 9), Property 7 (Pagination Bounds — AC 1), Property 8 (Accessible Form Errors — AC 4)*

---

### Requirement 6: Prescription Module

**User Story:** As a Doctor, Technician, or Pharmacist, I want to submit, process, and verify prescriptions through a defined workflow, so that medications are dispensed safely and any clinical warnings are surfaced before dispensing proceeds.

#### Acceptance Criteria

1. WHEN a Doctor submits `PrescriptionFormPage` with at least one valid item, THE Frontend SHALL call `POST /api/prescriptions` and display the returned prescription with status `SUBMITTED`.
2. THE `TechnicianPrescriptionQueuePage` SHALL display all prescriptions with status `SUBMITTED` or `PROCESSING` in a paginated list accessible to the Technician role only.
3. WHEN a Technician selects a prescription to process, THE Frontend SHALL call the process endpoint and display any returned `warnings[]` in a `WarningPanel`.
4. WHEN the `WarningPanel` contains at least one warning of severity `BLOCKING`, THE Frontend SHALL disable the "Proceed to Dispense" button and display a message explaining that a Pharmacist override is required.
5. WHEN the `WarningPanel` contains only `WARNING` or `INFO` severity entries, THE Frontend SHALL display the warnings but allow the Technician to proceed.
6. WHEN a Pharmacist verifies a prescription, THE Frontend SHALL call `PUT /api/prescriptions/{id}/verify` and update the prescription status to `VERIFIED`.
7. WHEN a Pharmacist overrides a blocked warning, THE Frontend SHALL require a non-empty override reason in a modal before calling the verify endpoint.
8. WHEN a Pharmacist rejects a prescription, THE Frontend SHALL call the reject endpoint and update the status to `REJECTED`.
9. THE `PrescriptionTimelinePage` SHALL render a visual status timeline showing all status transitions (SUBMITTED → PROCESSING → VERIFIED/REJECTED → DISPENSED) with timestamps.
10. IF a prescription is for a controlled substance (`controlledSubstance = true`), THEN THE Frontend SHALL display a controlled-substance indicator badge on the prescription card and queue page.
11. WHEN a Doctor views their prescription history, THE Frontend SHALL display a paginated list filtered to prescriptions submitted by that doctor.

> *Correctness Properties: Property 3 (Double-Submit Prevention — AC 1, 6, 8), Property 2 (Role Enforcement — AC 2)*

---

### Requirement 7: Dispensing Module

**User Story:** As a Technician or Pharmacist, I want to prepare, authorize, label, and document dispensing of medications, so that the correct drug and batch are dispensed to the correct patient with full traceability.

#### Acceptance Criteria

1. THE `DispensingWorkbench` SHALL accept prescription selection via barcode scan using the `html5-qrcode` library as the primary input method.
2. WHEN the camera or barcode scanner is unavailable, THE `DispensingWorkbench` SHALL provide a manual text input field as a fallback for entering the prescription or barcode identifier.
3. WHEN a prescription is loaded, THE `DispensingWorkbench` SHALL display FEFO-ordered batches as returned by the backend, with CRITICAL and WARNING expiry labels applied according to Requirement 5.3.
4. WHEN a Technician clicks "Prepare Dispense", THE Frontend SHALL call `POST /api/dispensing/prepare` with the selected batches and SHALL disable the prepare button immediately on first click until the API call resolves.
5. WHEN the prepare API returns successfully, THE Frontend SHALL navigate to `LabelPreviewPage` and render a `LabelPreview` component populated with the returned `LabelResponse` data.
6. THE `LabelPreview` SHALL include: patient name, drug name, batch number, expiry date, dosage, instructions, dispensed-by staff name, and dispensed-at timestamp.
7. WHEN a Pharmacist clicks "Print Label", THE Frontend SHALL call `window.print()` on the `LabelPreview` component using print-optimised CSS.
8. WHEN a Pharmacist clicks "Authorize Dispense", THE Frontend SHALL call `PUT /api/dispensing/{id}/authorize` and display a success toast; inventory deduction MUST NOT occur before this authorization call.
9. WHEN the patient provides a digital signature on the `PatientAcknowledgementPage` signature canvas, THE Frontend SHALL capture the signature and submit it to the acknowledgement endpoint.
10. WHEN a Technician processes a dispensing return, THE Frontend SHALL call the returns endpoint and update the dispensing record status.
11. WHEN a Technician submits a dispensing error report, THE Frontend SHALL call the error report endpoint and display a confirmation message.
12. WHEN a Pharmacist processes a balance order, THE Frontend SHALL call the balance order endpoint and link the balance order to the original dispensing record.
13. IF prepare is called with a batch that has a conflict (409 response), THEN THE `DispensingWorkbench` SHALL display the backend conflict message inline near the batch selector and SHALL NOT show a generic toast.
14. WHEN `DispensingWorkbench` loads a prescription requiring CS authorization, THE Frontend SHALL check `CsAuthContext.isExpired()` and show `CsReauthModal` if the result is `true` before making any CS API call.

> *Correctness Properties: Property 3 (Double-Submit Prevention — AC 4), Property 4 (CS Window Integrity — AC 14), Property 6 (Error Message Hygiene — implicit on all error paths)*

---

### Requirement 8: Controlled Substance Module

**User Story:** As a Pharmacist, I want a dedicated and time-gated controlled-substance register, so that every CS transaction is fully documented, dual-authorized, and reconciled in compliance with regulations.

#### Acceptance Criteria

1. WHEN a Pharmacist navigates to `ControlledSubstanceRegisterPage`, THE Frontend SHALL call `CsAuthContext.isExpired()` and render `CsReauthModal` if the result is `true`.
2. WHEN a Pharmacist enters a valid CS PIN in `CsReauthModal`, THE Frontend SHALL call `POST /api/controlled-substances/reauthenticate`, set `csAuthorized = true` and `authorizedAt = Date.now()` in `CsAuthContext`, and dismiss the modal.
3. WHILE `csAuthorized = true` and `isExpired() = false`, THE `ControlledSubstanceRegisterPage` SHALL display the CS register table populated from `GET /api/controlled-substances/register`.
4. WHEN 30 minutes have elapsed since `authorizedAt`, THE `CsAuthContext.isExpired()` function SHALL return `true`, causing all subsequent CS page renders to show `CsReauthModal`.
5. WHEN a Pharmacist submits a CS transaction form (RECEIPT, DISPENSE, ADJUSTMENT, or WASTAGE), THE Frontend SHALL require two valid PINs (performer and witness) before calling the transaction endpoint.
6. WHEN a Pharmacist submits a CS reconciliation, THE Frontend SHALL call the reconciliation endpoint and display the running balance alongside each register entry.
7. WHEN a discrepancy is detected during reconciliation, THE Frontend SHALL render a discrepancy report form and call the discrepancy report endpoint on submission.
8. WHEN an Auditor requests CS integrity verification, THE Frontend SHALL call the integrity verification endpoint and display the verification result.
9. THE CS register table SHALL display each entry with: drug name, schedule class, transaction type, quantity, running balance, performed-by, witnessed-by, and transaction date.
10. IF a CS API call is made while `isExpired() = true`, THEN THE Frontend SHALL intercept the call, prevent the API request, and show `CsReauthModal`.

> *Correctness Properties: Property 4 (CS Window Integrity — AC 1, 4, 10)*

---

### Requirement 9: Procurement Module

**User Story:** As a Procurement Officer, I want to manage purchase orders from reorder suggestions through approval, so that stock levels are replenished efficiently and spending is tracked.

#### Acceptance Criteria

1. THE `ReorderSuggestionsPage` SHALL display a list of drugs below reorder threshold as suggested by the backend, with current stock and suggested quantity columns.
2. WHEN a Procurement Officer builds a PO from suggestions, THE `PurchaseOrderBuilderPage` SHALL populate line items from the selected suggestions and allow quantity adjustment.
3. WHEN a Procurement Officer views the price comparison panel, THE Frontend SHALL display available supplier prices for each line item sorted by unit price ascending.
4. WHEN a Procurement Officer submits a purchase order, THE Frontend SHALL call the PO create endpoint and display the new PO in the `PurchaseOrderListPage` with status `PENDING`.
5. WHEN an Admin or authorized Procurement Officer approves a PO, THE Frontend SHALL call the approval endpoint and update the PO status to `APPROVED`.
6. WHEN an Admin or authorized Procurement Officer rejects a PO, THE Frontend SHALL call the rejection endpoint, require a non-empty rejection reason, and update the PO status to `REJECTED`.
7. THE `PurchaseOrderListPage` SHALL display a paginated list of all POs accessible to Procurement Officer and Admin roles.
8. IF the PO list returns `content = []` and `loading = false`, THEN THE Frontend SHALL render `EmptyState` with the message "No purchase orders found."

> *Correctness Properties: Property 5 (Empty State Completeness — AC 8), Property 3 (Double-Submit Prevention — AC 4, 5, 6)*

---

### Requirement 10: Billing Module

**User Story:** As a Pharmacist or Admin, I want to generate and manage patient bills, accept payments, process refunds, and handle insurance claims, so that the pharmacy's financial transactions are accurate and traceable.

#### Acceptance Criteria

1. WHEN a Pharmacist generates a bill from a completed dispensing record, THE Frontend SHALL call the bill generation endpoint and display the `BillDetailPage` with all line items and total amount.
2. WHEN a Pharmacist or Admin opens the payment modal, THE Frontend SHALL display payment method options: CASH, CARD, and UPI; each option SHALL render its appropriate payment fields.
3. WHEN a payment is submitted, THE Frontend SHALL call the payment endpoint and update the bill status to `PAID` or `PARTIALLY_PAID` as returned by the backend.
4. WHEN a Pharmacist submits a refund request, THE Frontend SHALL call the refund endpoint and display the updated bill status.
5. WHEN a Pharmacist initiates an insurance claim, THE Frontend SHALL render a multi-step `InsuranceClaimStepper` and call the insurance claim endpoint on final step submission.
6. WHEN a Pharmacist cancels a bill, THE Frontend SHALL require confirmation via a `Modal` before calling the cancellation endpoint.
7. THE `OutstandingBillsListPage` SHALL display all bills with status `PENDING` or `PARTIALLY_PAID` in a paginated list accessible to Admin and Pharmacist roles.
8. WHEN a Patient views their billing history, THE Frontend SHALL display only bills belonging to that patient's account via `PatientMyBillsPage`.
9. IF a bill is not found (404), THEN THE Frontend SHALL render `NotFoundPage`.

> *Correctness Properties: Property 3 (Double-Submit Prevention — AC 3, 4), Property 7 (Pagination Bounds — AC 7)*

---

### Requirement 11: Notifications Module

**User Story:** As any authenticated user, I want to receive and view system notifications in near real-time, so that I can respond to alerts without having to manually refresh the page.

#### Acceptance Criteria

1. WHILE a user is authenticated, THE `NotificationContext` SHALL poll `GET /api/notifications/unread-count` at 30-second intervals using `setInterval`.
2. WHEN the polling interval fires, THE `NotificationContext` SHALL update `unreadCount` with the count returned by the backend.
3. THE `NotificationBell` component SHALL display `unreadCount` as a badge; WHEN `unreadCount = 0`, THE badge SHALL be hidden.
4. WHEN an authenticated user clicks `NotificationBell`, THE Frontend SHALL navigate to or display `NotificationCenter` with the full notification list.
5. WHEN an Admin opens `BroadcastComposerPage`, THE Frontend SHALL display a form to compose and send system-wide notifications to all or role-specific recipients.
6. WHEN a Pharmacist or Admin receives a cold-chain breach alert, THE Frontend SHALL surface the breach report with affected batch details.
7. WHEN the user logs out, THE `NotificationContext` SHALL call `clearInterval` on the polling interval to stop background polling.
8. IF the polling request fails due to a network error, THEN THE `NotificationContext` SHALL silently ignore the error and resume polling on the next interval without displaying an error to the user.

> *Correctness Properties: Property 1 (Token Isolation — polling uses Bearer token from AuthContext memory)*

---

### Requirement 12: Reports Module

**User Story:** As an Auditor, Admin, or Pharmacist, I want to generate and export operational reports with date-range filters, so that I can analyze pharmacy performance and meet compliance obligations.

#### Acceptance Criteria

1. THE `ReportsHub` SHALL display navigation to all 10 report types: Prescription Volume, Procurement Spending, Revenue, Audit Logs, Drug Dispensing, Inventory Status, CS Register, Billing Summary, Supplier Performance, and Expiry Tracking.
2. WHEN a user selects a report type and provides a date range, THE Frontend SHALL call the corresponding report endpoint and display the results in a `DataTable`.
3. WHEN a user clicks "Export CSV" for a supported report (prescription-volume, procurement-spending, revenue, audit-logs), THE Frontend SHALL call the CSV export endpoint and trigger a file download in the browser.
4. WHEN a user views a report PDF button, THE Frontend SHALL render the button with the label "Not yet available" and `aria-disabled="true"`, and clicking it SHALL NOT trigger any navigation or API call.
5. WHEN a date-range filter is submitted with an end date before the start date, THE Frontend SHALL display an inline validation error and SHALL NOT call the report endpoint.
6. IF a report returns `content = []` and `loading = false`, THEN THE Frontend SHALL render `EmptyState` with the message "No data found for the selected date range."

> *Correctness Properties: Property 5 (Empty State Completeness — AC 6), Property 7 (Pagination Bounds — AC 2)*

---

### Requirement 13: Audit and Administration Module

**User Story:** As an Admin or Auditor, I want to search audit logs, manage users, and monitor system health, so that I can maintain operational oversight, enforce compliance, and manage the system configuration.

#### Acceptance Criteria

1. THE `AuditLogSearchPage` SHALL provide filter fields for user, action type, entity type, and date range; results SHALL be displayed in a paginated `DataTable` with an `aria-label` of "Audit log results".
2. WHEN an Auditor selects an audit log entry, THE Frontend SHALL display `AuditLogDetailPage` with full entry details including before/after state.
3. WHEN an Auditor clicks "Export Audit Log", THE Frontend SHALL call the audit export endpoint and trigger a CSV download.
4. THE `UserManagementPage` SHALL display a paginated list of all system users accessible to Admin only.
5. WHEN an Admin creates a new user, THE Frontend SHALL call `POST /api/admin/users` with a valid role selection and display the new user in the list on success.
6. WHEN an Admin deactivates a user, THE Frontend SHALL require confirmation via a `Modal` before calling the deactivation endpoint.
7. THE `SystemHealthPage` SHALL display backend health indicators (database connectivity, integration status, backup status) fetched from the health endpoints.
8. THE `ComplianceDashboardPage` SHALL display compliance metrics as returned by the compliance endpoint, accessible to Admin and Auditor roles.
9. WHEN an Admin updates system configuration (shifts, holidays, operating hours), THE Frontend SHALL call the corresponding config endpoint and display a success toast.
10. IF an audit log search returns `content = []` and `loading = false`, THEN THE Frontend SHALL render `EmptyState` with the message "No audit log entries match your filters."

> *Correctness Properties: Property 5 (Empty State Completeness — AC 10), Property 2 (Role Enforcement — AC 4)*

---

### Requirement 14: Shared Components and Custom Hooks

**User Story:** As a developer, I want a set of consistent shared components and hooks, so that UI behaviour, accessibility, and API interaction patterns are uniform across all modules.

#### Acceptance Criteria

1. THE `DataTable` component SHALL accept a required `aria-label` prop and SHALL render skeleton rows WHILE `loading = true` and an `EmptyState` component WHEN `data.length = 0` AND `loading = false`.
2. THE `Modal` component SHALL trap keyboard focus within the modal content WHILE `isOpen = true` and SHALL close WHEN the Escape key is pressed.
3. THE `Modal` component SHALL set `aria-modal="true"` on the dialog element and return focus to the triggering element WHEN closed.
4. THE `ValidatedInput` component SHALL set `aria-invalid="true"` on the `<input>` element AND `aria-describedby` referencing the error `<span>` element's `id` WHEN the `error` prop is non-empty.
5. THE `Toast` component SHALL render success and info messages in an `aria-live="polite"` region and SHALL render error messages in an `aria-live="assertive"` region.
6. THE `StatusBadge` component SHALL include a screen-reader-accessible `aria-label` that textually describes the status value.
7. THE `ErrorBoundary` component SHALL catch render-time JavaScript errors and display a generic fallback message and SHALL NOT display stack traces, Spring class names, or internal error details.
8. THE `useApi` hook SHALL set `loading = true` before the API call begins, set `loading = false` after the call resolves or rejects, and expose `data`, `error`, and `refetch`.
9. THE `usePagination` hook SHALL maintain `page` state as a non-negative integer; WHEN `pageSize` is changed, THE hook SHALL reset `page` to 0.
10. THE `useForm` hook SHALL prevent form submission WHEN `validateForm()` returns `isValid = false`, SHALL set `isSubmitting = true` before calling `onSubmit`, and SHALL set `isSubmitting = false` after `onSubmit` resolves or rejects.
11. WHEN `useForm.handleSubmit` is called, THE hook SHALL show errors for all fields regardless of `touched` state.
12. THE submit button managed by `useForm` SHALL be disabled WHILE `isSubmitting = true`, preventing double-submission.

> *Correctness Properties: Property 5 (Empty State Completeness — AC 1), Property 8 (Accessible Form Errors — AC 4), Property 3 (Double-Submit Prevention — AC 10, 12)*

---

### Requirement 15: Form Validation

**User Story:** As any user interacting with a form, I want immediate and accurate validation feedback, so that I can correct errors before submitting and reduce failed API calls.

#### Acceptance Criteria

1. WHEN a `ValidatedInput` loses focus (blur event), THE Frontend SHALL run `validateField` for that field and display any error message below the input.
2. WHEN a form is submitted, THE Frontend SHALL run `validateForm` for all fields regardless of `touched` state and prevent the API call if any field is invalid.
3. THE Frontend SHALL validate phone numbers against `/^\d{10}$/`; any string not exactly 10 digits SHALL produce the error "Phone must be exactly 10 digits".
4. THE Frontend SHALL validate Staff IDs against `/^[A-Za-z0-9]{4,20}$/`; any non-matching string SHALL produce the error "Staff ID must be 4–20 alphanumeric characters".
5. THE Frontend SHALL validate batch numbers against `/^[A-Za-z0-9]{3,50}$/`; any non-matching string SHALL produce the error "Batch number must be 3–50 alphanumeric characters".
6. WHEN the backend returns a 422 response, THE Frontend SHALL display the backend `message` field as an inline error near the relevant form field.
7. WHEN the backend returns a 409 response, THE Frontend SHALL display the backend-specific conflict message inline near the triggering action and SHALL NOT display a generic toast for conflict errors.
8. THE Frontend SHALL re-validate all fields on the server side; client-side validation is a UX aid and does not replace backend validation.

> *Correctness Properties: Property 8 (Accessible Form Errors — AC 1, 2)*

---

### Requirement 16: Error Handling

**User Story:** As any user, I want clear and appropriate error feedback for all failure conditions, so that I can understand what went wrong and take corrective action without being exposed to internal system details.

#### Acceptance Criteria

1. WHEN the backend returns a 500 response, THE Frontend SHALL display the generic message "Something went wrong. Please try again." via `Toast` and SHALL NOT display error stack traces, Spring exception class names, or any internal error details.
2. WHEN a network request fails with no response, THE Frontend SHALL display the message "Network error. Please check your connection." via `Toast`.
3. WHEN the backend returns a 403 response, THE Frontend SHALL render `AccessDeniedPage` with the message "You don't have permission to access this page."
4. WHEN the backend returns a 404 response (for entity lookups), THE Frontend SHALL render `NotFoundPage`.
5. WHEN a list page API call succeeds with `content = []` and `loading = false`, THE Frontend SHALL render the `EmptyState` component with a contextually appropriate message and SHALL NOT render an empty `<table>` or empty `<tbody>` element.
6. WHEN a loading state is active, THE Frontend SHALL render skeleton rows in `DataTable` or a spinner, and SHALL disable all action buttons WHILE `loading = true`.
7. WHEN a submit action is in progress (`isSubmitting = true`), THE Frontend SHALL disable the submit button and MUST NOT allow re-submission before the API call completes.
8. THE `ErrorBoundary` component SHALL be applied at minimum at the route-group level to prevent full application crashes from isolated module errors.

> *Correctness Properties: Property 5 (Empty State Completeness — AC 5), Property 6 (Error Message Hygiene — AC 1), Property 3 (Double-Submit Prevention — AC 7)*

---

### Requirement 17: Accessibility

**User Story:** As any user including those using assistive technologies, I want the application to meet WCAG 2.1 AA standards, so that I can use all features regardless of my ability or the assistive technology I rely on.

#### Acceptance Criteria

1. THE Frontend SHALL achieve zero axe-core WCAG 2.1 AA violations in automated tests for every page and shared component.
2. THE Frontend SHALL maintain a colour contrast ratio of at least 4.5:1 for body text and at least 3:1 for large text (18px+ or 14px+ bold) against their backgrounds.
3. THE `DataTable` component SHALL have an `aria-label` or `aria-labelledby` attribute on the `<table>` element for every rendered table.
4. WHEN a `Modal` is opened, THE Frontend SHALL move keyboard focus to the first focusable element inside the modal.
5. WHEN a `Modal` is closed, THE Frontend SHALL return keyboard focus to the element that triggered the modal open action.
6. ALL interactive elements (buttons, links, inputs, selects) SHALL be reachable and operable using keyboard navigation alone (Tab, Shift+Tab, Enter, Space, arrow keys as appropriate).
7. THE `RoleBasedSidebar` SHALL collapse to a hamburger toggle button WHEN the viewport width is below 768px, and the toggle SHALL be keyboard-accessible.
8. THE Frontend SHALL use semantic HTML elements (`<nav>`, `<main>`, `<header>`, `<footer>`, `<section>`, `<h1>`–`<h6>`) to define landmark regions on every page.
9. ALL images and icons that convey information SHALL have a non-empty `alt` attribute; decorative images SHALL have `alt=""`.
10. WHEN a form field validation error is present, THE `ValidatedInput` SHALL set `aria-invalid="true"` on the `<input>` and `aria-describedby` SHALL reference the id of the error message element.

> *Correctness Properties: Property 8 (Accessible Form Errors — AC 10)*

---

### Requirement 18: Security

**User Story:** As a system security officer, I want the frontend to implement token security, input safety, and role enforcement, so that the application cannot be exploited through client-side vulnerabilities.

#### Acceptance Criteria

1. THE `AuthContext` SHALL store `accessToken` and `refreshToken` exclusively in React component state (memory); neither token SHALL be written to `localStorage`, `sessionStorage`, browser cookies, or any other persistent browser storage.
2. THE Frontend SHALL NOT use `dangerouslySetInnerHTML` for any user-supplied or API-supplied string content.
3. THE `RoleGuard` component SHALL be applied to every route that requires role-based access; a missing or incorrect role SHALL always render `AccessDeniedPage`.
4. WHEN the CS PIN is submitted, THE Frontend SHALL NOT retain the PIN value in React state, a ref, or any other variable after the API call payload is sent.
5. THE Axios interceptor SHALL ensure only one token-refresh call is in-flight at a time; concurrent 401 responses SHALL be queued and replayed after the single refresh completes.
6. ALL sensitive actions (patient data modification, CS transactions, user management, PO approval) SHALL be guarded by `RoleGuard` on the frontend AND rely on backend `@PreAuthorize` as the authoritative enforcement layer.

> *Correctness Properties: Property 1 (Token Isolation — AC 1), Property 2 (Role Enforcement — AC 3)*

---

### Requirement 19: Performance

**User Story:** As any user, I want the application to respond quickly and not degrade under normal usage, so that I can complete my pharmacy workflow tasks efficiently.

#### Acceptance Criteria

1. THE Frontend SHALL use `React.lazy` and `Suspense` code-splitting for at minimum the Auth, Inventory, Dispensing, and Admin route groups to reduce initial bundle size.
2. ALL list pages SHALL use server-side pagination via `page` and `size` query parameters; the frontend SHALL NOT fetch full datasets and filter client-side.
3. WHEN a search or filter input changes, THE Frontend SHALL debounce API calls by at least 300ms before sending the request.
4. THE `NotificationContext` polling interval SHALL use a single `setInterval` instance; THE interval SHALL be cleared via the `useEffect` cleanup function on component unmount and on logout.
5. THE Frontend SHALL apply `useCallback` to stable event handler functions passed to child list components to prevent unnecessary re-renders.

---

### Requirement 20: Testing Coverage

**User Story:** As a developer, I want automated test coverage across unit, component, property-based, integration, end-to-end, and accessibility dimensions, so that regressions are caught before deployment.

#### Acceptance Criteria

1. THE Frontend SHALL include unit tests (Vitest + RTL) for `validateField`, `validateForm`, `isExpired()`, `handleApiError()`, `getPostLoginRoute()`, and `checkCsAccess()`.
2. THE Frontend SHALL include component tests (RTL + MSW) for `LoginPage`, `DispensingWorkbench`, `DataTable`, `ProtectedRoute`, `RoleGuard`, `Modal`, and `NotificationBell`.
3. THE Frontend SHALL include fast-check property-based tests for: phone validation (any 10-digit string returns null error), `isExpired()` boundary (any timestamp > 30 min ago returns true), and pagination bounds (pageNumber always in [0, max(0, totalPages-1)]).
4. THE Frontend SHALL include MSW-based API integration tests covering all major endpoint categories, `useApi` hook state transitions, the Axios refresh interceptor, and the 401-after-refresh → logout path.
5. THE Frontend SHALL include end-to-end tests for the Doctor→Technician→Pharmacist→Patient prescription-to-dispense chain and the billing chain.
6. THE Frontend SHALL include axe-core / jest-axe accessibility tests for every page and shared component, asserting zero WCAG 2.1 AA violations.
7. WHEN running property-based tests, THE Frontend SHALL use a minimum of 100 fast-check iterations per property.

---

## Correctness Properties (Requirements Mapping)

The 8 formal correctness properties from the design document map to requirements as follows:

### Property 1: Token Isolation

*For any user session, `accessToken` MUST NOT appear in `localStorage`, `sessionStorage`, cookies, or any persisted storage. It exists solely in `AuthContext` state.*

**Validates: Requirements 2.1, 2.11, 18.1**

---

### Property 2: Role Enforcement

*For any route with `allowedRoles = [R₁, R₂, …]`, any authenticated user whose `roles` set contains none of `{R₁, R₂, …}` MUST see `AccessDeniedPage` and MUST NOT see the protected content.*

**Validates: Requirements 1.3, 6.2, 18.3**

---

### Property 3: Double-Submit Prevention

*For any form submission, after the first click of the submit button, the button MUST be disabled until the API call completes (success or error).*

**Validates: Requirements 7.4, 9.4, 10.3, 14.10, 14.12, 16.7**

---

### Property 4: CS Window Integrity

*For any action on the Controlled Substance Register page, if `Date.now() - authorizedAt > 30 * 60 * 1000`, the user MUST be redirected to `CsReauthModal` before any CS API call is made.*

**Validates: Requirements 8.1, 8.4, 8.10, 7.14**

---

### Property 5: Empty State Completeness

*For any list page, when the API returns `content = []` and `loading = false`, the component MUST render an `EmptyState` element and MUST NOT render an empty `<table>` or `<tbody>`.*

**Validates: Requirements 4.7, 5.9, 8.8, 9.8, 12.6, 13.10, 14.1, 16.5**

---

### Property 6: Error Message Hygiene

*For any 500 response from the backend, the frontend MUST display a generic user-facing message and MUST NOT display the raw `error.stack`, Spring exception class name, or any internal stack trace.*

**Validates: Requirements 14.7, 16.1**

---

### Property 7: Pagination Bounds

*For any pagination state, `pageNumber` MUST be in the range `[0, max(0, totalPages - 1)]`. Navigating past the last page MUST NOT be possible via UI controls.*

**Validates: Requirements 3.1, 4.2, 5.1, 10.7, 14.9**

---

### Property 8: Accessible Form Errors

*For any `ValidatedInput` in an error state, the `aria-invalid="true"` attribute MUST be set on the `<input>` element and `aria-describedby` MUST reference the error message element's `id`.*

**Validates: Requirements 3.8, 5.4, 14.4, 15.1, 17.10**
