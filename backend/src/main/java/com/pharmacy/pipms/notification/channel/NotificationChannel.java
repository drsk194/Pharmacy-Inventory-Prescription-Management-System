package com.pharmacy.pipms.notification.channel;

import com.pharmacy.pipms.user.entity.User;

// FR10 + Module 1: external SMS/email integration is an interface with a
// mock implementation for now. A real implementation would need Twilio/AWS
// SNS credentials (SMS) or SendGrid/SES credentials (email) — out of scope
// until those are provisioned.
public interface NotificationChannel {
    void send(User recipient, String message);
}