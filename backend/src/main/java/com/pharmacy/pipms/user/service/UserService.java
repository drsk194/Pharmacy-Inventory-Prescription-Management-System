package com.pharmacy.pipms.user.service;

import com.pharmacy.pipms.common.constants.RoleName;
import com.pharmacy.pipms.exception.UserNotFoundException;
import com.pharmacy.pipms.user.dto.UserProfileResponse;
import com.pharmacy.pipms.user.entity.Role;
import com.pharmacy.pipms.user.entity.User;
import com.pharmacy.pipms.user.repository.RoleRepository;
import com.pharmacy.pipms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

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
        return toResponse(user);
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
}