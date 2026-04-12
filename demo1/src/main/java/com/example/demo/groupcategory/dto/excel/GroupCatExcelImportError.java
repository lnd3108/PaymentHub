package com.example.demo.groupcategory.dto.excel;

public record GroupCatExcelImportError(
        int rowNumber,
        String message
) {
}
