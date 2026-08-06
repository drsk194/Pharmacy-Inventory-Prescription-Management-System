package com.pharmacy.pipms.user.service;

import com.pharmacy.pipms.user.dto.PermissionResponse;
import com.pharmacy.pipms.user.dto.RoleResponse;
import com.pharmacy.pipms.user.repository.PermissionRepository;
import com.pharmacy.pipms.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(role -> new RoleResponse(
                        role.getId(),
                        role.getName().name(),
                        role.getDescription(),
                        role.getPermissions().stream()
                                .map(p -> p.getName().name())
                                .collect(Collectors.toSet())
                ))
                .collect(Collectors.toList());
    }

    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> new PermissionResponse(p.getId(), p.getName().name(), p.getDescription()))
                .collect(Collectors.toList());
    }
}