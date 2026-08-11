package com.pharmacy.pipms.user.repository;

import com.pharmacy.pipms.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByStaffId(String staffId);

    Optional<User> findByBadgeNumber(String badgeNumber);

    boolean existsByEmail(String email);

    boolean existsByStaffId(String staffId);

    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.staffId = :identifier OR u.badgeNumber = :identifier")
    Optional<User> findByLoginIdentifier(@Param("identifier") String identifier);
    @org.springframework.data.jpa.repository.Query(
        "SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name IN :roleNames AND u.active = true")
    java.util.List<User> findActiveByRoleIn(@org.springframework.data.repository.query.Param("roleNames")
                                             java.util.Set<com.pharmacy.pipms.common.constants.RoleName> roleNames);
        @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE " +
           "(:search IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.staffId) LIKE LOWER(CONCAT('%', :search, '%')))")
    org.springframework.data.domain.Page<User> search(@org.springframework.data.repository.query.Param("search") String search,
                                                        org.springframework.data.domain.Pageable pageable);
}