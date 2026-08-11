package com.pharmacy.pipms.dispensing.service;

import com.pharmacy.pipms.audit.service.AuditLogService;
import com.pharmacy.pipms.batch.entity.DrugBatch;
import com.pharmacy.pipms.batch.service.BatchService;
import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.dispensing.dto.*;
import com.pharmacy.pipms.dispensing.entity.*;
import com.pharmacy.pipms.dispensing.repository.*;
import com.pharmacy.pipms.drug.entity.Drug;
import com.pharmacy.pipms.exception.*;
import com.pharmacy.pipms.fefo.dto.BatchAllocationResult;
import com.pharmacy.pipms.fefo.dto.FefoPlanResponse;
import com.pharmacy.pipms.fefo.service.FefoAllocationService;
import com.pharmacy.pipms.inventory.entity.MovementType;
import com.pharmacy.pipms.inventory.service.StockMovementService;
import com.pharmacy.pipms.prescription.entity.Prescription;
import com.pharmacy.pipms.prescription.entity.PrescriptionItem;
import com.pharmacy.pipms.prescription.entity.PrescriptionStatus;
import com.pharmacy.pipms.prescription.repository.PrescriptionItemRepository;
import com.pharmacy.pipms.prescription.service.PrescriptionService;
import com.pharmacy.pipms.user.entity.User;
import com.pharmacy.pipms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DispensingService {

    private final DispensingRecordRepository dispensingRecordRepository;
    private final BalanceOrderRepository balanceOrderRepository;
    private final CounsellingRecordRepository counsellingRecordRepository;
    private final MedicationReturnRepository medicationReturnRepository;
    private final DispensingErrorRepository dispensingErrorRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final PrescriptionService prescriptionService;
    private final FefoAllocationService fefoAllocationService;
    private final BatchService batchService;
    private final StockMovementService stockMovementService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public DispensingRecordResponse prepare(DispensingPrepareRequest request, String technicianEmail) {
        PrescriptionItem item = prescriptionItemRepository.findById(request.getPrescriptionItemId())
                .orElseThrow(() -> new PrescriptionNotFoundException(
                        "Prescription item not found: " + request.getPrescriptionItemId()));

        Prescription prescription = item.getPrescription();
        if (prescription.getStatus() != PrescriptionStatus.VERIFIED
                && prescription.getStatus() != PrescriptionStatus.PARTIAL) {
            throw new InvalidPrescriptionStatusException(
                    "Prescription must be VERIFIED or PARTIAL before dispensing can be prepared (current: "
                            + prescription.getStatus() + ")");
        }

        BigDecimal remaining = item.getPrescribedQuantity().subtract(item.getDispensedQuantity());
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPrescriptionStatusException("This prescription item has already been fully dispensed");
        }

        Drug drug = item.getDrug();
        if (drug.getBarcode() != null && !drug.getBarcode().equals(request.getScannedBarcode())) {
            throw new IllegalArgumentException(
                    "Scanned barcode does not match the registered barcode for " + drug.getGenericName());
        }

        BigDecimal availableStock = fefoAllocationService.getTotalEligibleStock(drug.getId());
        BigDecimal quantityIntended = remaining.min(availableStock);
        if (quantityIntended.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InsufficientStockException("No eligible stock currently available for " + drug.getGenericName());
        }

        User technician = requireUser(technicianEmail);

        DispensingRecord record = new DispensingRecord();
        record.setPrescriptionItem(item);
        record.setTechnician(technician);
        record.setQuantityIntended(quantityIntended);
        record.setScannedBarcode(request.getScannedBarcode());
        record.setStatus(DispensingStatus.PREPARED);

        return toResponse(dispensingRecordRepository.save(record));
    }

    @Transactional
    public DispensingRecordResponse authorize(Long id, String pharmacistEmail) {
        DispensingRecord record = getEntity(id);
        if (record.getStatus() != DispensingStatus.PREPARED) {
            throw new InvalidPrescriptionStatusException(
                    "Only PREPARED dispensing records can be authorized (current: " + record.getStatus() + ")");
        }

        User pharmacist = requireUser(pharmacistEmail);
        PrescriptionItem item = record.getPrescriptionItem();
        Drug drug = item.getDrug();

        // Fresh FEFO consumption at authorization time, per Section 13's
        // workflow ordering (inventory reduced AFTER pharmacist sign-off).
        // If stock dropped below the prepared amount since prepare(), this
        // throws InsufficientStockException (409) rather than silently
        // dispensing less — see Assumption 2 in the module notes.
        FefoPlanResponse consumed = fefoAllocationService.consume(
                drug.getId(), record.getQuantityIntended(), "DISPENSING_RECORD", record.getId(), pharmacist);

        for (BatchAllocationResult allocation : consumed.getAllocations()) {
            DispensingBatchAllocation dba = new DispensingBatchAllocation();
            dba.setDispensingRecord(record);
            dba.setBatch(batchService.getBatchEntity(allocation.getBatchId()));
            dba.setQuantityAllocated(allocation.getQuantityAllocated());
            record.getBatchAllocations().add(dba); // cascade=ALL persists these on save below
        }

        record.setQuantityDispensed(record.getQuantityIntended());
        record.setPharmacist(pharmacist);
        record.setStatus(DispensingStatus.AUTHORIZED);
        record.setDispensedAt(LocalDateTime.now());
        DispensingRecord saved = dispensingRecordRepository.save(record);

        item.setDispensedQuantity(item.getDispensedQuantity().add(record.getQuantityDispensed()));
        prescriptionItemRepository.save(item);

        BigDecimal stillRemaining = item.getPrescribedQuantity().subtract(item.getDispensedQuantity());
        if (stillRemaining.compareTo(BigDecimal.ZERO) > 0) {
            BalanceOrder balanceOrder = new BalanceOrder();
            balanceOrder.setPrescriptionItem(item);
            balanceOrder.setQuantityPending(stillRemaining);
            balanceOrder.setStatus(BalanceOrderStatus.PENDING);
            balanceOrderRepository.save(balanceOrder);
        } else {
            balanceOrderRepository.findByPrescriptionItemIdAndStatus(item.getId(), BalanceOrderStatus.PENDING)
                    .forEach(bo -> {
                        bo.setStatus(BalanceOrderStatus.FULFILLED);
                        balanceOrderRepository.save(bo);
                    });
        }

        prescriptionService.refreshStatusAfterDispensing(item.getPrescription().getId(), pharmacistEmail);

        auditLogService.log(pharmacist, "DISPENSING_AUTHORIZED", "DispensingRecord", saved.getId(),
                null, "Authorized dispensing of " + saved.getQuantityDispensed() + " units of " + drug.getGenericName(),
                "SUCCESS", null);

        return toResponse(saved);
    }

    @Transactional
    public DispensingRecordResponse printLabel(Long id) {
        DispensingRecord record = getEntity(id);
        if (record.getStatus() != DispensingStatus.AUTHORIZED) {
            throw new InvalidPrescriptionStatusException(
                    "Label can only be printed for an AUTHORIZED dispensing record (current: " + record.getStatus() + ")");
        }
        record.setLabelPrinted(true);
        record.setStatus(DispensingStatus.LABEL_PRINTED);
        return toResponse(dispensingRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public LabelResponse getLabel(Long id) {
        DispensingRecord record = getEntity(id);
        PrescriptionItem item = record.getPrescriptionItem();
        Prescription prescription = item.getPrescription();
        return new LabelResponse(
                prescription.getPatient().getFullName(),
                item.getDrug().getGenericName(),
                item.getDosage(), item.getFrequency(), item.getDuration(), item.getInstructions(),
                prescription.getDoctor().getFullName(),
                record.getPharmacist() != null ? record.getPharmacist().getFullName() : null,
                record.getQuantityDispensed(),
                record.getDispensedAt()
        );
    }

    @Transactional
    public DispensingRecordResponse acknowledge(Long id, AcknowledgeRequest request) {
        DispensingRecord record = getEntity(id);
        if (record.getStatus() != DispensingStatus.LABEL_PRINTED) {
            throw new InvalidPrescriptionStatusException(
                    "Patient acknowledgement requires the label to be printed first (current: " + record.getStatus() + ")");
        }
        record.setPatientAcknowledged(true);
        record.setAcknowledgedByName(request.getAcknowledgedByName());
        record.setAcknowledgedRelation(request.getAcknowledgedRelation());
        record.setStatus(DispensingStatus.ACKNOWLEDGED);
        return toResponse(dispensingRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public DispensingRecordResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<DispensingRecordResponse> search(DispensingStatus status, Pageable pageable) {
        Page<DispensingRecord> page = dispensingRecordRepository.search(status, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional
    public CounsellingRecordResponse addCounselling(CounsellingRequest request, String pharmacistEmail) {
        DispensingRecord record = getEntity(request.getDispensingRecordId());
        User pharmacist = requireUser(pharmacistEmail);

        CounsellingRecord counselling = new CounsellingRecord();
        counselling.setDispensingRecord(record);
        counselling.setPharmacist(pharmacist);
        counselling.setCounsellingType(request.getCounsellingType());
        counselling.setNotes(request.getNotes());

        CounsellingRecord saved = counsellingRecordRepository.save(counselling);
        return new CounsellingRecordResponse(saved.getId(), record.getId(), pharmacist.getFullName(),
                saved.getCounsellingType().name(), saved.getNotes(), saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<CounsellingRecordResponse> getCounselling(Long dispensingRecordId) {
        return counsellingRecordRepository.findByDispensingRecordId(dispensingRecordId).stream()
                .map(c -> new CounsellingRecordResponse(c.getId(), dispensingRecordId, c.getPharmacist().getFullName(),
                        c.getCounsellingType().name(), c.getNotes(), c.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Transactional
    public MedicationReturnResponse processReturn(MedicationReturnRequest request, String processorEmail) {
        DispensingRecord record = getEntity(request.getDispensingRecordId());
        DrugBatch batch = batchService.getBatchEntity(request.getBatchId());

        boolean batchWasPartOfThisRecord = record.getBatchAllocations().stream()
                .anyMatch(a -> a.getBatch().getId().equals(batch.getId()));
        if (!batchWasPartOfThisRecord) {
            throw new IllegalArgumentException("The specified batch was not part of this dispensing record");
        }

        User processor = requireUser(processorEmail);

        // Inventory credit only — patient billing adjustment deferred to
        // Module 15, see Assumption 5 in the module notes.
        batchService.applyAdjustment(batch.getId(), request.getQuantityReturned());
        stockMovementService.record(batch, MovementType.RETURN, request.getQuantityReturned(),
                "MEDICATION_RETURN", record.getId(), request.getReason(), processor);

        MedicationReturn medicationReturn = new MedicationReturn();
        medicationReturn.setDispensingRecord(record);
        medicationReturn.setBatch(batch);
        medicationReturn.setQuantityReturned(request.getQuantityReturned());
        medicationReturn.setReason(request.getReason());
        medicationReturn.setProcessedBy(processor);

        MedicationReturn saved = medicationReturnRepository.save(medicationReturn);
        return new MedicationReturnResponse(saved.getId(), record.getId(), batch.getId(), batch.getBatchNumber(),
                saved.getQuantityReturned(), saved.getReason(), processor.getFullName(), saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<MedicationReturnResponse> getReturns() {
        return medicationReturnRepository.findAll().stream()
                .map(r -> new MedicationReturnResponse(r.getId(), r.getDispensingRecord().getId(), r.getBatch().getId(),
                        r.getBatch().getBatchNumber(), r.getQuantityReturned(), r.getReason(),
                        r.getProcessedBy().getFullName(), r.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Transactional
    public DispensingErrorResponse reportError(DispensingErrorRequest request, String reporterEmail) {
        User reporter = requireUser(reporterEmail);
        DispensingError error = new DispensingError();
        if (request.getDispensingRecordId() != null) {
            error.setDispensingRecord(getEntity(request.getDispensingRecordId()));
        }
        error.setErrorType(request.getErrorType());
        error.setDescription(request.getDescription());
        error.setCorrectiveAction(request.getCorrectiveAction());
        error.setReportedBy(reporter);

        DispensingError saved = dispensingErrorRepository.save(error);

        auditLogService.log(reporter, "DISPENSING_ERROR_REPORTED", "DispensingError", saved.getId(),
                null, request.getErrorType() + ": " + request.getDescription(), "SUCCESS", null);

        return toErrorResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DispensingErrorResponse> getErrors() {
        return dispensingErrorRepository.findAll().stream().map(this::toErrorResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BalanceOrderResponse> getBalanceOrders(BalanceOrderStatus status) {
        List<BalanceOrder> orders = status != null
                ? balanceOrderRepository.findByStatus(status)
                : balanceOrderRepository.findAll();
        return orders.stream().map(o -> new BalanceOrderResponse(o.getId(), o.getPrescriptionItem().getId(),
                        o.getPrescriptionItem().getDrug().getGenericName(), o.getQuantityPending(),
                        o.getStatus().name(), o.getCreatedAt()))
                .collect(Collectors.toList());
    }

    DispensingRecord getEntity(Long id) {
        return dispensingRecordRepository.findById(id)
                .orElseThrow(() -> new DispensingRecordNotFoundException("Dispensing record not found: " + id));
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private DispensingRecordResponse toResponse(DispensingRecord r) {
        List<BatchAllocationResult> allocations = r.getBatchAllocations().stream()
                .map(a -> new BatchAllocationResult(a.getBatch().getId(), a.getBatch().getBatchNumber(),
                        a.getBatch().getExpiryDate(), a.getQuantityAllocated(), a.getBatch().getCurrentQuantity()))
                .collect(Collectors.toList());

        return new DispensingRecordResponse(
                r.getId(), r.getPrescriptionItem().getId(), r.getPrescriptionItem().getDrug().getGenericName(),
                r.getTechnician().getFullName(), r.getPharmacist() != null ? r.getPharmacist().getFullName() : null,
                r.getQuantityIntended(), r.getQuantityDispensed(), r.getStatus().name(), r.isLabelPrinted(),
                r.getDispensedAt(), r.isPatientAcknowledged(), allocations
        );
    }

    private DispensingErrorResponse toErrorResponse(DispensingError e) {
        return new DispensingErrorResponse(e.getId(),
                e.getDispensingRecord() != null ? e.getDispensingRecord().getId() : null,
                e.getErrorType().name(), e.getDescription(), e.getCorrectiveAction(),
                e.getReportedBy().getFullName(), e.getCreatedAt());
    }
}