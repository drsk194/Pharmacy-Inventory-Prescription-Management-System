package com.pharmacy.pipms.shift.service;

import com.pharmacy.pipms.exception.ResourceNotFoundException;
import com.pharmacy.pipms.exception.UserNotFoundException;
import com.pharmacy.pipms.shift.dto.ShiftCreateRequest;
import com.pharmacy.pipms.shift.dto.ShiftResponse;
import com.pharmacy.pipms.user.entity.Shift;
import com.pharmacy.pipms.user.entity.User;
import com.pharmacy.pipms.user.repository.ShiftRepository;
import com.pharmacy.pipms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;

    @Transactional
    public ShiftResponse create(ShiftCreateRequest request) {
        Shift shift = new Shift();
        shift.setName(request.getName());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setActive(true);
        return toResponse(shiftRepository.save(shift));
    }

    @Transactional
    public ShiftResponse update(Long id, ShiftCreateRequest request) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + id));
        shift.setName(request.getName());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        return toResponse(shiftRepository.save(shift));
    }

    @Transactional(readOnly = true)
    public List<ShiftResponse> getAll() {
        return shiftRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void assignToUser(Long userId, Long shiftId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + shiftId));
        user.setShift(shift);
        userRepository.save(user);
    }

    @Transactional
    public void unassignFromShift(Long userId, Long shiftId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        if (user.getShift() != null && shiftId.equals(user.getShift().getId())) {
            user.setShift(null);
            userRepository.save(user);
        }
    }

    private ShiftResponse toResponse(Shift s) {
        List<User> assignedUsers = userRepository.findByShiftId(s.getId());
        List<Long> assignedIds = assignedUsers.stream().map(User::getId).collect(Collectors.toList());
        List<String> assignedNames = assignedUsers.stream()
                .map(User::getFullName)
                .collect(Collectors.toList());
        return new ShiftResponse(s.getId(), s.getName(), s.getStartTime(), s.getEndTime(), s.isActive(), assignedIds, assignedNames);
    }
}