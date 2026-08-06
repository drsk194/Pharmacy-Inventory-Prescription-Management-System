package com.pharmacy.pipms.user.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.common.constants.PermissionName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "permissions")
@Getter
@Setter
public class Permission extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR) // forces plain VARCHAR — see Module 2's Hibernate 7 enum bug
    @Column(nullable = false, unique = true, length = 60)
    private PermissionName name;

    @Column(length = 255)
    private String description;
}