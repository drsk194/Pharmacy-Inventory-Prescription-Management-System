-- ============================================================================
-- PIPMS sample data
-- ============================================================================
-- Run this in MySQL Workbench AFTER the backend has started at least once
-- (Hibernate's ddl-auto=update creates the tables on first boot, so the
-- tables must already exist before this script runs).
--
-- Target database: pipms_db  (matches application.properties)
--
-- All FK lookups use subqueries instead of hardcoded IDs, so this is safe
-- to run regardless of what the DataSeeder has already created.
-- ============================================================================

USE pipms_db;

-- ----------------------------------------------------------------------------
-- 1. Suppliers (approved + active, so they show up in the PO "Supplier" list)
-- ----------------------------------------------------------------------------
INSERT INTO suppliers
    (supplier_name, contact_person, phone, email, address, drug_license_number, credit_terms, rating, approved, active, created_at, updated_at)
SELECT * FROM (SELECT
    'ColdChain Logistics' AS supplier_name, 'Meera Iyer' AS contact_person, '9845012345' AS phone,
    'sales@coldchainlogistics.example' AS email, 'Plot 12, Industrial Estate, Chennai' AS address,
    'DL-TN-9911' AS drug_license_number, 'Net 45' AS credit_terms, 4.5 AS rating,
    1 AS approved, 1 AS active, NOW() AS created_at, NOW() AS updated_at
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM suppliers WHERE supplier_name = 'ColdChain Logistics');

INSERT INTO suppliers
    (supplier_name, contact_person, phone, email, address, drug_license_number, credit_terms, rating, approved, active, created_at, updated_at)
SELECT * FROM (SELECT
    'Regional Pharma Co' AS supplier_name, 'Arvind Menon' AS contact_person, '9845067890' AS phone,
    'orders@regionalpharma.example' AS email, '44 Anna Salai, Chennai' AS address,
    'DL-TN-8823' AS drug_license_number, 'Net 30' AS credit_terms, 4.1 AS rating,
    1 AS approved, 1 AS active, NOW() AS created_at, NOW() AS updated_at
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM suppliers WHERE supplier_name = 'Regional Pharma Co');

-- ----------------------------------------------------------------------------
-- 2. Inventory locations
-- ----------------------------------------------------------------------------
INSERT INTO inventory_locations (name, type, description, active, created_at, updated_at)
SELECT * FROM (SELECT 'Room Store B' AS name, 'ROOM_TEMPERATURE' AS type, 'Overflow room-temperature shelving' AS description, 1 AS active, NOW() AS created_at, NOW() AS updated_at) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM inventory_locations WHERE name = 'Room Store B');

INSERT INTO inventory_locations (name, type, description, active, created_at, updated_at)
SELECT * FROM (SELECT 'Cold Chain Fridge 2' AS name, 'REFRIGERATED' AS type, 'Secondary vaccine/insulin fridge' AS description, 1 AS active, NOW() AS created_at, NOW() AS updated_at) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM inventory_locations WHERE name = 'Cold Chain Fridge 2');

-- ----------------------------------------------------------------------------
-- 3. Drugs (a few extra, varied schedule/storage so filters have data)
-- ----------------------------------------------------------------------------
INSERT INTO drugs
    (generic_name, brand_name, ndc_code, drug_class, schedule, storage_condition, unit_of_measure,
     reorder_level, min_stock_level, max_stock_level, barcode, active,
     max_prescription_qty_per_fill, max_refills_allowed, created_at, updated_at)
SELECT * FROM (SELECT
    'Ibuprofen' AS generic_name, 'Brufen 400' AS brand_name, '0009-1234-01' AS ndc_code, 'NSAID' AS drug_class,
    'OTC' AS schedule, 'ROOM_TEMPERATURE' AS storage_condition, 'Tablet' AS unit_of_measure,
    300 AS reorder_level, 150 AS min_stock_level, 2500 AS max_stock_level, '8901234500011' AS barcode,
    1 AS active, NULL AS max_prescription_qty_per_fill, NULL AS max_refills_allowed, NOW() AS created_at, NOW() AS updated_at
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM drugs WHERE generic_name = 'Ibuprofen' AND brand_name = 'Brufen 400');

INSERT INTO drugs
    (generic_name, brand_name, ndc_code, drug_class, schedule, storage_condition, unit_of_measure,
     reorder_level, min_stock_level, max_stock_level, barcode, active,
     max_prescription_qty_per_fill, max_refills_allowed, created_at, updated_at)
