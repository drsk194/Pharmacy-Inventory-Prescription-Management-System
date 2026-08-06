package com.pharmacy.pipms.config;

import com.pharmacy.pipms.common.constants.PermissionName;
import com.pharmacy.pipms.common.constants.RoleName;
import com.pharmacy.pipms.user.entity.Permission;
import com.pharmacy.pipms.user.entity.Role;
import com.pharmacy.pipms.user.repository.PermissionRepository;
import com.pharmacy.pipms.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static com.pharmacy.pipms.common.constants.PermissionName.*;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        Map<PermissionName, Permission> permissionMap = seedPermissions();
        Map<RoleName, Role> roleMap = seedRoles();
        assignPermissionsToRoles(roleMap, permissionMap);
        roleRepository.saveAll(roleMap.values());
    }

    private Map<PermissionName, Permission> seedPermissions() {
        Map<PermissionName, Permission> map = new EnumMap<>(PermissionName.class);
        for (PermissionName name : PermissionName.values()) {
            Permission permission = permissionRepository.findByName(name).orElseGet(() -> {
                Permission p = new Permission();
                p.setName(name);
                p.setDescription("Auto-seeded permission: " + name);
                return permissionRepository.save(p);
            });
            map.put(name, permission);
        }
        return map;
    }

    private Map<RoleName, Role> seedRoles() {
        Map<RoleName, Role> map = new HashMap<>();
        for (RoleName name : RoleName.values()) {
            Role role = roleRepository.findByName(name).orElseGet(() -> {
                Role r = new Role();
                r.setName(name);
                r.setDescription("Auto-seeded role: " + name);
                return r;
            });
            map.put(name, role);
        }
        return map;
    }

    /**
     * Role -> permission mapping. The SRS gives a coarse role/feature matrix
     * (Appendix A) and a separate granular permission list (Module 3) but
     * never maps one to the other directly — this mapping bridges them,
     * built from each role's detailed bullet list in Section 4.
     */
    private void assignPermissionsToRoles(Map<RoleName, Role> roles, Map<PermissionName, Permission> perms) {

        // ADMIN: everything
        assign(roles.get(RoleName.ROLE_ADMIN), perms, PermissionName.values());

        // PHARMACIST: dispensing authority, prescription verification, controlled substances
        // PHARMACIST: add PATIENT_READ_ALL, PATIENT_MANAGE
        assign(roles.get(RoleName.ROLE_PHARMACIST), perms,
                DRUG_READ, BATCH_READ, BATCH_CREATE,
                PRESCRIPTION_READ_ALL, PRESCRIPTION_PROCESS, PRESCRIPTION_VERIFY, PRESCRIPTION_REJECT,
                DISPENSING_PREPARE, DISPENSING_AUTHORIZE,
                CONTROLLED_SUBSTANCE_READ, CONTROLLED_SUBSTANCE_AUTHORIZE,
                PURCHASE_ORDER_CREATE,
                REPORT_INVENTORY, REPORT_DISPENSING,
                PATIENT_READ_ALL, PATIENT_MANAGE,
                DOCTOR_READ_ALL,
                INVENTORY_COUNT, INVENTORY_ADJUST);

        // TECHNICIAN: add PATIENT_READ_ALL, PATIENT_MANAGE
        assign(roles.get(RoleName.ROLE_PROCUREMENT_OFFICER), perms,
                DRUG_READ, BATCH_READ, BATCH_CREATE,
                SUPPLIER_READ, SUPPLIER_MANAGE,
                PURCHASE_ORDER_CREATE, GRN_CREATE,
                REPORT_PROCUREMENT,
                INVENTORY_COUNT, INVENTORY_ADJUST);

        // PROCUREMENT_OFFICER: suppliers, POs, GRN — no dispensing/prescription access
        assign(roles.get(RoleName.ROLE_PROCUREMENT_OFFICER), perms,
                DRUG_READ, BATCH_READ,
                SUPPLIER_READ, SUPPLIER_MANAGE,
                PURCHASE_ORDER_CREATE, GRN_CREATE,
                REPORT_PROCUREMENT);

        // AUDITOR: read-only across the board
        assign(roles.get(RoleName.ROLE_AUDITOR), perms,
                DRUG_READ, BATCH_READ,
                PRESCRIPTION_READ_ALL,
                CONTROLLED_SUBSTANCE_READ,
                SUPPLIER_READ,
                REPORT_INVENTORY, REPORT_DISPENSING, REPORT_PROCUREMENT, REPORT_FINANCIAL,
                AUDIT_LOG_READ);

        // DOCTOR: full patient management, so a doctor can verify identity
        // and correct/update patient records during a consultation.
        assign(roles.get(RoleName.ROLE_DOCTOR), perms,
                DRUG_READ, PRESCRIPTION_CREATE, PRESCRIPTION_READ_OWN,
                PATIENT_READ_ALL, PATIENT_MANAGE,
                DOCTOR_READ_OWN);

        // PATIENT: add PATIENT_READ_OWN
        assign(roles.get(RoleName.ROLE_PATIENT), perms,
                DRUG_READ, PRESCRIPTION_READ_OWN, BILLING_READ_OWN, PATIENT_READ_OWN);
    }

    private void assign(Role role, Map<PermissionName, Permission> permissionMap, PermissionName... names) {
        for (PermissionName name : names) {
            role.getPermissions().add(permissionMap.get(name));
        }
    }
}