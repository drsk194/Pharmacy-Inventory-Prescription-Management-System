# Pharmacy Inventory and Prescription Management System (PIPMS)

A full-stack **Pharmacy Inventory and Prescription Management System** built with a **React + Vite frontend**, **Spring Boot REST backend**, **Spring Security + JWT authentication**, and **MySQL**.

The project is organized as a two-part application:

- `frontend/` — React user interface, routing, role guards, API clients, dashboards, pharmacy workflows, reports, and tests.
- `backend/` — Spring Boot REST API, security, business logic, persistence, reporting, auditing, notifications, and administration.
- `sample_data.sql` — sample MySQL data for development/demo use.

---

## 1. Project Overview

PIPMS supports pharmacy operations across inventory, prescriptions, dispensing, procurement, billing, controlled substances, reporting, notifications, auditing, and administration.

The application defines these roles:

| Role | Role Code | Main Responsibility |
|---|---|---|
| System Administrator | `ROLE_ADMIN` | Administration, user management, configuration, audit/compliance |
| Pharmacist | `ROLE_PHARMACIST` | Pharmacy operations, prescription queue, dispensing, billing |
| Pharmacy Technician | `ROLE_TECHNICIAN` | Inventory and dispensing support |
| Procurement Officer | `ROLE_PROCUREMENT_OFFICER` | Suppliers, purchase orders, GRN |
| Auditor | `ROLE_AUDITOR` | Reports, audit logs, compliance |
| Doctor | `ROLE_DOCTOR` | Doctor profile and prescription submission |
| Patient | `ROLE_PATIENT` | Patient profile, prescriptions, bills |

---

# 2. Technology Stack

## Frontend

- **React 19.2.8**
- **Vite 8.2.0**
- **React Router DOM 7.18.2**
- **Axios 1.19.0**
- **Recharts 3.10.1**
- Plain CSS
- React Context for authentication/session state
- Vitest
- Testing Library
- Mock Service Worker (MSW)
- Cypress

## Backend

- **Java 17**
- **Spring Boot 4.1.0**
- Spring Web
- Spring Data JPA
- Spring Security
- JWT (`jjwt` 0.12.6)
- MySQL
- Bean Validation
- Lombok
- Springdoc OpenAPI 3.0.3
- Spring Boot Actuator
- Maven
- JaCoCo

---

# 3. Project Structure

```text
Pharmacy-project/
├── README.md
├── sample_data.sql
├── FIXES_APPLIED.md
├── backend-restart.log
├── backend-restart-error.log
├── SELENIUM_AUTOMATION_README.md
├── .vscode/
│   └── settings.json
├── frontend/
│   ├── package.json
│   ├── package-lock.json
│   ├── vite.config.js
│   ├── index.html
│   ├── cypress.config.js
│   ├── .env
│   ├── .env.production
│   ├── src/
│   │   ├── api/
│   │   ├── assets/
│   │   ├── components/
│   │   ├── context/
│   │   ├── hooks/
│   │   ├── pages/
│   │   ├── test/
│   │   ├── App.jsx
│   │   ├── App.css
│   │   ├── index.css
│   │   └── main.jsx
│   ├── public/
│   ├── cypress/
│   └── dist/
└── backend/
    ├── pom.xml
    ├── mvnw
    ├── mvnw.cmd
    ├── README.md
    └── src/
        ├── main/
        │   ├── java/com/pharmacy/pipms/
        │   └── resources/
        │       └── application.properties
        └── test/
```

---

# 4. Frontend Architecture

The frontend is feature-oriented.

```text
frontend/src/
├── api/          # Axios API wrappers
├── components/   # Reusable UI and workflow components
├── context/      # Authentication/session state
├── hooks/        # Reusable React hooks
├── pages/        # Route-level screens
├── assets/       # Images and static assets
├── test/         # Vitest/MSW test utilities
├── App.jsx
├── index.css
└── main.jsx
```

## API Layer

The frontend contains API modules for:

- Authentication
- Users and user management
- Doctors
- Patients
- Drugs
- Drug interactions
- Batches
- Inventory
- Locations
- Prescriptions
- Dispensing
- Controlled substances
- Suppliers
- Purchase orders
- GRN
- Billing
- Notifications
- Reports
- Audit logs
- System configuration
- Scheduling

---

# 5. Frontend Pages and Modules

## Authentication

```text
/pages/auth/
├── LoginPage.jsx
├── RegisterPage.jsx
├── ForgotPasswordPage.jsx
└── ResetPasswordPage.jsx
```

