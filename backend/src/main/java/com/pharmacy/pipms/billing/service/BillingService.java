package com.pharmacy.pipms.billing.service;

import com.pharmacy.pipms.billing.dto.*;
import com.pharmacy.pipms.billing.entity.*;
import com.pharmacy.pipms.billing.repository.BillItemRepository;
import com.pharmacy.pipms.billing.repository.BillRepository;
import com.pharmacy.pipms.billing.repository.PaymentRepository;
import com.pharmacy.pipms.billing.repository.RefundRepository;
import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.config.BillingProperties;
import com.pharmacy.pipms.dispensing.entity.DispensingBatchAllocation;
import com.pharmacy.pipms.dispensing.entity.DispensingRecord;
import com.pharmacy.pipms.dispensing.entity.MedicationReturn;
import com.pharmacy.pipms.dispensing.repository.DispensingRecordRepository;
import com.pharmacy.pipms.dispensing.repository.MedicationReturnRepository;
import com.pharmacy.pipms.exception.*;
import com.pharmacy.pipms.patient.entity.Patient;
import com.pharmacy.pipms.patient.repository.PatientRepository;
import com.pharmacy.pipms.user.entity.User;
import com.pharmacy.pipms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pharmacy.pipms.audit.service.AuditLogService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final AuditLogService auditLogService;
    private final DispensingRecordRepository dispensingRecordRepository;
    private final MedicationReturnRepository medicationReturnRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final BillingProperties billingProperties;

    @Transactional
    public BillResponse createBill(BillCreateRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException("Patient not found: " + request.getPatientId()));

        Bill bill = new Bill();
        bill.setPatient(patient);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (Long dispensingRecordId : request.getDispensingRecordIds()) {
            if (billItemRepository.existsByDispensingRecordId(dispensingRecordId)) {
                throw new DuplicateResourceException(
                        "Dispensing record " + dispensingRecordId + " has already been billed");
            }
            DispensingRecord record = dispensingRecordRepository.findById(dispensingRecordId)
                    .orElseThrow(() -> new DispensingRecordNotFoundException(
                            "Dispensing record not found: " + dispensingRecordId));

            if (record.getQuantityDispensed() == null) {
                throw new IllegalArgumentException(
                        "Dispensing record " + dispensingRecordId + " has not been authorized/dispensed yet");
            }
            boolean belongsToPatient = record.getPrescriptionItem().getPrescription().getPatient().getId()
                    .equals(patient.getId());
            if (!belongsToPatient) {
                throw new IllegalArgumentException(
                        "Dispensing record " + dispensingRecordId + " does not belong to the specified patient");
            }

            BigDecimal weightedMrp = computeWeightedMrp(record);

            BillItem item = new BillItem();
            item.setBill(bill);
            item.setDispensingRecord(record);
            item.setDrug(record.getPrescriptionItem().getDrug());
            item.setQuantity(record.getQuantityDispensed());
            item.setUnitPrice(weightedMrp);
            item.setLineTotal(record.getQuantityDispensed().multiply(weightedMrp).setScale(2, RoundingMode.HALF_UP));

            bill.getItems().add(item);
            subtotal = subtotal.add(item.getLineTotal());
        }

        BigDecimal discountPercent = request.getDiscountPercent() != null ? request.getDiscountPercent() : BigDecimal.ZERO;
        BigDecimal discountAmount = subtotal.multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal dispensingFee = billingProperties.getDispensingFee();
        BigDecimal gstPercent = billingProperties.getGstPercent();

        BigDecimal taxableAmount = subtotal.subtract(discountAmount).add(dispensingFee);
        BigDecimal taxAmount = taxableAmount.multiply(gstPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = taxableAmount.add(taxAmount);

        bill.setSubtotal(subtotal);
        bill.setDispensingFee(dispensingFee);
        bill.setDiscountPercent(discountPercent);
        bill.setDiscountReason(request.getDiscountReason());
        bill.setDiscountAmount(discountAmount);
        bill.setGstPercent(gstPercent);
        bill.setTaxAmount(taxAmount);
        bill.setTotalAmount(totalAmount);
        bill.setCoPayment(totalAmount);
        bill.setOutstandingAmount(totalAmount);
        bill.setStatus(BillStatus.PENDING);
        Bill saved = billRepository.save(bill);
        auditLogService.log(null, "BILL_CREATED", "Bill", saved.getId(), null,
                "total=" + saved.getTotalAmount(), "SUCCESS", null);
        return toResponse(saved);
    }

    private BigDecimal computeWeightedMrp(DispensingRecord record) {
        List<DispensingBatchAllocation> allocations = record.getBatchAllocations();
        if (allocations == null || allocations.isEmpty()) {
            throw new IllegalStateException(
                    "Dispensing record " + record.getId() + " has no batch allocations to price from");
        }
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal weightedSum = BigDecimal.ZERO;
        for (DispensingBatchAllocation allocation : allocations) {
            BigDecimal qty = allocation.getQuantityAllocated();
            BigDecimal mrp = allocation.getBatch().getMrp();
            weightedSum = weightedSum.add(qty.multiply(mrp));
            totalQty = totalQty.add(qty);
        }
        return weightedSum.divide(totalQty, 2, RoundingMode.HALF_UP);
    }

    @Transactional
    public BillResponse recordPayment(PaymentCreateRequest request, String actorEmail) {
        Bill bill = getEntity(request.getBillId());
        if (bill.getStatus() == BillStatus.CANCELLED) {
            throw new InvalidPrescriptionStatusException("Cannot record a payment against a cancelled bill");
        }
        User receivedBy = requireUser(actorEmail);

        Payment payment = new Payment();
        payment.setBill(bill);
        payment.setAmount(request.getAmount());
        payment.setPaymentMode(request.getPaymentMode());
        payment.setTransactionReference(request.getTransactionReference());
        payment.setReceivedBy(receivedBy);
        bill.getPayments().add(payment);

        bill.setAmountPaid(bill.getAmountPaid().add(request.getAmount()));
        recomputeOutstandingAndStatus(bill);
        auditLogService.log(receivedBy, "PAYMENT_RECORDED", "Bill", bill.getId(), null,
                "amount=" + request.getAmount() + ", mode=" + request.getPaymentMode(), "SUCCESS", null);
        return toResponse(billRepository.save(bill));
    }

    @Transactional
    public BillResponse processRefund(RefundCreateRequest request, String actorEmail) {
        Bill bill = getEntity(request.getBillId());
        User processedBy = requireUser(actorEmail);

        BigDecimal netPaid = bill.getAmountPaid().subtract(bill.getAmountRefunded());
        if (request.getAmount().compareTo(netPaid) > 0) {
            throw new IllegalArgumentException(
                    "Refund amount exceeds the net amount actually collected on this bill (" + netPaid + ")");
        }

        Refund refund = new Refund();
        refund.setBill(bill);
        refund.setAmount(request.getAmount());
        refund.setReason(request.getReason());
        refund.setProcessedBy(processedBy);

        if (request.getMedicationReturnId() != null) {
            MedicationReturn medicationReturn = medicationReturnRepository.findById(request.getMedicationReturnId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Medication return not found: " + request.getMedicationReturnId()));
            refund.setMedicationReturn(medicationReturn);
        }

        bill.getRefunds().add(refund);
        bill.setAmountRefunded(bill.getAmountRefunded().add(request.getAmount()));
        recomputeOutstandingAndStatus(bill);
        auditLogService.log(processedBy, "REFUND_PROCESSED", "Bill", bill.getId(), null,
                "amount=" + request.getAmount() + ", reason=" + request.getReason(), "SUCCESS", null);
        return toResponse(billRepository.save(bill));
    }

    @Transactional
    public BillResponse submitInsuranceClaim(Long billId, InsuranceClaimRequest request) {
        Bill bill = getEntity(billId);
        if (bill.getInsuranceClaimStatus() != null) {
            throw new IllegalArgumentException("An insurance claim has already been submitted for this bill");
        }
        bill.setInsuranceProvider(request.getInsuranceProvider());
        bill.setInsuranceClaimNumber(request.getInsuranceClaimNumber());
        bill.setInsuranceAmount(request.getClaimedAmount());
        bill.setInsuranceClaimStatus(InsuranceClaimStatus.SUBMITTED);
        bill.setCoPayment(bill.getTotalAmount().subtract(request.getClaimedAmount()));

        return toResponse(billRepository.save(bill));
    }

    @Transactional
    public BillResponse updateInsuranceClaimStatus(Long billId, InsuranceClaimStatusUpdateRequest request) {
        Bill bill = getEntity(billId);
        if (bill.getInsuranceClaimStatus() == null) {
            throw new IllegalArgumentException("No insurance claim exists for this bill yet");
        }
        InsuranceClaimStatus newStatus = request.getStatus();

        // If the claim is being marked PAID, the insurer's portion counts
        // as money actually collected — added to amountPaid automatically.
        if (newStatus == InsuranceClaimStatus.PAID && bill.getInsuranceClaimStatus() != InsuranceClaimStatus.PAID) {
            bill.setAmountPaid(bill.getAmountPaid().add(bill.getInsuranceAmount()));
        }

        bill.setInsuranceClaimStatus(newStatus);
        recomputeOutstandingAndStatus(bill);

        return toResponse(billRepository.save(bill));
    }

    @Transactional
    public BillResponse cancel(Long billId) {
        Bill bill = getEntity(billId);
        if (bill.getStatus() != BillStatus.PENDING) {
            throw new InvalidPrescriptionStatusException(
                    "Only PENDING bills (no payments recorded) can be cancelled (current: " + bill.getStatus() + ")");
        }
        bill.setStatus(BillStatus.CANCELLED);
        return toResponse(billRepository.save(bill));
    }

    @Transactional(readOnly = true)
    public BillResponse getById(Long id, String requesterEmail, boolean hasFullAccess) {
        Bill bill = getEntity(id);
        if (!hasFullAccess) {
            User requester = requireUser(requesterEmail);
            boolean owns = bill.getPatient().getUser() != null
                    && bill.getPatient().getUser().getId().equals(requester.getId());
            if (!owns) {
                throw new AccessDeniedException("You may only view your own bills");
            }
        }
        auditLogService.log(null, "BILL_VIEWED", "Bill", bill.getId(), null, null, "SUCCESS", null);
        return toResponse(bill);
    }

    @Transactional(readOnly = true)
    public List<BillableDispensingResponse> findBillableDispensing(Long patientId) {
        patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found: " + patientId));
        return dispensingRecordRepository.findBillableForPatient(
                        patientId,
                        java.util.Set.of(
                                com.pharmacy.pipms.dispensing.entity.DispensingStatus.AUTHORIZED,
                                com.pharmacy.pipms.dispensing.entity.DispensingStatus.LABEL_PRINTED,
                                com.pharmacy.pipms.dispensing.entity.DispensingStatus.ACKNOWLEDGED))
                .stream()
                .map(record -> new BillableDispensingResponse(
                        record.getId(),
                        record.getPrescriptionItem().getDrug().getGenericName(),
                        record.getQuantityDispensed(),
                        record.getStatus().name()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<BillResponse> search(BillStatus status, Long patientId, Pageable pageable) {
        Page<Bill> page = billRepository.search(status, patientId, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<BillResponse> getMyBills(String email, Pageable pageable) {
        User user = requireUser(email);
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new PatientNotFoundException("No patient record linked to this account"));
        return PageResponse.from(billRepository.findByPatientIdOrderByBillDateDesc(patient.getId(), pageable)
                .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<BillResponse> getOutstanding(Pageable pageable) {
        return PageResponse.from(billRepository.findByStatusInOrderByBillDateAsc(
                        List.of(BillStatus.PENDING, BillStatus.PARTIALLY_PAID), pageable)
                .map(this::toResponse));
    }

    private void recomputeOutstandingAndStatus(Bill bill) {
        BigDecimal netPaid = bill.getAmountPaid().subtract(bill.getAmountRefunded());
        BigDecimal outstanding = bill.getTotalAmount().subtract(netPaid);
        bill.setOutstandingAmount(outstanding);

        if (bill.getStatus() == BillStatus.CANCELLED) {
            return; // cancelled bills never auto-transition
        }
        if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            bill.setStatus(BillStatus.PAID);
        } else if (netPaid.compareTo(BigDecimal.ZERO) > 0) {
            bill.setStatus(BillStatus.PARTIALLY_PAID);
        } else {
            bill.setStatus(BillStatus.PENDING);
        }
    }

    Bill getEntity(Long id) {
        return billRepository.findById(id).orElseThrow(() -> new BillNotFoundException("Bill not found: " + id));
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private BillResponse toResponse(Bill b) {
        List<BillItemResponse> items = b.getItems().stream()
                .map(i -> new BillItemResponse(i.getId(), i.getDispensingRecord().getId(), i.getDrug().getGenericName(),
                        i.getQuantity(), i.getUnitPrice(), i.getLineTotal()))
                .collect(Collectors.toList());
        List<PaymentResponse> payments = b.getPayments().stream()
                .map(p -> new PaymentResponse(p.getId(), p.getAmount(), p.getPaymentMode() != null ? p.getPaymentMode().name() : null,
                        p.getTransactionReference(), p.getReceivedBy() != null ? p.getReceivedBy().getFullName() : "System", p.getPaymentDate()))
                .collect(Collectors.toList());
        List<RefundResponse> refunds = b.getRefunds().stream()
                .map(r -> new RefundResponse(r.getId(), r.getAmount(), r.getReason(),
                        r.getMedicationReturn() != null ? r.getMedicationReturn().getId() : null,
                        r.getProcessedBy() != null ? r.getProcessedBy().getFullName() : "System", r.getRefundDate()))
                .collect(Collectors.toList());

        return new BillResponse(
                b.getId(), b.getPatient().getId(), b.getPatient().getFullName(), b.getBillDate(),
                b.getSubtotal(), b.getDispensingFee(), b.getDiscountPercent(), b.getDiscountReason(), b.getDiscountAmount(),
                b.getGstPercent(), b.getTaxAmount(), b.getTotalAmount(),
                b.getInsuranceProvider(), b.getInsuranceClaimNumber(),
                b.getInsuranceClaimStatus() != null ? b.getInsuranceClaimStatus().name() : null,
                b.getInsuranceAmount(), b.getCoPayment(), b.getAmountPaid(), b.getAmountRefunded(),
                b.getOutstandingAmount(), b.getStatus().name(), items, payments, refunds
        );
    }
}