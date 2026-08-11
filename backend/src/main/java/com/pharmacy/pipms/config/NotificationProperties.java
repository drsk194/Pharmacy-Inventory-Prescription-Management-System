package com.pharmacy.pipms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "notification")
@Getter
@Setter
public class NotificationProperties {
    private int escalationWindowMinutes;
}