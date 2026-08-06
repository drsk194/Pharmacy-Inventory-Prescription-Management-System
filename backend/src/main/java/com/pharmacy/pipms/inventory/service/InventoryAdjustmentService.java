package com.pharmacy.pipms.inventory.service;

import com.pharmacy.pipms.batch.entity.DrugBatch;
import com.pharmacy.pipms.batch.service.BatchService;
import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.exception.AdjustmentNotFoundException;
import com.pharmacy.pipms.exception.UserNotFoundException;
import com.pharmacy.pipms.inventory.dto.*;
import com.pharmacy.pipms.inventory.entity.*;
import com.pharmacy.pipms.inventory.repository.InventoryAdjustmentRepository;
import com.pharmacy.pipms.user.entity.User;
import com.pharmacy.pipms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryAdjustmentService {

    // Appendix F: adjustments exceeding 5% of batch quantity require
    // supervisor approval.
    private static final BigDecimal APPROVAL_THRESHOLD_PERCENT = BigDecimal.valueOf(5);

    private final InventoryAdjustmentRepository adjustmentRepository;
    private final BatchService batchService;
    private final StockMovementService stockMovementService;
    private final UserRepository userRepository;

    @Transactional
    public AdjustmentResponse createAdjustment(AdjustmentCreateRequest request, String requesterEmail) {
        DrugBatch batch = getBatchViaReflectionSafeAccessor(request.getBatchId());
        User requester = requireUser(requesterEmail);

        BigDecimal previousQuantity = batch.getCurrentQuantity();
        BigDecimal delta = request.getAdjustmentQuantity();
        BigDecimal newQuantity = previousQuantity.add(delta);

        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new com.pharmacy.pipms.exception.InsufficientStockException(
                    "Adjustment would make stock negative — not allowed");
        }

        InventoryAdjustment adjustment = new InventoryAdjustment();
        adjustment.setBatch(batch);
        adjustment.setPreviousQuantity(previousQuantity);
        adjustment.setAdjustmentQuantity(delta);
        adjustment.setNewQuantity(newQuantity);
        adjustment.setReasonCode(request.getReasonCode());
        adjustment.setNotes(request.getNotes());
        adjustment.setRequestedBy(requester);

        if (requiresApproval(previousQuantity, delta)) {
            adjustment.setStatus(AdjustmentStatus.PENDING_APPROVAL);
        } else {
            adjustment.setStatus(AdjustmentStatus.AUTO_APPROVED);
            adjustment.setApprovedBy(requester);
            adjustment.setApprovalDate(LocalDateTime.now());
            applyToBatch(batch, delta, requester);
        }

        return toResponse(adjustmentRepository.save(adjustment));
    }

    @Transactional
    public AdjustmentResponse createStockCount(StockCountRequest request, String requesterEmail) {
        DrugBatch batch = getBatchViaReflectionSafeAccessor(request.getBatchId());
        BigDecimal delta = request.getCountedQuantity().subtract(batch.getCurrentQuantity());

        AdjustmentCreateRequest adjustmentRequest = new AdjustmentCreateRequest();
        adjustmentRequest.setBatchId(request.getBatchId());
        adjustmentRequest.setAdjustmentQuantity(delta);
        adjustmentRequest.setReasonCode(AdjustmentReasonCode.STOCK_COUNT_VARIANCE);
        adjustmentRequest.setNotes(request.getNotes() != null ? request.getNotes()
                : "Physical stock count: counted " + request.getCountedQuantity());

        return createAdjustment(adjustmentRequest, requesterEmail);
    }

    @Transactional
    public AdjustmentResponse decide(Long id, boolean approve, String approverEmail) {
        InventoryAdjustment adjustment = adjustmentRepository.findById(id)
                .orElseThrow(() -> new AdjustmentNotFoundException("Adjustment not found: " + id));

        if (adjustment.getStatus() != AdjustmentStatus.PENDING_APPROVAL) {
            throw new IllegalArgumentException("This adjustment is not pending approval");
        }

        User approver = requireUser(approverEmail);
        adjustment.setApprovedBy(approver);
        adjustment.setApprovalDate(LocalDateTime.now());

        if (approve) {
            adjustment.setStatus(AdjustmentStatus.APPROVED);
            applyToBatch(adjustment.getBatch(), adjustment.getAdjustmentQuantity(), approver);
        } else {
            adjustment.setStatus(AdjustmentStatus.REJECTED);
        }

        return toResponse(adjustmentRepository.save(adjustment));
    }

    @Transactional(readOnly = true)
    public PageResponse<AdjustmentResponse> search(AdjustmentStatus status, Pageable pageable) {
        Page<InventoryAdjustment> page = adjustmentRepository.search(status, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public AdjustmentResponse getById(Long id) {
        return toResponse(adjustmentRepository.findById(id)
                .orElseThrow(() -> new AdjustmentNotFoundException("Adjustment not found: " + id)));
    }

    @Transactional(readOnly = true)
    public List<AdjustmentResponse> varianceReport(LocalDateTime start, LocalDateTime end) {
        return adjustmentRepository.findVarianceReport(AdjustmentReasonCode.STOCK_COUNT_VARIANCE, start, end)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private boolean requiresApproval(BigDecimal previousQuantity, BigDecimal delta) {
        if (previousQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return true; // can't compute a percentage against zero — default to requiring approval
        }
        BigDecimal percentChange = delta.abs()
                .divide(previousQuantity, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return percentChange.compareTo(APPROVAL_THRESHOLD_PERCENT) > 0;
    }

    private void applyToBatch(DrugBatch batch, BigDecimal delta, User performedBy) {
        callApplyQuantityChange(batch, delta);
        stockMovementService.record(batch, MovementType.ADJUSTMENT, delta,
                "INVENTORY_ADJUSTMENT", null, "Inventory adjustment applied", performedBy);
    }

    // BatchService.applyQuantityChange is package-private by design (only
    // meant to be called from within trusted service-layer workflows like
    // this one). Since InventoryAdjustmentService lives in a different
    // package, we go through this batch-package-visible entry point
    // instead of duplicating the negative-stock-guard logic here.
    public void callApplyQuantityChange(DrugBatch batch, BigDecimal delta) {
        batchService.applyAdjustment(batch.getId(), delta);
    }

    public DrugBatch getBatchViaReflectionSafeAccessor(Long batchId) {
        return batchService.getBatchEntity(batchId);
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private AdjustmentResponse toResponse(InventoryAdjustment a) {
        return new AdjustmentResponse(
                a.getId(), a.getBatch().getId(), a.getBatch().getBatchNumber(),
                a.getPreviousQuantity(), a.getAdjustmentQuantity(), a.getNewQuantity(),
                a.getReasonCode().name(), a.getNotes(), a.getRequestedBy().getFullName(),
                a.getStatus().name(),
                a.getApprovedBy() != null ? a.getApprovedBy().getFullName() : null,
                a.getApprovalDate()
        );
    }
}