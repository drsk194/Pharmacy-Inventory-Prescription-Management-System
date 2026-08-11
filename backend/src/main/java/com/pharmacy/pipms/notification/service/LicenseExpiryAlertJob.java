package com.pharmacy.pipms.notification.service;

import com.pharmacy.pipms.common.constants.RoleName;
import com.pharmacy.pipms.doctor.entity.DoctorProfile;
import com.pharmacy.pipms.doctor.repository.DoctorProfileRepository;
import com.pharmacy.pipms.notification.entity.NotificationPriority;
import com.pharmacy.pipms.notification.entity.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class LicenseExpiryAlertJob {

    private static final int LOOKAHEAD_DAYS = 30;

    private final DoctorProfileRepository doctorProfileRepository;
    private final NotificationService notificationService;

    // Runs daily at 3 AM — after the expiry (1 AM) and low-stock (2 AM) jobs.
    @Scheduled(cron = "0 0 3 * * *")
    public void run() {
        scan();
    }

    @Transactional
    public int scan() {
        LocalDate threshold = LocalDate.now().plusDays(LOOKAHEAD_DAYS);
        int alertCount = 0;

        for (DoctorProfile profile : doctorProfileRepository.findAll()) {
            if (profile.getLicenseExpiryDate() == null || profile.getLicenseExpiryDate().isAfter(threshold)) {
                continue;
            }
            String message = "License for Dr. " + profile.getUser().getFullName() + " expires on "
                    + profile.getLicenseExpiryDate();

            notificationService.createIfNotDuplicate(profile.getUser(), NotificationType.LICENSE_RENEWAL,
                    NotificationPriority.HIGH, message, "DoctorProfile", profile.getId());
            notificationService.notifyRoles(Set.of(RoleName.ROLE_ADMIN), NotificationType.LICENSE_RENEWAL,
                    NotificationPriority.MEDIUM, message, "DoctorProfile", profile.getId());
            alertCount++;
        }
        return alertCount;
    }
}