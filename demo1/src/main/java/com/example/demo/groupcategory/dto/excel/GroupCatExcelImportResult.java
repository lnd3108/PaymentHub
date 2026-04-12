package com.example.demo.groupcategory.dto.excel;

import java.util.List;

public record GroupCatExcelImportResult(
        int totalRows,
        int successRows,
        int failedRows,
        List<GroupCatExcelImportError> errors
) {
}
