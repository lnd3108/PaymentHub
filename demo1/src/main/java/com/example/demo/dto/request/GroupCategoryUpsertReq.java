package com.example.demo.dto.request;

import java.time.LocalDate;

public record GroupCategoryUpsertReq(
        String paramName,
        String paramValue,
        String paramType,
        String description,
        String componentCode,
        Integer isActive,
        Integer isDisplay,
        String newData,
        LocalDate effectiveDate,
        LocalDate endEffectiveDate
) {
}