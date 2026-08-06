package com.pharmacy.pipms.patient.entity;

// Not explicitly specified in the SRS, but needed for FR5's
// "allergy alert system" to be clinically meaningful.
public enum AllergySeverity {
    MILD, MODERATE, SEVERE, LIFE_THREATENING
}