package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.ErrorCode;
import com.example.demo.common.paging.PageResponse;
import com.example.demo.dto.request.GroupCategoryCreateReq;
import com.example.demo.dto.request.GroupCategorySearchReq;
import com.example.demo.dto.request.GroupCategoryUpdateReq;
import com.example.demo.entity.GroupCategory;
import com.example.demo.repository.GroupCategoryProcedureRepo;
import org.springframework.stereotype.Service;

@Service
public class GroupCategoryPrcService {

    private final GroupCategoryProcedureRepo repository;

    public GroupCategoryPrcService(GroupCategoryProcedureRepo repository){
        this.repository = repository;
    }

    public Long create(GroupCategoryCreateReq req){
        validateCreate(req);
        return repository.create(req);
    }

    public PageResponse<GroupCategory> getAll(int page, int size){
        return repository.getAll(page, size);
    }

    public GroupCategory getById(Long id){
        validateId(id);
        return repository.getById(id);
    }

    public Long update(Long id, GroupCategoryUpdateReq req){
        validateId(id);
        validateUpdate(req);
        return repository.update(id, req);
    }

    public void delete(Long id){
        validateId(id);
        repository.delete(id);
    }

    public PageResponse<GroupCategory> search(GroupCategorySearchReq req) {
        return repository.search(req);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Id không hợp lệ");
        }
    }

    private void validateCreate(GroupCategoryCreateReq req) {
        if (req == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (req.effectiveDate() != null && req.endEffectiveDate() != null
                && req.endEffectiveDate().isBefore(req.effectiveDate())) {
            throw new BusinessException(ErrorCode.GC_INVALID_DATE_RANGE);
        }
    }

    private void validateUpdate(GroupCategoryUpdateReq req) {
        if (req == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (req.effectiveDate() != null && req.endEffectiveDate() != null
                && req.endEffectiveDate().isBefore(req.effectiveDate())) {
            throw new BusinessException(ErrorCode.GC_INVALID_DATE_RANGE);
        }
    }
}