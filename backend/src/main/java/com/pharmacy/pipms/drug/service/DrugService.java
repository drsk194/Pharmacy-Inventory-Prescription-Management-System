package com.pharmacy.pipms.drug.service;

import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.drug.dto.*;
import com.pharmacy.pipms.drug.entity.Drug;
import com.pharmacy.pipms.drug.entity.DrugSchedule;
import com.pharmacy.pipms.drug.repository.DrugRepository;
import com.pharmacy.pipms.exception.DrugNotFoundException;
import com.pharmacy.pipms.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DrugService {

    private final DrugRepository drugRepository;
    private final com.pharmacy.pipms.audit.service.AuditLogService auditLogService;
    private final com.pharmacy.pipms.user.repository.UserRepository userRepository;

    @Transactional
    public DrugResponse createDrug(DrugCreateRequest request) {
        validateStockLevels(request.getReorderLevel(), request.getMinStockLevel(), request.getMaxStockLevel());

        if (request.getNdcCode() != null && drugRepository.existsByNdcCode(request.getNdcCode())) {
            throw new DuplicateResourceException("NDC code already exists: " + request.getNdcCode());
        }
        if (request.getBarcode() != null && drugRepository.existsByBarcode(request.getBarcode())) {
            throw new DuplicateResourceException("Barcode already exists: " + request.getBarcode());
        }

        Drug drug = new Drug();
        applyFields(drug, request.getGenericName(), request.getBrandName(), request.getNdcCode(),
                request.getDrugClass(), request.getSchedule(), request.getStorageCondition(),
                request.getUnitOfMeasure(), request.getReorderLevel(), request.getMinStockLevel(),
                request.getMaxStockLevel(), request.getBarcode(),
            request.getMaxPrescriptionQtyPerFill(), request.getMaxRefillsAllowed());
        drug.setActive(true);
        auditLogService.log(currentActor(), "DRUG_CREATED", "Drug", drug.getId(),
                null, drug.getGenericName(), "SUCCESS", null);
        return toResponse(drugRepository.save(drug));
    }

    @Transactional
    public DrugResponse updateDrug(Long id, DrugUpdateRequest request) {
        Drug drug = drugRepository.findById(id)
                .orElseThrow(() -> new DrugNotFoundException("Drug not found: " + id));

        validateStockLevels(request.getReorderLevel(), request.getMinStockLevel(), request.getMaxStockLevel());

        if (request.getNdcCode() != null && !request.getNdcCode().equals(drug.getNdcCode())
                && drugRepository.existsByNdcCode(request.getNdcCode())) {
            throw new DuplicateResourceException("NDC code already exists: " + request.getNdcCode());
        }
        if (request.getBarcode() != null && !request.getBarcode().equals(drug.getBarcode())
                && drugRepository.existsByBarcode(request.getBarcode())) {
            throw new DuplicateResourceException("Barcode already exists: " + request.getBarcode());
        }

        applyFields(drug, request.getGenericName(), request.getBrandName(), request.getNdcCode(),
                request.getDrugClass(), request.getSchedule(), request.getStorageCondition(),
                request.getUnitOfMeasure(), request.getReorderLevel(), request.getMinStockLevel(),
                request.getMaxStockLevel(), request.getBarcode(),request.getMaxPrescriptionQtyPerFill(), request.getMaxRefillsAllowed());
        auditLogService.log(currentActor(), "DRUG_UPDATED", "Drug", drug.getId(),
                null, drug.getGenericName(), "SUCCESS", null);
        return toResponse(drugRepository.save(drug));
    }
    
    @Transactional(readOnly = true)
    public DrugResponse getDrugById(Long id) {
        return toResponse(drugRepository.findById(id)
                .orElseThrow(() -> new DrugNotFoundException("Drug not found: " + id)));
    }

    @Transactional(readOnly = true)
    public PageResponse<DrugResponse> searchDrugs(String search, String drugClass, DrugSchedule schedule,
                                                    boolean activeOnly, Pageable pageable) {
        Page<Drug> page = drugRepository.search(search, drugClass, schedule, activeOnly, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<DrugCatalogResponse> getPublicCatalog(Pageable pageable) {
        Page<Drug> page = drugRepository.findByActiveTrue(pageable);
        return PageResponse.from(page.map(this::toCatalogResponse));
    }

    @Transactional
    public DrugResponse setActive(Long id, boolean active) {
        Drug drug = drugRepository.findById(id)
                .orElseThrow(() -> new DrugNotFoundException("Drug not found: " + id));
        drug.setActive(active);
        auditLogService.log(currentActor(), "DRUG_STATUS_CHANGED", "Drug", drug.getId(),
                null, "active=" + active, "SUCCESS", null);
        return toResponse(drugRepository.save(drug));
    }

    // Business rule (Appendix C / Section 9): reorder level must be
    // non-negative and less than max stock, when a max is configured.
    private void validateStockLevels(Integer reorderLevel, Integer minStockLevel, Integer maxStockLevel) {
        if (maxStockLevel != null && reorderLevel != null && reorderLevel >= maxStockLevel) {
            throw new IllegalArgumentException("Reorder level must be less than maximum stock level");
        }
        if (maxStockLevel != null && minStockLevel != null && minStockLevel > maxStockLevel) {
            throw new IllegalArgumentException("Minimum stock level cannot exceed maximum stock level");
        }
    }

    private void applyFields(Drug drug, String genericName, String brandName, String ndcCode, String drugClass,
                              com.pharmacy.pipms.drug.entity.DrugSchedule schedule,
                              com.pharmacy.pipms.drug.entity.StorageCondition storageCondition,
                              String unitOfMeasure, Integer reorderLevel, Integer minStockLevel,
                              Integer maxStockLevel, String barcode,
                              Integer maxPrescriptionQtyPerFill, Integer maxRefillsAllowed) {
        drug.setGenericName(genericName);
        drug.setBrandName(brandName);
        drug.setNdcCode(ndcCode);
        drug.setDrugClass(drugClass);
        drug.setSchedule(schedule);
        drug.setStorageCondition(storageCondition);
        drug.setUnitOfMeasure(unitOfMeasure);
        drug.setReorderLevel(reorderLevel);
        drug.setMinStockLevel(minStockLevel);
        drug.setMaxStockLevel(maxStockLevel);
        drug.setBarcode(barcode);
        drug.setMaxPrescriptionQtyPerFill(maxPrescriptionQtyPerFill);
        drug.setMaxRefillsAllowed(maxRefillsAllowed);
    }

    private DrugResponse toResponse(Drug d) {
        return new DrugResponse(
                d.getId(), d.getGenericName(), d.getBrandName(), d.getNdcCode(), d.getDrugClass(),
                d.getSchedule().name(), d.getStorageCondition().name(), d.getUnitOfMeasure(),
                d.getReorderLevel(), d.getMinStockLevel(), d.getMaxStockLevel(), d.getBarcode(), d.isActive(),
                d.getMaxPrescriptionQtyPerFill(), d.getMaxRefillsAllowed()
        );
    }

    private DrugCatalogResponse toCatalogResponse(Drug d) {
        return new DrugCatalogResponse(
                d.getId(), d.getGenericName(), d.getBrandName(), d.getDrugClass(),
                d.getSchedule().name(), d.getStorageCondition().name(), d.getUnitOfMeasure()
        );
    }
    private com.pharmacy.pipms.user.entity.User currentActor() {
        String email = com.pharmacy.pipms.audit.util.CurrentActorUtil.getCurrentUserEmail();
        return email != null ? userRepository.findByEmail(email).orElse(null) : null;
    }
}