package com.pharmacy.pipms.audit.repository;

import com.pharmacy.pipms.audit.entity.AuditLog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Aggregate-only for now — full searchable browsing is Module 18's job.
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.createdAt BETWEEN :start AND :end GROUP BY a.action")
    List<Object[]> countByActionGrouped(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.createdAt BETWEEN :start AND :end")
    long countInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:userId IS NULL OR a.user.id = :userId) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:entityId IS NULL OR a.entityId = :entityId) AND " +
           "(:result IS NULL OR a.result = :result) AND " +
           "a.createdAt BETWEEN :start AND :end ORDER BY a.createdAt DESC")
    Page<AuditLog> search(@Param("userId") Long userId, @Param("action") String action,
                           @Param("entityType") String entityType, @Param("entityId") Long entityId,
                           @Param("result") String result, @Param("start") LocalDateTime start,
                           @Param("end") LocalDateTime end, Pageable pageable);
}