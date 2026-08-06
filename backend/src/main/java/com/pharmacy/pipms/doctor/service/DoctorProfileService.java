package com.pharmacy.pipms.doctor.service;

import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.doctor.dto.*;
import com.pharmacy.pipms.doctor.entity.DoctorProfile;
import com.pharmacy.pipms.doctor.repository.DoctorProfileRepository;
import com.pharmacy.pipms.exception.DoctorNotFoundException;
import com.pharmacy.pipms.exception.DuplicateResourceException;
import com.pharmacy.pipms.exception.UserNotFoundException;
import com.pharmacy.pipms.user.entity.User;
import com.pharmacy.pipms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoctorProfileService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public DoctorProfileResponse createProfile(DoctorCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + request.getUserId()));

        if (doctorProfileRepository.findByUser(user).isPresent()) {
            throw new DuplicateResourceException("This user already has a doctor profile");
        }

        DoctorProfile profile = new DoctorProfile();
        profile.setUser(user);
        profile.setLicenseNumber(request.getLicenseNumber());
        profile.setRegistrationCouncil(request.getRegistrationCouncil());
        profile.setSpecialization(request.getSpecialization());
        profile.setQualification(request.getQualification());
        profile.setVerified(false);
        profile.setControlledSubstanceAuthorized(false);
        profile.setActive(true);

        return toResponse(doctorProfileRepository.save(profile));
    }

    /**
     * Called from AuthService.register() when a new user registers with
     * ROLE_DOCTOR, so every doctor account gets a linked profile
     * automatically, pending admin verification.
     */
    @Transactional
    public DoctorProfile createStubProfileForUser(User user, String licenseNumber) {
        DoctorProfile profile = new DoctorProfile();
        profile.setUser(user);
        profile.setLicenseNumber(licenseNumber);
        profile.setVerified(false);
        profile.setControlledSubstanceAuthorized(false);
        profile.setActive(true);
        return doctorProfileRepository.save(profile);
    }

    @Transactional
    public DoctorProfileResponse updateProfile(Long id, DoctorProfileUpdateRequest request) {
        DoctorProfile profile = doctorProfileRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor profile not found: " + id));

        profile.setLicenseNumber(request.getLicenseNumber());
        profile.setRegistrationCouncil(request.getRegistrationCouncil());
        profile.setSpecialization(request.getSpecialization());
        profile.setQualification(request.getQualification());

        return toResponse(doctorProfileRepository.save(profile));
    }

    @Transactional
    public DoctorProfileResponse selfUpdate(String email, DoctorSelfUpdateRequest request) {
        DoctorProfile profile = doctorProfileRepository.findByUser(requireUser(email))
                .orElseThrow(() -> new DoctorNotFoundException("No doctor profile linked to this account"));

        if (request.getRegistrationCouncil() != null) profile.setRegistrationCouncil(request.getRegistrationCouncil());
        if (request.getSpecialization() != null) profile.setSpecialization(request.getSpecialization());
        if (request.getQualification() != null) profile.setQualification(request.getQualification());

        return toResponse(doctorProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public DoctorProfileResponse getMyProfile(String email) {
        DoctorProfile profile = doctorProfileRepository.findByUser(requireUser(email))
                .orElseThrow(() -> new DoctorNotFoundException("No doctor profile linked to this account"));
        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    public DoctorProfileResponse getProfileById(Long id, String requesterEmail, boolean hasFullAccess) {
        DoctorProfile profile = doctorProfileRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor profile not found: " + id));

        if (!hasFullAccess) {
            User requester = requireUser(requesterEmail);
            if (!profile.getUser().getId().equals(requester.getId())) {
                throw new AccessDeniedException("You may only view your own doctor profile");
            }
        }
        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    public PageResponse<DoctorProfileResponse> searchProfiles(String search, Pageable pageable) {
        Page<DoctorProfile> page = doctorProfileRepository.search(search, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional
    public DoctorProfileResponse setActive(Long id, boolean active) {
        DoctorProfile profile = doctorProfileRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor profile not found: " + id));
        profile.setActive(active);
        return toResponse(doctorProfileRepository.save(profile));
    }

    @Transactional
    public DoctorProfileResponse setVerified(Long id, boolean verified) {
        DoctorProfile profile = doctorProfileRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor profile not found: " + id));
        profile.setVerified(verified);
        return toResponse(doctorProfileRepository.save(profile));
    }

    @Transactional
    public DoctorProfileResponse setControlledSubstanceAuthorization(Long id, ControlledSubstanceAuthorizationRequest request) {
        DoctorProfile profile = doctorProfileRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor profile not found: " + id));

        boolean authorized = Boolean.TRUE.equals(request.getAuthorized());

        if (authorized && (request.getAuthNumber() == null || request.getAuthNumber().isBlank())) {
            throw new IllegalArgumentException(
                    "An authorization number is required when granting controlled-substance prescribing authority");
        }

        profile.setControlledSubstanceAuthorized(authorized);
        profile.setControlledSubstanceAuthNumber(authorized ? request.getAuthNumber() : null);

        return toResponse(doctorProfileRepository.save(profile));
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private DoctorProfileResponse toResponse(DoctorProfile p) {
        return new DoctorProfileResponse(
                p.getId(),
                p.getUser().getId(),
                p.getUser().getFullName(),
                p.getUser().getEmail(),
                p.getLicenseNumber(),
                p.getRegistrationCouncil(),
                p.getSpecialization(),
                p.getQualification(),
                p.isVerified(),
                p.isControlledSubstanceAuthorized(),
                p.isActive()
        );
    }
}