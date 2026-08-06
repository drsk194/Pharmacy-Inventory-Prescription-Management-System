package com.pharmacy.pipms.supplier.service;

import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.exception.DuplicateResourceException;
import com.pharmacy.pipms.exception.SupplierNotFoundException;
import com.pharmacy.pipms.supplier.dto.SupplierCreateRequest;
import com.pharmacy.pipms.supplier.dto.SupplierResponse;
import com.pharmacy.pipms.supplier.dto.SupplierUpdateRequest;
import com.pharmacy.pipms.supplier.entity.Supplier;
import com.pharmacy.pipms.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Transactional
    public SupplierResponse createSupplier(SupplierCreateRequest request) {
        if (supplierRepository.existsByDrugLicenseNumber(request.getDrugLicenseNumber())) {
            throw new DuplicateResourceException("Drug license number already registered: " + request.getDrugLicenseNumber());
        }
        if (request.getEmail() != null && supplierRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        Supplier supplier = new Supplier();
        applyFields(supplier, request.getSupplierName(), request.getContactPerson(), request.getPhone(),
                request.getEmail(), request.getAddress(), request.getDrugLicenseNumber(),
                request.getCreditTerms(), request.getRating());
        supplier.setApproved(false);
        supplier.setActive(true);

        return toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse updateSupplier(Long id, SupplierUpdateRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found: " + id));

        if (!request.getDrugLicenseNumber().equals(supplier.getDrugLicenseNumber())
                && supplierRepository.existsByDrugLicenseNumber(request.getDrugLicenseNumber())) {
            throw new DuplicateResourceException("Drug license number already registered: " + request.getDrugLicenseNumber());
        }
        if (request.getEmail() != null && !request.getEmail().equals(supplier.getEmail())
                && supplierRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        applyFields(supplier, request.getSupplierName(), request.getContactPerson(), request.getPhone(),
                request.getEmail(), request.getAddress(), request.getDrugLicenseNumber(),
                request.getCreditTerms(), request.getRating());

        return toResponse(supplierRepository.save(supplier));
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long id) {
        return toResponse(supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found: " + id)));
    }

    @Transactional(readOnly = true)
    public PageResponse<SupplierResponse> searchSuppliers(String search, boolean approvedOnly,
                                                            boolean activeOnly, Pageable pageable) {
        Page<Supplier> page = supplierRepository.search(search, approvedOnly, activeOnly, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional
    public SupplierResponse setActive(Long id, boolean active) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found: " + id));
        supplier.setActive(active);
        return toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse setApproved(Long id, boolean approved) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found: " + id));
        supplier.setApproved(approved);
        return toResponse(supplierRepository.save(supplier));
    }

    private void applyFields(Supplier supplier, String name, String contactPerson, String phone,
                              String email, String address, String drugLicenseNumber,
                              String creditTerms, Double rating) {
        supplier.setSupplierName(name);
        supplier.setContactPerson(contactPerson);
        supplier.setPhone(phone);
        supplier.setEmail(email);
        supplier.setAddress(address);
        supplier.setDrugLicenseNumber(drugLicenseNumber);
        supplier.setCreditTerms(creditTerms);
        supplier.setRating(rating);
    }

    private SupplierResponse toResponse(Supplier s) {
        return new SupplierResponse(
                s.getId(), s.getSupplierName(), s.getContactPerson(), s.getPhone(), s.getEmail(),
                s.getAddress(), s.getDrugLicenseNumber(), s.getCreditTerms(), s.getRating(),
                s.isApproved(), s.isActive()
        );
    }
}