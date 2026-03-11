package com.example.demo.dto;

import java.time.LocalDate;

public record GroupCategoryUpdateReq(
        Long id,
        String paramName,
        String paramValue,
        String paramType,
        String description,
        String componentCode,
        Integer status,
        Integer isActive,
        Integer isDisplay,
        LocalDate effectiveDate,
        LocalDate endEffectiveDate
) {
}
