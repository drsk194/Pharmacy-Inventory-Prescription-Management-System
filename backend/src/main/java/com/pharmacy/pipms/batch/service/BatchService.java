package com.pharmacy.pipms.batch.service;

import com.pharmacy.pipms.batch.dto.BatchCreateRequest;
import com.pharmacy.pipms.batch.dto.BatchResponse;
import com.pharmacy.pipms.batch.entity.BatchStatus;
import com.pharmacy.pipms.batch.entity.DrugBatch;
import com.pharmacy.pipms.batch.repository.DrugBatchRepository;
import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.drug.entity.Drug;
import com.pharmacy.pipms.drug.repository.DrugRepository;
import com.pharmacy.pipms.exception.BatchNotFoundException;
import com.pharmacy.pipms.exception.DrugNotFoundException;
import com.pharmacy.pipms.exception.DuplicateResourceException;
import com.pharmacy.pipms.exception.SupplierNotFoundException;
import com.pharmacy.pipms.inventory.entity.InventoryLocation;
import com.pharmacy.pipms.inventory.entity.MovementType;
import com.pharmacy.pipms.inventory.service.InventoryBalanceService;
import com.pharmacy.pipms.inventory.service.InventoryLocationService;
import com.pharmacy.pipms.inventory.service.StockMovementService;
import com.pharmacy.pipms.supplier.entity.Supplier;
import com.pharmacy.pipms.supplier.repository.SupplierRepository;
import com.pharmacy.pipms.user.entity.User;
import com.pharmacy.pipms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;        
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pharmacy.pipms.notification.service.NotificationService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BatchService {

    private static final int MIN_SHELF_LIFE_MONTHS = 6;

    private final DrugBatchRepository batchRepository;
    private final DrugRepository drugRepository;
    private final SupplierRepository supplierRepository;
    private final NotificationService notificationService;
    private final InventoryLocationService locationService;
    private final InventoryBalanceService balanceService;
    private final StockMovementService stockMovementService;
    private final UserRepository userRepository;

    @Transactional
    public BatchResponse createBatch(BatchCreateRequest request, Authentication authentication) {
        Drug drug = drugRepository.findById(request.getDrugId())
                .orElseThrow(() -> new DrugNotFoundException("Drug not found: " + request.getDrugId()));
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found: " + request.getSupplierId()));
        InventoryLocation location = locationService.getEntityById(request.getLocationId());

        if (batchRepository.existsByDrugIdAndBatchNumber(request.getDrugId(), request.getBatchNumber())) {
            throw new DuplicateResourceException(
                    "Batch number already exists for this drug: " + request.getBatchNumber());
        }
        enforceShelfLifeRule(request.getExpiryDate(), request.getShortShelfLifeOverrideReason(), authentication);

        return persistBatch(drug, request.getBatchNumber(), request.getManufacturingDate(), request.getExpiryDate(),
                supplier, request.getQuantityReceived(), request.getPurchasePrice(), request.getMrp(),
                location, null, authentication);
    }
    @Transactional
    public BatchResponse createBatchFromGrn(Drug drug, String batchNumber, java.time.LocalDate manufacturingDate,
                                             java.time.LocalDate expiryDate, Supplier supplier,
                                             java.math.BigDecimal quantityReceived, java.math.BigDecimal purchasePrice,
                                             java.math.BigDecimal mrp, InventoryLocation location, Long grnId,
                                             String shortShelfLifeOverrideReason, Authentication authentication) {
        if (batchRepository.existsByDrugIdAndBatchNumber(drug.getId(), batchNumber)) {
            throw new DuplicateResourceException("Batch number already exists for this drug: " + batchNumber);
        }
        enforceShelfLifeRule(expiryDate, shortShelfLifeOverrideReason, authentication);

        return persistBatch(drug, batchNumber, manufacturingDate, expiryDate, supplier,
                quantityReceived, purchasePrice, mrp, location, grnId, authentication);
    }

    private BatchResponse persistBatch(Drug drug, String batchNumber, java.time.LocalDate manufacturingDate,
                                        java.time.LocalDate expiryDate, Supplier supplier,
                                        java.math.BigDecimal quantityReceived, java.math.BigDecimal purchasePrice,
                                        java.math.BigDecimal mrp, InventoryLocation location, Long grnId,
                                        Authentication authentication) {
        DrugBatch batch = new DrugBatch();
        batch.setDrug(drug);
        batch.setBatchNumber(batchNumber);
        batch.setManufacturingDate(manufacturingDate);
        batch.setExpiryDate(expiryDate);
        batch.setSupplier(supplier);
        batch.setQuantityReceived(quantityReceived);
        batch.setCurrentQuantity(quantityReceived);
        batch.setPurchasePrice(purchasePrice);
        batch.setMrp(mrp);
        batch.setLocation(location);
        batch.setGrnId(grnId);
        batch.setStatus(BatchStatus.ACTIVE);

        DrugBatch saved = batchRepository.save(batch);

        User performedBy = currentUser(authentication);
        stockMovementService.record(saved, MovementType.RECEIPT, quantityReceived,
                "BATCH_CREATION", saved.getId(), "Initial stock entry", performedBy);
        balanceService.adjust(drug, location, quantityReceived);

        return toResponse(saved);
    }

    private void enforceShelfLifeRule(LocalDate expiryDate, String overrideReason, Authentication authentication) {
        LocalDate minimumAcceptable = LocalDate.now().plusMonths(MIN_SHELF_LIFE_MONTHS);
        if (!expiryDate.isBefore(minimumAcceptable)) {
            return; // shelf life is fine, no override needed
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (overrideReason == null || overrideReason.isBlank()) {
            throw new IllegalArgumentException(
                    "Batch has less than 6 months shelf life remaining and cannot be accepted without an override reason");
        }
        if (!isAdmin) {
            throw new IllegalArgumentException(
                    "Only an administrator may override the minimum shelf-life requirement");
        }
    }

    @Transactional(readOnly = true)
    public BatchResponse getById(Long id) {
        return toResponse(batchRepository.findById(id)
                .orElseThrow(() -> new BatchNotFoundException("Batch not found: " + id)));
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> getByDrug(Long drugId) {
        return batchRepository.findByDrugIdOrderByExpiryDateAsc(drugId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<BatchResponse> search(BatchStatus status, Long locationId, Pageable pageable) {
        Page<DrugBatch> page = batchRepository.search(status, locationId, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<BatchResponse> getExpired(Pageable pageable) {
        return PageResponse.from(batchRepository.findByStatusOrderByExpiryDateAsc(BatchStatus.EXPIRED, pageable)
                .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<BatchResponse> getQuarantined(Pageable pageable) {
        return PageResponse.from(batchRepository.findByStatusOrderByExpiryDateAsc(BatchStatus.QUARANTINED, pageable)
                .map(this::toResponse));
    }

    @Transactional
    public BatchResponse quarantine(Long id, String reason, Authentication authentication) {
        DrugBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new BatchNotFoundException("Batch not found: " + id));
        batch.setStatus(BatchStatus.QUARANTINED);
        DrugBatch saved = batchRepository.save(batch);
        stockMovementService.record(saved, MovementType.QUARANTINE, BigDecimal.ZERO,
                "MANUAL_QUARANTINE", null, reason, currentUser(authentication));
        return toResponse(saved);
    }

    // ---- Used by Module 6's deferred low-stock/near-expiry endpoints ----

    @Transactional(readOnly = true)
    public boolean isLowStock(Drug drug) {
        BigDecimal totalActive = batchRepository.sumActiveQuantityByDrug(drug.getId());
        return totalActive.compareTo(BigDecimal.valueOf(drug.getReorderLevel())) <= 0;
    }

    @Transactional(readOnly = true)
    public List<Long> findExpiringDrugIds(int days) {
        LocalDate threshold = LocalDate.now().plusDays(days);
        return batchRepository.findExpiringBy(threshold).stream()
                .map(b -> b.getDrug().getId()).distinct().collect(Collectors.toList());
    }

    // ---- Used by BatchAutoQuarantineJob and its manual-trigger endpoint ----

    @Transactional
    public int runExpiryCheck() {
        int count = 0;
        LocalDate today = LocalDate.now();

        for (DrugBatch batch : batchRepository.findNewlyExpired(today)) {
            batch.setStatus(BatchStatus.QUARANTINED);
            batchRepository.save(batch);
            stockMovementService.record(batch, MovementType.QUARANTINE, BigDecimal.ZERO,
                    "AUTO_EXPIRY_QUARANTINE", null, "Automatically quarantined: past expiry date", null);

            notificationService.notifyRoles(
                    java.util.Set.of(com.pharmacy.pipms.common.constants.RoleName.ROLE_PHARMACIST,
                            com.pharmacy.pipms.common.constants.RoleName.ROLE_ADMIN),
                    com.pharmacy.pipms.notification.entity.NotificationType.EXPIRED_STOCK,
                    com.pharmacy.pipms.notification.entity.NotificationPriority.CRITICAL,
                    "Batch " + batch.getBatchNumber() + " of '" + batch.getDrug().getGenericName()
                            + "' has expired and was automatically quarantined",
                    "DrugBatch", batch.getId());
            count++;
        }

        LocalDate nearExpiryThreshold = today.plusDays(90);
        for (DrugBatch batch : batchRepository.findExpiringBy(nearExpiryThreshold)) {
            if (batch.getStatus() == BatchStatus.ACTIVE) {
                batch.setStatus(BatchStatus.NEAR_EXPIRY);
                batchRepository.save(batch);

                notificationService.notifyRoles(
                        java.util.Set.of(com.pharmacy.pipms.common.constants.RoleName.ROLE_PHARMACIST),
                        com.pharmacy.pipms.notification.entity.NotificationType.NEAR_EXPIRY,
                        com.pharmacy.pipms.notification.entity.NotificationPriority.MEDIUM,
                        "Batch " + batch.getBatchNumber() + " of '" + batch.getDrug().getGenericName()
                                + "' expires on " + batch.getExpiryDate(),
                        "DrugBatch", batch.getId());
                count++;
            }
        }
        return count;
    }

    // Package-private — used by InventoryAdjustmentService to apply
    // approved/auto-approved quantity changes to the actual batch.
    void applyQuantityChange(DrugBatch batch, BigDecimal delta) {
        BigDecimal newQuantity = batch.getCurrentQuantity().add(delta);
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new com.pharmacy.pipms.exception.InsufficientStockException(
                    "Adjustment would make stock negative — not allowed");
        }
        batch.setCurrentQuantity(newQuantity);
        if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
            batch.setStatus(BatchStatus.EXHAUSTED);
        }
        batchRepository.save(batch);
        balanceService.adjust(batch.getDrug(), batch.getLocation(), delta);
    }

    DrugBatch getEntityById(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new BatchNotFoundException("Batch not found: " + id));
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null) return null;
        return userRepository.findByEmail(authentication.getName()).orElse(null);
    }

    private BatchResponse toResponse(DrugBatch b) {
        return new BatchResponse(
                b.getId(), b.getDrug().getId(), b.getDrug().getGenericName(), b.getBatchNumber(),
                b.getManufacturingDate(), b.getExpiryDate(), b.getSupplier().getId(), b.getSupplier().getSupplierName(),
                b.getQuantityReceived(), b.getCurrentQuantity(), b.getPurchasePrice(), b.getMrp(),
                b.getStatus().name(), b.getLocation().getId(), b.getLocation().getName()
        );
    }
    // Public entry points for InventoryAdjustmentService (different package)
    public DrugBatch getBatchEntity(Long id) {
        return getEntityById(id);
    }

    public void applyAdjustment(Long batchId, BigDecimal delta) {
        applyQuantityChange(getEntityById(batchId), delta);
    }
    
}