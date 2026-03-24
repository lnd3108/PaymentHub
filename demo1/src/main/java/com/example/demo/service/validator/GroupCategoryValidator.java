package com.example.demo.service.validator;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.ErrorCode;
import com.example.demo.dto.request.GroupCategoryUpsertReq;
import com.example.demo.entity.GroupCategory;
import com.example.demo.repository.GroupCategoryRepository;
import org.springframework.stereotype.Component;

import static com.example.demo.common.util.StringUtil.isBlank;
import static com.example.demo.common.util.StringUtil.normalizeRequired;

@Component
public class GroupCategoryValidator {

    private final GroupCategoryRepository repository;

    public GroupCategoryValidator(GroupCategoryRepository repository) {
        this.repository = repository;
    }

    public void validateRequired(GroupCategoryUpsertReq req) {
        if (isBlank(req.paramName())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "paramName không được để trống");
        }
        if (isBlank(req.paramValue())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "paramValue không được để trống");
        }
        if (isBlank(req.paramType())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "paramType không được để trống");
        }
        if (req.effectiveDate() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "effectiveDate không được để trống");
        }

        if (req.endEffectiveDate() != null && req.endEffectiveDate().isBefore(req.effectiveDate())) {
            throw new BusinessException(ErrorCode.GC_INVALID_DATE_RANGE);
        }
    }

    public void validateRequiredEntity(GroupCategory entity) {
        if (isBlank(entity.getParamName())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "paramName không được để trống");
        }
        if (isBlank(entity.getParamValue())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "paramValue không được để trống");
        }
        if (isBlank(entity.getParamType())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "paramType không được để trống");
        }
        if (entity.getEffectiveDate() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "effectiveDate không được để trống");
        }

        if (entity.getEndEffectiveDate() != null
                && entity.getEndEffectiveDate().isBefore(entity.getEffectiveDate())) {
            throw new BusinessException(ErrorCode.GC_INVALID_DATE_RANGE);
        }
    }

    public void validateDuplicateForUpsert(GroupCategoryUpsertReq req, Long excludeId) {
        String paramName = normalizeRequired(req.paramName());
        String paramValue = normalizeRequired(req.paramValue());
        String paramType = normalizeRequired(req.paramType());

        boolean exists = excludeId == null
                ? repository.existsByParamNameAndParamValueAndParamType(paramName, paramValue, paramType)
                : repository.existsByParamNameAndParamValueAndParamTypeAndIdNot(paramName, paramValue, paramType, excludeId);

        if (exists) {
            throw new BusinessException(
                    ErrorCode.GC_DUPLICATE,
                    "Bản ghi đã tồn tại với bộ paramName + paramValue + paramType"
            );
        }
    }

    public void validateDuplicateForEntity(GroupCategory entity, Long excludeId) {
        boolean exists = excludeId == null
                ? repository.existsByParamNameAndParamValueAndParamType(
                normalizeRequired(entity.getParamName()),
                normalizeRequired(entity.getParamValue()),
                normalizeRequired(entity.getParamType())
        )
                : repository.existsByParamNameAndParamValueAndParamTypeAndIdNot(
                normalizeRequired(entity.getParamName()),
                normalizeRequired(entity.getParamValue()),
                normalizeRequired(entity.getParamType()),
                excludeId
        );

        if (exists) {
            throw new BusinessException(
                    ErrorCode.GC_DUPLICATE,
                    "Bản ghi đã tồn tại với bộ paramName + paramValue + paramType"
            );
        }
    }
}