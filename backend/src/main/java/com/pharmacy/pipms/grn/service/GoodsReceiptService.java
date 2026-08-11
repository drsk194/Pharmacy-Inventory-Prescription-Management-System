package com.pharmacy.pipms.grn.service;

import com.pharmacy.pipms.batch.dto.BatchResponse;
import com.pharmacy.pipms.batch.service.BatchService;
import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.drug.entity.Drug;
import com.pharmacy.pipms.exception.*;
import com.pharmacy.pipms.grn.dto.*;
import com.pharmacy.pipms.grn.entity.GoodsReceiptItem;
import com.pharmacy.pipms.grn.entity.GoodsReceiptNote;
import com.pharmacy.pipms.grn.repository.GoodsReceiptItemRepository;
import com.pharmacy.pipms.grn.repository.GoodsReceiptNoteRepository;
import com.pharmacy.pipms.inventory.entity.InventoryLocation;
import com.pharmacy.pipms.inventory.service.InventoryLocationService;
import com.pharmacy.pipms.purchaseorder.entity.PurchaseOrder;
import com.pharmacy.pipms.purchaseorder.entity.PurchaseOrderItem;
import com.pharmacy.pipms.purchaseorder.entity.PurchaseOrderItemStatus;
import com.pharmacy.pipms.purchaseorder.entity.PurchaseOrderStatus;
import com.pharmacy.pipms.purchaseorder.repository.PurchaseOrderItemRepository;
import com.pharmacy.pipms.purchaseorder.service.PurchaseOrderService;
import com.pharmacy.pipms.supplier.entity.Supplier;
import com.pharmacy.pipms.supplier.repository.SupplierRepository;
import com.pharmacy.pipms.user.entity.User;
import com.pharmacy.pipms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pharmacy.pipms.notification.service.NotificationService;
import com.pharmacy.pipms.audit.service.AuditLogService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoodsReceiptService {

    private final GoodsReceiptNoteRepository goodsReceiptNoteRepository;
    private final GoodsReceiptItemRepository goodsReceiptItemRepository;
    private final PurchaseOrderService purchaseOrderService;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final SupplierRepository supplierRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final InventoryLocationService locationService;
    private final BatchService batchService;
    private final UserRepository userRepository;

    @Transactional
    public GoodsReceiptNoteResponse create(GoodsReceiptNoteCreateRequest request, Authentication authentication) {
        PurchaseOrder po = purchaseOrderService.getEntity(request.getPurchaseOrderId());

        if (po.getStatus() != PurchaseOrderStatus.APPROVED && po.getStatus() != PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            throw new InvalidPurchaseOrderStatusException(
                    "GRN can only be processed against an APPROVED or PARTIALLY_RECEIVED purchase order (current: "
                            + po.getStatus() + ")");
        }

        enforceReceiptTimingRule(request, authentication);

        User receivedBy = requireUser(authentication.getName());

        GoodsReceiptNote grn = new GoodsReceiptNote();
        grn.setPurchaseOrder(po);
        grn.setReceivedDate(request.getReceivedDate());
        grn.setReceivedBy(receivedBy);
        grn.setGeneralNotes(request.getGeneralNotes());
        grn.setTotalItems(request.getItems().size());
        GoodsReceiptNote savedGrn = goodsReceiptNoteRepository.save(grn); // saved first to obtain an id for grnId

        List<GoodsReceiptItem> savedItems = new ArrayList<>();
        List<String> discrepancySummaries = new ArrayList<>();

        for (GoodsReceiptItemRequest itemRequest : request.getItems()) {
            PurchaseOrderItem poItem = purchaseOrderItemRepository.findById(itemRequest.getPurchaseOrderItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Purchase order item not found: " + itemRequest.getPurchaseOrderItemId()));
            if (!poItem.getPurchaseOrder().getId().equals(po.getId())) {
                throw new IllegalArgumentException(
                        "Purchase order item " + poItem.getId() + " does not belong to purchase order " + po.getId());
            }

            Drug drug = poItem.getDrug();
            Supplier supplier = po.getSupplier();
            InventoryLocation location = locationService.getEntityById(itemRequest.getLocationId());

            BigDecimal expectedRemaining = poItem.getOrderedQuantity().subtract(poItem.getReceivedQuantity());
            BigDecimal discrepancy = itemRequest.getReceivedQuantity().subtract(expectedRemaining);
            BigDecimal purchasePrice = itemRequest.getPurchasePrice() != null
                    ? itemRequest.getPurchasePrice() : poItem.getUnitPrice();

            BatchResponse createdBatch = batchService.createBatchFromGrn(
                    drug, itemRequest.getBatchNumber(), itemRequest.getManufacturingDate(), itemRequest.getExpiryDate(),
                    supplier, itemRequest.getReceivedQuantity(), purchasePrice, itemRequest.getMrp(), location,
                    savedGrn.getId(), itemRequest.getShortShelfLifeOverrideReason(), authentication);

            poItem.setReceivedQuantity(poItem.getReceivedQuantity().add(itemRequest.getReceivedQuantity()));
            poItem.setStatus(poItem.getReceivedQuantity().compareTo(poItem.getOrderedQuantity()) >= 0
                    ? PurchaseOrderItemStatus.RECEIVED : PurchaseOrderItemStatus.PARTIALLY_RECEIVED);
            purchaseOrderItemRepository.save(poItem);

            GoodsReceiptItem grnItem = new GoodsReceiptItem();
            grnItem.setGoodsReceiptNote(savedGrn);
            grnItem.setPurchaseOrderItem(poItem);
            grnItem.setBatchNumber(itemRequest.getBatchNumber());
            grnItem.setManufacturingDate(itemRequest.getManufacturingDate());
            grnItem.setExpiryDate(itemRequest.getExpiryDate());
            grnItem.setExpectedQuantity(expectedRemaining);
            grnItem.setReceivedQuantity(itemRequest.getReceivedQuantity());
            grnItem.setQuantityDiscrepancy(discrepancy);
            grnItem.setPurchasePrice(purchasePrice);
            grnItem.setMrp(itemRequest.getMrp());
            grnItem.setConditionNotes(itemRequest.getConditionNotes());
            grnItem.setQualityDiscrepancy(itemRequest.isQualityDiscrepancy());
            grnItem.setQualityNotes(itemRequest.getQualityNotes());
            grnItem.setCreatedBatchId(createdBatch.getId());

            savedItems.add(goodsReceiptItemRepository.save(grnItem));

            if (discrepancy.compareTo(BigDecimal.ZERO) != 0) {
                discrepancySummaries.add(drug.getGenericName() + ": quantity discrepancy of " + discrepancy);
            }
            if (itemRequest.isQualityDiscrepancy()) {
                discrepancySummaries.add(drug.getGenericName() + ": quality issue — " + itemRequest.getQualityNotes());
            }
        }

        if (!discrepancySummaries.isEmpty()) {
            savedGrn.setDiscrepancyNotes(String.join("; ", discrepancySummaries));
            savedGrn = goodsReceiptNoteRepository.save(savedGrn);
        }

        purchaseOrderService.refreshStatusAfterReceipt(po.getId());
        auditLogService.log(receivedBy, "GRN_PROCESSED", "GoodsReceiptNote", savedGrn.getId(),
                null, "purchaseOrderId=" + po.getId() + ", itemCount=" + savedGrn.getTotalItems(), "SUCCESS", null);
        return toResponse(savedGrn, savedItems);
    }

    private void enforceReceiptTimingRule(GoodsReceiptNoteCreateRequest request, Authentication authentication) {
        if (!request.getReceivedDate().isBefore(LocalDateTime.now().minusHours(48))) {
            return; // within 48 hours, no override needed
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (request.getLateReceiptOverrideReason() == null || request.getLateReceiptOverrideReason().isBlank()) {
            throw new IllegalArgumentException(
                    "This GRN is being recorded more than 48 hours after goods were received — provide a lateReceiptOverrideReason to proceed");
        }
        if (!isAdmin) {
            throw new IllegalArgumentException("Only an administrator may override the 48-hour GRN completion requirement");
        }
    }

    @Transactional(readOnly = true)
    public GoodsReceiptNoteResponse getById(Long id) {
        GoodsReceiptNote grn = goodsReceiptNoteRepository.findById(id)
                .orElseThrow(() -> new GrnNotFoundException("GRN not found: " + id));
        return toResponse(grn, goodsReceiptItemRepository.findByGoodsReceiptNoteId(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<GoodsReceiptNoteResponse> search(Long purchaseOrderId, Pageable pageable) {
        Page<GoodsReceiptNote> page = purchaseOrderId != null
                ? goodsReceiptNoteRepository.findByPurchaseOrderId(purchaseOrderId, pageable)
                : goodsReceiptNoteRepository.findAll(pageable);
        return PageResponse.from(page.map(grn ->
                toResponse(grn, goodsReceiptItemRepository.findByGoodsReceiptNoteId(grn.getId()))));
    }

    @Transactional(readOnly = true)
    public List<GoodsReceiptItemResponse> getDiscrepancies() {
        return goodsReceiptItemRepository.findDiscrepancies().stream()
                .map(this::toItemResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupplierPerformanceResponse getSupplierPerformance(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found: " + supplierId));

        List<GoodsReceiptNote> grns = goodsReceiptNoteRepository.findByPurchaseOrder_Supplier_Id(supplierId);
        if (grns.isEmpty()) {
            return new SupplierPerformanceResponse(supplierId, supplier.getSupplierName(), 0, 0.0, 0.0, 0.0);
        }

        long onTimeCount = grns.stream()
                .filter(g -> !g.getReceivedDate().toLocalDate().isAfter(g.getPurchaseOrder().getExpectedDeliveryDate()))
                .count();
        double onTimeRate = (onTimeCount * 100.0) / grns.size();

        List<GoodsReceiptItem> allItems = grns.stream()
                .flatMap(g -> goodsReceiptItemRepository.findByGoodsReceiptNoteId(g.getId()).stream())
                .collect(Collectors.toList());

        BigDecimal totalExpected = allItems.stream().map(GoodsReceiptItem::getExpectedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalReceived = allItems.stream().map(GoodsReceiptItem::getReceivedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        double fillRate = totalExpected.compareTo(BigDecimal.ZERO) == 0 ? 100.0
                : totalReceived.divide(totalExpected, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();

        long qualityIssues = allItems.stream().filter(GoodsReceiptItem::isQualityDiscrepancy).count();
        double qualityDiscrepancyRate = allItems.isEmpty() ? 0.0 : (qualityIssues * 100.0) / allItems.size();

        return new SupplierPerformanceResponse(supplierId, supplier.getSupplierName(), grns.size(),
                onTimeRate, fillRate, qualityDiscrepancyRate);
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private GoodsReceiptItemResponse toItemResponse(GoodsReceiptItem i) {
        return new GoodsReceiptItemResponse(i.getId(), i.getPurchaseOrderItem().getId(),
                i.getPurchaseOrderItem().getDrug().getGenericName(), i.getBatchNumber(),
                i.getExpectedQuantity(), i.getReceivedQuantity(), i.getQuantityDiscrepancy(),
                i.isQualityDiscrepancy(), i.getQualityNotes(), i.getConditionNotes(), i.getCreatedBatchId());
    }

    private GoodsReceiptNoteResponse toResponse(GoodsReceiptNote grn, List<GoodsReceiptItem> items) {
        return new GoodsReceiptNoteResponse(grn.getId(), grn.getPurchaseOrder().getId(), grn.getReceivedDate(),
                grn.getReceivedBy().getFullName(), grn.getTotalItems(), grn.getDiscrepancyNotes(),
                grn.getGeneralNotes(), grn.getCreatedAt(),
                items.stream().map(this::toItemResponse).collect(Collectors.toList()));
    }
}