Authentication-related routes:

```text
/login
/register
/forgot-password
/reset-password
```

A separate unauthorized page is provided at:

```text
/not-authorized
```

---

## Dashboards

Role-specific dashboards are implemented for:

```text
/dashboard/admin
/dashboard/pharmacist
/dashboard/technician
/dashboard/procurement
/dashboard/auditor
/dashboard/doctor
/dashboard/patient
```

Dashboard components include reusable cards and charts such as:

- `StatCard`
- `ChartCard`
- `BarChartCard`
- `DonutChart`
- `TrendAreaChart`

---

## Patient Management

Routes:

```text
/patients
/patients/me
/patients/:id
```

Pages/components include:

- Patient list
- Patient profile
- Patient detail
- Allergies
- Conditions
- Medications
- Patient form

---

## Doctor Management

Routes:

```text
/doctors
/doctors/me
/doctors/:id
```

Includes:

- Doctor list
- Doctor details
- Doctor profile
- Doctor form

---

## Drug and Inventory Management

Drug routes:

```text
/drugs
/drugs/interactions
```

Inventory routes:

```text
/inventory
/inventory/batches/:id
/inventory/locations
/inventory/adjustments/approvals
```

The frontend includes components for:

- Drug forms
- Batch forms
- Inventory adjustments
- Stock counting
- Quarantine
- Locations
- Status badges
- Pagination

---

## Prescriptions

Routes:

```text
/prescriptions/new
/prescriptions/queue
/prescriptions/my
/prescriptions/:id
```

Includes:

- Prescription submission
- Prescription queue
- Prescription details
- Prescription history
- Status timeline
- Warning panel

---

## Dispensing

Routes:

```text
/dispensing
/dispensing/balance-orders
```

The dispensing UI includes components for:

- Barcode scanning
- Label preview
- Signature capture
- Counselling
- Returns
- Error reporting
- Balance orders

---

## Controlled Substances

Routes:

```text
/controlled-substances/register
/controlled-substances/discrepancies
```

Components include:

- Controlled-substance authorization badge
- Transaction form
- Re-authentication modal
- PIN setup
- Reconciliation
- Discrepancy reporting

---

## Procurement

Supplier route:

```text
/suppliers
```

Purchase-order routes:

```text
/purchase-orders
/purchase-orders/new
/purchase-orders/reorder-suggestions
/purchase-orders/:id
```

GRN routes:

```text
/grn
/grn/new
/grn/discrepancies
/grn/:id
```

Procurement UI includes:

- Supplier management
- Purchase-order creation
- Purchase-order details
- Reorder suggestions
- Price comparison
- GRN creation/details
- GRN discrepancy handling
- Supplier performance panel

---

## Billing

Routes:

```text
/bills/new
/bills/outstanding
/bills/my
/bills/:id
```

Billing components include:

- Payment modal
- Refund modal
- Insurance claim panel

---

## Reports

The frontend contains a reports hub plus individual reporting pages:

```text
/reports
/reports/inventory-summary
/reports/dead-stock
/reports/slow-moving
/reports/stock-turnover
/reports/prescription-volume
/reports/dispensing-turnaround
/reports/technician-activity
/reports/pharmacist-activity
/reports/drug-utilization
/reports/procurement-spending
/reports/revenue
/reports/outstanding
```

---

## Administration

Routes:

```text
/admin/audit-logs
/admin/users
/admin/config
/admin/shifts
/admin/holidays
/admin/operating-hours
/admin/system-health
/admin/compliance
```

Administrative components include:

- User form
- Audit-log details
- Status panel
- Holiday form
- Shift form

---

## Notifications

Protected route:

```text
/notifications
```

Components include:

- Notification bell
- Notification center
- Broadcast modal
- Cold-chain breach modal

---

# 6. Application Entry Points

## `frontend/src/main.jsx`

The frontend entry point starts the React application.

## `frontend/src/App.jsx`

The main application file defines:

- Application routes
- Public routes
- Protected routes
- Role-specific guards
- Common application shell
- Error boundary
- Authentication provider

## `frontend/src/index.css`

Global application styling and UI layout styles.

## `frontend/src/components/shell/`

Contains the application shell:

```text
AppShellLayout.jsx
Sidebar.jsx
TopBar.jsx
Footer.jsx
navConfig.js
```

The navigation configuration is role-aware and presents different menus for each role.

---

# 7. Authentication and Authorization

The frontend uses:

```text
AuthContext.jsx
authStore.js
csAuthStore.js
useAuth.js
ProtectedRoute.jsx
RoleGuard.jsx
```

