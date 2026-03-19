package com.example.demo.service;

import com.example.demo.dto.GroupCategorySearchReq;
import com.example.demo.dto.GroupCategoryUpsertReq;
import com.example.demo.entity.GroupCategory;
import com.example.demo.repository.GroupCategoryRepository;
import com.example.demo.repository.GroupCategorySpecs;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class GroupCategoryService {

    private static final int STATUS_DRAFT = 1;
    private static final int STATUS_PENDING = 3;
    private static final int STATUS_APPROVED = 4;
    private static final int STATUS_REJECTED = 5;
    private static final int STATUS_CANCEL_APPROVE = 7;

    private static final int DISPLAY_HIDDEN = 1;
    private static final int DISPLAY_VISIBLE = 2;

    private final GroupCategoryRepository repository;
    private final ObjectMapper objectMapper;

    public GroupCategoryService(GroupCategoryRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public GroupCategory create(GroupCategoryUpsertReq req){
        validateRequired(req);
        validateDuplicateForUpsert(req, null);

        GroupCategory entity = new GroupCategory();
        applyNormalData(entity, req);

        entity.setId(null);
        entity.setStatus(STATUS_DRAFT);
        entity.setIsActive(req.isActive() == null ? 1 : req.isActive());
        entity.setIsDisplay(req.isDisplay() == null ? 1 : req.isDisplay());
        entity.setNewData(null);

        return repository.save(entity);
    }

    public GroupCategory createAndSubmit(GroupCategoryUpsertReq req) {
        validateRequired(req);
        validateDuplicateForUpsert(req, null);

        GroupCategory entity = new GroupCategory();
        applyNormalData(entity, req);

        entity.setId(null);
        entity.setStatus(STATUS_PENDING);
        entity.setIsActive(req.isActive() == null ? 1 : req.isActive());
        entity.setIsDisplay(req.isDisplay() == null ? DISPLAY_HIDDEN : req.isDisplay());
        entity.setNewData(null);

        return repository.save(entity);
    }

    public GroupCategory update(Long id, GroupCategoryUpsertReq req) {
        validateRequired(req);
        validateDuplicateForUpsert(req, id);

        GroupCategory cur = getRequired(id);

        if (Objects.equals(cur.getStatus(), STATUS_APPROVED) || Objects.equals(cur.getIsDisplay(), DISPLAY_VISIBLE)) {
            GroupCategory pending = cloneForPending(cur, req);
            cur.setNewData(toJson(pending));

            return repository.save(cur);
        }

        applyNormalData(cur, req);
        cur.setIsActive(req.isActive() == null ? cur.getIsActive() : req.isActive());
        cur.setIsDisplay(req.isDisplay() == null ? cur.getIsDisplay() : req.isDisplay());

        return repository.save(cur);
    }

    public GroupCategory submit(Long id) {
        GroupCategory cur = getRequired(id);

        if (Objects.equals(cur.getStatus(), STATUS_PENDING)) {
            throw new RuntimeException("Bản ghi đang ở trạng thái chờ phê duyệt");
        }

        if ((Objects.equals(cur.getStatus(), STATUS_APPROVED) || Objects.equals(cur.getIsDisplay(), DISPLAY_VISIBLE))
                && !hasMeaningfulNewData(cur.getNewData())) {
            throw new RuntimeException("Không có dữ liệu thay đổi để gửi duyệt");
        }

        cur.setStatus(STATUS_PENDING);

        if(!hasMeaningfulNewData(cur.getNewData()) && !Objects.equals(cur.getIsDisplay(), DISPLAY_VISIBLE)){
            cur.setNewData(toJson(cur));
        }

        return repository.save(cur);
    }

    public GroupCategory approve(Long id) {
        GroupCategory cur = getRequired(id);

        if (!Objects.equals(cur.getStatus(), STATUS_PENDING)) {
            throw new RuntimeException("Chỉ bản ghi chờ phê duyệt mới được duyệt");
        }

        GroupCategory target = cur;

        if (hasMeaningfulNewData(cur.getNewData())) {
            GroupCategory pending = parseNewData(cur.getNewData());
            validateDuplicateForEntity(pending, cur.getId());
            applyEntityData(cur, pending);
        } else {
            validateDuplicateForEntity(cur, cur.getId());
        }

        cur.setStatus(STATUS_APPROVED);
        cur.setIsDisplay(DISPLAY_VISIBLE);
        cur.setNewData(null);

        return repository.save(cur);
    }

    public GroupCategory reject(Long id, String reason) {
        GroupCategory cur = getRequired(id);

        if (!Objects.equals(cur.getStatus(), STATUS_PENDING)) {
            throw new RuntimeException("Chỉ bản ghi chờ phê duyệt mới được từ chối");
        }

        cur.setStatus(STATUS_REJECTED);
        if (cur.getIsDisplay() == null) {
            cur.setIsDisplay(DISPLAY_HIDDEN);
        }
        return repository.save(cur);
    }

    public GroupCategory cancelApprove(Long id) {
        GroupCategory cur = getRequired(id);

        if (!Objects.equals(cur.getStatus(), STATUS_APPROVED)) {
            throw new RuntimeException("Chỉ bản ghi đã phê duyệt mới được hủy duyệt");
        }

        cur.setStatus(STATUS_CANCEL_APPROVE);
        cur.setIsDisplay(DISPLAY_VISIBLE);
        cur.setNewData(null);

        return repository.save(cur);
    }

    public void delete(Long id) {
        GroupCategory cur = getRequired(id);

        if (Objects.equals(cur.getStatus(), STATUS_APPROVED) || Objects.equals(cur.getIsDisplay(), DISPLAY_VISIBLE)) {
            throw new RuntimeException("Bản ghi đã duyệt hoặc đang hiển thị thì không được xóa");
        }

        repository.delete(cur);
    }

    public Page<GroupCategory> search(GroupCategorySearchReq req, Pageable pageable) {
        return repository.findAll(GroupCategorySpecs.search(req), pageable);
    }

    public Page<GroupCategory> getCategory(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public GroupCategory getCategoryById(Long id) {
        return getRequired(id);
    }

    private GroupCategory getRequired(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi với id = " + id));
    }

    private void validateRequired(GroupCategoryUpsertReq req) {
        if (isBlank(req.paramName())) throw new RuntimeException("paramName không được để trống");
        if (isBlank(req.paramValue())) throw new RuntimeException("paramValue không được để trống");
        if (isBlank(req.paramType())) throw new RuntimeException("paramType không được để trống");
        if (req.effectiveDate() == null) throw new RuntimeException("effectiveDate không được để trống");

        if (req.endEffectiveDate() != null && req.endEffectiveDate().isBefore(req.effectiveDate())) {
            throw new RuntimeException("endEffectiveDate phải lớn hơn hoặc bằng effectiveDate");
        }
    }

    private void validateDuplicateForUpsert(GroupCategoryUpsertReq req, Long excludeId) {
        boolean exists = excludeId == null
                ? repository.existsByParamNameAndParamValueAndParamType(
                req.paramName(),
                req.paramValue(),
                req.paramType()
        )
                : repository.existsByParamNameAndParamValueAndParamTypeAndIdNot(
                req.paramName(),
                req.paramValue(),
                req.paramType(),
                excludeId
        );

        if (exists) {
            throw new RuntimeException("Bản ghi đã tồn tại với bộ paramName + paramValue + paramType");
        }
    }

    private void validateDuplicateForEntity(GroupCategory entity, Long excludeId) {
        boolean exists = repository.existsByParamNameAndParamValueAndParamTypeAndIdNot(
                entity.getParamName(),
                entity.getParamValue(),
                entity.getParamType(),
                excludeId
        );

        if (exists) {
            throw new RuntimeException("Bản ghi đã tồn tại với bộ paramName + paramValue + paramType");
        }
    }

    private void applyNormalData(GroupCategory entity, GroupCategoryUpsertReq req) {
        entity.setParamName(req.paramName());
        entity.setParamValue(req.paramValue());
        entity.setParamType(req.paramType());
        entity.setDescription(req.description());
        entity.setComponentCode(req.componentCode());
        entity.setEffectiveDate(req.effectiveDate());
        entity.setEndEffectiveDate(req.endEffectiveDate());
    }

    private void applyEntityData(GroupCategory target, GroupCategory source) {
        target.setParamName(source.getParamName());
        target.setParamValue(source.getParamValue());
        target.setParamType(source.getParamType());
        target.setDescription(source.getDescription());
        target.setComponentCode(source.getComponentCode());
        target.setIsActive(source.getIsActive());
        target.setIsDisplay(source.getIsDisplay());
        target.setEffectiveDate(source.getEffectiveDate());
        target.setEndEffectiveDate(source.getEndEffectiveDate());
    }

    private GroupCategory cloneForPending(GroupCategory cur, GroupCategoryUpsertReq req) {
        GroupCategory pending = new GroupCategory();
        pending.setId(cur.getId());
        pending.setParamName(req.paramName());
        pending.setParamValue(req.paramValue());
        pending.setParamType(req.paramType());
        pending.setDescription(req.description());
        pending.setComponentCode(req.componentCode());
        pending.setStatus(STATUS_PENDING);
        pending.setIsActive(req.isActive() == null ? cur.getIsActive() : req.isActive());
        pending.setIsDisplay(req.isDisplay() == null ? cur.getIsDisplay() : req.isDisplay());
        pending.setEffectiveDate(req.effectiveDate());
        pending.setEndEffectiveDate(req.endEffectiveDate());
        pending.setNewData(null);
        return pending;
    }

    private GroupCategory parseNewData(String newData) {
        try {
            return objectMapper.readValue(newData, GroupCategory.class);
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

            if ((node.isObject() || node.isArray()) && node.size() == 0) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return true;
        }
    }

    private String toJson(GroupCategory req) {
        try {
            return objectMapper.writeValueAsString(req);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Không serialize được NEW_DATA", e);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}