package com.pharmacy.pipms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "procurement.po")
@Getter
@Setter
public class ProcurementProperties {
    private BigDecimal approvalThreshold;
}