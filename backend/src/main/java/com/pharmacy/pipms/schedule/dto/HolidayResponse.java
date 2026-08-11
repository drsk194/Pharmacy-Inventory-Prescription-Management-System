package com.pharmacy.pipms.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class HolidayResponse {
    private Long id;
    private LocalDate date;
    private String description;
    private boolean closed;
}