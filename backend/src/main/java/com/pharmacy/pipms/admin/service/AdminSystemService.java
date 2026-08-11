package com.pharmacy.pipms.admin.service;

import com.pharmacy.pipms.admin.dto.*;
import com.pharmacy.pipms.batch.service.BatchService;
import com.pharmacy.pipms.controlledsubstance.repository.ControlledSubstanceReconciliationRepository;
import com.pharmacy.pipms.doctor.repository.DoctorProfileRepository;
import com.pharmacy.pipms.drug.entity.Drug;
import com.pharmacy.pipms.drug.repository.DrugRepository;
import com.pharmacy.pipms.notification.channel.MockEmailChannel;
import com.pharmacy.pipms.notification.channel.MockSmsChannel;
import com.pharmacy.pipms.prescription.entity.PrescriptionStatus;
import com.pharmacy.pipms.prescription.repository.PrescriptionRepository;
import com.pharmacy.pipms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSystemService {

    private final UserRepository userRepository;
    private final DrugRepository drugRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final BatchService batchService;
    private final DoctorProfileRepository doctorProfileRepository;
    private final ControlledSubstanceReconciliationRepository reconciliationRepository;
    private final MockSmsChannel smsChannel;
    private final MockEmailChannel emailChannel;

    @Transactional(readOnly = true)
    public SystemHealthResponse getSystemHealth() {
        boolean dbOk;
        try {
            userRepository.count();
            dbOk = true;
        } catch (Exception e) {
            dbOk = false;
        }

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream().filter(u -> u.isActive()).count();
        long totalDrugs = drugRepository.count();
        long pendingQueue = prescriptionRepository.search(PrescriptionStatus.RECEIVED, null, null, false,
                org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements()
                + prescriptionRepository.search(PrescriptionStatus.UNDER_VERIFICATION, null, null, false,
                org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
        long lowStock = drugRepository.findAll().stream().filter(Drug::isActive).filter(batchService::isLowStock).count();

        return new SystemHealthResponse(dbOk, totalUsers, activeUsers, totalDrugs, pendingQueue, lowStock, LocalDateTime.now());
    }

    public List<IntegrationStatusResponse> getIntegrationStatus() {
        return List.of(
                new IntegrationStatusResponse("Hospital HIS", "NOT_CONFIGURED",
                        "Requires HIS vendor API endpoint and auth credentials — not provisioned"),
                new IntegrationStatusResponse("Drug Database API (NDC/interactions)", "NOT_CONFIGURED",
                        "Requires a licensed drug-information API subscription — using local DrugInteraction table instead"),
                new IntegrationStatusResponse("Supplier Portal (EDI)", "NOT_CONFIGURED",
                        "Requires per-supplier EDI/API integration agreements"),
                new IntegrationStatusResponse("SMS Notifications", "MOCK",
                        "Console-logged via " + smsChannel.getClass().getSimpleName() + " — needs Twilio/AWS SNS credentials for real dispatch"),
                new IntegrationStatusResponse("Email Notifications", "MOCK",
                        "Console-logged via " + emailChannel.getClass().getSimpleName() + " — needs SendGrid/SES credentials for real dispatch"),
                new IntegrationStatusResponse("Payment Gateway", "NOT_CONFIGURED",
                        "Payments are recorded as entered (Module 15); no live gateway processes cards/UPI"),
                new IntegrationStatusResponse("CDSCO Regulatory Database", "NOT_CONFIGURED",
                        "Requires government API access for controlled-substance classification verification")
        );
    }

    public BackupStatusResponse getBackupStatus() {
        return new BackupStatusResponse("NOT_CONFIGURED", null, "DAILY (intended)",
                "Structure only, per FR12 — no real backup job runs. Real implementation is an ops/deployment concern " +
                        "(e.g. mysqldump cron job + offsite storage), not application code.");
    }

    @Transactional(readOnly = true)
    public ComplianceDashboardResponse getComplianceDashboard() {
        LocalDate threshold = LocalDate.now().plusDays(30);
        long expiring = doctorProfileRepository.findAll().stream()
                .filter(p -> p.getLicenseExpiryDate() != null && !p.getLicenseExpiryDate().isAfter(threshold))
                .count();
        long unverified = doctorProfileRepository.findAll().stream().filter(p -> !p.isVerified()).count();
        long discrepancies = reconciliationRepository.findByDiscrepancyFlaggedTrueOrderByCreatedAtDesc().size();

        return new ComplianceDashboardResponse(expiring, unverified, discrepancies,
                "Regulatory submission deadlines and formal inspection-readiness scoring are not modeled — " +
                        "the SRS gives no schema for that data. This dashboard reflects only what's genuinely trackable today.");
    }
}