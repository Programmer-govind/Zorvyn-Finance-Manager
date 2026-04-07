package com.zorvyn.finance.mappers;

import com.zorvyn.finance.dto.request.RecordRequest;
import com.zorvyn.finance.dto.response.RecordResponse;
import com.zorvyn.finance.model.FinancialRecord;
import com.zorvyn.finance.model.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RecordMapper {

    public FinancialRecord toEntity(RecordRequest request, User createdBy) {
        return FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory().trim().toLowerCase())
                .transactionDate(request.getTransactionDate())
                .notes(request.getNotes())
                .createdBy(createdBy)
                .build();
    }

    public RecordResponse toResponse(FinancialRecord record) {
        return RecordResponse.builder()
                .id(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                .category(record.getCategory())
                .transactionDate(record.getTransactionDate())
                .notes(record.getNotes())
                .createdByName(record.getCreatedBy().getFullName())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    public void updateEntity(FinancialRecord record, RecordRequest request) {
        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory().trim().toLowerCase());
        record.setTransactionDate(request.getTransactionDate());
        record.setNotes(request.getNotes());
    }
}