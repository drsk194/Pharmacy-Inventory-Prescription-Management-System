package com.pharmacy.pipms.report.service;

import com.pharmacy.pipms.audit.repository.AuditLogRepository;
import com.pharmacy.pipms.batch.entity.BatchStatus;
import com.pharmacy.pipms.batch.repository.DrugBatchRepository;
import com.pharmacy.pipms.batch.service.BatchService;
import com.pharmacy.pipms.billing.repository.BillRepository;
import com.pharmacy.pipms.billing.entity.BillStatus;
import com.pharmacy.pipms.config.ReportingProperties;
import com.pharmacy.pipms.dispensing.repository.DispensingRecordRepository;
import com.pharmacy.pipms.drug.entity.Drug;
import com.pharmacy.pipms.drug.repository.DrugRepository;
import com.pharmacy.pipms.inventory.repository.StockMovementRepository;
import com.pharmacy.pipms.prescription.repository.PrescriptionRepository;
import com.pharmacy.pipms.purchaseorder.repository.PurchaseOrderRepository;
import com.pharmacy.pipms.report.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final DrugRepository drugRepository;
    private final DrugBatchRepository drugBatchRepository;
    private final StockMovementRepository stockMovementRepository;
    private final BatchService batchService;
    private final PrescriptionRepository prescriptionRepository;
    private final DispensingRecordRepository dispensingRecordRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final BillRepository billRepository;
    private final AuditLogRepository auditLogRepository;
    private final ReportingProperties reportingProperties;
    
    @Transactional(readOnly = true)
    public InventorySummaryResponse getInventorySummary() {
        long totalDrugs = drugRepository.count();
        long lowStockCount = drugRepository.findAll().stream()
                .filter(Drug::isActive).filter(batchService::isLowStock).count();

        return new InventorySummaryResponse(
                totalDrugs,
                drugBatchRepository.countByStatus(BatchStatus.ACTIVE),
                drugBatchRepository.countByStatus(BatchStatus.NEAR_EXPIRY),
                drugBatchRepository.countByStatus(BatchStatus.EXPIRED),
                drugBatchRepository.countByStatus(BatchStatus.QUARANTINED),
                drugBatchRepository.countByStatus(BatchStatus.EXHAUSTED),
                drugBatchRepository.sumTotalStockValue(),
                lowStockCount
        );
    }

    @Transactional(readOnly = true)
    public List<DrugMovementResponse> getDeadStock(LocalDateTime start, LocalDateTime end) {
        Set<Long> drugsWithActivity = stockMovementRepository.dispensingActivityByDrug(start, end).stream()
                .map(r -> (Long) r[0]).collect(Collectors.toSet());

        return drugRepository.findAll().stream()
                .filter(Drug::isActive)
                .filter(d -> drugBatchRepository.sumActiveQuantityByDrug(d.getId()).compareTo(BigDecimal.ZERO) > 0)
                .filter(d -> !drugsWithActivity.contains(d.getId()))
                .map(d -> new DrugMovementResponse(d.getId(), d.getGenericName(), 0, BigDecimal.ZERO))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DrugMovementResponse> getSlowMovingStock(LocalDateTime start, LocalDateTime end) {
        int threshold = reportingProperties.getSlowMovingThreshold();
        return stockMovementRepository.dispensingActivityByDrug(start, end).stream()
                .filter(r -> ((Number) r[2]).longValue() > 0 && ((Number) r[2]).longValue() < threshold)
                .map(r -> new DrugMovementResponse((Long) r[0], (String) r[1],
                        ((Number) r[2]).longValue(), new BigDecimal(r[3].toString())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StockTurnoverResponse> getStockTurnover(LocalDateTime start, LocalDateTime end) {
        return stockMovementRepository.dispensingActivityByDrug(start, end).stream()
                .map(r -> {
                    Long drugId = (Long) r[0];
                    BigDecimal dispensed = new BigDecimal(r[3].toString());
                    BigDecimal currentStock = drugBatchRepository.sumActiveQuantityByDrug(drugId);
                    BigDecimal ratio = currentStock.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : dispensed.divide(currentStock, 4, RoundingMode.HALF_UP);
                    return new StockTurnoverResponse(drugId, (String) r[1], dispensed, currentStock, ratio);
                })
                .collect(Collectors.toList());
    }

    // ---------- Prescription / Dispensing ----------

    @Transactional(readOnly = true)
    public PrescriptionVolumeResponse getPrescriptionVolume(LocalDateTime start, LocalDateTime end) {
        long total = prescriptionRepository.countInPeriod(start, end);
        List<LabeledCountResponse> byStatus = prescriptionRepository.countByStatusGrouped(start, end).stream()
                .map(r -> new LabeledCountResponse(r[0].toString(), (Long) r[1])).collect(Collectors.toList());
        List<LabeledCountResponse> bySource = prescriptionRepository.countBySourceGrouped(start, end).stream()
                .map(r -> new LabeledCountResponse(r[0].toString(), (Long) r[1])).collect(Collectors.toList());
        return new PrescriptionVolumeResponse(total, byStatus, bySource);
    }

    @Transactional(readOnly = true)
    public DispensingTurnaroundResponse getDispensingTurnaround(LocalDateTime start, LocalDateTime end) {
        Double avg = dispensingRecordRepository.averageTurnaroundMinutes(start, end);
        long total = dispensingRecordRepository.countInPeriod(start, end);
        return new DispensingTurnaroundResponse(avg, total);
    }

    @Transactional(readOnly = true)
    public List<LabeledCountResponse> getTechnicianActivity(LocalDateTime start, LocalDateTime end) {
        return dispensingRecordRepository.countByTechnician(start, end).stream()
                .map(r -> new LabeledCountResponse((String) r[1], (Long) r[2])).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LabeledCountResponse> getPharmacistActivity(LocalDateTime start, LocalDateTime end) {
        return dispensingRecordRepository.countByPharmacist(start, end).stream()
                .map(r -> new LabeledCountResponse((String) r[1], (Long) r[2])).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DrugMovementResponse> getDrugUtilization(LocalDateTime start, LocalDateTime end) {
        return stockMovementRepository.dispensingActivityByDrug(start, end).stream()
                .map(r -> new DrugMovementResponse((Long) r[0], (String) r[1],
                        ((Number) r[2]).longValue(), new BigDecimal(r[3].toString())))
                .collect(Collectors.toList());
    }

    // ---------- Procurement ----------

    @Transactional(readOnly = true)
    public ProcurementSpendingResponse getProcurementSpending(LocalDate start, LocalDate end) {
        BigDecimal total = purchaseOrderRepository.sumTotalSpending(start, end);
        List<LabeledAmountResponse> bySupplier = purchaseOrderRepository.sumSpendingBySupplier(start, end).stream()
                .map(r -> new LabeledAmountResponse((String) r[1], new BigDecimal(r[2].toString())))
                .collect(Collectors.toList());
        return new ProcurementSpendingResponse(total, bySupplier);
    }

    // ---------- Financial ----------

    @Transactional(readOnly = true)
    public RevenueSummaryResponse getRevenue(LocalDateTime start, LocalDateTime end) {
        BigDecimal total = billRepository.sumRevenueInPeriod(start, end);
        List<LabeledAmountResponse> daily = billRepository.dailyRevenue(start, end).stream()
                .map(r -> new LabeledAmountResponse(r[0].toString(), new BigDecimal(r[1].toString())))
                .collect(Collectors.toList());
        return new RevenueSummaryResponse(total, daily);
    }

    @Transactional(readOnly = true)
    public OutstandingSummaryResponse getOutstanding() {
        BigDecimal total = billRepository.sumOutstanding();
        long count = billRepository.countByStatusIn(List.of(BillStatus.PENDING, BillStatus.PARTIALLY_PAID));
        return new OutstandingSummaryResponse(total, count);
    }

    // ---------- Audit ----------

    @Transactional(readOnly = true)
    public List<LabeledCountResponse> getAuditActivity(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.countByActionGrouped(start, end).stream()
                .map(r -> new LabeledCountResponse((String) r[0], (Long) r[1])).collect(Collectors.toList());
    }

    // ---------- Dashboard ----------

    @Transactional(readOnly = true)
    public AdminAnalyticsResponse getAdminAnalytics(long prescriptionQueueCount) {
        long lowStock = drugRepository.findAll().stream()
                .filter(Drug::isActive).filter(batchService::isLowStock).count();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDateTime.now();
        long dispensingToday = dispensingRecordRepository.countInPeriod(todayStart, todayEnd);
        return new AdminAnalyticsResponse(prescriptionQueueCount, lowStock, dispensingToday);
    }
}