SELECT * FROM (SELECT
    'Tramadol' AS generic_name, 'Tramazac 50' AS brand_name, '0010-5566-02' AS ndc_code, 'Opioid Analgesic' AS drug_class,
    'SCHEDULE_H1' AS schedule, 'CONTROLLED_ROOM_TEMPERATURE' AS storage_condition, 'Capsule' AS unit_of_measure,
    60 AS reorder_level, 30 AS min_stock_level, 400 AS max_stock_level, '8901234500028' AS barcode,
    1 AS active, 20 AS max_prescription_qty_per_fill, 0 AS max_refills_allowed, NOW() AS created_at, NOW() AS updated_at
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM drugs WHERE generic_name = 'Tramadol' AND brand_name = 'Tramazac 50');

INSERT INTO drugs
    (generic_name, brand_name, ndc_code, drug_class, schedule, storage_condition, unit_of_measure,
     reorder_level, min_stock_level, max_stock_level, barcode, active,
     max_prescription_qty_per_fill, max_refills_allowed, created_at, updated_at)
SELECT * FROM (SELECT
    'Cetirizine' AS generic_name, 'Cetzine' AS brand_name, '0011-7788-03' AS ndc_code, 'Antihistamine' AS drug_class,
    'OTC' AS schedule, 'ROOM_TEMPERATURE' AS storage_condition, 'Tablet' AS unit_of_measure,
    200 AS reorder_level, 100 AS min_stock_level, 1800 AS max_stock_level, '8901234500035' AS barcode,
    1 AS active, NULL AS max_prescription_qty_per_fill, NULL AS max_refills_allowed, NOW() AS created_at, NOW() AS updated_at
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM drugs WHERE generic_name = 'Cetirizine' AND brand_name = 'Cetzine');

-- ----------------------------------------------------------------------------
-- 4. Drug batches (gives Inventory / dispensing / expiry pages real stock)
--    Uses the two suppliers + two locations inserted above.
-- ----------------------------------------------------------------------------
INSERT INTO drug_batches
    (drug_id, batch_number, manufacturing_date, expiry_date, supplier_id,
     quantity_received, current_quantity, purchase_price, mrp, grn_id, status, location_id,
     created_at, updated_at)
SELECT
    d.id, 'BATCH-IBU-01', DATE_SUB(CURDATE(), INTERVAL 3 MONTH), DATE_ADD(CURDATE(), INTERVAL 18 MONTH),
    s.id, 1000, 820, 2.10, 4.50, NULL, 'AVAILABLE', l.id, NOW(), NOW()
FROM drugs d, suppliers s, inventory_locations l
WHERE d.generic_name = 'Ibuprofen' AND d.brand_name = 'Brufen 400'
  AND s.supplier_name = 'Regional Pharma Co'
  AND l.name = 'Room Store B'
  AND NOT EXISTS (SELECT 1 FROM drug_batches WHERE batch_number = 'BATCH-IBU-01');

INSERT INTO drug_batches
    (drug_id, batch_number, manufacturing_date, expiry_date, supplier_id,
     quantity_received, current_quantity, purchase_price, mrp, grn_id, status, location_id,
     created_at, updated_at)
SELECT
    d.id, 'BATCH-TRA-01', DATE_SUB(CURDATE(), INTERVAL 1 MONTH), DATE_ADD(CURDATE(), INTERVAL 2 MONTH),
    s.id, 200, 140, 8.75, 15.00, NULL, 'AVAILABLE', l.id, NOW(), NOW()
FROM drugs d, suppliers s, inventory_locations l
WHERE d.generic_name = 'Tramadol' AND d.brand_name = 'Tramazac 50'
  AND s.supplier_name = 'ColdChain Logistics'
  AND l.name = 'Cold Chain Fridge 2'
  AND NOT EXISTS (SELECT 1 FROM drug_batches WHERE batch_number = 'BATCH-TRA-01');
-- Note: expiry is set ~2 months out on purpose so it shows up under "Near Expiry".

-- ----------------------------------------------------------------------------
-- 5. Patients
-- ----------------------------------------------------------------------------
INSERT INTO patients
    (medical_record_number, full_name, date_of_birth, gender, phone_number, email, address,
     emergency_contact_name, emergency_contact_phone, active, created_at, updated_at)
SELECT * FROM (SELECT
    'MRN-1001' AS medical_record_number, 'Karthik Subramaniam' AS full_name, '1990-04-12' AS date_of_birth,
    'MALE' AS gender, '9876500011' AS phone_number, 'karthik.s@example.com' AS email,
    '12 Gandhi Nagar, Chennai' AS address, 'Lakshmi Subramaniam' AS emergency_contact_name,
    '9876500012' AS emergency_contact_phone, 1 AS active, NOW() AS created_at, NOW() AS updated_at
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM patients WHERE medical_record_number = 'MRN-1001');

INSERT INTO patients
    (medical_record_number, full_name, date_of_birth, gender, phone_number, email, address,
     emergency_contact_name, emergency_contact_phone, active, created_at, updated_at)
