package com.pharmacy.pipms.audit.service;

import com.pharmacy.pipms.audit.dto.AuditLogResponse;
import com.pharmacy.pipms.audit.entity.AuditLog;
import com.pharmacy.pipms.audit.repository.AuditLogRepository;
import com.pharmacy.pipms.audit.util.RequestContextHolder;
import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.exception.AuditLogNotFoundException;
import com.pharmacy.pipms.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// Write-only for log(); read-only for search()/getById(). No update/delete
// methods are exposed anywhere — audit records stay immutable, per Section 14.
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(User user, String action, String entityType, Long entityId,
                     String oldValue, String newValue, String result, String failureReason) {
        AuditLog entry = new AuditLog();
        entry.setUser(user);
        entry.setAction(action);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setOldValue(oldValue);
        entry.setNewValue(newValue);
        entry.setResult(result);
        entry.setFailureReason(failureReason);

        RequestContextHolder.RequestContext ctx = RequestContextHolder.get();
        if (ctx != null) {
            entry.setIpAddress(ctx.getIpAddress());
            entry.setRequestId(ctx.getRequestId());
        }

        auditLogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> search(Long userId, String action, String entityType, Long entityId,
                                                  String result, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.search(userId, action, entityType, entityId, result, start, end, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public AuditLogResponse getById(Long id) {
        return toResponse(auditLogRepository.findById(id)
                .orElseThrow(() -> new AuditLogNotFoundException("Audit log entry not found: " + id)));
    }

    private AuditLogResponse toResponse(AuditLog a) {
        return new AuditLogResponse(a.getId(), a.getUser() != null ? a.getUser().getEmail() : null,
                a.getAction(), a.getEntityType(), a.getEntityId(), a.getOldValue(), a.getNewValue(),
                a.getResult(), a.getFailureReason(), a.getIpAddress(), a.getRequestId(), a.getCreatedAt());
    }
}