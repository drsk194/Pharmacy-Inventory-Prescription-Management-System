package com.pharmacy.pipms.notification.channel;

import com.pharmacy.pipms.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class MockSmsChannel implements NotificationChannel {
    @Override
    public void send(User recipient, String message) {
        System.out.println("[MOCK SMS] To " + recipient.getPhoneNumber() + " (" + recipient.getFullName()
                + "): " + message);
    }
}