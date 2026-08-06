package com.pharmacy.pipms.doctor.repository;

import com.pharmacy.pipms.doctor.entity.DoctorProfile;
import com.pharmacy.pipms.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {

    Optional<DoctorProfile> findByUser(User user);

    @Query("SELECT d FROM DoctorProfile d JOIN d.user u WHERE " +
           "(:search IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.licenseNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<DoctorProfile> search(@Param("search") String search, Pageable pageable);
}