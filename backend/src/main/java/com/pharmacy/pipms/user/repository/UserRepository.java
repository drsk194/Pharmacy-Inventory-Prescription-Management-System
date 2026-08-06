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
}