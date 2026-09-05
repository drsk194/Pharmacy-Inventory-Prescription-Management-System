package com.pharmacy.pipms.auth.service;

import com.pharmacy.pipms.auth.dto.*;
import com.pharmacy.pipms.auth.entity.RefreshToken;
import com.pharmacy.pipms.auth.entity.TokenBlacklist;
import com.pharmacy.pipms.auth.repository.RefreshTokenRepository;
import com.pharmacy.pipms.auth.repository.TokenBlacklistRepository;
import com.pharmacy.pipms.common.constants.RoleName;
import com.pharmacy.pipms.exception.*;
import com.pharmacy.pipms.security.jwt.JwtProperties;
import com.pharmacy.pipms.security.jwt.JwtTokenProvider;
import com.pharmacy.pipms.security.userdetails.UserPrincipal;
import com.pharmacy.pipms.user.entity.PasswordResetOtp;
import com.pharmacy.pipms.user.entity.Role;
import com.pharmacy.pipms.user.entity.User;
import com.pharmacy.pipms.user.repository.PasswordResetOtpRepository;
import com.pharmacy.pipms.user.repository.RoleRepository;
import com.pharmacy.pipms.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.pharmacy.pipms.doctor.service.DoctorProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pharmacy.pipms.patient.service.PatientService;
import com.pharmacy.pipms.audit.service.AuditLogService;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PatientService patientService;
    private final DoctorProfileService doctorProfileService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Transactional
    public UserProfileLikeResult register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }
        boolean isStaffRole = request.getRole() != RoleName.ROLE_PATIENT
                && request.getRole() != RoleName.ROLE_DOCTOR;

        if (isStaffRole) {
            if (request.getStaffId() == null || request.getStaffId().isBlank()) {
                throw new IllegalArgumentException("Staff ID is required for staff accounts");
            }
            if (userRepository.existsByStaffId(request.getStaffId())) {
                throw new DuplicateResourceException("Staff ID already exists");
            }
        }
        if ((request.getRole() == RoleName.ROLE_PHARMACIST || request.getRole() == RoleName.ROLE_DOCTOR)
                && (request.getLicenseNumber() == null || request.getLicenseNumber().isBlank())) {
            throw new IllegalArgumentException("License number is required for this role");
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new IllegalStateException(
                        "Role not seeded: " + request.getRole() + " — run the role seeder first"));

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setStaffId(request.getStaffId());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setLicenseNumber(request.getLicenseNumber());
        user.getRoles().add(role);
        user.setActive(true);
        user.setPasswordChangedAt(LocalDateTime.now());
        // FR1: 60-day expiry for staff only
        if (isStaffRole) {
            user.setPasswordExpiryDate(LocalDateTime.now().plusDays(60));
        }

        User saved = userRepository.save(user);

        if (request.getRole() == RoleName.ROLE_PATIENT) {
            patientService.createStubPatientForUser(saved);
        }
        if (request.getRole() == RoleName.ROLE_DOCTOR) {
            doctorProfileService.createStubProfileForUser(saved, request.getLicenseNumber());
        }

        return new UserProfileLikeResult(saved.getId(), saved.getEmail());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        var userOpt = userRepository.findByLoginIdentifier(request.getIdentifier());
        if (userOpt.isEmpty()) {
            auditLogService.log(null, "LOGIN_FAILED", "User", null, null,
                    "No account found for identifier: " + request.getIdentifier(), "FAILURE", "Unknown identifier");
            throw new BadCredentialsException("Invalid credentials");
        }
        User user = userOpt.get();

        if (user.isAccountLocked()) {
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
                auditLogService.log(user, "LOGIN_BLOCKED", "User", user.getId(), null,
                        "Account locked until " + user.getLockedUntil(), "FAILURE", "Account locked");
                throw new AccountLockedException(
                        "Account locked until " + user.getLockedUntil() + " due to repeated failed login attempts");
            } else {
                user.setAccountLocked(false);
                user.setFailedLoginAttempts(0);
            }
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            registerFailedAttempt(user);
            auditLogService.log(user, "LOGIN_FAILED", "User", user.getId(), null,
                    null, "FAILURE", "Incorrect password");
            throw ex;
        }

        user.setFailedLoginAttempts(0);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        String refreshTokenValue = issueRefreshToken(user);

        auditLogService.log(user, "LOGIN_SUCCESS", "User", user.getId(), null, null, "SUCCESS", null);

        return new AuthResponse(
                accessToken, refreshTokenValue, "Bearer", user.getId(), user.getFullName(), user.getEmail(),
                user.getRoles().stream().map(r -> r.getName().name()).collect(java.util.stream.Collectors.toSet())
        );
    }
    private void registerFailedAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountLocked(true);
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
        }
        userRepository.save(user);
    }

    private String issueRefreshToken(User user) {
        boolean isPatient = user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ROLE_PATIENT);
        long ttl = isPatient
                ? jwtProperties.getRefreshToken().getPatient().getExpirationMs()
                : jwtProperties.getRefreshToken().getStaff().getExpirationMs();

        RefreshToken rt = new RefreshToken();
        rt.setToken(jwtTokenProvider.generateOpaqueRefreshToken());
        rt.setUser(user);
        rt.setExpiryDate(LocalDateTime.now().plusSeconds(ttl / 1000));
        rt.setRevoked(false);
        refreshTokenRepository.save(rt);
        return rt.getToken();
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken existing = refreshTokenRepository.findByTokenAndRevokedFalse(request.getRefreshToken())
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found or already revoked"));

        if (existing.getExpiryDate().isBefore(LocalDateTime.now())) {
            existing.setRevoked(true);
            refreshTokenRepository.save(existing);
            throw new InvalidRefreshTokenException("Refresh token expired — please log in again");
        }

        // Rotation: revoke old, issue new
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        User user = existing.getUser();
        UserPrincipal principal = new UserPrincipal(user);
        String newAccessToken = jwtTokenProvider.generateAccessToken(principal);
        String newRefreshToken = issueRefreshToken(user);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet())
        );
    }

    @Transactional(readOnly = true)
    public MeResponse me(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
        Set<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());
        return new MeResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getStaffId(),
                roles
        );
    }

    @Transactional
    public void logout(String accessToken, RefreshTokenRequest request) {
        if (accessToken != null && !accessToken.isBlank()) {
            try {
                Claims claims = jwtTokenProvider.parseClaims(accessToken);
                TokenBlacklist blacklist = new TokenBlacklist();
                blacklist.setJti(claims.getId());
                blacklist.setExpiresAt(claims.getExpiration().toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                tokenBlacklistRepository.save(blacklist);
                userRepository.findByEmail(claims.getSubject()).ifPresent(user ->
                        auditLogService.log(user, "LOGOUT", "User", user.getId(), null, null, "SUCCESS", null));
            } catch (Exception ignored) {
                // Ignore token parsing errors during logout
            }
        }

        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            refreshTokenRepository.findByTokenAndRevokedFalse(request.getRefreshToken())
                    .ifPresent(rt -> {
                        rt.setRevoked(true);
                        refreshTokenRepository.save(rt);
                    });
        }
    }

    @Transactional
    public void logoutAll(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        refreshTokenRepository.revokeAllForUser(user);
        // Note: existing access tokens for this user remain valid until natural expiry
        // unless individually blacklisted — acceptable given short-lived access tokens.
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        boolean isStaff = user.getRoles().stream()
                .noneMatch(r -> r.getName() == RoleName.ROLE_PATIENT || r.getName() == RoleName.ROLE_DOCTOR);
        if (isStaff) {
            user.setPasswordExpiryDate(LocalDateTime.now().plusDays(60));
        }
        userRepository.save(user);

        // Security best practice: force re-login everywhere after a password change
        refreshTokenRepository.revokeAllForUser(user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            // Keep account existence confidential: the public API returns the same
            // success response whether or not the address is registered.
            return;
        }

        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));

        PasswordResetOtp entity = new PasswordResetOtp();
        entity.setEmail(user.getEmail());
        entity.setOtpCode(otp);
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        entity.setUsed(false);
        otpRepository.save(entity);

        // MOCK: real SMS/email dispatch is Module 16. For now we log it so you can
        // test the flow locally without external credentials.
        System.out.println("[MOCK EMAIL] Password reset OTP for " + user.getEmail() + ": " + otp);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetOtp otp = otpRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("No pending reset request for this email"));

        if (otp.isUsed() || otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP expired or already used");
        }
        if (!otp.getOtpCode().equals(request.getOtpCode())) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        otp.setUsed(true);
        otpRepository.save(otp);

        refreshTokenRepository.revokeAllForUser(user);
    }

    // Small internal DTO just for the register() return value
    public record UserProfileLikeResult(Long userId, String email) {}
    @Transactional
    public void setControlledSubstancePin(String email, SetControlledSubstancePinRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setControlledSubstancePinHash(passwordEncoder.encode(request.getNewPin()));
        user.setFailedControlledSubstancePinAttempts(0);
        user.setControlledSubstancePinLockedUntil(null);
        userRepository.save(user);
    }
    @Transactional
    public UserProfileLikeResult adminCreateUser(com.pharmacy.pipms.admin.dto.AdminCreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }
        if (request.getStaffId() != null && userRepository.existsByStaffId(request.getStaffId())) {
            throw new DuplicateResourceException("Staff ID already exists");
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new IllegalStateException("Role not seeded: " + request.getRole()));

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setStaffId(request.getStaffId());
        user.setPasswordHash(passwordEncoder.encode(request.getTemporaryPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setLicenseNumber(request.getLicenseNumber());
        user.getRoles().add(role);
        user.setActive(true);
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setPasswordExpiryDate(LocalDateTime.now().plusDays(60));

        User saved = userRepository.save(user);
        if (request.getRole() == RoleName.ROLE_DOCTOR) {
            doctorProfileService.createStubProfileForUser(saved, request.getLicenseNumber());
        }
        return new UserProfileLikeResult(saved.getId(), saved.getEmail());
    }
}