The route flow is:

```text
User Login
    ↓
Spring Boot Authentication API
    ↓
JWT Access Token
    ↓
Frontend Auth Context / Session State
    ↓
ProtectedRoute
    ↓
RoleGuard
    ↓
Role-specific page
```

Client-side guards improve navigation and user experience. Backend authorization remains the actual security boundary.

The backend uses Spring Security and JWT.

---

# 8. Role-Based Navigation

The frontend defines role-specific navigation in:

```text
src/components/shell/navConfig.js
```

Examples:

### Administrator

Includes navigation for:

- Dashboard
- Patients
- Doctors
- Drugs
- Interactions
- Inventory
- Controlled substances
- Suppliers
- Purchase orders
- GRN
- Billing
- Reports
- Audit logs
- Users
- Configuration
- Shifts
- Holidays
- Operating hours
- System health
- Compliance

### Pharmacist

Includes:

- Dashboard
- Patients
- Doctors
- Drugs
- Inventory
- Prescription queue
- Dispensing
- Controlled substances
- Billing

### Technician

Includes:

- Dashboard
- Patients
- Prescription queue
- Inventory

### Procurement Officer

Includes:

- Dashboard
- Inventory
- Suppliers
- Purchase orders
- GRN

### Auditor

Includes:

- Dashboard
- Inventory
- Reports
- Audit logs
- Compliance

### Doctor

Includes:

- Dashboard
- My profile
- New prescription
- My prescriptions

### Patient

Includes:

- Dashboard
- My profile
- My prescriptions
- My bills

---

# 9. Frontend API Configuration

Frontend environment configuration:

```text
frontend/.env
```

Current development API base URL:

```text
VITE_API_BASE_URL=http://localhost:8080
```

Production template:

```text
frontend/.env.production
```

contains a production API placeholder.

Vite development server configuration in `vite.config.js` uses:

```text
http://localhost:8081
```

---

# 10. Frontend API Modules

Actual API wrapper files include:

```text
src/api/
├── adminApi.js
├── auditLogApi.js
├── authApi.js
├── batchApi.js
├── billingApi.js
├── client.js
├── controlledSubstanceApi.js
├── dispensingApi.js
├── doctorApi.js
├── drugApi.js
├── grnApi.js
├── interactionApi.js
├── inventoryApi.js
├── locationApi.js
├── notificationApi.js
├── patientApi.js
├── prescriptionApi.js
├── purchaseOrderApi.js
├── reportApi.js
├── scheduleApi.js
├── supplierApi.js
├── systemAdminApi.js
├── systemConfigApi.js
├── userApi.js
└── userManagementApi.js
```

`client.js` provides the common Axios client used by the feature API modules.

---

# 11. Backend Architecture

The backend uses a feature-based Spring Boot architecture.

```text
backend/src/main/java/com/pharmacy/pipms/
├── admin/
├── audit/
├── auth/
├── batch/
├── billing/
├── common/
├── config/
├── controlledsubstance/
├── dispensing/
├── doctor/
├── drug/
├── exception/
├── fefo/
├── grn/
├── interaction/
├── inventory/
├── notification/
├── patient/
├── prescription/
├── purchaseorder/
├── report/
├── schedule/
├── security/
├── shift/
├── supplier/
├── systemconfig/
├── user/
└── PipmsApplication.java
```

Most feature modules are further divided into:

```text
controller/
dto/
entity/
repository/
service/
```

as applicable.

---

# 12. Backend REST API

The backend exposes REST APIs under `/api/**`.

Main API groups:

| Module | Base Path |
|---|---|
| Authentication | `/api/auth` |
| Users | `/api/users` |
| Admin Users | `/api/admin/users` |
| Roles | `/api/roles` |
| Permissions | `/api/permissions` |
| Drugs | `/api/drugs` |
| Batches | `/api/batches` |
| Inventory | `/api/inventory` |
| Inventory Locations | `/api/inventory/locations` |
| FEFO | `/api/fefo` |
| Patients | `/api/patients` |
| Doctors | `/api/doctors` |
| Prescriptions | `/api/prescriptions` |
| Drug Interactions | `/api/drug-interactions` |
| Dispensing | `/api/dispensing` |
| Controlled Substances | `/api/controlled-substances` |
| Suppliers | `/api/suppliers` |
| Purchase Orders | `/api/purchase-orders` |
| GRN | `/api/grn` |
| Bills | `/api/bills` |
| Payments | `/api/payments` |
| Refunds | `/api/refunds` |
| Notifications | `/api/notifications` |
| Reports | `/api/reports` |
| Admin Analytics | `/api/admin/analytics` |
| Audit Logs | `/api/admin/audit-logs` |
| System Configuration | `/api/admin/config` |
| Shifts | `/api/admin/shifts` |
| Operating Schedule | `/api/admin/schedule` |
| Admin System Operations | `/api/admin` |

