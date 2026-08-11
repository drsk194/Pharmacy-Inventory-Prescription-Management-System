package com.pharmacy.pipms.notification.controller;

import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.drug.entity.Drug;
import com.pharmacy.pipms.drug.repository.DrugRepository;
import com.pharmacy.pipms.exception.DrugNotFoundException;
import com.pharmacy.pipms.notification.dto.BroadcastNotificationRequest;
import com.pharmacy.pipms.notification.dto.ColdChainBreachRequest;
import com.pharmacy.pipms.notification.dto.NotificationResponse;
import com.pharmacy.pipms.notification.entity.NotificationPriority;
import com.pharmacy.pipms.notification.entity.NotificationType;
import com.pharmacy.pipms.notification.service.LowStockAlertJob;
import com.pharmacy.pipms.notification.service.NotificationEscalationJob;
import com.pharmacy.pipms.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Personal notification inbox, admin broadcasts, cold-chain reports, and escalation")
public class NotificationController {

    private final NotificationService notificationService;
    private final LowStockAlertJob lowStockAlertJob;
    private final NotificationEscalationJob escalationJob;
    private final DrugRepository drugRepository;

    @GetMapping("/my")
    public ApiResponse<PageResponse<NotificationResponse>> myNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(notificationService.getMyNotifications(authentication.getName(), PageRequest.of(page, size)));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount(Authentication authentication) {
        return ApiResponse.success(notificationService.getUnreadCount(authentication.getName()));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markRead(@PathVariable Long id, Authentication authentication) {
        return ApiResponse.success("Marked as read", notificationService.markRead(id, authentication.getName()));
    }

    @PatchMapping("/read-all")
    public ApiResponse<String> markAllRead(Authentication authentication) {
        int count = notificationService.markAllRead(authentication.getName());
        return ApiResponse.success(count + " notification(s) marked as read");
    }

    @PostMapping
    @PreAuthorize("hasAuthority('NOTIFICATION_MANAGE')")
    public ApiResponse<String> broadcast(@Valid @RequestBody BroadcastNotificationRequest request) {
        notificationService.notifyRoles(request.getTargetRoles(), request.getType(), request.getPriority(),
                request.getMessage(), "BROADCAST", null);
        return ApiResponse.success("Broadcast sent to " + request.getTargetRoles().size() + " role(s)");
    }

    @PostMapping("/cold-chain-breach")
    @PreAuthorize("hasAuthority('INVENTORY_COUNT')")
    public ApiResponse<String> reportColdChainBreach(@Valid @RequestBody ColdChainBreachRequest request) {
        Drug drug = drugRepository.findById(request.getDrugId())
                .orElseThrow(() -> new DrugNotFoundException("Drug not found: " + request.getDrugId()));

        String message = "Cold-chain breach reported for '" + drug.getGenericName() + "': " + request.getDescription();
        notificationService.notifyRoles(
                java.util.Set.of(com.pharmacy.pipms.common.constants.RoleName.ROLE_PHARMACIST,
                        com.pharmacy.pipms.common.constants.RoleName.ROLE_ADMIN),
                NotificationType.COLD_CHAIN_BREACH, NotificationPriority.CRITICAL, message, "Drug", drug.getId());

        return ApiResponse.success("Cold-chain breach reported and pharmacy staff notified");
    }

    @GetMapping("/escalated")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<NotificationResponse>> escalated() {
        return ApiResponse.success(notificationService.getEscalated());
    }

    // Manual triggers for the scheduled jobs — testing convenience, same
    // pattern as Module 8's /run-expiry-check.
    @PostMapping("/run-low-stock-check")
    @PreAuthorize("hasAuthority('SYSTEM_CONFIGURE')")
    public ApiResponse<String> runLowStockCheck() {
        int count = lowStockAlertJob.scan();
        return ApiResponse.success("Low-stock scan completed", count + " drug(s) alerted");
    }

    @PostMapping("/run-escalation-check")
    @PreAuthorize("hasAuthority('SYSTEM_CONFIGURE')")
    public ApiResponse<String> runEscalationCheck() {
        int count = escalationJob.escalate();
        return ApiResponse.success("Escalation check completed", count + " notification(s) escalated");
    }
}