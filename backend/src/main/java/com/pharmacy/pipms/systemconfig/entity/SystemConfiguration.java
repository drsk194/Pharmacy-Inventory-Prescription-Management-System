package com.pharmacy.pipms.systemconfig.entity;

import com.pharmacy.pipms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "system_configurations")
@Getter
@Setter
public class SystemConfiguration extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(nullable = false, length = 500)
    private String configValue;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private ConfigDataType dataType;

    @Column(length = 50)
    private String category;

    @Column(length = 500)
    private String description;
}