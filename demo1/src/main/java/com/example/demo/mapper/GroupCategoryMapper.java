package com.example.demo.mapper;

import com.example.demo.dto.request.GroupCategoryUpsertReq;
import com.example.demo.entity.GroupCategory;
import org.springframework.stereotype.Component;

import static com.example.demo.common.util.StringUtil.normalizeNullable;
import static com.example.demo.common.util.StringUtil.normalizeRequired;

@Component
public class GroupCategoryMapper {

    public void applyBaseFields(GroupCategory entity, GroupCategoryUpsertReq req) {
        entity.setParamName(normalizeRequired(req.paramName()));
        entity.setParamValue(normalizeRequired(req.paramValue()));
        entity.setParamType(normalizeRequired(req.paramType()));
        entity.setDescription(normalizeNullable(req.description()));
        entity.setComponentCode(normalizeNullable(req.componentCode()));
        entity.setEffectiveDate(req.effectiveDate());
        entity.setEndEffectiveDate(req.endEffectiveDate());
    }

    public GroupCategory buildPreviewEntity(GroupCategory current, GroupCategoryUpsertReq req) {
        GroupCategory preview = cloneEntity(current);

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

    public GroupCategory cloneEntity(GroupCategory source) {
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
}