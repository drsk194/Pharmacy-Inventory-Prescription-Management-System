package com.pharmacy.pipms.shift.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ShiftResponse {
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean active;
    private List<Long> assignedUserIds;
    private List<String> assignedUserNames;
}