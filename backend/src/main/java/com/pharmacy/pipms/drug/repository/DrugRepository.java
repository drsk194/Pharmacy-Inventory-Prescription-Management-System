package com.pharmacy.pipms.drug.repository;

import com.pharmacy.pipms.drug.entity.Drug;
import com.pharmacy.pipms.drug.entity.DrugSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DrugRepository extends JpaRepository<Drug, Long> {

    boolean existsByNdcCode(String ndcCode);

    boolean existsByBarcode(String barcode);

    @Query("SELECT d FROM Drug d WHERE " +
           "(:search IS NULL OR LOWER(d.genericName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.brandName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.ndcCode) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:drugClass IS NULL OR d.drugClass = :drugClass) " +
           "AND (:schedule IS NULL OR d.schedule = :schedule) " +
           "AND (:activeOnly = false OR d.active = true)")
    Page<Drug> search(@Param("search") String search,
                       @Param("drugClass") String drugClass,
                       @Param("schedule") DrugSchedule schedule,
                       @Param("activeOnly") boolean activeOnly,
                       Pageable pageable);

    Page<Drug> findByActiveTrue(Pageable pageable);
}