package com.pharmacy.pipms.supplier.repository;

import com.pharmacy.pipms.supplier.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    boolean existsByDrugLicenseNumber(String drugLicenseNumber);

    boolean existsByEmail(String email);

    @Query("SELECT s FROM Supplier s WHERE " +
           "(:search IS NULL OR LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(s.drugLicenseNumber) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:approvedOnly = false OR s.approved = true) " +
           "AND (:activeOnly = false OR s.active = true)")
    Page<Supplier> search(@Param("search") String search,
                           @Param("approvedOnly") boolean approvedOnly,
                           @Param("activeOnly") boolean activeOnly,
                           Pageable pageable);

    // Reused by Module 13 (Purchase Orders) to enforce Appendix F's rule
    // that only approved, active suppliers may receive POs.
    List<Supplier> findByApprovedTrueAndActiveTrue();
}