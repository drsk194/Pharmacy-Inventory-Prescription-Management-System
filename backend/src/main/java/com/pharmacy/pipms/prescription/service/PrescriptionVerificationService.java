package com.pharmacy.pipms.prescription.service;

import com.pharmacy.pipms.doctor.entity.DoctorProfile;
import com.pharmacy.pipms.doctor.repository.DoctorProfileRepository;
import com.pharmacy.pipms.drug.entity.Drug;
import com.pharmacy.pipms.drug.entity.DrugSchedule;
import com.pharmacy.pipms.interaction.entity.DrugInteraction;
import com.pharmacy.pipms.interaction.entity.InteractionSeverity;
import com.pharmacy.pipms.interaction.repository.DrugInteractionRepository;
import com.pharmacy.pipms.patient.entity.Patient;
import com.pharmacy.pipms.patient.entity.PatientAllergy;
import com.pharmacy.pipms.patient.entity.AllergySeverity;
import com.pharmacy.pipms.patient.repository.PatientAllergyRepository;
import com.pharmacy.pipms.patient.repository.PatientConditionRepository;
import com.pharmacy.pipms.prescription.dto.VerificationWarningResponse;
import com.pharmacy.pipms.prescription.entity.PrescriptionItem;
import com.pharmacy.pipms.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The "checking structure" required by FR5: allergy, interaction, duplicate
 * therapy, contraindication, dosage, and controlled-substance checks.
 * Deliberately simple/structural matching — see Assumption 3 in the module
 * notes for why this isn't clinically authoritative.
 */
@Service
@RequiredArgsConstructor
public class PrescriptionVerificationService {

    private static final Set<DrugSchedule> CONTROLLED_SCHEDULES =
            Set.of(DrugSchedule.H, DrugSchedule.H1, DrugSchedule.X);

    private final PatientAllergyRepository allergyRepository;
    private final PatientConditionRepository conditionRepository;
    private final DrugInteractionRepository interactionRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    @Transactional(readOnly = true)
    public List<VerificationWarningResponse> runAllChecks(Patient patient, User doctor, List<PrescriptionItem> items) {
        List<VerificationWarningResponse> warnings = new ArrayList<>();
        warnings.addAll(checkAllergies(patient, items));
        warnings.addAll(checkInteractions(items));
        warnings.addAll(checkDuplicateTherapy(items));
        warnings.addAll(checkContraindications(patient, items));
        warnings.addAll(checkDosageStructure(items));
        warnings.addAll(checkDoctorVerification(doctor));
        return warnings;
    }
    @Transactional(readOnly = true)
    public void enforceControlledSubstanceAuthorization(User doctor, List<PrescriptionItem> items) {
        boolean prescribesControlled = items.stream()
                .anyMatch(item -> CONTROLLED_SCHEDULES.contains(item.getDrug().getSchedule()));
        if (!prescribesControlled) {
            return;
        }
        DoctorProfile profile = doctorProfileRepository.findByUser(doctor).orElse(null);
        if (profile == null || !profile.isControlledSubstanceAuthorized()) {
            throw new com.pharmacy.pipms.exception.ControlledSubstanceAuthException(
                    "Prescriber is not authorized to prescribe controlled substances (Schedule H/H1/X)");
        }
    }

    public boolean isControlled(List<PrescriptionItem> items) {
        return items.stream().anyMatch(item -> CONTROLLED_SCHEDULES.contains(item.getDrug().getSchedule()));
    }

    private List<VerificationWarningResponse> checkAllergies(Patient patient, List<PrescriptionItem> items) {
        List<VerificationWarningResponse> results = new ArrayList<>();
        List<PatientAllergy> allergies = allergyRepository.findByPatientId(patient.getId());

        for (PatientAllergy allergy : allergies) {
            String allergen = allergy.getAllergen().toLowerCase();
            for (PrescriptionItem item : items) {
                Drug drug = item.getDrug();
                boolean matches = containsIgnoreCase(drug.getGenericName(), allergen)
                        || containsIgnoreCase(drug.getBrandName(), allergen)
                        || containsIgnoreCase(drug.getDrugClass(), allergen);
                if (matches) {
                    boolean isSevere = allergy.getSeverity() == AllergySeverity.SEVERE
                            || allergy.getSeverity() == AllergySeverity.LIFE_THREATENING;
                    results.add(new VerificationWarningResponse("ALLERGY", isSevere ? "BLOCKING" : "WARNING",
                            "Patient has a documented " + allergy.getSeverity() + " allergy to '" + allergy.getAllergen()
                                    + "', which may match prescribed drug '" + drug.getGenericName() + "'"));
                }
            }
        }
        return results;
    }

