package com.pharmacy.pipms.grn.repository;

import com.pharmacy.pipms.grn.entity.GoodsReceiptNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoodsReceiptNoteRepository extends JpaRepository<GoodsReceiptNote, Long> {
    Page<GoodsReceiptNote> findByPurchaseOrderId(Long purchaseOrderId, Pageable pageable);
    List<GoodsReceiptNote> findByPurchaseOrder_Supplier_Id(Long supplierId);
}