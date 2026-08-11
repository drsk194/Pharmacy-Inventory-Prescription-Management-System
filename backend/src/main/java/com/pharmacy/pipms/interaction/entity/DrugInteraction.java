package com.pharmacy.pipms.interaction.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.drug.entity.Drug;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "drug_interactions")
@Getter
@Setter
public class DrugInteraction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drug_a_id", nullable = false)
    private Drug drugA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drug_b_id", nullable = false)
    private Drug drugB;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private InteractionSeverity severity;

    @Column(length = 500)
    private String description;
}