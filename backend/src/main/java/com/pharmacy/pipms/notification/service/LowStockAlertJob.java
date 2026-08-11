package com.pharmacy.pipms.notification.service;

import com.pharmacy.pipms.batch.repository.DrugBatchRepository;
import com.pharmacy.pipms.batch.service.BatchService;
import com.pharmacy.pipms.common.constants.RoleName;
import com.pharmacy.pipms.drug.entity.Drug;
import com.pharmacy.pipms.drug.repository.DrugRepository;
import com.pharmacy.pipms.notification.entity.NotificationPriority;
import com.pharmacy.pipms.notification.entity.NotificationType;
import com.pharmacy.pipms.user.entity.User;
import com.pharmacy.pipms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class LowStockAlertJob {

    private final DrugRepository drugRepository;
    private final BatchService batchService;
    private final DrugBatchRepository drugBatchRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // Runs daily at 2:00 AM — after Module 8's expiry job (1 AM) so status
    // changes from that job are already reflected in what counts as stock.
    @Scheduled(cron = "0 0 2 * * *")
    public void run() {
        scan();
    }

    @Transactional
    public int scan() {
        List<User> procurementAndAdmins = userRepository.findActiveByRoleIn(
                Set.of(RoleName.ROLE_PROCUREMENT_OFFICER, RoleName.ROLE_ADMIN));
        int alertsSent = 0;

        for (Drug drug : drugRepository.findAll()) {
            if (!drug.isActive() || !batchService.isLowStock(drug)) continue;

            var currentStock = drugBatchRepository.sumActiveQuantityByDrug(drug.getId());
            String message = "Stock alert: '" + drug.getGenericName() + "' is at " + currentStock
                    + " units, at or below its reorder level of " + drug.getReorderLevel();

            for (User recipient : procurementAndAdmins) {
                notificationService.createIfNotDuplicate(recipient, NotificationType.LOW_STOCK,
                        NotificationPriority.HIGH, message, "Drug", drug.getId());
            }
            alertsSent++;
        }
        return alertsSent;
    }
}