package com.example.demo.groupcategory.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.ErrorCode;
import com.example.demo.groupcategory.constant.GroupCategoryConstant;
import com.example.demo.groupcategory.dto.excel.GroupCatExcelImportError;
import com.example.demo.groupcategory.dto.excel.GroupCatExcelImportResult;
import com.example.demo.groupcategory.dto.excel.GroupCategoryExcelRow;
import com.example.demo.groupcategory.dto.request.GroupCategorySearchReq;
import com.example.demo.groupcategory.entity.GroupCategory;
import com.example.demo.groupcategory.repository.GroupCategoryRepository;
import com.example.demo.groupcategory.repository.specification.GroupCategorySpecification;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupCategoryExcelService {

    private static final List<String> EXPORT_HEADERS = List.of(
            "Param Name",
            "Param Value",
            "Param Type",
            "Description",
            "Component Code",
            "Status",
            "Is Active",
            "Is Display",
            "Effective Date",
            "End Effective Date"
    );

    private static final DateTimeFormatter EXPORT_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final List<DateTimeFormatter> IMPORT_DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("d/M/uuuu"),
            DateTimeFormatter.ofPattern("d-M-uuuu"),
            DateTimeFormatter.ofPattern("d.M.uuuu")
    );

    private static final int BATCH_SIZE = 500;

    private final GroupCategoryRepository repository;
    private final DataFormatter dataFormatter = new DataFormatter(Locale.ROOT);

    public byte[] exportExcel(GroupCategorySearchReq req) {
        try {
            Specification<GroupCategory> spec = req == null
                    ? Specification.allOf()
                    : GroupCategorySpecification.search(req);

            Sort sort = buildSort(req);
            List<GroupCategory> rows = repository.findAll(spec, sort);

            try (Workbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                Sheet sheet = workbook.createSheet("group-categories");
                writeHeader(sheet);
                writeData(sheet, rows);

                for (int i = 0; i < EXPORT_HEADERS.size(); i++) {
                    sheet.autoSizeColumn(i);
                }

                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.GC_SEARCH_FAILED, "Xuất Excel thất bại: " + ex.getMessage());
        }
    }

    @Transactional
    public GroupCatExcelImportResult importExcel(MultipartFile file, boolean submitAfterImport) {
        validateFile(file);

        List<GroupCatExcelImportError> errors = new ArrayList<>();
        List<GroupCategoryExcelRow> parsedRows = new ArrayList<>();
        int totalRows = 0;

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "File Excel khong co du lieu");
            }

            Map<String, Integer> headerIndex = readHeaderIndex(sheet.getRow(sheet.getFirstRowNum()));

            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (isEmptyRow(row)) {
                    continue;
                }

                totalRows++;

                try {
                    GroupCategoryExcelRow excelRow = mapExcelRow(row, headerIndex, rowIndex + 1);
                    validateBusiness(excelRow);
                    parsedRows.add(excelRow);
                } catch (BusinessException ex) {
                    errors.add(new GroupCatExcelImportError(rowIndex + 1, ex.getMessage()));
                } catch (RuntimeException ex) {
                    errors.add(new GroupCatExcelImportError(rowIndex + 1, "Du lieu khong hop le: " + ex.getMessage()));
                }
            }

            List<GroupCategoryExcelRow> afterFileDuplicate = filterDuplicateInFile(parsedRows, errors);
            List<GroupCategoryExcelRow> finalValidRows = filterDuplicateInDb(afterFileDuplicate, errors);

            List<GroupCategory> entities = finalValidRows.stream()
                    .map(row -> toEntity(row, submitAfterImport))
                    .toList();

            saveInBatches(entities);

            return new GroupCatExcelImportResult(
                    totalRows,
                    entities.size(),
                    errors.size(),
                    errors
            );

        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Khong doc duoc file Excel");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "File import khong duoc de trong");
        }
    }

    private Sort buildSort(GroupCategorySearchReq req) {
        Set<String> allowedSortFields = Set.of(
                "id",
                "paramName",
                "paramValue",
                "paramType",
                "description",
                "componentCode",
                "status",
                "isActive",
                "isDisplay",
                "effectiveDate",
                "endEffectiveDate"
        );

        String sortBy = (req != null && hasText(req.sortBy()))
                ? req.sortBy().trim()
                : "id";

        if (!allowedSortFields.contains(sortBy)) {
            sortBy = "id";
        }

        Sort.Direction direction = (req != null && "asc".equalsIgnoreCase(req.sortDir()))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, sortBy);
    }

    private void writeHeader(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < EXPORT_HEADERS.size(); i++) {
            headerRow.createCell(i).setCellValue(EXPORT_HEADERS.get(i));
        }
    }

    private void writeData(Sheet sheet, List<GroupCategory> rows) {
        int rowIndex = 1;
        for (GroupCategory item : rows) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(stringValue(item.getParamName()));
            row.createCell(1).setCellValue(stringValue(item.getParamValue()));
            row.createCell(2).setCellValue(stringValue(item.getParamType()));
            row.createCell(3).setCellValue(stringValue(item.getDescription()));
            row.createCell(4).setCellValue(stringValue(item.getComponentCode()));
            row.createCell(5).setCellValue(numberValue(item.getStatus()));
            row.createCell(6).setCellValue(numberValue(item.getIsActive()));
            row.createCell(7).setCellValue(numberValue(item.getIsDisplay()));
            row.createCell(8).setCellValue(dateValue(item.getEffectiveDate()));
            row.createCell(9).setCellValue(dateValue(item.getEndEffectiveDate()));
        }
    }

    private Map<String, Integer> readHeaderIndex(Row headerRow) {
        if (headerRow == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "File Excel thieu dong header");
        }

        Map<String, Integer> headerIndex = new HashMap<>();
        for (Cell cell : headerRow) {
            String normalized = normalizeHeader(dataFormatter.formatCellValue(cell));
            if (!normalized.isEmpty()) {
                headerIndex.put(normalized, cell.getColumnIndex());
            }
        }

        for (String required : List.of("paramname", "paramvalue", "paramtype", "effectivedate")) {
            if (!headerIndex.containsKey(required)) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "Header Excel thieu cot bat buoc: " + required);
            }
        }

        return headerIndex;
    }

    private GroupCategoryExcelRow mapExcelRow(Row row, Map<String, Integer> headerIndex, int rowNumber) {
        return new GroupCategoryExcelRow(
                rowNumber,
                readString(row, headerIndex, "paramname"),
                readString(row, headerIndex, "paramvalue"),
                readString(row, headerIndex, "paramtype"),
                readString(row, headerIndex, "description"),
                readString(row, headerIndex, "componentcode"),
                readInteger(row, headerIndex, "isactive"),
                readInteger(row, headerIndex, "isdisplay"),
                readDate(row, headerIndex, "effectivedate"),
                readDate(row, headerIndex, "endeffectivedate")
        );
    }

    private void validateBusiness(GroupCategoryExcelRow row) {
        if (!hasText(row.paramName())) {
            throw new IllegalArgumentException("Param Name khong duoc de trong");
        }
        if (!hasText(row.paramValue())) {
            throw new IllegalArgumentException("Param Value khong duoc de trong");
        }
        if (!hasText(row.paramType())) {
            throw new IllegalArgumentException("Param Type khong duoc de trong");
        }
        if (row.effectiveDate() == null) {
            throw new IllegalArgumentException("Effective Date khong duoc de trong");
        }
        if (row.endEffectiveDate() != null && row.endEffectiveDate().isBefore(row.effectiveDate())) {
            throw new IllegalArgumentException("End Effective Date phai lon hon hoac bang Effective Date");
        }
        if (row.isActive() != null && row.isActive() != 0 && row.isActive() != 1) {
            throw new IllegalArgumentException("Is Active chi nhan 0 hoac 1");
        }
        if (row.isDisplay() != null && row.isDisplay() != 0 && row.isDisplay() != 1) {
            throw new IllegalArgumentException("Is Display chi nhan 0 hoac 1");
        }
    }

    private List<GroupCategoryExcelRow> filterDuplicateInFile(
            List<GroupCategoryExcelRow> rows,
            List<GroupCatExcelImportError> errors
    ) {
        Map<String, Integer> firstSeenRowByKey = new HashMap<>();
        List<GroupCategoryExcelRow> result = new ArrayList<>();

        for (GroupCategoryExcelRow row : rows) {
            String key = buildUniqueKey(row.paramName(), row.paramValue(), row.paramType());

            if (firstSeenRowByKey.containsKey(key)) {
                errors.add(new GroupCatExcelImportError(
                        row.rowNumber(),
                        "Trung du lieu trong file voi dong " + firstSeenRowByKey.get(key)
                ));
                continue;
            }

            firstSeenRowByKey.put(key, row.rowNumber());
            result.add(row);
        }

        return result;
    }

    private List<GroupCategoryExcelRow> filterDuplicateInDb(
            List<GroupCategoryExcelRow> rows,
            List<GroupCatExcelImportError> errors
    ) {
        if (rows.isEmpty()) {
            return rows;
        }

        Set<String> paramNames = rows.stream()
                .map(GroupCategoryExcelRow::paramName)
                .filter(this::hasText)
                .map(this::normalize)
                .collect(Collectors.toSet());

        Set<String> paramValues = rows.stream()
                .map(GroupCategoryExcelRow::paramValue)
                .filter(this::hasText)
                .map(this::normalize)
                .collect(Collectors.toSet());

        Set<String> paramTypes = rows.stream()
                .map(GroupCategoryExcelRow::paramType)
                .filter(this::hasText)
                .map(this::normalize)
                .collect(Collectors.toSet());

        List<GroupCategory> existing = repository.findForImportDuplicateCheck(paramNames, paramValues, paramTypes);

        Set<String> existingKeys = existing.stream()
                .map(item -> buildUniqueKey(item.getParamName(), item.getParamValue(), item.getParamType()))
                .collect(Collectors.toSet());

        List<GroupCategoryExcelRow> result = new ArrayList<>();
        for (GroupCategoryExcelRow row : rows) {
            String key = buildUniqueKey(row.paramName(), row.paramValue(), row.paramType());
            if (existingKeys.contains(key)) {
                errors.add(new GroupCatExcelImportError(
                        row.rowNumber(),
                        "Du lieu da ton tai trong he thong"
                ));
                continue;
            }
            result.add(row);
        }

        return result;
    }

    private GroupCategory toEntity(GroupCategoryExcelRow row, boolean submitAfterImport) {
        GroupCategory entity = new GroupCategory();
        entity.setParamName(trimToNull(row.paramName()));
        entity.setParamValue(trimToNull(row.paramValue()));
        entity.setParamType(trimToNull(row.paramType()));
        entity.setDescription(trimToNull(row.description()));
        entity.setComponentCode(trimToNull(row.componentCode()));
        entity.setEffectiveDate(row.effectiveDate());
        entity.setEndEffectiveDate(row.endEffectiveDate());
        entity.setIsActive(row.isActive() == null ? GroupCategoryConstant.ACTIVE_DEFAULT : row.isActive());
        entity.setIsDisplay(row.isDisplay() == null ? GroupCategoryConstant.DISPLAY_HIDDEN : row.isDisplay());
        entity.setNewData(null);
        entity.setStatus(submitAfterImport
                ? GroupCategoryConstant.STATUS_PENDING
                : GroupCategoryConstant.STATUS_DRAFT);
        return entity;
    }

    private void saveInBatches(List<GroupCategory> entities) {
        if (entities.isEmpty()) {
            return;
        }

        for (int i = 0; i < entities.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, entities.size());
            List<GroupCategory> batch = entities.subList(i, end);
            repository.saveAll(batch);
            repository.flush();
        }
    }

    private String readString(Row row, Map<String, Integer> headerIndex, String key) {
        Integer columnIndex = headerIndex.get(key);
        if (columnIndex == null) {
            return null;
        }

        Cell cell = row.getCell(columnIndex);
        return cell == null ? null : dataFormatter.formatCellValue(cell).trim();
    }

    private Integer readInteger(Row row, Map<String, Integer> headerIndex, String key) {
        String value = readString(row, headerIndex, key);
        if (!hasText(value)) {
            return null;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Cot " + key + " phai la so nguyen");
        }
    }

    private LocalDate readDate(Row row, Map<String, Integer> headerIndex, String key) {
        Integer columnIndex = headerIndex.get(key);
        if (columnIndex == null) {
            return null;
        }

        Cell cell = row.getCell(columnIndex);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        if (DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }

        String rawValue = dataFormatter.formatCellValue(cell).trim();
        if (!hasText(rawValue)) {
            return null;
        }

        for (DateTimeFormatter formatter : IMPORT_DATE_FORMATS) {
            try {
                return LocalDate.parse(rawValue, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        throw new IllegalArgumentException("Cot " + key + " phai theo dinh dang yyyy-MM-dd hoac dd/MM/yyyy");
    }

    private boolean isEmptyRow(Row row) {
        if (row == null) {
            return true;
        }

        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            if (i < 0) {
                continue;
            }

            Cell cell = row.getCell(i);
            if (cell != null && hasText(dataFormatter.formatCellValue(cell))) {
                return false;
            }
        }

        return true;
    }

    private String normalizeHeader(String header) {
        if (header == null) {
            return "";
        }

        String compact = header.trim()
                .toLowerCase(Locale.ROOT)
                .replace("_", "")
                .replace(" ", "");

        Map<String, String> aliases = new HashMap<>();
        aliases.put("paramname", "paramname");
        aliases.put("paramvalue", "paramvalue");
        aliases.put("paramtype", "paramtype");
        aliases.put("description", "description");
        aliases.put("componentcode", "componentcode");
        aliases.put("isactive", "isactive");
        aliases.put("isdisplay", "isdisplay");
        aliases.put("effectivedate", "effectivedate");
        aliases.put("endeffectivedate", "endeffectivedate");

        return aliases.getOrDefault(compact, compact);
    }

    private String buildUniqueKey(String paramName, String paramValue, String paramType) {
        return normalize(paramName) + "|" + normalize(paramValue) + "|" + normalize(paramType);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.ROOT);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (!hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String stringValue(String value) {
        return value == null ? "" : value;
    }

    private double numberValue(Integer value) {
        return value == null ? 0 : value;
    }

    private String dateValue(LocalDate value) {
        return value == null ? "" : value.format(EXPORT_DATE_FORMAT);
    }
}