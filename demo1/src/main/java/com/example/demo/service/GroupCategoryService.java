package com.example.demo.service;

import com.example.demo.common.constant.GroupCategoryConstant;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.ErrorCode;
import com.example.demo.common.paging.PageResponse;
import com.example.demo.common.paging.PagingRequest;
import com.example.demo.common.paging.PagingUtils;
import com.example.demo.dto.request.GroupCategorySearchReq;
import com.example.demo.dto.request.GroupCategoryUpsertReq;
import com.example.demo.dto.response.GroupCategoryResponse;
import com.example.demo.entity.GroupCategory;
import com.example.demo.mapper.GroupCategoryMapper;
import com.example.demo.repository.GroupCategoryRepository;
import com.example.demo.service.helper.GroupCategoryNewDataHelper;
import com.example.demo.service.validator.GroupCategoryValidator;
import com.example.demo.specification.GroupCategorySpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class GroupCategoryService {

    private final GroupCategoryRepository repository;
    private final GroupCategoryValidator validator;
    private final GroupCategoryMapper mapper;
    private final GroupCategoryNewDataHelper newDataHelper;

    public GroupCategoryService(GroupCategoryRepository repository,
                                GroupCategoryValidator validator,
                                GroupCategoryMapper mapper,
                                GroupCategoryNewDataHelper newDataHelper) {
        this.repository = repository;
        this.validator = validator;
        this.mapper = mapper;
        this.newDataHelper = newDataHelper;
    }

    public GroupCategory create(GroupCategoryUpsertReq req) {
        validator.validateRequired(req);
        validator.validateDuplicateForUpsert(req, null);

        GroupCategory entity = new GroupCategory();
        mapper.applyBaseFields(entity, req);

        entity.setStatus(GroupCategoryConstant.STATUS_DRAFT);
        entity.setIsActive(req.isActive() == null ? GroupCategoryConstant.ACTIVE_DEFAULT : req.isActive());
        entity.setIsDisplay(req.isDisplay() == null ? GroupCategoryConstant.DISPLAY_HIDDEN : req.isDisplay());
        entity.setNewData(null);

        return repository.save(entity);
    }

    public GroupCategory createAndSubmit(GroupCategoryUpsertReq req) {
        validator.validateRequired(req);
        validator.validateDuplicateForUpsert(req, null);

        GroupCategory entity = new GroupCategory();
        mapper.applyBaseFields(entity, req);

        entity.setStatus(GroupCategoryConstant.STATUS_PENDING);
        entity.setIsActive(req.isActive() == null ? GroupCategoryConstant.ACTIVE_DEFAULT : req.isActive());
        entity.setIsDisplay(req.isDisplay() == null ? GroupCategoryConstant.DISPLAY_HIDDEN : req.isDisplay());
        entity.setNewData(null);

        return repository.save(entity);
    }

    public GroupCategory update(Long id, GroupCategoryUpsertReq req) {
        validator.validateRequired(req);

        GroupCategory current = getRequired(id);

        if (isPublished(current)) {
            GroupCategory preview = mapper.buildPreviewEntity(current, req);
            validator.validateDuplicateForEntity(preview, current.getId());

            String patchJson = newDataHelper.buildPatchJson(current, req);
            if (!newDataHelper.hasMeaningfulNewData(patchJson)) {
                throw new BusinessException(ErrorCode.GC_NO_CHANGES);
            }

            current.setNewData(patchJson);
            return repository.save(current);
        }

        validator.validateDuplicateForUpsert(req, id);

        mapper.applyBaseFields(current, req);
        current.setIsActive(req.isActive() == null ? current.getIsActive() : req.isActive());
        current.setIsDisplay(req.isDisplay() == null ? current.getIsDisplay() : req.isDisplay());
        current.setStatus(GroupCategoryConstant.STATUS_DRAFT);
        current.setNewData(null);

        return repository.save(current);
    }

    public GroupCategory submit(Long id) {
        GroupCategory current = getRequired(id);

        if (isPending(current)) {
            throw new BusinessException(ErrorCode.GC_ALREADY_PENDING);
        }

        if (isPublished(current)) {
            if (!newDataHelper.hasMeaningfulNewData(current.getNewData())) {
                throw new BusinessException(
                        ErrorCode.GC_NO_CHANGES,
                        "Không có dữ liệu thay đổi để gửi duyệt"
                );
            }

            GroupCategory preview = mapper.cloneEntity(current);
            newDataHelper.applyPatchJson(preview, current.getNewData());

            validator.validateRequiredEntity(preview);
            validator.validateDuplicateForEntity(preview, current.getId());

            current.setStatus(GroupCategoryConstant.STATUS_PENDING);
            return repository.save(current);
        }

        validator.validateRequiredEntity(current);
        validator.validateDuplicateForEntity(current, current.getId());

        current.setStatus(GroupCategoryConstant.STATUS_PENDING);
        current.setIsDisplay(GroupCategoryConstant.DISPLAY_HIDDEN);

        return repository.save(current);
    }

    public GroupCategory approve(Long id) {
        GroupCategory current = getRequired(id);

        if (!isPending(current)) {
            throw new BusinessException(ErrorCode.GC_ONLY_PENDING_CAN_APPROVE);
        }

        if (newDataHelper.hasMeaningfulNewData(current.getNewData())) {
            newDataHelper.applyPatchJson(current, current.getNewData());
        }

        validator.validateRequiredEntity(current);
        validator.validateDuplicateForEntity(current, current.getId());

        current.setStatus(GroupCategoryConstant.STATUS_APPROVED);
        current.setIsDisplay(GroupCategoryConstant.DISPLAY_VISIBLE);
        current.setNewData(null);

        return repository.save(current);
    }

    public GroupCategory reject(Long id, String reason) {
        GroupCategory current = getRequired(id);

        if (!isPending(current)) {
            throw new BusinessException(ErrorCode.GC_ONLY_PENDING_CAN_REJECT);
        }

        if (newDataHelper.hasMeaningfulNewData(current.getNewData())) {
            current.setStatus(GroupCategoryConstant.STATUS_APPROVED);
            current.setIsDisplay(GroupCategoryConstant.DISPLAY_VISIBLE);
            current.setNewData(null);
        } else {
            current.setStatus(GroupCategoryConstant.STATUS_REJECTED);
            current.setIsDisplay(GroupCategoryConstant.DISPLAY_HIDDEN);
        }

        return repository.save(current);
    }

    public GroupCategory cancelApprove(Long id) {
        GroupCategory current = getRequired(id);

        if (!Objects.equals(current.getStatus(), GroupCategoryConstant.STATUS_APPROVED)) {
            throw new BusinessException(ErrorCode.GC_ONLY_APPROVED_CAN_CANCEL);
        }

        current.setStatus(GroupCategoryConstant.STATUS_CANCEL_APPROVE);
        current.setIsDisplay(GroupCategoryConstant.DISPLAY_HIDDEN);
        current.setNewData(null);

        return repository.save(current);
    }

    public void delete(Long id) {
        GroupCategory current = getRequired(id);

        if (isPending(current)) {
            throw new BusinessException(ErrorCode.GC_PENDING_CANNOT_DELETE);
        }

        if (isPublished(current)) {
            throw new BusinessException(ErrorCode.GC_APPROVED_CANNOT_DELETE);
        }

        repository.delete(current);
    }

    public PageResponse<GroupCategoryResponse> getCategory(PagingRequest pagingRequest) {
        Pageable pageable = PagingUtils.toPageable(pagingRequest);
        Page<GroupCategory> pageData = repository.findAll(pageable);
        return PageResponse.from(pageData, GroupCategoryResponse::from);
    }

    public PageResponse<GroupCategoryResponse> search(GroupCategorySearchReq req, PagingRequest pagingRequest) {
        Pageable pageable = PagingUtils.toPageable(pagingRequest);
        Page<GroupCategory> pageData = repository.findAll(GroupCategorySpecification.search(req), pageable);
        return PageResponse.from(pageData, GroupCategoryResponse::from);
    }

    public GroupCategory getCategoryById(Long id) {
        return getRequired(id);
    }

    private GroupCategory getRequired(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.GC_NOT_FOUND,
                        "Không tìm thấy bản ghi với id = " + id
                ));
    }

    private boolean isPending(GroupCategory entity) {
        return Objects.equals(entity.getStatus(), GroupCategoryConstant.STATUS_PENDING);
    }

    private boolean isPublished(GroupCategory entity) {
        return Objects.equals(entity.getStatus(), GroupCategoryConstant.STATUS_APPROVED)
                || Objects.equals(entity.getIsDisplay(), GroupCategoryConstant.DISPLAY_VISIBLE);
    }
}