package com.pharmacy.pipms.grn.repository;

import com.pharmacy.pipms.grn.entity.GoodsReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GoodsReceiptItemRepository extends JpaRepository<GoodsReceiptItem, Long> {

    List<GoodsReceiptItem> findByGoodsReceiptNoteId(Long goodsReceiptNoteId);

    @Query("SELECT i FROM GoodsReceiptItem i WHERE i.quantityDiscrepancy <> 0 OR i.qualityDiscrepancy = true")
    List<GoodsReceiptItem> findDiscrepancies();
}