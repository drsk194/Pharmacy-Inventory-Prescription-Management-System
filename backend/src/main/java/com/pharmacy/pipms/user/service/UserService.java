package com.pharmacy.pipms.user.service;

import com.pharmacy.pipms.common.constants.RoleName;
import com.pharmacy.pipms.exception.UserNotFoundException;
import com.pharmacy.pipms.user.dto.UserProfileResponse;
import com.pharmacy.pipms.user.entity.Role;
import com.pharmacy.pipms.user.entity.User;
import com.pharmacy.pipms.user.repository.RoleRepository;
import com.pharmacy.pipms.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pharmacy.pipms.audit.service.AuditLogService;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditLogService auditLogService;
        private final PasswordEncoder passwordEncoder;

    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse setActive(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        user.setActive(active);
        userRepository.save(user);
        User actor = resolveActor();
        auditLogService.log(actor, "USER_STATUS_CHANGED", "User", user.getId(), null,
                "active=" + active, "SUCCESS", null);
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse assignRoles(Long userId, Set<RoleName> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        Set<Role> newRoles = roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(() -> new IllegalStateException("Role not seeded: " + name)))
                .collect(Collectors.toSet());

        user.setRoles(newRoles);
        userRepository.save(user);
        User actor = resolveActor();
        auditLogService.log(actor, "USER_ROLES_CHANGED", "User", user.getId(),
                null, "roles=" + roleNames, "SUCCESS", null);        
        return toResponse(user);
    }

        @Transactional
        public UserProfileResponse setControlledSubstancePin(Long userId, String newPin) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
                user.setControlledSubstancePinHash(passwordEncoder.encode(newPin));
                user.setFailedControlledSubstancePinAttempts(0);
                user.setControlledSubstancePinLockedUntil(null);
                userRepository.save(user);
                User actor = resolveActor();
                auditLogService.log(actor, "CONTROLLED_SUBSTANCE_PIN_PROVISIONED", "User", user.getId(),
                                null, "Controlled-substance PIN provisioned", "SUCCESS", null);
                return toResponse(user);
        }
    @Transactional(readOnly = true)
    public com.pharmacy.pipms.common.PageResponse<com.pharmacy.pipms.admin.dto.AdminUserSummaryResponse> searchUsers(
            String search, org.springframework.data.domain.Pageable pageable) {
        return com.pharmacy.pipms.common.PageResponse.from(userRepository.search(search, pageable).map(u ->
                new com.pharmacy.pipms.admin.dto.AdminUserSummaryResponse(u.getId(), u.getFullName(), u.getEmail(),
                        u.getStaffId(), u.getRoles().stream().map(r -> r.getName().name()).collect(java.util.stream.Collectors.toSet()),
                        u.isActive(), u.isAccountLocked())));
    }

    @Transactional(readOnly = true)
    public java.util.List<com.pharmacy.pipms.admin.dto.AdminUserSummaryResponse> getActiveStaffByRoles(java.util.Set<RoleName> roles) {
        return userRepository.findActiveByRoleIn(roles).stream().map(u ->
                new com.pharmacy.pipms.admin.dto.AdminUserSummaryResponse(
                        u.getId(), u.getFullName(), u.getEmail(), u.getStaffId(),
                        u.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()),
                        u.isActive(), u.isAccountLocked()))
                .toList();
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getStaffId(),
                user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()),
                user.getPhoneNumber(),
                user.isActive(),
                user.getLicenseNumber(),
                user.isControlledSubstanceAuthorized(),
                user.getLastLogin()
        );
    }
    private User resolveActor() {
        String email = com.pharmacy.pipms.audit.util.CurrentActorUtil.getCurrentUserEmail();
        return email != null ? userRepository.findByEmail(email).orElse(null) : null;
    }
    
}