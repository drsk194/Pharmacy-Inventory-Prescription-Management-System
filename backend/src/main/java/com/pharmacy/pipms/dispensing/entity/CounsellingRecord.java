package com.pharmacy.pipms.dispensing.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "counselling_records")
@Getter
@Setter
public class CounsellingRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispensing_record_id", nullable = false)
    private DispensingRecord dispensingRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacist_id", nullable = false)
    private User pharmacist;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private CounsellingType counsellingType;

    @Column(length = 1000)
    private String notes;
}