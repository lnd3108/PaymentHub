package com.example.demo.service;

import com.example.demo.dto.GroupCategorySearchReq;
import com.example.demo.dto.GroupCategoryUpsertReq;
import com.example.demo.entity.GroupCategory;
import com.example.demo.repository.GroupCategoryRepository;
import com.example.demo.repository.GroupCategorySpecs;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class GroupCategoryService {

    private static final int STATUS_DRAFT = 1;
    private static final int STATUS_PENDING = 3;
    private static final int STATUS_APPROVED = 4;
    private static final int STATUS_REJECTED = 5;
    private static final int STATUS_CANCEL_APPROVE = 6;

    private final GroupCategoryRepository repository;
    private final ObjectMapper objectMapper;

    public GroupCategoryService(GroupCategoryRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public GroupCategory create(GroupCategoryUpsertReq req) {
        validateRequired(req);
        validateDuplicateForCreate(req);

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
        GroupCategory entity = create(req);
        entity.setStatus(STATUS_PENDING);
        return repository.save(entity);
    }

    public GroupCategory update(Long id, GroupCategoryUpsertReq req) {
        validateRequired(req);

        GroupCategory cur = getRequired(id);

        if (Objects.equals(cur.getStatus(), STATUS_APPROVED)) {
            GroupCategory pending = cloneForPending(cur, req);
            cur.setNewData(toJson(pending));
            cur.setStatus(STATUS_PENDING);
            return repository.save(cur);
        }

        applyNormalData(cur, req);
        cur.setIsActive(req.isActive() == null ? cur.getIsActive() : req.isActive());
        cur.setIsDisplay(req.isDisplay() == null ? cur.getIsDisplay() : req.isDisplay());

        return repository.save(cur);
    }

    public GroupCategory submit(Long id) {
        GroupCategory cur = getRequired(id);

        if (Objects.equals(cur.getStatus(), STATUS_APPROVED)) {
            throw new RuntimeException("Bản ghi đã được phê duyệt, không thể gửi duyệt lại trực tiếp");
        }

        cur.setStatus(STATUS_PENDING);
        return repository.save(cur);
    }

    public GroupCategory approve(Long id) {
        GroupCategory cur = getRequired(id);

        if (!Objects.equals(cur.getStatus(), STATUS_PENDING)) {
            throw new RuntimeException("Chỉ bản ghi chờ phê duyệt mới được duyệt");
        }

        if (cur.getNewData() != null && !cur.getNewData().isBlank()) {
            try {
                GroupCategory pending = objectMapper.readValue(cur.getNewData(), GroupCategory.class);

                cur.setParamName(pending.getParamName());
                cur.setParamValue(pending.getParamValue());
                cur.setParamType(pending.getParamType());
                cur.setDescription(pending.getDescription());
                cur.setComponentCode(pending.getComponentCode());
                cur.setIsActive(pending.getIsActive());
                cur.setIsDisplay(pending.getIsDisplay());
                cur.setEffectiveDate(pending.getEffectiveDate());
                cur.setEndEffectiveDate(pending.getEndEffectiveDate());

                cur.setNewData(null);
            } catch (Exception e) {
                throw new RuntimeException("NEW_DATA không đúng định dạng JSON", e);
            }
        }

        cur.setStatus(STATUS_APPROVED);
        cur.setIsDisplay(2);
        return repository.save(cur);
    }

    public GroupCategory reject(Long id, String reason) {
        GroupCategory cur = getRequired(id);

        if (!Objects.equals(cur.getStatus(), STATUS_PENDING)) {
            throw new RuntimeException("Chỉ bản ghi chờ phê duyệt mới được từ chối");
        }

        cur.setStatus(STATUS_REJECTED);

        if (reason != null && !reason.isBlank()) {
            String oldDesc = cur.getDescription() == null ? "" : cur.getDescription();
            String suffix = oldDesc.isBlank() ? "" : " | ";
            cur.setDescription(oldDesc + suffix + "Lý do từ chối: " + reason);
        }

        return repository.save(cur);
    }

    public GroupCategory cancelApprove(Long id) {
        GroupCategory cur = getRequired(id);

        if (!Objects.equals(cur.getStatus(), STATUS_PENDING)) {
            throw new RuntimeException("Chỉ bản ghi chờ phê duyệt mới được hủy duyệt");
        }

        cur.setStatus(STATUS_CANCEL_APPROVE);
        cur.setNewData(null);
        return repository.save(cur);
    }

    public void delete(Long id) {
        GroupCategory cur = getRequired(id);

        if (Objects.equals(cur.getStatus(), STATUS_APPROVED) || Objects.equals(cur.getIsDisplay(), 2)) {
            throw new RuntimeException("Bản ghi đã duyệt thì không được xóa");
        }

        repository.delete(cur);
    }

    public Page<GroupCategory> search(GroupCategorySearchReq req, Pageable pageable) {
        return repository.findAll(GroupCategorySpecs.search(req), pageable);
    }

    public List<GroupCategory> getCategory() {
        return repository.findAll();
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

    private void validateDuplicateForCreate(GroupCategoryUpsertReq req) {
        boolean exists = repository.existsByParamNameAndParamValueAndParamType(
                req.paramName(),
                req.paramValue(),
                req.paramType()
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