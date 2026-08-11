package com.pharmacy.pipms.patient.service;

import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.exception.PatientNotFoundException;
import com.pharmacy.pipms.exception.UserNotFoundException;
import com.pharmacy.pipms.patient.dto.*;
import com.pharmacy.pipms.patient.entity.Patient;
import com.pharmacy.pipms.patient.entity.PatientAllergy;
import com.pharmacy.pipms.patient.entity.PatientMedication;
import com.pharmacy.pipms.patient.repository.PatientAllergyRepository;
import com.pharmacy.pipms.patient.repository.PatientMedicationRepository;
import com.pharmacy.pipms.patient.repository.PatientRepository;
import com.pharmacy.pipms.patient.dto.PatientConditionRequest;
import com.pharmacy.pipms.patient.dto.PatientConditionResponse;
import com.pharmacy.pipms.user.entity.User;
import com.pharmacy.pipms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientAllergyRepository allergyRepository;
    private final PatientMedicationRepository medicationRepository;
    private final UserRepository userRepository;

    @Transactional
    public PatientResponse createPatient(PatientCreateRequest request) {
        Patient patient = new Patient();
        applyFields(patient, request.getFullName(), request.getDateOfBirth(), request.getGender(),
                request.getPhoneNumber(), request.getEmail(), request.getAddress(),
                request.getEmergencyContactName(), request.getEmergencyContactPhone());
        patient.setActive(true);

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + request.getUserId()));
            if (patientRepository.findByUser(user).isPresent()) {
                throw new IllegalArgumentException("This user account is already linked to a patient record");
            }
            patient.setUser(user);
        }

        return toResponse(saveWithMrn(patient));
    }

    @Transactional
    public Patient createStubPatientForUser(User user) {
        Patient patient = new Patient();
        patient.setFullName(user.getFullName());
        patient.setPhoneNumber(user.getPhoneNumber() != null ? user.getPhoneNumber() : "0000000000");
        patient.setDateOfBirth(LocalDate.of(1900, 1, 1)); // placeholder — must be corrected
        patient.setEmail(user.getEmail());
        patient.setUser(user);
        patient.setActive(true);
        return saveWithMrn(patient);
    }

    private Patient saveWithMrn(Patient patient) {
        String tempMrn = "T-" + Long.toString(System.nanoTime(), 36).toUpperCase();
        patient.setMedicalRecordNumber(tempMrn);

        Patient saved = patientRepository.save(patient);
        saved.setMedicalRecordNumber("PT-" + String.format("%06d", saved.getId()));
        return patientRepository.save(saved);
    }

    @Transactional
    public PatientResponse updatePatient(Long id, PatientUpdateRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found: " + id));
        applyFields(patient, request.getFullName(), request.getDateOfBirth(), request.getGender(),
                request.getPhoneNumber(), request.getEmail(), request.getAddress(),
                request.getEmergencyContactName(), request.getEmergencyContactPhone());
        return toResponse(patientRepository.save(patient));
    }

    @Transactional
    public PatientResponse selfUpdate(String email, PatientSelfUpdateRequest request) {
        Patient patient = patientRepository.findByUser(requireUser(email))
                .orElseThrow(() -> new PatientNotFoundException("No patient record linked to this account"));

        if (request.getPhoneNumber() != null) patient.setPhoneNumber(request.getPhoneNumber());
        if (request.getEmail() != null) patient.setEmail(request.getEmail());
        if (request.getAddress() != null) patient.setAddress(request.getAddress());
        if (request.getEmergencyContactName() != null) patient.setEmergencyContactName(request.getEmergencyContactName());
        if (request.getEmergencyContactPhone() != null) patient.setEmergencyContactPhone(request.getEmergencyContactPhone());

        return toResponse(patientRepository.save(patient));
    }

    @Transactional(readOnly = true)
    public PatientResponse getMyPatientRecord(String email) {
        Patient patient = patientRepository.findByUser(requireUser(email))
                .orElseThrow(() -> new PatientNotFoundException("No patient record linked to this account"));
        return toResponse(patient);
    }

    /**
     * Central ownership check for any staff-vs-patient read boundary.
     * hasFullAccess = true for callers holding PATIENT_READ_ALL (staff);
     * everyone else must own the record via their linked User.
     */
    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long id, String requesterEmail, boolean hasFullAccess) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found: " + id));

        if (!hasFullAccess) {
            User requester = requireUser(requesterEmail);
            if (patient.getUser() == null || !patient.getUser().getId().equals(requester.getId())) {
                throw new AccessDeniedException("You may only view your own patient record");
            }
        }
        return toResponse(patient);
    }

    @Transactional(readOnly = true)
    public PageResponse<PatientResponse> searchPatients(String search, Pageable pageable) {
        Page<Patient> page = patientRepository.search(search, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional
    public PatientResponse setActive(Long id, boolean active) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found: " + id));
        patient.setActive(active);
        return toResponse(patientRepository.save(patient));
    }

    @Transactional
    public PatientAllergyResponse addAllergy(Long patientId, PatientAllergyRequest request) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found: " + patientId));

        PatientAllergy allergy = new PatientAllergy();
        allergy.setPatient(patient);
        allergy.setAllergen(request.getAllergen());
        allergy.setSeverity(request.getSeverity());
        allergy.setReactionDescription(request.getReactionDescription());
        allergy.setNotedDate(LocalDate.now());

        return toAllergyResponse(allergyRepository.save(allergy));
    }

    @Transactional(readOnly = true)
    public List<PatientAllergyResponse> getAllergies(Long patientId) {
        requirePatientExists(patientId);
        return allergyRepository.findByPatientId(patientId).stream()
                .map(this::toAllergyResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PatientMedicationResponse addMedication(Long patientId, PatientMedicationRequest request) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found: " + patientId));

        PatientMedication medication = new PatientMedication();
        medication.setPatient(patient);
        medication.setDrugName(request.getDrugName());
        medication.setDosage(request.getDosage());
        medication.setFrequency(request.getFrequency());
        medication.setStartDate(request.getStartDate());
        medication.setEndDate(request.getEndDate());
        medication.setPrescribingDoctor(request.getPrescribingDoctor());
        medication.setNotes(request.getNotes());
        medication.setActive(request.getEndDate() == null);

        return toMedicationResponse(medicationRepository.save(medication));
    }

    @Transactional(readOnly = true)
    public List<PatientMedicationResponse> getMedications(Long patientId) {
        requirePatientExists(patientId);
        return medicationRepository.findByPatientId(patientId).stream()
                .map(this::toMedicationResponse)
                .collect(Collectors.toList());
    }

    private void requirePatientExists(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new PatientNotFoundException("Patient not found: " + patientId);
        }
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private void applyFields(Patient patient, String fullName, LocalDate dob,
                              com.pharmacy.pipms.patient.entity.Gender gender, String phone,
                              String email, String address, String emergencyName, String emergencyPhone) {
        patient.setFullName(fullName);
        patient.setDateOfBirth(dob);
        patient.setGender(gender);
        patient.setPhoneNumber(phone);
        patient.setEmail(email);
        patient.setAddress(address);
        patient.setEmergencyContactName(emergencyName);
        patient.setEmergencyContactPhone(emergencyPhone);
    }

    private PatientResponse toResponse(Patient p) {
        return new PatientResponse(
                p.getId(), p.getMedicalRecordNumber(), p.getFullName(), p.getDateOfBirth(),
                p.getGender() != null ? p.getGender().name() : null,
                p.getPhoneNumber(), p.getEmail(), p.getAddress(),
                p.getEmergencyContactName(), p.getEmergencyContactPhone(), p.isActive(),
                p.getUser() != null ? p.getUser().getId() : null
        );
    }

    private PatientAllergyResponse toAllergyResponse(PatientAllergy a) {
        return new PatientAllergyResponse(a.getId(), a.getAllergen(), a.getSeverity().name(),
                a.getReactionDescription(), a.getNotedDate());
    }

    private PatientMedicationResponse toMedicationResponse(PatientMedication m) {
        return new PatientMedicationResponse(m.getId(), m.getDrugName(), m.getDosage(), m.getFrequency(),
                m.getStartDate(), m.getEndDate(), m.getPrescribingDoctor(), m.getNotes(), m.isActive());
    }
    // Add these fields at the top alongside the existing ones:
    private final com.pharmacy.pipms.patient.repository.PatientConditionRepository conditionRepository;

    @Transactional
    public PatientConditionResponse addCondition(Long patientId, PatientConditionRequest request) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found: " + patientId));

        com.pharmacy.pipms.patient.entity.PatientCondition condition = new com.pharmacy.pipms.patient.entity.PatientCondition();
        condition.setPatient(patient);
        condition.setConditionName(request.getConditionName());
        condition.setDiagnosedDate(request.getDiagnosedDate());
        condition.setNotes(request.getNotes());
        condition.setActive(true);

        com.pharmacy.pipms.patient.entity.PatientCondition saved = conditionRepository.save(condition);
        return new PatientConditionResponse(saved.getId(), saved.getConditionName(), saved.getDiagnosedDate(),
                saved.getNotes(), saved.isActive());
    }

    @Transactional(readOnly = true)
    public List<PatientConditionResponse> getConditions(Long patientId) {
        requirePatientExists(patientId);
        return conditionRepository.findByPatientIdAndActiveTrue(patientId).stream()
                .map(c -> new PatientConditionResponse(c.getId(), c.getConditionName(), c.getDiagnosedDate(),
                        c.getNotes(), c.isActive()))
                .collect(Collectors.toList());
    }
}