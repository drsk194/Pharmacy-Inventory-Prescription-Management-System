# PIPMS Frontend

React frontend for the Pharmacy Inventory and Prescription Management System. It communicates with the Spring Boot API through Axios and uses role-based routing for staff and patient workflows.

## Stack

Vite, React, React Router, Axios, React Context, plain CSS, Vitest, Testing Library, MSW, and Cypress.

## Setup

```bash
npm install
```

Create `.env` in the frontend root:

```text
VITE_API_BASE_URL=http://localhost:8080
```

## Run

```bash
npm run dev
```

The development server runs at `http://localhost:8081`. The backend must be running at the URL configured in `.env`.

## Test

```bash
npm run test
npm run test:watch
npm run e2e
npm run e2e:run
```

Cypress tests require the frontend dev server and seeded backend accounts. Update credentials and fixture names in `cypress/e2e` before running against a real database.

## Build

```bash
npm run build
npm run preview
```

Production builds write to `dist/` and split framework dependencies into a vendor chunk. Set `VITE_API_BASE_URL` in `.env.production` before building for deployment.

## Structure

- `src/api`: thin Axios wrappers for backend modules
- `src/context`: auth and controlled-substance session state
- `src/components`: shared shell, routing, admin, and feature components
- `src/pages`: route-level feature screens
- `src/test`: Vitest setup, MSW handlers, and render helpers
- `cypress`: browser support and end-to-end specs

## Conventions

Access tokens remain in memory only. List pages expose loading, error, and empty states. Client-side guards improve UX, while backend authorization remains the real security boundary. Status panels render backend-reported values directly and do not infer healthy or configured states.

Request and response field names should be cross-checked against Swagger before production use. The final Definition of Done includes walking every role's primary workflow at desktop and tablet widths with the backend active.
