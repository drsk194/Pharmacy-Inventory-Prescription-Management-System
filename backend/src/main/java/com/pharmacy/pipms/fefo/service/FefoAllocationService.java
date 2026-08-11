package com.pharmacy.pipms.fefo.service;

import com.pharmacy.pipms.audit.service.AuditLogService;
import com.pharmacy.pipms.batch.entity.BatchStatus;
import com.pharmacy.pipms.batch.entity.DrugBatch;
import com.pharmacy.pipms.batch.repository.DrugBatchRepository;
import com.pharmacy.pipms.batch.service.BatchService;
import com.pharmacy.pipms.drug.entity.Drug;
import com.pharmacy.pipms.drug.repository.DrugRepository;
import com.pharmacy.pipms.exception.DrugNotFoundException;
import com.pharmacy.pipms.exception.ExpiredDrugException;
import com.pharmacy.pipms.exception.InsufficientStockException;
import com.pharmacy.pipms.fefo.dto.BatchAllocationResult;
import com.pharmacy.pipms.fefo.dto.FefoPlanResponse;
import com.pharmacy.pipms.inventory.entity.MovementType;
import com.pharmacy.pipms.inventory.service.StockMovementService;
import com.pharmacy.pipms.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Central FEFO (First Expiry First Out) allocation engine.
 * Module 11 (Dispensing) is expected to call consume(...) directly as the
 * real production entry point once prescriptions exist. The controller in
 * this module exposes the same methods over HTTP purely so this algorithm
 * can be verified independently before Module 11 is built.
 */
@Service
@RequiredArgsConstructor
public class FefoAllocationService {

    private final DrugBatchRepository batchRepository;
    private final DrugRepository drugRepository;
    private final BatchService batchService;
    private final StockMovementService stockMovementService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public FefoPlanResponse planAllocation(Long drugId, BigDecimal quantityNeeded) {
        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new DrugNotFoundException("Drug not found: " + drugId));
        List<DrugBatch> eligibleBatches = batchRepository.findEligibleForDispensing(drugId);

        List<BatchAllocationResult> allocations = new ArrayList<>();
        BigDecimal remaining = quantityNeeded;

        for (DrugBatch batch : eligibleBatches) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal available = batch.getCurrentQuantity();
            BigDecimal takeFromThisBatch = available.min(remaining);

            allocations.add(new BatchAllocationResult(
                    batch.getId(), batch.getBatchNumber(), batch.getExpiryDate(),
                    takeFromThisBatch, available.subtract(takeFromThisBatch)
            ));
            remaining = remaining.subtract(takeFromThisBatch);
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            throw new InsufficientStockException(
                    "Insufficient stock for '" + drug.getGenericName() + "': short by " + remaining);
        }

        return new FefoPlanResponse(drugId, drug.getGenericName(), quantityNeeded, allocations);
    }

    /**
     * Actually consumes stock following the FEFO plan. Runs in a single
     * transaction: if any batch fails its defensive re-check (e.g. was
     * quarantined by another process between planning and execution), the
     * ENTIRE operation rolls back — including any deductions already
     * applied earlier in the same loop. This implements algorithm step 7.
     */
    @Transactional
    public FefoPlanResponse consume(Long drugId, BigDecimal quantityNeeded,
                                     String referenceType, Long referenceId, User performedBy) {
        FefoPlanResponse plan = planAllocation(drugId, quantityNeeded);

        for (BatchAllocationResult allocation : plan.getAllocations()) {
            DrugBatch batch = batchService.getBatchEntity(allocation.getBatchId());

            // Defensive re-check: the plan was computed a moment ago;
            // guard against a concurrent quarantine/expiry in between.
            if (batch.getStatus() == BatchStatus.QUARANTINED || batch.getStatus() == BatchStatus.EXPIRED) {
                throw new ExpiredDrugException(
                        "Batch " + batch.getBatchNumber() + " is no longer eligible for dispensing");
            }

            batchService.applyAdjustment(batch.getId(), allocation.getQuantityAllocated().negate());
            stockMovementService.record(batch, MovementType.DISPENSING,
                    allocation.getQuantityAllocated().negate(),
                    referenceType, referenceId, "FEFO auto-allocation", performedBy);
        }

        return plan;
    }

    /**
     * Pharmacist override — bypasses strict expiry ORDERING (dispense from
     * a specific batch chosen by the pharmacist rather than automatically
     * picking the earliest), but still enforces the unconditional
     * expired/quarantined block. Requires a reason, and writes an audit
     * log entry recording who did it and when (via BaseEntity's
     * createdAt/createdBy, captured automatically on the AuditLog row).
     */
    @Transactional
    public FefoPlanResponse consumeWithOverride(Long batchId, BigDecimal quantity, String reason,
                                                 String referenceType, Long referenceId, User performedBy) {
        DrugBatch batch = batchService.getBatchEntity(batchId);

        if (batch.getStatus() == BatchStatus.QUARANTINED || batch.getStatus() == BatchStatus.EXPIRED) {
            throw new ExpiredDrugException(
                    "Cannot dispense from an expired or quarantined batch, even with an override");
        }
        if (batch.getCurrentQuantity().compareTo(quantity) < 0) {
            throw new InsufficientStockException(
                    "Insufficient stock in the selected batch: available " + batch.getCurrentQuantity());
        }

        BigDecimal remainingAfter = batch.getCurrentQuantity().subtract(quantity);

        batchService.applyAdjustment(batch.getId(), quantity.negate());
        stockMovementService.record(batch, MovementType.DISPENSING, quantity.negate(),
                referenceType, referenceId, "PHARMACIST OVERRIDE: " + reason, performedBy);

        auditLogService.log(performedBy, "FEFO_OVERRIDE", "DrugBatch", batch.getId(),
                null,
                "Overrode FEFO ordering — dispensed " + quantity + " from batch " + batch.getBatchNumber()
                        + " instead of the earliest-eligible batch. Reason: " + reason,
                "SUCCESS", null);

        List<BatchAllocationResult> allocations = List.of(new BatchAllocationResult(
                batch.getId(), batch.getBatchNumber(), batch.getExpiryDate(), quantity, remainingAfter));

        return new FefoPlanResponse(batch.getDrug().getId(), batch.getDrug().getGenericName(), quantity, allocations);
    }
    // Used by DispensingService.prepare() to determine how much stock is
    // actually available before deciding the intended dispensed quantity
    // (supports partial dispensing without throwing).
    @Transactional(readOnly = true)
    public BigDecimal getTotalEligibleStock(Long drugId) {
        return batchRepository.findEligibleForDispensing(drugId).stream()
                .map(DrugBatch::getCurrentQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}