package com.pharmacy.pipms.interaction.repository;

import com.pharmacy.pipms.interaction.entity.DrugInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DrugInteractionRepository extends JpaRepository<DrugInteraction, Long> {

    // Interactions are symmetric — A-B is the same as B-A — so check both orderings.
    @Query("SELECT i FROM DrugInteraction i WHERE " +
           "(i.drugA.id = :drugId1 AND i.drugB.id = :drugId2) OR " +
           "(i.drugA.id = :drugId2 AND i.drugB.id = :drugId1)")
    List<DrugInteraction> findBetween(@Param("drugId1") Long drugId1, @Param("drugId2") Long drugId2);
}