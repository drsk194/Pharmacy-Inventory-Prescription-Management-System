package com.pharmacy.pipms.shift.controller;

import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.shift.dto.ShiftCreateRequest;
import com.pharmacy.pipms.shift.dto.ShiftResponse;
import com.pharmacy.pipms.shift.service.ShiftService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/shifts")
@RequiredArgsConstructor
@Tag(name = "Shifts", description = "Shift definitions and staff shift assignment")
@PreAuthorize("hasAuthority('SYSTEM_CONFIGURE')")
public class ShiftController {

    private final ShiftService shiftService;

    @PostMapping
    public ApiResponse<ShiftResponse> create(@Valid @RequestBody ShiftCreateRequest request) {
        return ApiResponse.success("Shift created", shiftService.create(request));
    }

    @GetMapping
    public ApiResponse<List<ShiftResponse>> getAll() {
        return ApiResponse.success(shiftService.getAll());
    }

    @PatchMapping("/assign")
    public ApiResponse<Void> assign(@RequestParam Long userId, @RequestParam Long shiftId) {
        shiftService.assignToUser(userId, shiftId);
        return ApiResponse.success("Shift assigned", null);
    }
}