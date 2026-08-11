package com.pharmacy.pipms.schedule.service;

import com.pharmacy.pipms.exception.DuplicateResourceException;
import com.pharmacy.pipms.exception.ResourceNotFoundException;
import com.pharmacy.pipms.schedule.dto.*;
import com.pharmacy.pipms.schedule.entity.OperatingHours;
import com.pharmacy.pipms.schedule.entity.PharmacyHoliday;
import com.pharmacy.pipms.schedule.repository.OperatingHoursRepository;
import com.pharmacy.pipms.schedule.repository.PharmacyHolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final PharmacyHolidayRepository holidayRepository;
    private final OperatingHoursRepository operatingHoursRepository;

    @Transactional
    public HolidayResponse createHoliday(HolidayCreateRequest request) {
        if (holidayRepository.existsByDate(request.getDate())) {
            throw new DuplicateResourceException("A holiday entry already exists for " + request.getDate());
        }
        PharmacyHoliday holiday = new PharmacyHoliday();
        holiday.setDate(request.getDate());
        holiday.setDescription(request.getDescription());
        holiday.setClosed(request.isClosed());
        return toHolidayResponse(holidayRepository.save(holiday));
    }

    @Transactional(readOnly = true)
    public List<HolidayResponse> getHolidays(LocalDate start, LocalDate end) {
        return holidayRepository.findByDateBetween(start, end).stream()
                .map(this::toHolidayResponse).collect(Collectors.toList());
    }

    @Transactional
    public OperatingHoursResponse setOperatingHours(OperatingHoursRequest request) {
        OperatingHours hours = operatingHoursRepository.findByDayOfWeek(request.getDayOfWeek())
                .orElseGet(() -> {
                    OperatingHours h = new OperatingHours();
                    h.setDayOfWeek(request.getDayOfWeek());
                    return h;
                });
        hours.setOpenTime(request.getOpenTime());
        hours.setCloseTime(request.getCloseTime());
        hours.setClosedAllDay(request.isClosedAllDay());
        return toHoursResponse(operatingHoursRepository.save(hours));
    }

    @Transactional(readOnly = true)
    public List<OperatingHoursResponse> getOperatingHours() {
        return operatingHoursRepository.findAll().stream().map(this::toHoursResponse).collect(Collectors.toList());
    }

    private HolidayResponse toHolidayResponse(PharmacyHoliday h) {
        return new HolidayResponse(h.getId(), h.getDate(), h.getDescription(), h.isClosed());
    }

    private OperatingHoursResponse toHoursResponse(OperatingHours h) {
        return new OperatingHoursResponse(h.getId(), h.getDayOfWeek(), h.getOpenTime(), h.getCloseTime(), h.isClosedAllDay());
    }
}