SELECT * FROM (SELECT
    'MRN-1002' AS medical_record_number, 'Divya Krishnan' AS full_name, '1985-11-02' AS date_of_birth,
    'FEMALE' AS gender, '9876500021' AS phone_number, 'divya.k@example.com' AS email,
    '78 Kamaraj Salai, Coimbatore' AS address, 'Ramesh Krishnan' AS emergency_contact_name,
    '9876500022' AS emergency_contact_phone, 1 AS active, NOW() AS created_at, NOW() AS updated_at
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM patients WHERE medical_record_number = 'MRN-1002');

-- ----------------------------------------------------------------------------
-- 6. A staff user with the Doctor role, plus a matching doctor profile
--    (needed because "Create Doctor" requires an existing user with
--    ROLE_DOCTOR before you can attach license details to them).
--    Login password for this seeded account: Passw0rd!
-- ----------------------------------------------------------------------------
INSERT INTO users
    (full_name, email, staff_id, password_hash, phone_number, active, account_locked,
     failed_login_attempts, controlled_substance_authorized, created_at, updated_at)
SELECT * FROM (SELECT
    'Dr. Meenakshi Rao' AS full_name, 'meenakshi.rao@pipms.com' AS email, 'DOC0099' AS staff_id,
    '$2b$10$1zRrO2samx.W7WU.Vc9QIudIPk.pRo3Ls154MoJEXi1yvz1chRKke' AS password_hash,
    '9845099001' AS phone_number, 1 AS active, 0 AS account_locked, 0 AS failed_login_attempts,
    0 AS controlled_substance_authorized, NOW() AS created_at, NOW() AS updated_at
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'meenakshi.rao@pipms.com');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'meenakshi.rao@pipms.com' AND r.name = 'ROLE_DOCTOR'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur
      WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

INSERT INTO doctor_profiles
    (user_id, license_number, registration_council, specialization, qualification,
     verified, controlled_substance_authorized, active, license_expiry_date, created_at, updated_at)
SELECT
    u.id, 'TNMC-77821', 'Tamil Nadu Medical Council', 'Internal Medicine', 'MBBS, MD',
    1, 0, 1, DATE_ADD(CURDATE(), INTERVAL 2 YEAR), NOW(), NOW()
FROM users u
WHERE u.email = 'meenakshi.rao@pipms.com'
  AND NOT EXISTS (SELECT 1 FROM doctor_profiles dp WHERE dp.user_id = u.id);

-- ----------------------------------------------------------------------------
-- 7. An APPROVED purchase order + items, so the GRN "New goods receipt"
--    dropdown has something to select.
-- ----------------------------------------------------------------------------
INSERT INTO purchase_orders
    (supplier_id, order_date, expected_delivery_date, status, total_value,
     approved_by_id, approval_date, delivery_terms, created_at, updated_at)
SELECT
    s.id, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'APPROVED', 0,
    (SELECT id FROM users WHERE staff_id = 'ADM001' LIMIT 1), NOW(), 'FOB destination', NOW(), NOW()
FROM suppliers s
WHERE s.supplier_name = 'Regional Pharma Co'
  AND NOT EXISTS (
      SELECT 1 FROM purchase_orders po
      WHERE po.supplier_id = s.id AND po.status = 'APPROVED' AND po.delivery_terms = 'FOB destination'
  );

INSERT INTO po_items (purchase_order_id, drug_id, ordered_quantity, unit_price, received_quantity, status, created_at, updated_at)
SELECT po.id, d.id, 500, 2.10, 0, 'PENDING', NOW(), NOW()
FROM purchase_orders po
JOIN suppliers s ON s.id = po.supplier_id AND s.supplier_name = 'Regional Pharma Co'
JOIN drugs d ON d.generic_name = 'Ibuprofen' AND d.brand_name = 'Brufen 400'
WHERE po.status = 'APPROVED' AND po.delivery_terms = 'FOB destination'
  AND NOT EXISTS (SELECT 1 FROM po_items WHERE purchase_order_id = po.id AND drug_id = d.id);

-- ============================================================================
-- Done. Restart the backend (or just refresh the frontend) and you should see:
--  - 2 more approved suppliers to pick from on the PO form
--  - 2 new inventory locations
--  - 3 new drugs (Ibuprofen, Tramadol, Cetirizine) with real stock batches
--  - Tramadol batch expiring in ~2 months (shows under Near Expiry)
--  - 2 new patients for billing/prescriptions
--  - A Doctor-role staff login: meenakshi.rao@pipms.com / Passw0rd!
--    (already has a verified DoctorProfile attached)
--  - One APPROVED purchase order ready to receive via New GRN
-- ============================================================================
