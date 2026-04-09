package com.example.demo.groupcategory.service.validator;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.ErrorCode;
import com.example.demo.groupcategory.dto.request.GroupCategoryUpsertReq;
import com.example.demo.groupcategory.entity.GroupCategory;
import com.example.demo.groupcategory.repository.GroupCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.example.demo.common.util.StringUtil.isBlank;
import static com.example.demo.common.util.StringUtil.normalizeRequired;

@Component
@RequiredArgsConstructor
public class GroupCategoryValidator {
    //Repo dùng để kiểm tra bản ghi trùng trong database
    private final GroupCategoryRepository repository;

    //kiểm tra các trường có trôngs hay không nếu trống thì báo INVALID
    public void validateRequired(GroupCategoryUpsertReq req) {
        //KIểm tra paramname có được gửi lên hay không
        if (isBlank(req.paramName())) {
            //nếu thiếu paramname thì ném lỗi request không hợp lệ
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
        //nếu có ngày kết thúc, ngày này không được sớm hơn ngày hiệu lực
        if (req.endEffectiveDate() != null && req.endEffectiveDate().isBefore(req.effectiveDate())) {
            //nếu ngày hết hiệu lưcj không hợp lệ thì ném lỗi businesss
            throw new BusinessException(ErrorCode.GC_INVALID_DATE_RANGE);
        }
    }

    public void validateRequiredEntity(GroupCategory entity) {
        //Kiểm tra entity sau khi map xem dữ liệu nội bộ có đủ paramName hay không
        if (isBlank(entity.getParamName())) {
            //Paramname rỗng thì entity không hợp lệ
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "paramName không được để trống");
        }
        //Kiểm tra entity khi mao xme dữ liệu nội bộ có đủ paramName hay không
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
        //Normalize các field khóa để tránh trùng do khác biệt khoảng trắng/ chuẩn hóa
        String paramName = normalizeRequired(req.paramName());
        String paramValue = normalizeRequired(req.paramValue());
        String paramType = normalizeRequired(req.paramType());

        //Nếu excludeId null thì kiểm tra duplicate cho create, ngược lại thì bỏ qua bản ghi đang update
        boolean exists = excludeId == null
                ? repository.existsByParamNameAndParamValueAndParamType(paramName, paramValue, paramType)
                : repository.existsByParamNameAndParamValueAndParamTypeAndIdNot(paramName, paramValue, paramType, excludeId);
        //Nếu đã tồn tại bản ghi cũng bỏ lỗi business chặn thao tác upsert
        if (exists) {
            throw new BusinessException(
                    ErrorCode.GC_DUPLICATE,
                    "Bản ghi đã tồn tại với bộ paramName + paramValue + paramType"
            );
        }
    }

    public void validateDuplicateForEntity(GroupCategory entity, Long excludeId) {
       //kiểm tra duplicate trên entity đã có sẵn dữ liệu, dùng sau khi map/patch
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
        //nếu đã tồn tại bản ghi khác thì ném lỗi business
        if (exists) {
            throw new BusinessException(
                    ErrorCode.GC_DUPLICATE,
                    "Bản ghi đã tồn tại với bộ paramName + paramValue + paramType"
            );
        }
    }
}