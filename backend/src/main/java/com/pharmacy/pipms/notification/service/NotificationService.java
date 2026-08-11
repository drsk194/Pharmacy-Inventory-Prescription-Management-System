package com.pharmacy.pipms.notification.service;

import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.common.constants.RoleName;
import com.pharmacy.pipms.exception.NotificationNotFoundException;
import com.pharmacy.pipms.exception.UserNotFoundException;
import com.pharmacy.pipms.notification.channel.NotificationChannel;
import com.pharmacy.pipms.notification.dto.NotificationResponse;
import com.pharmacy.pipms.notification.entity.Notification;
import com.pharmacy.pipms.notification.entity.NotificationPriority;
import com.pharmacy.pipms.notification.entity.NotificationType;
import com.pharmacy.pipms.notification.repository.NotificationRepository;
import com.pharmacy.pipms.user.entity.User;
import com.pharmacy.pipms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final List<NotificationChannel> channels; 
    @Transactional
    public void create(User recipient, NotificationType type, NotificationPriority priority, String message,
                        String referenceType, Long referenceId) {
        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setType(type);
        notification.setPriority(priority);
        notification.setMessage(message);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notificationRepository.save(notification);

        if (priority == NotificationPriority.HIGH || priority == NotificationPriority.CRITICAL) {
            channels.forEach(channel -> channel.send(recipient, message));
        }
    }
    @Transactional
    public void createIfNotDuplicate(User recipient, NotificationType type, NotificationPriority priority,
                                      String message, String referenceType, Long referenceId) {
        boolean exists = notificationRepository.existsByUserIdAndTypeAndReferenceTypeAndReferenceIdAndIsReadFalse(
                recipient.getId(), type, referenceType, referenceId);
        if (!exists) {
            create(recipient, type, priority, message, referenceType, referenceId);
        }
    }

    @Transactional
    public void notifyRoles(Set<RoleName> roles, NotificationType type, NotificationPriority priority,
                             String message, String referenceType, Long referenceId) {
        userRepository.findActiveByRoleIn(roles)
                .forEach(user -> create(user, type, priority, message, referenceType, referenceId));
    }

    @Transactional
    public void notifyUserIfPresent(User user, NotificationType type, NotificationPriority priority,
                                     String message, String referenceType, Long referenceId) {
        if (user != null) {
            create(user, type, priority, message, referenceType, referenceId);
        }
    }

    /** Resolves a recipient from BaseEntity.createdBy (a stamped email) — see module notes, Assumption 5. */
    @Transactional
    public void notifyByCreatedByEmail(String createdByEmail, NotificationType type, NotificationPriority priority,
                                        String message, String referenceType, Long referenceId) {
        if (createdByEmail == null) return;
        userRepository.findByEmail(createdByEmail)
                .ifPresent(user -> create(user, type, priority, message, referenceType, referenceId));
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getMyNotifications(String email, Pageable pageable) {
        User user = requireUser(email);
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        User user = requireUser(email);
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Transactional
    public NotificationResponse markRead(Long id, String email) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found: " + id));
        User user = requireUser(email);
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You may only manage your own notifications");
        }
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public int markAllRead(String email) {
        User user = requireUser(email);
        List<Notification> unread = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId(), org.springframework.data.domain.Pageable.unpaged())
                .stream().filter(n -> !n.isRead()).collect(Collectors.toList());
        LocalDateTime now = LocalDateTime.now();
        unread.forEach(n -> { n.setRead(true); n.setReadAt(now); });
        notificationRepository.saveAll(unread);
        return unread.size();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getEscalated() {
        return notificationRepository.findByEscalatedTrueOrderByCreatedAtDesc().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(n.getId(), n.getMessage(), n.getType().name(), n.getPriority().name(),
                n.isRead(), n.getReadAt(), n.isEscalated(), n.getReferenceType(), n.getReferenceId(), n.getCreatedAt());
    }
}