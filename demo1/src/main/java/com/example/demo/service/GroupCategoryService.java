package com.example.demo.service;

import com.example.demo.dto.GroupCategorySearchReq;
import com.example.demo.dto.GroupCategoryUpsertReq;
import com.example.demo.entity.GroupCategory;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.repository.GroupCategoryRepository;
import com.example.demo.specification.GroupCategorySpecification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;

@Service
@Transactional
public class GroupCategoryService {

    private static final Integer STATUS_DRAFT = 1;
    private static final Integer STATUS_PENDING = 3;
    private static final Integer STATUS_APPROVED = 4;
    private static final Integer STATUS_REJECTED = 5;
    private static final Integer STATUS_CANCEL_APPROVE = 7;

    private static final Integer DISPLAY_HIDDEN = 1;
    private static final Integer DISPLAY_VISIBLE = 2;

    private static final Integer ACTIVE_DEFAULT = 1;

    private final GroupCategoryRepository repository;
    private final ObjectMapper objectMapper;
    public GroupCategoryService(GroupCategoryRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public GroupCategory create(GroupCategoryUpsertReq req) {
        validateRequired(req);
        validateDuplicateForUpsert(req, null);

        GroupCategory entity = new GroupCategory();
        applyBaseFields(entity, req);

        entity.setStatus(STATUS_DRAFT);
        entity.setIsActive(req.isActive() == null ? ACTIVE_DEFAULT : req.isActive());
        entity.setIsDisplay(req.isDisplay() == null ? DISPLAY_HIDDEN : req.isDisplay());
        entity.setNewData(null);

        return repository.save(entity);
    }

    public GroupCategory createAndSubmit(GroupCategoryUpsertReq req) {
        validateRequired(req);
        validateDuplicateForUpsert(req, null);

        GroupCategory entity = new GroupCategory();
        applyBaseFields(entity, req);

        entity.setStatus(STATUS_PENDING);
        entity.setIsActive(req.isActive() == null ? ACTIVE_DEFAULT : req.isActive());
        entity.setIsDisplay(req.isDisplay() == null ? DISPLAY_HIDDEN : req.isDisplay());
        entity.setNewData(null);

        return repository.save(entity);
    }

    public GroupCategory update(Long id, GroupCategoryUpsertReq req) {
        validateRequired(req);

        GroupCategory cur = getRequired(id);

        if (isPublished(cur)) {
            GroupCategory preview = buildPreviewEntity(cur, req);
            validateDuplicateForEntity(preview, cur.getId());

            String patchJson = buildPatchJson(cur, req);
            if (!hasMeaningfulNewData(patchJson)) {
                throw new BusinessException("Không có dữ liệu thay đổi");
            }

            cur.setNewData(patchJson);
            return repository.save(cur);
        }

        validateDuplicateForUpsert(req, id);

        applyBaseFields(cur, req);
        cur.setIsActive(req.isActive() == null ? cur.getIsActive() : req.isActive());
        cur.setIsDisplay(req.isDisplay() == null ? cur.getIsDisplay() : req.isDisplay());

        cur.setStatus(STATUS_DRAFT);
        cur.setNewData(null);

        return repository.save(cur);
    }

    public GroupCategory submit(Long id) {
        GroupCategory cur = getRequired(id);

        if (isPending(cur)) {
            throw new BusinessException("Bản ghi đang ở trạng thái chờ phê duyệt");
        }

        if (isPublished(cur)) {
            if (!hasMeaningfulNewData(cur.getNewData())) {
                throw new BusinessException("Không có dữ liệu thay đổi để gửi duyệt");
            }

            GroupCategory preview = cloneEntity(cur);
            applyPatchJson(preview, cur.getNewData());

            validateRequiredEntity(preview);
            validateDuplicateForEntity(preview, cur.getId());

            cur.setStatus(STATUS_PENDING);
            return repository.save(cur);
        }

        validateRequiredEntity(cur);
        validateDuplicateForEntity(cur, cur.getId());

        cur.setStatus(STATUS_PENDING);
        cur.setIsDisplay(DISPLAY_HIDDEN);

        return repository.save(cur);
    }

    public GroupCategory approve(Long id) {
        GroupCategory cur = getRequired(id);

        if (!isPending(cur)) {
            throw new BusinessException("Chỉ bản ghi chờ phê duyệt mới được duyệt");
        }

        if (hasMeaningfulNewData(cur.getNewData())) {
            applyPatchJson(cur, cur.getNewData());
        }

        validateRequiredEntity(cur);
        validateDuplicateForEntity(cur, cur.getId());

        cur.setStatus(STATUS_APPROVED);
        cur.setIsDisplay(DISPLAY_VISIBLE);
        cur.setNewData(null);

        return repository.save(cur);
    }

    public GroupCategory reject(Long id, String reason) {
        GroupCategory cur = getRequired(id);

        if (!isPending(cur)) {
            throw new BusinessException("Chỉ bản ghi chờ phê duyệt mới được từ chối");
        }

        if (hasMeaningfulNewData(cur.getNewData())) {
            cur.setStatus(STATUS_APPROVED);
            cur.setIsDisplay(DISPLAY_VISIBLE);
            cur.setNewData(null);
        } else {
            cur.setStatus(STATUS_REJECTED);
            cur.setIsDisplay(DISPLAY_HIDDEN);
        }

        return repository.save(cur);
    }

    public GroupCategory cancelApprove(Long id) {
        GroupCategory cur = getRequired(id);

        if (!Objects.equals(cur.getStatus(), STATUS_APPROVED)) {
            throw new BusinessException("Chỉ bản ghi đã phê duyệt mới được hủy duyệt");
        }

        cur.setStatus(STATUS_CANCEL_APPROVE);
        cur.setIsDisplay(DISPLAY_HIDDEN);
        cur.setNewData(null);

        return repository.save(cur);
    }

    public void delete(Long id) {
        GroupCategory cur = getRequired(id);

        if (isPending(cur)) {
            throw new BusinessException("Bản ghi đang chờ duyệt thì không được xóa");
        }

        if (isPublished(cur)) {
            throw new BusinessException("Bản ghi đã duyệt / đang hiển thị thì không được xóa");
        }

        repository.delete(cur);
    }

    public Page<GroupCategory> search(GroupCategorySearchReq req, Pageable pageable) {
        return repository.findAll(GroupCategorySpecification.search(req), pageable);
    }

    public Page<GroupCategory> getCategory(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public GroupCategory getCategoryById(Long id) {
        return getRequired(id);
    }


    private GroupCategory getRequired(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy bản ghi với id = " + id));
    }

    private boolean isPending(GroupCategory entity) {
        return Objects.equals(entity.getStatus(), STATUS_PENDING);
    }

    private boolean isPublished(GroupCategory entity) {
        return Objects.equals(entity.getStatus(), STATUS_APPROVED)
                || Objects.equals(entity.getIsDisplay(), DISPLAY_VISIBLE);
    }

    private void validateRequired(GroupCategoryUpsertReq req) {
        if (isBlank(req.paramName())) throw new BusinessException("paramName không được để trống");
        if (isBlank(req.paramValue())) throw new BusinessException("paramValue không được để trống");
        if (isBlank(req.paramType())) throw new BusinessException("paramType không được để trống");
        if (req.effectiveDate() == null) throw new BusinessException("effectiveDate không được để trống");

        if (req.endEffectiveDate() != null && req.endEffectiveDate().isBefore(req.effectiveDate())) {
            throw new BusinessException("endEffectiveDate phải lớn hơn hoặc bằng effectiveDate");
        }
    }

    private void validateRequiredEntity(GroupCategory entity) {
        if (isBlank(entity.getParamName())) throw new BusinessException("paramName không được để trống");
        if (isBlank(entity.getParamValue())) throw new BusinessException("paramValue không được để trống");
        if (isBlank(entity.getParamType())) throw new BusinessException("paramType không được để trống");
        if (entity.getEffectiveDate() == null) throw new BusinessException("effectiveDate không được để trống");

        if (entity.getEndEffectiveDate() != null
                && entity.getEndEffectiveDate().isBefore(entity.getEffectiveDate())) {
            throw new BusinessException("endEffectiveDate phải lớn hơn hoặc bằng effectiveDate");
        }
    }

    private void validateDuplicateForUpsert(GroupCategoryUpsertReq req, Long excludeId) {
        String paramName = normalizeRequired(req.paramName());
        String paramValue = normalizeRequired(req.paramValue());
        String paramType = normalizeRequired(req.paramType());

        boolean exists = excludeId == null
                ? repository.existsByParamNameAndParamValueAndParamType(paramName, paramValue, paramType)
                : repository.existsByParamNameAndParamValueAndParamTypeAndIdNot(paramName, paramValue, paramType, excludeId);

        if (exists) {
            throw new BusinessException("Bản ghi đã tồn tại với bộ paramName + paramValue + paramType");
        }
    }

    private void validateDuplicateForEntity(GroupCategory entity, Long excludeId) {
        boolean exists = repository.existsByParamNameAndParamValueAndParamTypeAndIdNot(
                normalizeRequired(entity.getParamName()),
                normalizeRequired(entity.getParamValue()),
                normalizeRequired(entity.getParamType()),
                excludeId
        );

        if (exists) {
            throw new BusinessException("Bản ghi đã tồn tại với bộ paramName + paramValue + paramType");
        }
    }

    private void applyBaseFields(GroupCategory entity, GroupCategoryUpsertReq req) {
        entity.setParamName(normalizeRequired(req.paramName()));
        entity.setParamValue(normalizeRequired(req.paramValue()));
        entity.setParamType(normalizeRequired(req.paramType()));
        entity.setDescription(normalizeNullable(req.description()));
        entity.setComponentCode(normalizeNullable(req.componentCode()));
        entity.setEffectiveDate(req.effectiveDate());
        entity.setEndEffectiveDate(req.endEffectiveDate());
    }

    private GroupCategory buildPreviewEntity(GroupCategory cur, GroupCategoryUpsertReq req) {
        GroupCategory preview = cloneEntity(cur);

        preview.setParamName(normalizeRequired(req.paramName()));
        preview.setParamValue(normalizeRequired(req.paramValue()));
        preview.setParamType(normalizeRequired(req.paramType()));
        preview.setDescription(normalizeNullable(req.description()));
        preview.setComponentCode(normalizeNullable(req.componentCode()));
        preview.setEffectiveDate(req.effectiveDate());
        preview.setEndEffectiveDate(req.endEffectiveDate());

        if (req.isActive() != null) {
            preview.setIsActive(req.isActive());
        }
        if (req.isDisplay() != null) {
            preview.setIsDisplay(req.isDisplay());
        }

        return preview;
    }

    private GroupCategory cloneEntity(GroupCategory source) {
        GroupCategory target = new GroupCategory();
        target.setId(source.getId());
        target.setParamName(source.getParamName());
        target.setParamValue(source.getParamValue());
        target.setParamType(source.getParamType());
        target.setDescription(source.getDescription());
        target.setComponentCode(source.getComponentCode());
        target.setStatus(source.getStatus());
        target.setIsActive(source.getIsActive());
        target.setIsDisplay(source.getIsDisplay());
        target.setEffectiveDate(source.getEffectiveDate());
        target.setEndEffectiveDate(source.getEndEffectiveDate());
        target.setNewData(source.getNewData());
        return target;
    }

    private String buildPatchJson(GroupCategory cur, GroupCategoryUpsertReq req) {
        ObjectNode patch = objectMapper.createObjectNode();

        putIfChanged(patch, "paramName", cur.getParamName(), normalizeRequired(req.paramName()));
        putIfChanged(patch, "paramValue", cur.getParamValue(), normalizeRequired(req.paramValue()));
        putIfChanged(patch, "paramType", cur.getParamType(), normalizeRequired(req.paramType()));
        putIfChanged(patch, "description", cur.getDescription(), normalizeNullable(req.description()));
        putIfChanged(patch, "componentCode", cur.getComponentCode(), normalizeNullable(req.componentCode()));
        putIfChanged(patch, "effectiveDate", toString(cur.getEffectiveDate()), toString(req.effectiveDate()));
        putIfChanged(patch, "endEffectiveDate", toString(cur.getEndEffectiveDate()), toString(req.endEffectiveDate()));

        if (req.isActive() != null && !Objects.equals(cur.getIsActive(), req.isActive())) {
            patch.put("isActive", req.isActive());
        }

        if (req.isDisplay() != null && !Objects.equals(cur.getIsDisplay(), req.isDisplay())) {
            patch.put("isDisplay", req.isDisplay());
        }

        try {
            return objectMapper.writeValueAsString(patch);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Không serialize được NEW_DATA", e);
        }
    }

    private void applyPatchJson(GroupCategory target, String patchJson) {
        try {
            JsonNode node = objectMapper.readTree(patchJson);

            if (node.has("paramName")) {
                target.setParamName(asTextOrNull(node.get("paramName")));
            }
            if (node.has("paramValue")) {
                target.setParamValue(asTextOrNull(node.get("paramValue")));
            }
            if (node.has("paramType")) {
                target.setParamType(asTextOrNull(node.get("paramType")));
            }
            if (node.has("description")) {
                target.setDescription(asTextOrNull(node.get("description")));
            }
            if (node.has("componentCode")) {
                target.setComponentCode(asTextOrNull(node.get("componentCode")));
            }
            if (node.has("effectiveDate")) {
                target.setEffectiveDate(parseLocalDate(node.get("effectiveDate")));
            }
            if (node.has("endEffectiveDate")) {
                target.setEndEffectiveDate(parseLocalDate(node.get("endEffectiveDate")));
            }
            if (node.has("isActive")) {
                target.setIsActive(node.get("isActive").isNull() ? null : node.get("isActive").asInt());
            }
            if (node.has("isDisplay")) {
                target.setIsDisplay(node.get("isDisplay").isNull() ? null : node.get("isDisplay").asInt());
            }

        } catch (Exception e) {
            throw new RuntimeException("NEW_DATA không đúng định dạng JSON", e);
        }
    }

    private boolean hasMeaningfulNewData(String newData) {
        if (isBlank(newData)) {
            return false;
        }

        String trimmed = newData.trim();
        if ("{}".equals(trimmed) || "null".equalsIgnoreCase(trimmed)) {
            return false;
        }

        try {
            JsonNode node = objectMapper.readTree(trimmed);
            if (node == null || node.isNull()) {
                return false;
            }
            return !(node.isObject() || node.isArray()) || node.size() > 0;
        } catch (Exception e) {
            return true;
        }
    }

    private void putIfChanged(ObjectNode patch, String field, String oldValue, String newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            if (newValue == null) {
                patch.putNull(field);
            } else {
                patch.put(field, newValue);
            }
        }
    }

    private String asTextOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private LocalDate parseLocalDate(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return LocalDate.parse(node.asText());
    }

    private String toString(LocalDate date) {
        return date == null ? null : date.toString();
    }

    private String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}