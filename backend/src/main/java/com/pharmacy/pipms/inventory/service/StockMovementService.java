package com.pharmacy.pipms.inventory.service;

import com.pharmacy.pipms.batch.entity.DrugBatch;
import com.pharmacy.pipms.inventory.dto.StockMovementResponse;
import com.pharmacy.pipms.inventory.entity.MovementType;
import com.pharmacy.pipms.inventory.entity.StockMovement;
import com.pharmacy.pipms.inventory.repository.StockMovementRepository;
import com.pharmacy.pipms.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository movementRepository;

    @Transactional
    public void record(DrugBatch batch, MovementType type, BigDecimal signedQuantity,
                        String referenceType, Long referenceId, String notes, User performedBy) {
        StockMovement movement = new StockMovement();
        movement.setBatch(batch);
        movement.setMovementType(type);
        movement.setQuantity(signedQuantity);
        movement.setBalanceAfter(batch.getCurrentQuantity());
        movement.setReferenceType(referenceType);
        movement.setReferenceId(referenceId);
        movement.setNotes(notes);
        movement.setPerformedBy(performedBy);
        movementRepository.save(movement);
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getHistory(Long batchId) {
        return movementRepository.findByBatchIdOrderByCreatedAtDesc(batchId).stream()
                .map(m -> new StockMovementResponse(
                        m.getId(), m.getMovementType().name(), m.getQuantity(), m.getBalanceAfter(),
                        m.getReferenceType(), m.getReferenceId(), m.getNotes(),
                        m.getPerformedBy() != null ? m.getPerformedBy().getFullName() : "System", m.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}