    private List<VerificationWarningResponse> checkInteractions(List<PrescriptionItem> items) {
        List<VerificationWarningResponse> results = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {
                Drug a = items.get(i).getDrug();
                Drug b = items.get(j).getDrug();
                List<DrugInteraction> found = interactionRepository.findBetween(a.getId(), b.getId());
                for (DrugInteraction interaction : found) {
                    boolean isContraindicated = interaction.getSeverity() == InteractionSeverity.CONTRAINDICATED;
                    results.add(new VerificationWarningResponse("INTERACTION",
                            isContraindicated ? "BLOCKING" : "WARNING",
                            interaction.getSeverity() + " interaction between " + a.getGenericName()
                                    + " and " + b.getGenericName()
                                    + (interaction.getDescription() != null ? ": " + interaction.getDescription() : "")));
                }
            }
        }
        return results;
    }

    private List<VerificationWarningResponse> checkDuplicateTherapy(List<PrescriptionItem> items) {
        List<VerificationWarningResponse> results = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {
                Drug a = items.get(i).getDrug();
                Drug b = items.get(j).getDrug();
                if (a.getDrugClass().equalsIgnoreCase(b.getDrugClass())) {
                    results.add(new VerificationWarningResponse("DUPLICATE_THERAPY", "WARNING",
                            "Both '" + a.getGenericName() + "' and '" + b.getGenericName()
                                    + "' belong to the same drug class (" + a.getDrugClass() + ") — possible duplicate therapy"));
                }
            }
        }
        return results;
    }

    private List<VerificationWarningResponse> checkContraindications(Patient patient, List<PrescriptionItem> items) {
        List<VerificationWarningResponse> results = new ArrayList<>();
        var conditions = conditionRepository.findByPatientIdAndActiveTrue(patient.getId());
        for (var condition : conditions) {
            for (PrescriptionItem item : items) {
                // Rough structural keyword match only — see Assumption 3.
                if (containsIgnoreCase(item.getDrug().getDrugClass(), condition.getConditionName())) {
                    results.add(new VerificationWarningResponse("CONTRAINDICATION", "WARNING",
                            "Patient's documented condition '" + condition.getConditionName()
                                    + "' may be relevant to prescribed drug class '" + item.getDrug().getDrugClass()
                                    + "' — pharmacist should clinically review"));
                }
            }
        }
        return results;
    }

    private List<VerificationWarningResponse> checkDosageStructure(List<PrescriptionItem> items) {
        // Structural placeholder — no dosing-guideline database exists to
        // validate against age/weight/renal function (Patient has neither
        // weight nor lab values modeled). Always informational.
        List<VerificationWarningResponse> results = new ArrayList<>();
        for (PrescriptionItem item : items) {
            results.add(new VerificationWarningResponse("DOSAGE", "INFO",
                    "Dosage for '" + item.getDrug().getGenericName() + "' (" + item.getDosage() + " "
                            + item.getFrequency() + ") is not automatically validated against clinical dosing "
                            + "guidelines — pharmacist must confirm appropriateness"));
        }
        return results;
    }

    private List<VerificationWarningResponse> checkDoctorVerification(User doctor) {
        List<VerificationWarningResponse> results = new ArrayList<>();
        DoctorProfile profile = doctorProfileRepository.findByUser(doctor).orElse(null);
        if (profile == null || !profile.isVerified()) {
            results.add(new VerificationWarningResponse("DOCTOR_UNVERIFIED", "WARNING",
                    "Prescribing doctor's license has not been verified by an administrator"));
        }
        return results;
    }

    private boolean containsIgnoreCase(String haystack, String needle) {
        if (haystack == null || needle == null) return false;
        return haystack.toLowerCase().contains(needle.toLowerCase());
    }
}