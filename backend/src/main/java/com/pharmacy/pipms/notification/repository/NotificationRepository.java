package com.pharmacy.pipms.notification.repository;

import com.pharmacy.pipms.notification.entity.Notification;
import com.pharmacy.pipms.notification.entity.NotificationPriority;
import com.pharmacy.pipms.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndIsReadFalse(Long userId);

    boolean existsByUserIdAndTypeAndReferenceTypeAndReferenceIdAndIsReadFalse(
            Long userId, NotificationType type, String referenceType, Long referenceId);

    @Query("SELECT n FROM Notification n WHERE n.isRead = false AND n.escalated = false " +
           "AND n.priority IN :priorities AND n.createdAt < :cutoff")
    List<Notification> findEscalationCandidates(@Param("priorities") List<NotificationPriority> priorities,
                                                  @Param("cutoff") LocalDateTime cutoff);

    List<Notification> findByEscalatedTrueOrderByCreatedAtDesc();
}