package com.zorvyn.finance.service;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zorvyn.finance.dto.request.RecordRequest;
import com.zorvyn.finance.dto.response.RecordResponse;
import com.zorvyn.finance.enums.TransactionType;
import com.zorvyn.finance.exception.ResourceNotFoundException;
import com.zorvyn.finance.mappers.RecordMapper;
import com.zorvyn.finance.model.FinancialRecord;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.repository.FinancialRecordRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserService               userService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public RecordResponse createRecord(RecordRequest request) {
        User currentUser = getCurrentUser();
        FinancialRecord record = RecordMapper.toEntity(request, currentUser);
        return RecordMapper.toResponse(recordRepository.save(record));
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER','ROLE_ANALYST','ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public Map<String, Object> getAllRecords(
            TransactionType type,
            String          category,
            LocalDate       dateFrom,
            LocalDate       dateTo,
            int             page,
            int             size) {

        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by(Sort.Direction.DESC, "transactionDate")
        );

        Page<FinancialRecord> resultPage = recordRepository
                .findAllFiltered(type, category, dateFrom, dateTo, pageable);

        return Map.of(
                "records",       resultPage.getContent()
                                           .stream()
                                           .map(RecordMapper::toResponse)
                                           .toList(),
                "currentPage",   resultPage.getNumber(),
                "totalPages",    resultPage.getTotalPages(),
                "totalElements", resultPage.getTotalElements(),
                "pageSize",      resultPage.getSize()
        );
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER','ROLE_ANALYST','ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public RecordResponse getRecordById(Long id) {
        return RecordMapper.toResponse(findRecordOrThrow(id));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public RecordResponse updateRecord(Long id, RecordRequest request) {
        FinancialRecord record = findRecordOrThrow(id);
        RecordMapper.updateEntity(record, request);
        return RecordMapper.toResponse(recordRepository.save(record));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public void deleteRecord(Long id) {
        FinancialRecord record = findRecordOrThrow(id);
        record.softDelete();
        recordRepository.save(record);
    }

    // ── Internal helpers ─────────────────────────────────────────────

    private FinancialRecord findRecordOrThrow(Long id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Financial record not found with id: " + id
                ));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();
        String email = auth.getName();
        return userService.findUserOrThrow(
                userService.findUserOrThrow(
                        ((User) auth.getPrincipal()).getId()
                ).getId()
        );
    }
}