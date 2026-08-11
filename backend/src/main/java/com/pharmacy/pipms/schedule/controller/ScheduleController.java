package com.pharmacy.pipms.schedule.controller;

import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.schedule.dto.*;
import com.pharmacy.pipms.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/schedule")
@RequiredArgsConstructor
@Tag(name = "Pharmacy Schedule", description = "Holidays and weekly operating hours")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("/holidays")
    @PreAuthorize("hasAuthority('SYSTEM_CONFIGURE')")
    public ApiResponse<HolidayResponse> createHoliday(@Valid @RequestBody HolidayCreateRequest request) {
        return ApiResponse.success("Holiday added", scheduleService.createHoliday(request));
    }

    @GetMapping("/holidays")
    public ApiResponse<List<HolidayResponse>> getHolidays(@RequestParam String startDate, @RequestParam String endDate) {
        return ApiResponse.success(scheduleService.getHolidays(LocalDate.parse(startDate), LocalDate.parse(endDate)));
    }

    @PutMapping("/operating-hours")
    @PreAuthorize("hasAuthority('SYSTEM_CONFIGURE')")
    public ApiResponse<OperatingHoursResponse> setOperatingHours(@Valid @RequestBody OperatingHoursRequest request) {
        return ApiResponse.success("Operating hours set", scheduleService.setOperatingHours(request));
    }

    @GetMapping("/operating-hours")
    public ApiResponse<List<OperatingHoursResponse>> getOperatingHours() {
        return ApiResponse.success(scheduleService.getOperatingHours());
    }
}