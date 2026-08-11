package com.pharmacy.pipms.notification.channel;

import com.pharmacy.pipms.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class MockEmailChannel implements NotificationChannel {
    @Override
    public void send(User recipient, String message) {
        System.out.println("[MOCK EMAIL] To " + recipient.getEmail() + ": " + message);
    }
}