package com.pharmacy.pipms.interaction.service;

import com.pharmacy.pipms.drug.entity.Drug;
import com.pharmacy.pipms.drug.repository.DrugRepository;
import com.pharmacy.pipms.exception.DrugNotFoundException;
import com.pharmacy.pipms.interaction.dto.DrugInteractionCreateRequest;
import com.pharmacy.pipms.interaction.dto.DrugInteractionResponse;
import com.pharmacy.pipms.interaction.entity.DrugInteraction;
import com.pharmacy.pipms.interaction.repository.DrugInteractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DrugInteractionService {

    private final DrugInteractionRepository interactionRepository;
    private final DrugRepository drugRepository;

    @Transactional
    public DrugInteractionResponse create(DrugInteractionCreateRequest request) {
        Drug drugA = drugRepository.findById(request.getDrugAId())
                .orElseThrow(() -> new DrugNotFoundException("Drug not found: " + request.getDrugAId()));
        Drug drugB = drugRepository.findById(request.getDrugBId())
                .orElseThrow(() -> new DrugNotFoundException("Drug not found: " + request.getDrugBId()));

        DrugInteraction interaction = new DrugInteraction();
        interaction.setDrugA(drugA);
        interaction.setDrugB(drugB);
        interaction.setSeverity(request.getSeverity());
        interaction.setDescription(request.getDescription());

        return toResponse(interactionRepository.save(interaction));
    }

    @Transactional(readOnly = true)
    public List<DrugInteractionResponse> getAll() {
        return interactionRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    private DrugInteractionResponse toResponse(DrugInteraction i) {
        return new DrugInteractionResponse(i.getId(), i.getDrugA().getId(), i.getDrugA().getGenericName(),
                i.getDrugB().getId(), i.getDrugB().getGenericName(), i.getSeverity().name(), i.getDescription());
    }
}