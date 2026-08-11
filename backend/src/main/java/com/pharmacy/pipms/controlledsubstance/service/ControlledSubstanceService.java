package com.pharmacy.pipms.controlledsubstance.service;

import com.pharmacy.pipms.audit.service.AuditLogService;
import com.pharmacy.pipms.batch.entity.DrugBatch;
import com.pharmacy.pipms.batch.repository.DrugBatchRepository;
import com.pharmacy.pipms.batch.service.BatchService;
import com.pharmacy.pipms.common.constants.RoleName;
import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.controlledsubstance.dto.*;
import com.pharmacy.pipms.controlledsubstance.entity.*;
import com.pharmacy.pipms.controlledsubstance.repository.*;
import com.pharmacy.pipms.drug.entity.Drug;
import com.pharmacy.pipms.drug.entity.DrugSchedule;
import com.pharmacy.pipms.drug.repository.DrugRepository;
import com.pharmacy.pipms.exception.*;
import com.pharmacy.pipms.fefo.dto.FefoPlanResponse;
import com.pharmacy.pipms.fefo.service.FefoAllocationService;
import com.pharmacy.pipms.inventory.entity.MovementType;
import com.pharmacy.pipms.inventory.service.StockMovementService;
import com.pharmacy.pipms.notification.service.NotificationService;
import com.pharmacy.pipms.prescription.entity.Prescription;
import com.pharmacy.pipms.prescription.repository.PrescriptionRepository;
import com.pharmacy.pipms.security.jwt.JwtProperties;
import com.pharmacy.pipms.user.entity.User;
import com.pharmacy.pipms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ControlledSubstanceService {

    private static final Set<DrugSchedule> CONTROLLED_SCHEDULES = Set.of(DrugSchedule.H, DrugSchedule.H1, DrugSchedule.X);
    private static final String GENESIS_HASH = "GENESIS";
    private final NotificationService notificationService;
    private final ControlledSubstanceAuthorizationRepository authorizationRepository;
    private final ControlledSubstanceRegisterRepository registerRepository;
    private final ControlledSubstanceReconciliationRepository reconciliationRepository;
    private final DrugRepository drugRepository;
    private final DrugBatchRepository drugBatchRepository;
    private final BatchService batchService;
    private final StockMovementService stockMovementService;
    private final FefoAllocationService fefoAllocationService;
    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final AuditLogService auditLogService;

    @Transactional
    public ReauthenticateResponse reauthenticate(String pharmacistEmail, String pin) {
        User pharmacist = requireUser(pharmacistEmail);
        verifyPin(pharmacist, pin, "pharmacist");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusSeconds(jwtProperties.getControlledSubstance().getReauthWindowMs() / 1000);

        ControlledSubstanceAuthorization auth = new ControlledSubstanceAuthorization();
        auth.setUser(pharmacist);
        auth.setAuthorizedAt(now);
        auth.setExpiresAt(expiresAt);
        authorizationRepository.save(auth);

        return new ReauthenticateResponse(true, expiresAt);
    }

    @Transactional
    public CsTransactionResponse createTransaction(CsTransactionCreateRequest request, String pharmacistEmail) {
        User pharmacist = requireUser(pharmacistEmail);
        requireValidReauth(pharmacist);

        Drug drug = drugRepository.findById(request.getDrugId())
                .orElseThrow(() -> new DrugNotFoundException("Drug not found: " + request.getDrugId()));
        if (!CONTROLLED_SCHEDULES.contains(drug.getSchedule())) {
            throw new IllegalArgumentException(
                    "'" + drug.getGenericName() + "' is not a controlled substance (schedule " + drug.getSchedule() + ")");
        }

        User technician = userRepository.findById(request.getTechnicianId())
                .orElseThrow(() -> new UserNotFoundException("Technician not found: " + request.getTechnicianId()));
        if (technician.getId().equals(pharmacist.getId())) {
            throw new DualAuthorizationRequiredException(
                    "The co-signing technician must be a different person from the authorizing pharmacist");
        }
        boolean technicianEligible = technician.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleName.ROLE_TECHNICIAN || r.getName() == RoleName.ROLE_ADMIN);
        if (!technicianEligible) {
            throw new DualAuthorizationRequiredException(
                    "The specified co-signer does not hold a technician (or admin) role");
        }
        verifyPin(technician, request.getTechnicianPin(), "technician");

        User witness = null;
        if (request.getTransactionType() == CsTransactionType.DISPOSAL) {
            if (request.getWitnessId() == null) {
                throw new DualAuthorizationRequiredException("Disposal requires a witness");
            }
            witness = userRepository.findById(request.getWitnessId())
                    .orElseThrow(() -> new UserNotFoundException("Witness not found: " + request.getWitnessId()));
            if (witness.getId().equals(pharmacist.getId()) || witness.getId().equals(technician.getId())) {
                throw new DualAuthorizationRequiredException(
                        "The witness must be a different person from both the pharmacist and the technician");
            }
        }

        Prescription prescription = null;
        if (request.getPrescriptionId() != null) {
            prescription = prescriptionRepository.findById(request.getPrescriptionId())
                    .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found: " + request.getPrescriptionId()));
        }

        enforceQuantityAndRefillLimits(drug, request, prescription);

        BigDecimal quantitySigned = applyTransactionAndGetSignedQuantity(drug, request, pharmacist, prescription);
        BigDecimal balanceAfter = drugBatchRepository.sumActiveQuantityByDrug(drug.getId());

        ControlledSubstanceRegister entry = new ControlledSubstanceRegister();
        entry.setDrug(drug);
        entry.setTransactionType(request.getTransactionType());
        entry.setQuantity(quantitySigned);
        entry.setBalanceAfter(balanceAfter);
        entry.setPrescription(prescription);
        entry.setTechnician(technician);
        entry.setPharmacist(pharmacist);
        entry.setWitness(witness);
        entry.setTransactionDate(LocalDateTime.now());
        entry.setNotes(request.getNotes());

        String previousHash = registerRepository.findTopByOrderByIdDesc()
                .map(ControlledSubstanceRegister::getEntryHash)
                .orElse(GENESIS_HASH);
        entry.setPreviousHash(previousHash);
        entry.setEntryHash(computeHash(previousHash, entry));

        ControlledSubstanceRegister saved = registerRepository.save(entry);

        auditLogService.log(pharmacist, "CONTROLLED_SUBSTANCE_TRANSACTION", "ControlledSubstanceRegister", saved.getId(),
                null, request.getTransactionType() + " of " + quantitySigned.abs() + " units of " + drug.getGenericName()
                        + ", co-signed by " + technician.getFullName(), "SUCCESS", null);

        List<String> warnings = buildWarnings(drug, prescription);

        return new CsTransactionResponse(toEntryResponse(saved), warnings);
    }

    private BigDecimal applyTransactionAndGetSignedQuantity(Drug drug, CsTransactionCreateRequest request,
                                                             User pharmacist, Prescription prescription) {
        CsTransactionType type = request.getTransactionType();
        BigDecimal magnitude = request.getQuantity();

        switch (type) {
            case DISPENSING: {
                if (magnitude.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Quantity must be positive for DISPENSING");
                }
                FefoPlanResponse result = fefoAllocationService.consume(drug.getId(), magnitude,
                        "CONTROLLED_SUBSTANCE", prescription != null ? prescription.getId() : null, pharmacist);
                return magnitude.negate();
            }
            case RECEIPT: {
                if (magnitude.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Quantity must be positive for RECEIPT");
                }
                DrugBatch batch = requireBatch(request.getBatchId());
                batchService.applyAdjustment(batch.getId(), magnitude);
                stockMovementService.record(batch, MovementType.RECEIPT, magnitude,
                        "CONTROLLED_SUBSTANCE", null, request.getNotes(), pharmacist);
                return magnitude;
            }
            case RETURN: {
                if (magnitude.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Quantity must be positive for RETURN");
                }
                DrugBatch batch = requireBatch(request.getBatchId());
                batchService.applyAdjustment(batch.getId(), magnitude);
                stockMovementService.record(batch, MovementType.RETURN, magnitude,
                        "CONTROLLED_SUBSTANCE", null, request.getNotes(), pharmacist);
                return magnitude;
            }
            case DISPOSAL: {
                if (magnitude.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Quantity must be positive for DISPOSAL");
                }
                DrugBatch batch = requireBatch(request.getBatchId());
                batchService.applyAdjustment(batch.getId(), magnitude.negate());
                stockMovementService.record(batch, MovementType.DISPOSAL, magnitude.negate(),
                        "CONTROLLED_SUBSTANCE", null, request.getNotes(), pharmacist);
                return magnitude.negate();
            }
            case ADJUSTMENT: {
                if (magnitude.compareTo(BigDecimal.ZERO) == 0) {
                    throw new IllegalArgumentException("Adjustment quantity cannot be zero");
                }
                DrugBatch batch = requireBatch(request.getBatchId());
                // CS dual-authorization already IS the approval mechanism —
                // bypasses Module 8's separate 5% supervisor-approval workflow.
                batchService.applyAdjustment(batch.getId(), magnitude);
                stockMovementService.record(batch, MovementType.ADJUSTMENT, magnitude,
                        "CONTROLLED_SUBSTANCE", null, request.getNotes(), pharmacist);
                return magnitude;
            }
            default:
                throw new IllegalArgumentException("Unsupported transaction type: " + type);
        }
    }

    private void enforceQuantityAndRefillLimits(Drug drug, CsTransactionCreateRequest request, Prescription prescription) {
        if (request.getTransactionType() != CsTransactionType.DISPENSING) {
            return;
        }
        if (drug.getMaxPrescriptionQtyPerFill() != null
                && request.getQuantity().compareTo(BigDecimal.valueOf(drug.getMaxPrescriptionQtyPerFill())) > 0) {
            throw new IllegalArgumentException(
                    "Requested quantity exceeds the maximum allowed per fill for '" + drug.getGenericName()
                            + "' (" + drug.getMaxPrescriptionQtyPerFill() + ")");
        }
        if (prescription != null && drug.getMaxRefillsAllowed() != null) {
            long priorDispensingCount = registerRepository.countByPrescriptionIdAndTransactionType(
                    prescription.getId(), CsTransactionType.DISPENSING);
            if (priorDispensingCount >= drug.getMaxRefillsAllowed()) {
                throw new IllegalArgumentException(
                        "This prescription has already reached its maximum allowed refills (" + drug.getMaxRefillsAllowed() + ")");
            }
        }
    }

    private List<String> buildWarnings(Drug drug, Prescription prescription) {
        List<String> warnings = new ArrayList<>();
        if (prescription == null) {
            return warnings;
        }
        LocalDateTime lookback = LocalDateTime.now().minusDays(30);
        Set<Long> distinctDoctors = new HashSet<>();
        prescriptionRepository.findByPatientIdOrderByReceiptDateDesc(prescription.getPatient().getId(),
                        org.springframework.data.domain.PageRequest.of(0, 50))
                .getContent().stream()
                .filter(p -> Boolean.TRUE.equals(p.isControlled()))
                .filter(p -> p.getReceiptDate() != null && p.getReceiptDate().isAfter(lookback))
                .forEach(p -> distinctDoctors.add(p.getDoctor().getId()));
        distinctDoctors.add(prescription.getDoctor().getId());

        if (distinctDoctors.size() > 1) {
            warnings.add("Patient has received controlled-substance prescriptions from " + distinctDoctors.size()
                    + " different prescribers in the last 30 days — recommend clinical review");
        }
        return warnings;
    }

    @Transactional(readOnly = true)
    public PageResponse<CsRegisterEntryResponse> getRegister(Long drugId, CsTransactionType type, Pageable pageable) {
        Page<ControlledSubstanceRegister> page = registerRepository.search(drugId, type, pageable);
        return PageResponse.from(page.map(this::toEntryResponse));
    }

    @Transactional
    public ReconciliationResponse reconcile(ReconcileRequest request, String actorEmail) {
        return doReconciliation(request.getDrugId(), request.getCountedQuantity(), request.getNotes(), actorEmail, false);
    }

    @Transactional
    public ReconciliationResponse reportDiscrepancy(ReconcileRequest request, String actorEmail) {
        return doReconciliation(request.getDrugId(), request.getCountedQuantity(), request.getNotes(), actorEmail, true);
    }

    private ReconciliationResponse doReconciliation(Long drugId, BigDecimal countedQuantity, String notes,
                                                     String actorEmail, boolean forceFlag) {
        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new DrugNotFoundException("Drug not found: " + drugId));
        User actor = requireUser(actorEmail);

        BigDecimal expected = drugBatchRepository.sumActiveQuantityByDrug(drugId);
        BigDecimal variance = countedQuantity.subtract(expected);
        boolean flagged = forceFlag || variance.compareTo(BigDecimal.ZERO) != 0;

        ControlledSubstanceReconciliation reconciliation = new ControlledSubstanceReconciliation();
        reconciliation.setDrug(drug);
        reconciliation.setExpectedQuantity(expected);
        reconciliation.setCountedQuantity(countedQuantity);
        reconciliation.setVariance(variance);
        reconciliation.setPerformedBy(actor);
        reconciliation.setDiscrepancyFlagged(flagged);
        reconciliation.setManuallyReported(forceFlag);
        reconciliation.setNotes(notes);

        ControlledSubstanceReconciliation saved = reconciliationRepository.save(reconciliation);

        if (flagged) {
            auditLogService.log(actor, "CONTROLLED_SUBSTANCE_DISCREPANCY", "ControlledSubstanceReconciliation",
                    saved.getId(), null,
                    "Discrepancy of " + variance + " units for '" + drug.getGenericName() + "'. Notes: " + notes,
                    "FAILURE", "Balance variance detected");
        }
        notificationService.notifyRoles(
                    java.util.Set.of(RoleName.ROLE_PHARMACIST, RoleName.ROLE_ADMIN),
                    com.pharmacy.pipms.notification.entity.NotificationType.CONTROLLED_SUBSTANCE_DISCREPANCY,
                    com.pharmacy.pipms.notification.entity.NotificationPriority.CRITICAL,
                    "Controlled-substance discrepancy: '" + drug.getGenericName() + "' variance of " + variance,
                    "ControlledSubstanceReconciliation", saved.getId());
        return toReconciliationResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReconciliationResponse> getDiscrepancies() {
        return reconciliationRepository.findByDiscrepancyFlaggedTrueOrderByCreatedAtDesc().stream()
                .map(this::toReconciliationResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CsDrugSummaryResponse> getReports() {
        return drugRepository.findAll().stream()
                .filter(d -> CONTROLLED_SCHEDULES.contains(d.getSchedule()))
                .map(drug -> {
                    List<ControlledSubstanceRegister> entries = registerRepository.findByDrugIdOrderByTransactionDateAsc(drug.getId());
                    BigDecimal received = sumByType(entries, CsTransactionType.RECEIPT);
                    BigDecimal dispensed = sumByType(entries, CsTransactionType.DISPENSING).abs();
                    BigDecimal returned = sumByType(entries, CsTransactionType.RETURN);
                    BigDecimal disposed = sumByType(entries, CsTransactionType.DISPOSAL).abs();
                    BigDecimal balance = drugBatchRepository.sumActiveQuantityByDrug(drug.getId());
                    return new CsDrugSummaryResponse(drug.getId(), drug.getGenericName(), drug.getSchedule().name(),
                            received, dispensed, returned, disposed, balance, entries.size());
                })
                .collect(Collectors.toList());
    }

    private BigDecimal sumByType(List<ControlledSubstanceRegister> entries, CsTransactionType type) {
        return entries.stream().filter(e -> e.getTransactionType() == type)
                .map(ControlledSubstanceRegister::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public IntegrityCheckResponse verifyIntegrity() {
        List<ControlledSubstanceRegister> all = registerRepository.findAllByOrderByIdAsc();
        List<Long> tampered = new ArrayList<>();
        String expectedPreviousHash = GENESIS_HASH;

        for (ControlledSubstanceRegister entry : all) {
            if (!entry.getPreviousHash().equals(expectedPreviousHash)) {
                tampered.add(entry.getId());
            }
            String recomputed = computeHash(entry.getPreviousHash(), entry);
            if (!recomputed.equals(entry.getEntryHash())) {
                tampered.add(entry.getId());
            }
            expectedPreviousHash = entry.getEntryHash();
        }

        return new IntegrityCheckResponse(tampered.isEmpty(), all.size(), tampered);
    }

    private DrugBatch requireBatch(Long batchId) {
        if (batchId == null) {
            throw new IllegalArgumentException("A batch ID is required for this transaction type");
        }
        return batchService.getBatchEntity(batchId);
    }

    private void requireValidReauth(User pharmacist) {
        ControlledSubstanceAuthorization auth = authorizationRepository
                .findTopByUserIdOrderByExpiresAtDesc(pharmacist.getId())
                .orElseThrow(() -> new ControlledSubstanceAuthException(
                        "No active controlled-substance re-authentication session — call /reauthenticate first"));
        if (auth.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ControlledSubstanceAuthException(
                    "Controlled-substance re-authentication session has expired — re-authenticate again");
        }
    }

    private void verifyPin(User user, String rawPin, String roleLabel) {
        if (user.getControlledSubstancePinHash() == null) {
            throw new ControlledSubstanceAuthException(
                    "The " + roleLabel + " has not set a controlled-substance PIN yet");
        }
        if (!passwordEncoder.matches(rawPin, user.getControlledSubstancePinHash())) {
            throw new ControlledSubstanceAuthException("Invalid " + roleLabel + " PIN");
        }
    }

    private String computeHash(String previousHash, ControlledSubstanceRegister entry) {
        String canonical = previousHash + "|" + entry.getDrug().getId() + "|" + entry.getTransactionType() + "|"
                + entry.getQuantity().toPlainString() + "|" + entry.getBalanceAfter().toPlainString() + "|"
                + entry.getTechnician().getId() + "|" + entry.getPharmacist().getId() + "|"
                + (entry.getWitness() != null ? entry.getWitness().getId() : "NONE") + "|"
                + entry.getTransactionDate() + "|" + (entry.getNotes() != null ? entry.getNotes() : "");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private CsRegisterEntryResponse toEntryResponse(ControlledSubstanceRegister e) {
        return new CsRegisterEntryResponse(
                e.getId(), e.getDrug().getId(), e.getDrug().getGenericName(), e.getTransactionType().name(),
                e.getQuantity(), e.getBalanceAfter(), e.getPrescription() != null ? e.getPrescription().getId() : null,
                e.getTechnician().getFullName(), e.getPharmacist().getFullName(),
                e.getWitness() != null ? e.getWitness().getFullName() : null,
                e.getTransactionDate(), e.getNotes(), e.getEntryHash()
        );
    }

    private ReconciliationResponse toReconciliationResponse(ControlledSubstanceReconciliation r) {
        return new ReconciliationResponse(r.getId(), r.getDrug().getId(), r.getDrug().getGenericName(),
                r.getExpectedQuantity(), r.getCountedQuantity(), r.getVariance(), r.isDiscrepancyFlagged(),
                r.isManuallyReported(), r.getPerformedBy().getFullName(), r.getNotes(), r.getCreatedAt());
    }
}