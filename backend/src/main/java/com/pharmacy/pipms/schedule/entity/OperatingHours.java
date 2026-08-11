package com.pharmacy.pipms.schedule.entity;

import com.pharmacy.pipms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "operating_hours", uniqueConstraints = @UniqueConstraint(columnNames = "dayOfWeek"))
@Getter
@Setter
public class OperatingHours extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 15)
    private DayOfWeek dayOfWeek;

    private LocalTime openTime;
    private LocalTime closeTime;

    @Column(nullable = false)
    private boolean closedAllDay = false;
}