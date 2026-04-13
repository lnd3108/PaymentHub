package com.example.demo.groupcategory.service.Impl;

import com.example.demo.groupcategory.constant.GroupCategoryConstant;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.ErrorCode;
import com.example.demo.common.paging.PageResponse;
import com.example.demo.common.paging.PagingRequest;
import com.example.demo.common.paging.PagingUtils;
import com.example.demo.groupcategory.dto.request.GroupCategoryBatchReq;
import com.example.demo.groupcategory.dto.request.GroupCategorySearchReq;
import com.example.demo.groupcategory.dto.request.GroupCategoryUpsertReq;
import com.example.demo.groupcategory.dto.response.GroupCategoryBatchActionResponse;
import com.example.demo.groupcategory.dto.response.GroupCategoryBatchError;
import com.example.demo.groupcategory.dto.response.GroupCategoryResponse;
import com.example.demo.groupcategory.dto.response.GroupCategoryStatusOnlyResponse;
import com.example.demo.groupcategory.entity.GroupCategory;
import com.example.demo.groupcategory.mapper.GroupCategoryMapper;
import com.example.demo.groupcategory.repository.GroupCategoryRepository;
import com.example.demo.groupcategory.service.GroupCategoryService;
import com.example.demo.groupcategory.service.helper.GroupCategoryNewDataHelper;
import com.example.demo.groupcategory.service.validator.GroupCategoryValidator;
import com.example.demo.groupcategory.repository.specification.GroupCategorySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ImplGroupCategoryService implements GroupCategoryService {

    private final GroupCategoryRepository repository;
    private final GroupCategoryValidator validator;
    private final GroupCategoryMapper mapper;
    private final GroupCategoryNewDataHelper newDataHelper;

    public GroupCategory create(GroupCategoryUpsertReq req) {
        //kiểm trả các trường gửi lên có trống hay không
        validator.validateRequired(req);
        //chuẩn hóa các field khóa để tránh trùng do khác biệt khoảng trắng
        validator.validateDuplicateForUpsert(req, null);

        //tạo entity mới để copy bản ghi từ nguồn
        GroupCategory entity = new GroupCategory();
        //gắn trường đã được chuẩn hóa vào entity
        mapper.applyBaseFields(entity, req);

        //Set status = 1 - Tạo Mới
        entity.setStatus(GroupCategoryConstant.STATUS_DRAFT);
        //set is active về trạng thái đang hoạt động
        entity.setIsActive(req.isActive() == null ? GroupCategoryConstant.ACTIVE_DEFAULT : req.isActive());
        //để display về trạng thái chưa duyệt có thể xóa
        entity.setIsDisplay(req.isDisplay() == null ? GroupCategoryConstant.DISPLAY_HIDDEN : req.isDisplay());
        //set newData = null
        entity.setNewData(null);

        //lưu
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
        //kiểm tra các trường có trống hay không
        validator.validateRequired(req);

        //kiểm tra với id đấy thfi có tồn tại bản ghi hay không nếu không thì ném lỗi ra ngoài
        GroupCategory current = getRequired(id);

        //kiểm tra trạng thái status và display
        if (isPublished(current)) {
            //Tạo biến chưas bản ghi xem trước, tránh sửa trực tiếp object gốc
            GroupCategory preview = mapper.buildPreviewEntity(current, req);
            //check trùng sau khi map
            validator.validateDuplicateForEntity(preview, current.getId());

            //Tạo json object rỗng, chỉ chứa các field thay đổi
            String patchJson = newDataHelper.buildPatchJson(current, req);
            //kiểm tra nếu chuỗi null, rỗng hoặc chỉ có khoảng trắng thì trả ra business logic
            if (!newDataHelper.hasMeaningfulNewData(patchJson)) {
                throw new BusinessException(ErrorCode.GC_NO_CHANGES);
            }
            //nếu dữ liệu mới sau khi check thì map vào bảng
            current.setNewData(patchJson);
            //lưu
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

    public GroupCategoryBatchActionResponse submitBatch(GroupCategoryBatchReq req) {
        return processBatch(req, this::submit);
    }

    public GroupCategoryBatchActionResponse approveBatch(GroupCategoryBatchReq req) {
        return processBatch(req, this::approve);
    }

    public GroupCategoryBatchActionResponse batchCancelApprove(GroupCategoryBatchReq req) {
        return processBatch(req, this::cancelApprove);
    }

    private GroupCategoryBatchActionResponse processBatch(
            GroupCategoryBatchReq req,
            BatchActionExecutor executor
    ) {
        Set<Long> uniqueIds = new LinkedHashSet<>(req.ids());
        if (uniqueIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "ids không được để trống");
        }

        List<GroupCategoryStatusOnlyResponse> updated = new ArrayList<>();
        List<GroupCategoryBatchError> failed = new ArrayList<>();

        for (Long id : uniqueIds) {
            if (id == null) {
                failed.add(new GroupCategoryBatchError(null, ErrorCode.INVALID_REQUEST.getCode(), "id không hợp lệ"));
                continue;
            }

            try {
                GroupCategory entity = executor.execute(id);
                updated.add(GroupCategoryStatusOnlyResponse.from(entity));
            } catch (BusinessException ex) {
                failed.add(new GroupCategoryBatchError(id, ex.getCode(), ex.getMessage()));
            } catch (Exception ex) {
                failed.add(new GroupCategoryBatchError(id, ErrorCode.INTERNAL_ERROR.getCode(), "Lỗi hệ thống"));
            }
        }

        return new GroupCategoryBatchActionResponse(
                uniqueIds.size(),
                updated.size(),
                failed.size(),
                updated,
                failed
        );
    }

    @FunctionalInterface
    private interface BatchActionExecutor {
        GroupCategory execute(Long id);
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