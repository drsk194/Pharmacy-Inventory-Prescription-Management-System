package com.pharmacy.pipms.purchaseorder.service;

import com.pharmacy.pipms.batch.repository.DrugBatchRepository;
import com.pharmacy.pipms.batch.service.BatchService;
import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.config.ProcurementProperties;
import com.pharmacy.pipms.drug.entity.Drug;
import com.pharmacy.pipms.drug.repository.DrugRepository;
import com.pharmacy.pipms.exception.*;
import com.pharmacy.pipms.purchaseorder.dto.*;
import com.pharmacy.pipms.purchaseorder.entity.*;
import com.pharmacy.pipms.purchaseorder.repository.PurchaseOrderItemRepository;
import com.pharmacy.pipms.purchaseorder.repository.PurchaseOrderRepository;
import com.pharmacy.pipms.supplier.entity.Supplier;
import com.pharmacy.pipms.supplier.repository.SupplierRepository;
import com.pharmacy.pipms.user.entity.User;
import com.pharmacy.pipms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pharmacy.pipms.notification.service.NotificationService;
import com.pharmacy.pipms.audit.service.AuditLogService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository itemRepository;
    private final SupplierRepository supplierRepository;
    private final DrugRepository drugRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final DrugBatchRepository drugBatchRepository;
    private final BatchService batchService;
    private final UserRepository userRepository;
    private final ProcurementProperties procurementProperties;

    @Transactional
    public PurchaseOrderResponse create(PurchaseOrderCreateRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found: " + request.getSupplierId()));

        // Appendix F: only approved suppliers with valid licenses may
        // receive POs — enforced here, at draft creation.
        if (!supplier.isApproved() || !supplier.isActive()) {
            throw new IllegalArgumentException(
                    "Supplier '" + supplier.getSupplierName() + "' is not approved and active — cannot issue a purchase order");
        }

        PurchaseOrder po = new PurchaseOrder();
        po.setSupplier(supplier);
        po.setOrderDate(LocalDate.now());
        po.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        po.setDeliveryTerms(request.getDeliveryTerms());
        po.setStatus(PurchaseOrderStatus.DRAFT);

        for (PurchaseOrderItemRequest itemRequest : request.getItems()) {
            po.getItems().add(buildItem(po, itemRequest));
        }
        recomputeTotal(po);

        return toResponse(purchaseOrderRepository.save(po));
    }

    @Transactional
    public PurchaseOrderResponse addItem(Long poId, PurchaseOrderItemRequest request) {
        PurchaseOrder po = requireDraft(poId);
        po.getItems().add(buildItem(po, request));
        recomputeTotal(po);
        return toResponse(purchaseOrderRepository.save(po));
    }

    @Transactional
    public PurchaseOrderResponse updateItem(Long poId, Long itemId, PurchaseOrderItemRequest request) {
        PurchaseOrder po = requireDraft(poId);
        PurchaseOrderItem item = po.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found on this purchase order: " + itemId));

        Drug drug = drugRepository.findById(request.getDrugId())
                .orElseThrow(() -> new DrugNotFoundException("Drug not found: " + request.getDrugId()));
        item.setDrug(drug);
        item.setOrderedQuantity(request.getOrderedQuantity());
        item.setUnitPrice(request.getUnitPrice());

        recomputeTotal(po);
        return toResponse(purchaseOrderRepository.save(po));
    }

    @Transactional
    public PurchaseOrderResponse removeItem(Long poId, Long itemId) {
        PurchaseOrder po = requireDraft(poId);
        boolean removed = po.getItems().removeIf(i -> i.getId().equals(itemId));
        if (!removed) {
            throw new ResourceNotFoundException("Item not found on this purchase order: " + itemId);
        }
        if (po.getItems().isEmpty()) {
            throw new IllegalArgumentException("A purchase order must retain at least one item");
        }
        recomputeTotal(po);
        return toResponse(purchaseOrderRepository.save(po));
    }

    @Transactional
    public PurchaseOrderResponse submit(Long id, String submitterEmail) {
        PurchaseOrder po = requireDraft(id);
        User submitter = requireUser(submitterEmail);

        recomputeTotal(po);

        if (po.getTotalValue().compareTo(procurementProperties.getApprovalThreshold()) <= 0) {
            notificationService.notifyByCreatedByEmail(po.getCreatedBy(),
                    com.pharmacy.pipms.notification.entity.NotificationType.PURCHASE_ORDER_APPROVAL,
                    com.pharmacy.pipms.notification.entity.NotificationPriority.MEDIUM,
                    "Purchase order #" + po.getId() + " was auto-approved (under threshold)",
                    "PurchaseOrder", po.getId());
            po.setStatus(PurchaseOrderStatus.APPROVED);
            po.setApprovedBy(submitter);
            po.setApprovalDate(LocalDateTime.now());
        } else {
            po.setStatus(PurchaseOrderStatus.SUBMITTED);
        }
        auditLogService.log(submitter, "PURCHASE_ORDER_AUTO_APPROVED", "PurchaseOrder", po.getId(),
                    null, "totalValue=" + po.getTotalValue(), "SUCCESS", null);
        return toResponse(purchaseOrderRepository.save(po));
    }

    @Transactional
    public PurchaseOrderResponse approve(Long id, String approverEmail) {
        PurchaseOrder po = getEntity(id);
        if (po.getStatus() != PurchaseOrderStatus.SUBMITTED) {
            throw new InvalidPurchaseOrderStatusException(
                    "Only SUBMITTED purchase orders can be approved (current: " + po.getStatus() + ")");
        }
        User approver = requireUser(approverEmail);
        po.setStatus(PurchaseOrderStatus.APPROVED);
        po.setApprovedBy(approver);
        po.setApprovalDate(LocalDateTime.now());
        notificationService.notifyByCreatedByEmail(po.getCreatedBy(),
                com.pharmacy.pipms.notification.entity.NotificationType.PURCHASE_ORDER_APPROVAL,
                com.pharmacy.pipms.notification.entity.NotificationPriority.MEDIUM,
                "Purchase order #" + po.getId() + " has been approved", "PurchaseOrder", po.getId());
                auditLogService.log(approver, "PURCHASE_ORDER_APPROVED", "PurchaseOrder", po.getId(), null, null, "SUCCESS", null);
        return toResponse(purchaseOrderRepository.save(po));
    }

    @Transactional
    public PurchaseOrderResponse reject(Long id, RejectPurchaseOrderRequest request) {
        PurchaseOrder po = getEntity(id);
        if (po.getStatus() != PurchaseOrderStatus.SUBMITTED) {
            throw new InvalidPurchaseOrderStatusException(
                    "Only SUBMITTED purchase orders can be rejected (current: " + po.getStatus() + ")");
        }
        po.setStatus(PurchaseOrderStatus.REJECTED);
        po.setRejectionReason(request.getReason());
        auditLogService.log(null, "PURCHASE_ORDER_REJECTED", "PurchaseOrder", po.getId(),
                null, request.getReason(), "SUCCESS", null);
        return toResponse(purchaseOrderRepository.save(po));
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<PurchaseOrderResponse> search(PurchaseOrderStatus status, Long supplierId, Pageable pageable) {
        Page<PurchaseOrder> page = purchaseOrderRepository.search(status, supplierId, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<ReorderSuggestionResponse> getReorderSuggestions() {
        return drugRepository.findAll().stream()
                .filter(Drug::isActive)
                .filter(batchService::isLowStock)
                .map(this::toSuggestion)
                .collect(Collectors.toList());
    }

    private ReorderSuggestionResponse toSuggestion(Drug drug) {
        BigDecimal currentStock = drugBatchRepository.sumActiveQuantityByDrug(drug.getId());
        int targetLevel = drug.getMaxStockLevel() != null ? drug.getMaxStockLevel() : drug.getReorderLevel() * 2;
        BigDecimal suggested = BigDecimal.valueOf(targetLevel).subtract(currentStock);
        if (suggested.compareTo(BigDecimal.ZERO) < 0) {
            suggested = BigDecimal.ZERO;
        }
        return new ReorderSuggestionResponse(drug.getId(), drug.getGenericName(), currentStock,
                drug.getReorderLevel(), targetLevel, suggested);
    }

    @Transactional(readOnly = true)
    public List<PriceComparisonResponse> getPriceComparison(Long drugId) {
        if (!drugRepository.existsById(drugId)) {
            throw new DrugNotFoundException("Drug not found: " + drugId);
        }
        List<Object[]> rows = drugBatchRepository.getPriceComparisonForDrug(drugId);
        return rows.stream()
                .map(row -> new PriceComparisonResponse(
                        (Long) row[0],
                        (String) row[1],
                        ((Number) row[2]).equals(0) ? BigDecimal.ZERO : new BigDecimal(row[2].toString()),
                        (LocalDateTime) row[3]))
                .collect(Collectors.toList());
    }

    private PurchaseOrderItem buildItem(PurchaseOrder po, PurchaseOrderItemRequest request) {
        Drug drug = drugRepository.findById(request.getDrugId())
                .orElseThrow(() -> new DrugNotFoundException("Drug not found: " + request.getDrugId()));

        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setPurchaseOrder(po);
        item.setDrug(drug);
        item.setOrderedQuantity(request.getOrderedQuantity());
        item.setUnitPrice(request.getUnitPrice());
        item.setStatus(PurchaseOrderItemStatus.PENDING);
        return item;
    }

    private void recomputeTotal(PurchaseOrder po) {
        BigDecimal total = po.getItems().stream()
                .map(i -> i.getOrderedQuantity().multiply(i.getUnitPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        po.setTotalValue(total);
    }

    private PurchaseOrder requireDraft(Long id) {
        PurchaseOrder po = getEntity(id);
        if (po.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new InvalidPurchaseOrderStatusException(
                    "Items can only be modified while the purchase order is in DRAFT status (current: " + po.getStatus() + ")");
        }
        return po;
    }

    public PurchaseOrder getEntity(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new PurchaseOrderNotFoundException("Purchase order not found: " + id));
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private PurchaseOrderResponse toResponse(PurchaseOrder po) {
        List<PurchaseOrderItemResponse> items = po.getItems().stream()
                .map(i -> new PurchaseOrderItemResponse(i.getId(), i.getDrug().getId(), i.getDrug().getGenericName(),
                        i.getOrderedQuantity(), i.getUnitPrice(), i.getReceivedQuantity(), i.getStatus().name()))
                .collect(Collectors.toList());

        return new PurchaseOrderResponse(
                po.getId(), po.getSupplier().getId(), po.getSupplier().getSupplierName(),
                po.getOrderDate(), po.getExpectedDeliveryDate(), po.getStatus().name(), po.getTotalValue(),
                po.getApprovedBy() != null ? po.getApprovedBy().getFullName() : null, po.getApprovalDate(),
                po.getRejectionReason(), po.getDeliveryTerms(), items
        );
    }
    // Called by GoodsReceiptService after processing a GRN — recomputes
    // whether the PO is now COMPLETED, PARTIALLY_RECEIVED, or unchanged.
    @Transactional
    public void refreshStatusAfterReceipt(Long id) {
        PurchaseOrder po = getEntity(id);

        boolean allReceived = po.getItems().stream()
                .allMatch(i -> i.getStatus() == PurchaseOrderItemStatus.RECEIVED
                        || i.getStatus() == PurchaseOrderItemStatus.CANCELLED);
        boolean anyReceived = po.getItems().stream()
                .anyMatch(i -> i.getReceivedQuantity().compareTo(BigDecimal.ZERO) > 0);

        PurchaseOrderStatus newStatus = allReceived ? PurchaseOrderStatus.COMPLETED
                : anyReceived ? PurchaseOrderStatus.PARTIALLY_RECEIVED : po.getStatus();

        if (newStatus != po.getStatus()) {
            po.setStatus(newStatus);
            purchaseOrderRepository.save(po);
        }
    }
    
}