package com.example.demo.service.helper;

import com.example.demo.dto.request.GroupCategoryUpsertReq;
import com.example.demo.entity.GroupCategory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;

import static com.example.demo.common.util.StringUtil.isBlank;
import static com.example.demo.common.util.StringUtil.normalizeNullable;
import static com.example.demo.common.util.StringUtil.normalizeRequired;

@Component
public class GroupCategoryNewDataHelper {

    private final ObjectMapper objectMapper;

    public GroupCategoryNewDataHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String buildPatchJson(GroupCategory current, GroupCategoryUpsertReq req) {
        ObjectNode patch = objectMapper.createObjectNode();

        putIfChanged(patch, "paramName", current.getParamName(), normalizeRequired(req.paramName()));
        putIfChanged(patch, "paramValue", current.getParamValue(), normalizeRequired(req.paramValue()));
        putIfChanged(patch, "paramType", current.getParamType(), normalizeRequired(req.paramType()));
        putIfChanged(patch, "description", current.getDescription(), normalizeNullable(req.description()));
        putIfChanged(patch, "componentCode", current.getComponentCode(), normalizeNullable(req.componentCode()));
        putIfChanged(patch, "effectiveDate", toString(current.getEffectiveDate()), toString(req.effectiveDate()));
        putIfChanged(patch, "endEffectiveDate", toString(current.getEndEffectiveDate()), toString(req.endEffectiveDate()));

        if (req.isActive() != null && !Objects.equals(current.getIsActive(), req.isActive())) {
            patch.put("isActive", req.isActive());
        }

        if (req.isDisplay() != null && !Objects.equals(current.getIsDisplay(), req.isDisplay())) {
            patch.put("isDisplay", req.isDisplay());
        }

        try {
            return objectMapper.writeValueAsString(patch);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Không serialize được NEW_DATA", e);
        }
    }

    public void applyPatchJson(GroupCategory target, String patchJson) {
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

    public boolean hasMeaningfulNewData(String newData) {
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
}