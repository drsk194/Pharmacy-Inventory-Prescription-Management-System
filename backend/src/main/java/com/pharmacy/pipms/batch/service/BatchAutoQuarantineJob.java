package com.pharmacy.pipms.batch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BatchAutoQuarantineJob {

    private final BatchService batchService;

    // Runs daily at 1:00 AM server time.
    @Scheduled(cron = "0 0 1 * * *")
    public void run() {
        batchService.runExpiryCheck();
    }
}