---

# 13. Authentication APIs

Authentication base path:

```text
/api/auth
```

Implemented endpoints include:

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
POST /api/auth/logout-all
GET  /api/auth/me
PUT  /api/auth/change-password
POST /api/auth/forgot-password
POST /api/auth/reset-password
PUT  /api/auth/controlled-substance-pin
```

Protected requests use:

```http
Authorization: Bearer <ACCESS_TOKEN>
```

---

# 14. Database

The backend is configured for **MySQL**.

Database name:

```text
pipms_db
```

Configured JDBC URL:

```text
jdbc:mysql://localhost:3306/pipms_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
```

JPA/Hibernate configuration uses:

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

The project uses JPA entities/repositories for persistence.

---

# 15. Sample Data

The repository contains:

```text
sample_data.sql
```

The script targets:

```text
pipms_db
```

It is intended to be run **after the backend has started at least once**, because Hibernate creates/updates the required tables using:

```text
spring.jpa.hibernate.ddl-auto=update
```

The sample SQL contains development/demo records for areas such as:

- Suppliers
- Inventory locations
- Drugs
- Drug batches

The script is designed to use lookup queries rather than relying entirely on hard-coded generated IDs.

---

# 16. Swagger / OpenAPI

After starting the backend:

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/api-docs
```

---

# 17. Health Check

Spring Boot Actuator health endpoint:

```text
http://localhost:8080/actuator/health
```

---

# 18. Running the Full-Stack Project

## Step 1 — Start MySQL

Make sure MySQL Server is running.

The default configuration in the supplied project is:

```text
Database: pipms_db
Username: root
Password: root
```

For a local environment, update `backend/src/main/resources/application.properties` to match your own MySQL credentials.

---

## Step 2 — Start Backend

Open a terminal:

```powershell
cd backend
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On Linux/macOS:

```bash
./mvnw spring-boot:run
```

Backend URL:

```text
http://localhost:8080
```

---

## Step 3 — Load Sample Data

After the backend has started once and the schema exists, run:

```text
sample_data.sql
```

using MySQL Workbench or another MySQL client while connected to:

```text
pipms_db
```

---

## Step 4 — Start Frontend

Open another terminal:

```powershell
cd frontend
npm install
npm run dev
```

Frontend URL:

```text
http://localhost:8081
```

The frontend reads:

```text
VITE_API_BASE_URL=http://localhost:8080
```

from its environment configuration.

---

# 19. Frontend Commands

Install dependencies:

```bash
npm install
```

Run development server:

```bash
npm run dev
```

Build production bundle:

```bash
npm run build
```

Preview production build:

```bash
npm run preview
```

Lint:

```bash
npm run lint
```

---

# 20. Testing

## Frontend Unit/Component Tests

Run Vitest tests:

```bash
npm run test
```

Watch mode:

```bash
npm run test:watch
```

The project includes tests for areas such as:

```text
AuthContext
LoginPage
RoleGuard
Axios/API client
Prescription warnings
```

MSW test handlers and setup are located under:

```text
src/test/
```

---

## Cypress End-to-End Tests

Open Cypress:

```bash
npm run e2e
```

Run Cypress headlessly:

```bash
npm run e2e:run
```

The project includes:

```text
cypress/e2e/auth-role-flow.cy.js
```

The browser tests require the frontend to be running and appropriate seeded backend accounts.

---

# 21. Backend Build and Test

From the `backend` directory:

Compile:

```powershell
.\mvnw.cmd clean compile
```

Run tests:

```powershell
.\mvnw.cmd test
```

Create package/JAR:

```powershell
.\mvnw.cmd clean package
```

Linux/macOS equivalents:

```bash
./mvnw clean compile
./mvnw test
./mvnw clean package
```

JaCoCo coverage output:

```text
target/site/jacoco/index.html
```

---

# 22. Docker

No `Dockerfile` or `docker-compose.yml` was found in the supplied project root during inspection.

Therefore, Docker execution commands are intentionally **not** documented as available project functionality here.

---

# 23. Application Data Flow

The main full-stack communication flow is:

```text
React Page
   ↓
React Component / Form
   ↓
