package com.example.demo.dto;

import java.time.LocalDate;

public record GroupCategoryUpsertReq(
        String paramName,
        String paramValue,
        String paramType,
        String description,
        String componentCode,
        Integer isActive,
        Integer isDisplay,
        LocalDate effectiveDate,
        LocalDate endEffectiveDate
) {
}