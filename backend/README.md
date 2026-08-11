# Pharmacy Inventory and Prescription Management System (PIPMS) — Backend

Backend REST API for the **Pharmacy Inventory and Prescription Management System (PIPMS)**. The application is built with Spring Boot and MySQL and provides JWT-based authentication, role-based access control, pharmacy inventory management, prescriptions, dispensing, procurement, billing, reporting, auditing, notifications, and administrative operations.

## Table of Contents

- [Project Overview](#project-overview)
- [Backend Features](#backend-features)
- [User Roles](#user-roles)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Database Configuration](#database-configuration)
- [Running the Backend](#running-the-backend)
- [API Documentation](#api-documentation)
- [Authentication](#authentication)
- [Main API Groups](#main-api-groups)
- [Frontend Connection](#frontend-connection)
- [Build and Test](#build-and-test)
- [Important Security Notes](#important-security-notes)

## Project Overview

PIPMS is a pharmacy management backend designed to support the end-to-end workflow of a pharmacy. It manages users and permissions, drug information, stock and batches, prescriptions, dispensing, procurement, suppliers, goods receipt, controlled substances, payments, refunds, reports, audit trails, and system administration.

The backend exposes REST APIs under `/api/**` and secures protected endpoints using Spring Security and JWT tokens.

## Backend Features

- User registration, login, logout, refresh tokens, password change and reset
- JWT-based stateless authentication
- Role-based and permission-based access control
- BCrypt password hashing
- Drug master and drug catalog management
- Batch and expiry tracking
- FEFO (First Expiry, First Out) stock planning and consumption
- Inventory locations, stock counts, adjustments and variance reporting
- Patient profiles, allergies, medications and medical conditions
- Doctor profiles and controlled-substance authorization
- Prescription creation, verification, processing, rejection and history
- Drug interaction management
- Dispensing workflow, labels, acknowledgement, counselling and returns
- Controlled-substance register, reconciliation and discrepancy handling
- Supplier and purchase-order management
- Goods Receipt Note (GRN) processing and supplier performance
- Billing, insurance claims, payments and refunds
- Notifications, escalation and low-stock checks
- Reporting and analytics
- Audit logging and audit-log export
- Admin user, system configuration, shift, schedule and compliance functions
- Spring Boot Actuator health endpoint
- Swagger/OpenAPI documentation
- JaCoCo test coverage support

## User Roles

The backend defines the following roles:

| Role | Purpose |
|---|---|
| `ROLE_ADMIN` | System administration and privileged management |
| `ROLE_PHARMACIST` | Prescription verification, dispensing and pharmacy operations |
| `ROLE_TECHNICIAN` | Operational pharmacy and dispensing support |
| `ROLE_PROCUREMENT_OFFICER` | Suppliers, purchase orders and procurement operations |
| `ROLE_AUDITOR` | Audit, compliance and reporting access |
| `ROLE_DOCTOR` | Doctor-related and prescription operations |
| `ROLE_PATIENT` | Patient profile and patient-facing operations |

Endpoint-level authorization is enforced using Spring Security and method-security rules such as `@PreAuthorize`.

## Technology Stack

| Technology | Usage |
|---|---|
| Java 17 | Programming language |
| Spring Boot 4.1.0 | Backend framework |
| Spring Web | REST API development |
| Spring Data JPA | Database persistence |
| Spring Security | Authentication and authorization |
| JWT (`jjwt` 0.12.6) | Access and refresh token handling |
| MySQL | Relational database |
| Hibernate | ORM/JPA implementation |
| Bean Validation | Request validation |
| Lombok | Boilerplate reduction |
| Springdoc OpenAPI 3.0.3 | Swagger/OpenAPI documentation |
| Spring Boot Actuator | Health and application monitoring |
| Maven | Build and dependency management |
| JaCoCo | Test coverage reporting |

## Project Structure

```text
backend/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── README.md
└── src/
    ├── main/
    │   ├── java/com/pharmacy/pipms/
    │   │   ├── admin/
    │   │   ├── audit/
    │   │   ├── auth/
    │   │   ├── batch/
    │   │   ├── billing/
    │   │   ├── common/
    │   │   ├── config/
    │   │   ├── controlledsubstance/
    │   │   ├── dispensing/
    │   │   ├── doctor/
    │   │   ├── drug/
    │   │   ├── exception/
    │   │   ├── fefo/
    │   │   ├── grn/
    │   │   ├── interaction/
    │   │   ├── inventory/
    │   │   ├── notification/
    │   │   ├── patient/
    │   │   ├── prescription/
    │   │   ├── purchaseorder/
    │   │   ├── report/
    │   │   ├── schedule/
    │   │   ├── security/
    │   │   ├── shift/
    │   │   ├── supplier/
    │   │   ├── systemconfig/
    │   │   ├── user/
    │   │   └── PipmsApplication.java
    │   └── resources/
    │       └── application.properties
    └── test/
```

The project follows a feature-based structure. Most modules contain their own controller, DTO, entity, repository and service classes.

## Prerequisites

Install the following before running the backend:

- **Java 17 or later compatible JDK**
- **MySQL Server**
- **Git**

Maven does not need to be installed separately because the project contains the Maven Wrapper (`mvnw` / `mvnw.cmd`).

Check Java:

```bash
java -version
```

Check MySQL is running before starting the application.

## Database Configuration

The database configuration is located at:

```text
src/main/resources/application.properties
```

The project uses a MySQL database named:

```text
pipms_db
```

Before running the backend, update the MySQL username and password in `application.properties` to match your local MySQL installation.

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pipms_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

Hibernate is currently configured with:

```properties
spring.jpa.hibernate.ddl-auto=update
```

This allows Hibernate to create/update the required tables from the JPA entities during development.

## Running the Backend

### Windows PowerShell / Command Prompt

Open a terminal inside the `backend` folder:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

If Maven is already installed globally, you can also run:

```powershell
mvn spring-boot:run
```

### Linux / macOS

```bash
cd backend
./mvnw spring-boot:run
```

The backend runs by default on:

```text
http://localhost:8080
```

## API Documentation

After starting the backend, open Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON is available at:

```text
http://localhost:8080/api-docs
```

Health check:

```text
http://localhost:8080/actuator/health
```

Swagger UI and the health endpoint are publicly accessible in the current security configuration.

## Authentication

Authentication APIs use the base path:

```text
/api/auth
```

Important endpoints include:

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/auth/register` | Register a user |
| `POST` | `/api/auth/login` | Login and obtain tokens |
| `POST` | `/api/auth/refresh` | Refresh authentication token |
| `POST` | `/api/auth/logout` | Logout current session/token |
| `POST` | `/api/auth/logout-all` | Logout all sessions |
| `GET` | `/api/auth/me` | Get authenticated user information |
| `PUT` | `/api/auth/change-password` | Change password |
| `POST` | `/api/auth/forgot-password` | Start password reset |
| `POST` | `/api/auth/reset-password` | Reset password |
| `PUT` | `/api/auth/controlled-substance-pin` | Configure controlled-substance PIN |

For protected endpoints, send the JWT access token in the request header:

```http
Authorization: Bearer <ACCESS_TOKEN>
```

## Main API Groups

| Module | Base API |
|---|---|
| Authentication | `/api/auth` |
| Users | `/api/users` |
| Admin Users | `/api/admin/users` |
| Roles & Permissions | `/api/roles`, `/api/permissions` |
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
| Goods Receipt (GRN) | `/api/grn` |
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

For the full endpoint list, request/response schemas and authorization requirements, use Swagger UI.

## Frontend Connection

The backend enables CORS for local frontend development on:

```text
http://localhost:3000
http://localhost:8081
```

Use this backend base URL in the frontend API configuration:

```text
http://localhost:8080
```

Example with Axios:

```javascript
import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080",
});

export default api;
```

For authenticated requests, attach the access token as a Bearer token.

## Build and Test

### Compile the project

Windows:

```powershell
.\mvnw.cmd clean compile
```

Linux/macOS:

```bash
./mvnw clean compile
```

### Run tests

Windows:

```powershell
.\mvnw.cmd test
```

Linux/macOS:

```bash
./mvnw test
```

### Create the JAR

Windows:

```powershell
.\mvnw.cmd clean package
```

Linux/macOS:

```bash
./mvnw clean package
```

The generated JAR will be placed inside:

```text
target/
```

### JaCoCo coverage report

After running tests, the JaCoCo HTML report is generated under:

```text
target/site/jacoco/index.html
```

## Important Security Notes

Before using the project outside local development:

1. Replace the development JWT secret with a long, securely generated secret.
2. Do not commit real database passwords or production secrets to Git.
3. Use environment variables or a secure secrets-management solution for production credentials.
4. Restrict CORS origins to the actual deployed frontend URL.
5. Review database schema-update settings before production deployment.
6. Use HTTPS in production.
7. Keep role/permission checks enabled for all sensitive endpoints.

---

## Project

**Pharmacy Inventory and Prescription Management System (PIPMS)**  
Backend: Spring Boot + MySQL + JWT + RBAC
