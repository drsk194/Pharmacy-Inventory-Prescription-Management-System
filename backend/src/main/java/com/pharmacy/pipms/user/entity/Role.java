package com.pharmacy.pipms.user.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.common.constants.RoleName;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, unique = true, length = 50)
    private RoleName name;

    @Column(length = 255)
    private String description;

    // EAGER is deliberate here too (same reasoning as User.roles in Module 2):
    // spring.jpa.open-in-view=false means the Hibernate session closes right
    // after the repository call returns. JwtAuthFilter reads permissions
    // *after* that point on every request, so LAZY would throw
    // LazyInitializationException constantly.
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
}