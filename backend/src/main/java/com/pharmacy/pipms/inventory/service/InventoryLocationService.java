package com.pharmacy.pipms.inventory.service;

import com.pharmacy.pipms.exception.DuplicateResourceException;
import com.pharmacy.pipms.exception.LocationNotFoundException;
import com.pharmacy.pipms.inventory.dto.LocationCreateRequest;
import com.pharmacy.pipms.inventory.dto.LocationResponse;
import com.pharmacy.pipms.inventory.entity.InventoryLocation;
import com.pharmacy.pipms.inventory.repository.InventoryLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryLocationService {

    private final InventoryLocationRepository locationRepository;

    @Transactional
    public LocationResponse create(LocationCreateRequest request) {
        if (locationRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Location name already exists: " + request.getName());
        }
        InventoryLocation location = new InventoryLocation();
        location.setName(request.getName());
        location.setType(request.getType());
        location.setDescription(request.getDescription());
        location.setActive(true);
        return toResponse(locationRepository.save(location));
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> getAll() {
        return locationRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LocationResponse getById(Long id) {
        return toResponse(locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Location not found: " + id)));
    }

    @Transactional
    public LocationResponse setActive(Long id, boolean active) {
        InventoryLocation location = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Location not found: " + id));
        location.setActive(active);
        return toResponse(locationRepository.save(location));
    }

    public InventoryLocation getEntityById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Location not found: " + id));
    }

    private LocationResponse toResponse(InventoryLocation l) {
        return new LocationResponse(l.getId(), l.getName(), l.getType().name(), l.getDescription(), l.isActive());
    }
}