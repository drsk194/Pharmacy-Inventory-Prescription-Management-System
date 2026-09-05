# PIPMS — Fixes Applied

This build applies the confirmed bugs from the supplied live/static reports and additional frontend/backend response-contract mismatches found during the audit.

## Fixed areas

- Authentication: forgot-password account enumeration removed; login accepts email/staff ID/badge in the UI; post-login redirects only to role-allowed routes; 10-minute authenticated idle timeout added.
- Controlled substances: transaction types and payload names aligned with the backend enum/DTO; technician and witness are selected from staff instead of raw IDs; batch labels use the real response fields; reconciliation is drug-level; CS PIN attempts are rate-limited with a 5-attempt/15-minute lock and reset on successful/new PIN.
- Doctor/prescription: doctor-profile creation rejects non-doctor users; prescription creation resolves doctor profile/user identity robustly and verifies the ROLE_DOCTOR + linked profile.
- RBAC: pharmacist/doctor stale PATIENT_MANAGE/DOCTOR_MANAGE privileges are removed by the seeder; pharmacist GRN navigation is removed; create-patient/create-doctor controls are admin-only; technician no longer sees Create Batch; batch creation options tolerate independently forbidden lookups.
- Inventory/batch: create-batch form contains all required backend fields; shelf-life override uses the correct DTO property; adjustment approval and batch movement screens now read the actual response property names.
- Procurement/GRN: price comparison reads average price/most recent purchase date; reorder suggestions use the current response names; GRN discrepancy fields are mapped to the current DTO; GRN line-item labels are present.
- Shift management: assignment awaits the save request and reloads the shift list.
- Billing: outstanding-bill errors are contained by the application error boundary and backend response null-safety; bill generation now lets the operator select eligible, unbilled dispensing records for the chosen patient rather than typing numeric IDs; bill creation audit logging occurs after persistence.
- Tests/code quality: stale Google OAuth assertion removed from the login test; unused dispensing state removed; the project’s existing intentional async-fetch lint exceptions are documented on the affected pages.

## Verification performed in this sandbox

- Checked modified Java source for balanced braces and inspected all changed DTO/controller/service references.
- Audited the reported request/response field names against the corresponding backend DTOs.
- Maven backend compilation could not be executed because Maven Central is blocked in this sandbox and the wrapper distribution is not cached.
- Frontend tests/build could not be executed after the uploaded project’s partial `node_modules` became incomplete; offline npm cache is empty and network package installation is unavailable here.

Run the final verification locally with:

```text
cd frontend
npm ci
npm test
npm run lint
npm run build

cd ../backend
./mvnw test
./mvnw spring-boot:run
```


## Additional Create Drug Fix
- Fixed `DrugFormModal.jsx` schedule enum values to `OTC, H, H1, X, NOT_SCHEDULED`.
- Fixed storage enum values to `ROOM_TEMP, REFRIGERATED, FROZEN, CONTROLLED_TEMP`.
- Fixed default storage value to `ROOM_TEMP`.
- Fixed scheduled-drug detection so `NOT_SCHEDULED` behaves as non-scheduled.
- Improved save error display to include backend `fieldErrors`, `message`, and `error`.
