package com.pharmacy.pipms.notification.service;

import com.pharmacy.pipms.audit.service.AuditLogService;
import com.pharmacy.pipms.common.constants.RoleName;
import com.pharmacy.pipms.config.NotificationProperties;
import com.pharmacy.pipms.notification.entity.Notification;
import com.pharmacy.pipms.notification.entity.NotificationPriority;
import com.pharmacy.pipms.notification.entity.NotificationType;
import com.pharmacy.pipms.notification.repository.NotificationRepository;
import com.pharmacy.pipms.user.entity.User;
import com.pharmacy.pipms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class NotificationEscalationJob {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationProperties notificationProperties;
    private final AuditLogService auditLogService;

    // Runs every 30 minutes.
    @Scheduled(cron = "0 */30 * * * *")
    public void run() {
        escalate();
    }

    @Transactional
    public int escalate() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(notificationProperties.getEscalationWindowMinutes());
        List<Notification> candidates = notificationRepository.findEscalationCandidates(
                List.of(NotificationPriority.HIGH, NotificationPriority.CRITICAL), cutoff);

        List<User> admins = userRepository.findActiveByRoleIn(Set.of(RoleName.ROLE_ADMIN));

        for (Notification original : candidates) {
            String message = "ESCALATION: " + original.getType() + " notification for "
                    + original.getUser().getFullName() + " has been unread for over "
                    + notificationProperties.getEscalationWindowMinutes() + " minutes. Original: "
                    + original.getMessage();

            for (User admin : admins) {
                Notification escalation = new Notification();
                escalation.setUser(admin);
                escalation.setType(NotificationType.ESCALATION);
                escalation.setPriority(NotificationPriority.CRITICAL);
                escalation.setMessage(message);
                escalation.setReferenceType("Notification");
                escalation.setReferenceId(original.getId());
                notificationRepository.save(escalation);
            }

            original.setEscalated(true);
            original.setEscalatedAt(LocalDateTime.now());
            notificationRepository.save(original);

            auditLogService.log(null, "NOTIFICATION_ESCALATED", "Notification", original.getId(),
                    null, message, "SUCCESS", null);
        }
        return candidates.size();
    }
}