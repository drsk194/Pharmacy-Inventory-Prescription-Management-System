package com.pharmacy.pipms.dispensing.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "dispensing_errors")
@Getter
@Setter
public class DispensingError extends BaseEntity {

    // Nullable — an error may be reported before a formal record exists
    // (e.g. a near-miss caught during preparation).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispensing_record_id")
    private DispensingRecord dispensingRecord;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private DispensingErrorType errorType;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(length = 1000)
    private String correctiveAction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_id", nullable = false)
    private User reportedBy;
}