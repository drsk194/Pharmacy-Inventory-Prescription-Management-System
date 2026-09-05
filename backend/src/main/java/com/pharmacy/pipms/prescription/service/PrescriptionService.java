package com.pharmacy.pipms.prescription.service;

import com.pharmacy.pipms.audit.service.AuditLogService;
import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.common.constants.RoleName;
import com.pharmacy.pipms.drug.entity.Drug;
import com.pharmacy.pipms.drug.repository.DrugRepository;
import com.pharmacy.pipms.exception.*;
import com.pharmacy.pipms.doctor.entity.DoctorProfile;
import com.pharmacy.pipms.doctor.repository.DoctorProfileRepository;
import com.pharmacy.pipms.notification.service.NotificationService;
import com.pharmacy.pipms.patient.entity.Patient;
import com.pharmacy.pipms.patient.repository.PatientRepository;
import com.pharmacy.pipms.prescription.dto.*;
import com.pharmacy.pipms.prescription.entity.*;
import com.pharmacy.pipms.prescription.repository.PrescriptionRepository;
import com.pharmacy.pipms.prescription.repository.PrescriptionStatusHistoryRepository;
import com.pharmacy.pipms.user.entity.User;
import com.pharmacy.pipms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionStatusHistoryRepository historyRepository;
    private final PatientRepository patientRepository;
    private final DrugRepository drugRepository;
    private final UserRepository userRepository;
    private final PrescriptionVerificationService verificationService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final DoctorProfileRepository doctorProfileRepository;

    @Transactional
    public PrescriptionResponse create(PrescriptionCreateRequest request, String submitterEmail) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException("Patient not found: " + request.getPatientId()));
        User doctor = doctorProfileRepository.findById(request.getDoctorId())
                .map(DoctorProfile::getUser)
                .orElseGet(() -> userRepository.findById(request.getDoctorId()).orElse(null));
        if (doctor == null) {
            throw new UserNotFoundException("Doctor not found: " + request.getDoctorId());
        }

        boolean doctorHasRole = doctor.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ROLE_DOCTOR);
        boolean doctorHasProfile = doctorProfileRepository.findByUser(doctor).isPresent();
        if (!doctorHasRole || !doctorHasProfile) {
            throw new IllegalArgumentException("The specified user is not registered as a doctor");
        }

        if (request.getSource() == PrescriptionSource.ELECTRONIC
                && (request.getDigitalSignatureReference() == null || request.getDigitalSignatureReference().isBlank())) {
            throw new IllegalArgumentException("Electronic prescriptions require a digital signature reference");
        }

        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setPrescriptionDate(request.getPrescriptionDate());
        prescription.setSource(request.getSource());
        prescription.setNotes(request.getNotes());
        prescription.setDigitalSignatureReference(request.getDigitalSignatureReference());
        prescription.setStatus(PrescriptionStatus.RECEIVED);

        for (PrescriptionItemRequest itemRequest : request.getItems()) {
            Drug drug = drugRepository.findById(itemRequest.getDrugId())
                    .orElseThrow(() -> new DrugNotFoundException("Drug not found: " + itemRequest.getDrugId()));

            PrescriptionItem item = new PrescriptionItem();
            item.setPrescription(prescription);
            item.setDrug(drug);
            item.setPrescribedQuantity(itemRequest.getPrescribedQuantity());
            item.setDosage(itemRequest.getDosage());
            item.setFrequency(itemRequest.getFrequency());
            item.setDuration(itemRequest.getDuration());
            item.setInstructions(itemRequest.getInstructions());
            prescription.getItems().add(item);
        }

        prescription.setControlled(verificationService.isControlled(prescription.getItems()));

        Prescription saved = prescriptionRepository.save(prescription);
        recordHistory(saved, null, PrescriptionStatus.RECEIVED, submitterEmail, "Prescription submitted");
        notificationService.notifyRoles(
                java.util.Set.of(RoleName.ROLE_PHARMACIST, RoleName.ROLE_TECHNICIAN),
                com.pharmacy.pipms.notification.entity.NotificationType.PRESCRIPTION_RECEIVED,
                com.pharmacy.pipms.notification.entity.NotificationPriority.MEDIUM,
                "New prescription received for patient " + patient.getFullName(),
                "Prescription", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public PrescriptionCheckResponse process(Long id, ProcessPrescriptionRequest request, String actorEmail) {
        Prescription prescription = getEntity(id);

        if (prescription.getStatus() != PrescriptionStatus.RECEIVED) {
            throw new InvalidPrescriptionStatusException(
                    "Only prescriptions in RECEIVED status can be processed (current: " + prescription.getStatus() + ")");
        }

        List<VerificationWarningResponse> warnings = verificationService.runAllChecks(
                prescription.getPatient(), prescription.getDoctor(), prescription.getItems());

        PrescriptionStatus previous = prescription.getStatus();
        prescription.setStatus(PrescriptionStatus.UNDER_VERIFICATION);
        Prescription saved = prescriptionRepository.save(prescription);
        recordHistory(saved, previous, PrescriptionStatus.UNDER_VERIFICATION, actorEmail, request.getNotes());

        return new PrescriptionCheckResponse(toResponse(saved), warnings);
    }

    @Transactional
    public PrescriptionCheckResponse verify(Long id, VerifyPrescriptionRequest request, String pharmacistEmail) {
        Prescription prescription = getEntity(id);

        if (prescription.getStatus() != PrescriptionStatus.UNDER_VERIFICATION) {
            throw new InvalidPrescriptionStatusException(
                    "Only prescriptions in UNDER_VERIFICATION status can be verified (current: " + prescription.getStatus() + ")");
        }

        // Hard, non-overridable gate first — no justification can bypass this.
        verificationService.enforceControlledSubstanceAuthorization(prescription.getDoctor(), prescription.getItems());

        List<VerificationWarningResponse> warnings = verificationService.runAllChecks(
                prescription.getPatient(), prescription.getDoctor(), prescription.getItems());

        boolean hasBlockingWarning = warnings.stream().anyMatch(w -> "BLOCKING".equals(w.getSeverity()));
        User pharmacist = userRepository.findByEmail(pharmacistEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (hasBlockingWarning) {
            if (request.getOverrideJustification() == null || request.getOverrideJustification().isBlank()) {
                String blockers = warnings.stream()
                        .filter(w -> "BLOCKING".equals(w.getSeverity()))
                        .map(VerificationWarningResponse::getMessage)
                        .collect(Collectors.joining("; "));
                throw new AllergyConflictException(
                        "Verification blocked by: " + blockers + ". An override justification is required to proceed.");
            }
            auditLogService.log(pharmacist, "PRESCRIPTION_VERIFICATION_OVERRIDE", "Prescription", prescription.getId(),
                    null, "Overrode blocking warnings. Justification: " + request.getOverrideJustification(),
                    "SUCCESS", null);
        }

        PrescriptionStatus previous = prescription.getStatus();
        prescription.setStatus(PrescriptionStatus.VERIFIED);
        prescription.setVerifyingPharmacist(pharmacist);
        Prescription saved = prescriptionRepository.save(prescription);
        recordHistory(saved, previous, PrescriptionStatus.VERIFIED, pharmacistEmail,
                hasBlockingWarning ? "Verified with override: " + request.getOverrideJustification() : "Verified");
        notificationService.notifyUserIfPresent(saved.getDoctor(),
                com.pharmacy.pipms.notification.entity.NotificationType.PRESCRIPTION_VERIFIED,
                com.pharmacy.pipms.notification.entity.NotificationPriority.LOW,
                "Your prescription for " + saved.getPatient().getFullName() + " has been verified",
                "Prescription", saved.getId());
        if (saved.getPatient().getUser() != null) {
            notificationService.notifyUserIfPresent(saved.getPatient().getUser(),
                    com.pharmacy.pipms.notification.entity.NotificationType.PRESCRIPTION_VERIFIED,
                    com.pharmacy.pipms.notification.entity.NotificationPriority.LOW,
                    "Your prescription has been verified and is being prepared", "Prescription", saved.getId());
        }
        return new PrescriptionCheckResponse(toResponse(saved), warnings);
    }

    @Transactional
    public PrescriptionResponse reject(Long id, RejectPrescriptionRequest request, String pharmacistEmail) {
        Prescription prescription = getEntity(id);

        if (prescription.getStatus() != PrescriptionStatus.RECEIVED
                && prescription.getStatus() != PrescriptionStatus.UNDER_VERIFICATION) {
            throw new InvalidPrescriptionStatusException(
                    "Cannot reject a prescription in status: " + prescription.getStatus());
        }

        PrescriptionStatus previous = prescription.getStatus();
        prescription.setStatus(PrescriptionStatus.REJECTED);
        prescription.setRejectionReason(request.getReason());
        Prescription saved = prescriptionRepository.save(prescription);
        recordHistory(saved, previous, PrescriptionStatus.REJECTED, pharmacistEmail, request.getReason());
        notificationService.notifyUserIfPresent(saved.getDoctor(),
                com.pharmacy.pipms.notification.entity.NotificationType.PRESCRIPTION_REJECTED,
                com.pharmacy.pipms.notification.entity.NotificationPriority.MEDIUM,
                "Prescription for " + saved.getPatient().getFullName() + " was rejected: " + request.getReason(),
                "Prescription", saved.getId());
        if (saved.getPatient().getUser() != null) {
            notificationService.notifyUserIfPresent(saved.getPatient().getUser(),
                    com.pharmacy.pipms.notification.entity.NotificationType.PRESCRIPTION_REJECTED,
                    com.pharmacy.pipms.notification.entity.NotificationPriority.MEDIUM,
                    "Your prescription was rejected: " + request.getReason(), "Prescription", saved.getId());
        }
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getById(Long id, String requesterEmail, boolean hasFullAccess) {
        Prescription prescription = getEntity(id);

        if (!hasFullAccess) {
            User requester = userRepository.findByEmail(requesterEmail)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            boolean isOwningPatient = prescription.getPatient().getUser() != null
                    && prescription.getPatient().getUser().getId().equals(requester.getId());
            boolean isOwningDoctor = prescription.getDoctor().getId().equals(requester.getId());
            if (!isOwningPatient && !isOwningDoctor) {
                throw new AccessDeniedException("You may only view your own prescriptions");
            }
        }
        return toResponse(prescription);
    }

    @Transactional(readOnly = true)
    public PageResponse<PrescriptionResponse> search(PrescriptionStatus status, Long patientId, Long doctorId,
                                                       boolean controlledOnly, Pageable pageable) {
        Page<Prescription> page = prescriptionRepository.search(status, patientId, doctorId, controlledOnly, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getQueue() {
        return prescriptionRepository.findByStatusInOrderByReceiptDateAsc(
                        List.of(PrescriptionStatus.RECEIVED, PrescriptionStatus.UNDER_VERIFICATION))
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<PrescriptionResponse> getMyPrescriptions(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        boolean isDoctor = user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ROLE_DOCTOR);
        if (isDoctor) {
            return PageResponse.from(prescriptionRepository.findByDoctorIdOrderByReceiptDateDesc(user.getId(), pageable)
                    .map(this::toResponse));
        }

        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new PatientNotFoundException("No patient record linked to this account"));
        return PageResponse.from(prescriptionRepository.findByPatientIdOrderByReceiptDateDesc(patient.getId(), pageable)
                .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<PrescriptionStatusHistoryResponse> getHistory(Long id) {
        return historyRepository.findByPrescriptionIdOrderByCreatedAtAsc(id).stream()
                .map(h -> new PrescriptionStatusHistoryResponse(
                        h.getFromStatus() != null ? h.getFromStatus().name() : null,
                        h.getToStatus().name(),
                        h.getChangedBy() != null ? h.getChangedBy().getFullName() : "System",
                        h.getNotes(), h.getCreatedAt()))
                .collect(Collectors.toList());
    }

    private void recordHistory(Prescription prescription, PrescriptionStatus from, PrescriptionStatus to,
                                String actorEmail, String notes) {
        User actor = userRepository.findByEmail(actorEmail).orElse(null);

        PrescriptionStatusHistory history = new PrescriptionStatusHistory();
        history.setPrescription(prescription);
        history.setFromStatus(from);
        history.setToStatus(to);
        history.setChangedBy(actor);
        history.setNotes(notes);
        historyRepository.save(history);

        auditLogService.log(actor, "PRESCRIPTION_STATUS_CHANGE", "Prescription", prescription.getId(),
                from != null ? from.name() : null, to.name(), "SUCCESS", null);
    }

    public Prescription getEntity(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found: " + id));
    }

    private PrescriptionResponse toResponse(Prescription p) {
        List<PrescriptionItemResponse> items = p.getItems().stream()
                .map(i -> new PrescriptionItemResponse(i.getId(), i.getDrug().getId(), i.getDrug().getGenericName(),
                        i.getPrescribedQuantity(), i.getDispensedQuantity(), i.getDosage(), i.getFrequency(),
                        i.getDuration(), i.getInstructions()))
                .collect(Collectors.toList());

        return new PrescriptionResponse(
                p.getId(), p.getPatient().getId(), p.getPatient().getFullName(),
                p.getDoctor().getId(), p.getDoctor().getFullName(),
                p.getPrescriptionDate(), p.getReceiptDate(), p.getSource().name(), p.getStatus().name(),
                p.isControlled(), p.getVerifyingPharmacist() != null ? p.getVerifyingPharmacist().getFullName() : null,
                p.getNotes(), p.getRejectionReason(), items
        );
    }
    // Called by DispensingService after every authorize() — recomputes
    // whether the prescription as a whole is now PARTIAL or fully DISPENSED,
    // based on each item's accumulated dispensedQuantity vs prescribedQuantity.
    @Transactional
    public void refreshStatusAfterDispensing(Long prescriptionId, String actorEmail) {
        Prescription prescription = getEntity(prescriptionId);

        boolean allFullyDispensed = prescription.getItems().stream()
                .allMatch(i -> i.getDispensedQuantity().compareTo(i.getPrescribedQuantity()) >= 0);
        boolean anyDispensed = prescription.getItems().stream()
                .anyMatch(i -> i.getDispensedQuantity().compareTo(java.math.BigDecimal.ZERO) > 0);

        PrescriptionStatus newStatus = allFullyDispensed ? PrescriptionStatus.DISPENSED
                : anyDispensed ? PrescriptionStatus.PARTIAL : prescription.getStatus();

        if (newStatus != prescription.getStatus()) {
            PrescriptionStatus previous = prescription.getStatus();
            prescription.setStatus(newStatus);
            prescriptionRepository.save(prescription);
            recordHistory(prescription, previous, newStatus, actorEmail, "Status updated after dispensing activity");
            if (newStatus == PrescriptionStatus.DISPENSED && prescription.getPatient().getUser() != null) {
                notificationService.notifyUserIfPresent(prescription.getPatient().getUser(),
                        com.pharmacy.pipms.notification.entity.NotificationType.PRESCRIPTION_READY,
                        com.pharmacy.pipms.notification.entity.NotificationPriority.HIGH,
                        "Your prescription is ready for pickup", "Prescription", prescription.getId());
            }
        }
    }
}