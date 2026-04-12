package com.example.demo.groupcategory.dto.excel;

import java.time.LocalDate;

public record GroupCategoryExcelRow(
        int rowNumber,
        String paramName,
        String paramValue,
        String paramType,
        String description,
        String componentCode,
        Integer isActive,
        Integer isDisplay,
        LocalDate effectiveDate,
        LocalDate endEffectiveDate
) {}