Frontend API Module
   ↓
Axios Client
   ↓
HTTP REST Request
   ↓
Spring Boot Controller
   ↓
Service Layer
   ↓
Repository / JPA
   ↓
MySQL Database
   ↓
Response
   ↓
Axios
   ↓
React UI Update
```

For protected operations:

```text
JWT Access Token
       ↓
Authorization: Bearer <TOKEN>
       ↓
Spring Security
       ↓
Role / Permission Check
       ↓
Controller / Service
```

---

# 24. Security

The project uses:

- Spring Security
- JWT access/refresh tokens
- BCrypt password hashing
- Role-based authorization
- Method-level authorization such as `@PreAuthorize`
- Frontend protected routes
- Frontend role guards

Important production configuration:

```text
jwt.secret=CHANGE_THIS_TO_A_LONG_RANDOM_256_BIT_SECRET_BEFORE_PRODUCTION_USE
```

This value must be replaced before production deployment.

Database credentials and secrets should also be moved out of source-controlled configuration for production use.

---

# 25. Main Configuration Files

## Frontend

```text
frontend/package.json
frontend/vite.config.js
frontend/.env
frontend/.env.production
frontend/index.html
frontend/cypress.config.js
frontend/eslint.config.js
```

## Backend

```text
backend/pom.xml
backend/src/main/resources/application.properties
```

---

# 26. Important Development Configuration

Backend port:

```text
8080
```

Frontend development port:

```text
8081
```

MySQL database:

```text
pipms_db
```

Configured development billing values include:

```text
GST: 12.00%
Dispensing fee: 20.00
```

Procurement approval threshold:

```text
50000
```

Other configurable backend properties cover:

- JWT expiration
- Controlled-substance re-authentication
- Notification escalation
- Reporting thresholds

---

# 27. Project Notes

The frontend contains a compiled `dist/` directory, but the normal development workflow is to run the source project using:

```bash
npm run dev
```

The backend includes Maven Wrapper files:

```text
mvnw
mvnw.cmd
```

so a system-wide Maven installation is not required for normal backend development.

---

# 28. Recommended Startup Order

```text
1. Start MySQL
        ↓
2. Start Spring Boot backend
        ↓
3. Allow Hibernate to create/update schema
        ↓
4. Load sample_data.sql
        ↓
5. Start React frontend
        ↓
6. Open http://localhost:8081
        ↓
7. Use the role-specific application workflow
```

---

# 29. Troubleshooting

## Frontend cannot reach backend

Check:

```text
frontend/.env
```

and verify:

```text
VITE_API_BASE_URL=http://localhost:8080
```

Also verify the backend is running on port `8080`.

## Database connection error

Check:

```text
backend/src/main/resources/application.properties
```

Verify:

- MySQL is running
- Database credentials are correct
- Database name is `pipms_db`

## No sample data

Start the backend at least once so Hibernate creates the schema, then execute:

```text
sample_data.sql
```

## Unauthorized page

Check that:

- The user is logged in
- The JWT/session is valid
- The account has the required role
- Backend authorization permits the requested operation

---

# 30. Project Summary

PIPMS is a full-stack pharmacy management application with a role-aware React frontend and a secured Spring Boot backend.

### Frontend

The React application provides:

- Authentication screens
- Role-specific dashboards
- Inventory workflows
- Prescription workflows
- Dispensing workflows
- Procurement and GRN workflows
- Controlled-substance workflows
- Billing
- Reports
- Administration
- Notifications
- Patient and doctor interfaces

### Backend

The Spring Boot application provides:

- REST APIs
- JWT authentication
- Role-based authorization
- JPA/MySQL persistence
- Pharmacy business modules
- Reporting
- Auditing
- Notifications
- Administration
- Swagger/OpenAPI
- Actuator health monitoring

### Database

MySQL database:

```text
pipms_db
```

---

# 31. Quick Start

```bash
# Terminal 1
cd backend
./mvnw spring-boot:run

# Terminal 2
cd frontend
npm install
npm run dev
```

Then open:

```text
Frontend: http://localhost:8081
Backend:  http://localhost:8080
Swagger:  http://localhost:8080/swagger-ui.html
Health:   http://localhost:8080/actuator/health
```

For Windows, use:

```powershell
.\mvnw.cmd spring-boot:run
```

instead of `./mvnw`.

---

# 32. Repository Reference

This README describes the project based on the supplied source tree and configuration files. File names, routes, roles, API groups, ports, and runtime configuration listed here correspond to the inspected project rather than a generic pharmacy-system template.
