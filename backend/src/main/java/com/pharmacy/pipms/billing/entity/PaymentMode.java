package com.pharmacy.pipms.billing.entity;

// INSURANCE is deliberately excluded — insurance is handled through the
// dedicated claim workflow (see module notes, Assumption 3), not as a
// direct payment mode here.
public enum PaymentMode {
    CASH, CARD, UPI
}