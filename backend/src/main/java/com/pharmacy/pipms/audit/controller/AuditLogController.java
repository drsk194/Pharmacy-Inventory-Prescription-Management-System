package com.pharmacy.pipms.audit.controller;

import com.pharmacy.pipms.audit.dto.AuditLogResponse;
import com.pharmacy.pipms.audit.service.AuditLogService;
import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.common.CsvExportUtil;
import com.pharmacy.pipms.common.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Searchable, immutable system-wide audit trail")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_LOG_READ')")
    public ApiResponse<PageResponse<AuditLogResponse>> search(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        LocalDateTime[] range = resolveRange(startDate, endDate);
        return ApiResponse.success(auditLogService.search(userId, action, entityType, entityId, result,
                range[0], range[1], PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('AUDIT_LOG_READ')")
    public ApiResponse<AuditLogResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(auditLogService.getById(id));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('AUDIT_LOG_READ')")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) String startDate,
                                          @RequestParam(required = false) String endDate) {
        LocalDateTime[] range = resolveRange(startDate, endDate);
        List<AuditLogResponse> entries = auditLogService.search(null, null, null, null, null,
                range[0], range[1], PageRequest.of(0, 5000)).getContent();

        List<String[]> rows = entries.stream()
                .map(a -> new String[]{String.valueOf(a.getId()), a.getUserEmail(), a.getAction(),
                        a.getEntityType(), String.valueOf(a.getEntityId()), a.getResult(),
                        a.getIpAddress(), String.valueOf(a.getTimestamp())})
                .collect(Collectors.toList());

        return CsvExportUtil.toCsvResponse("audit-logs.csv",
                new String[]{"ID", "User", "Action", "Entity Type", "Entity ID", "Result", "IP", "Timestamp"}, rows);
    }

    private LocalDateTime[] resolveRange(String startDate, String endDate) {
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate + "T00:00:00")
                : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate + "T23:59:59") : LocalDateTime.now();
        return new LocalDateTime[]{start, end};
    }
}