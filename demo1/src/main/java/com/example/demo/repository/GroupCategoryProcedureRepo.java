package com.example.demo.repository;

import com.example.demo.common.response.PageResponse;
import com.example.demo.dto.GroupCategoryCreateReq;
import com.example.demo.dto.GroupCategorySearchReq;
import com.example.demo.dto.GroupCategoryUpdateReq;
import com.example.demo.entity.GroupCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional
public class GroupCategoryProcedureRepo {

    @PersistenceContext
    private EntityManager em;

    public Long create(GroupCategoryCreateReq req){
        StoredProcedureQuery sp = em.createStoredProcedureQuery("LND_PRC_GC_CREATE");

        sp.registerStoredProcedureParameter("P_PARAM_NAME", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_PARAM_VALUE", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_PARAM_TYPE", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_DESCRIPTION", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_COMPONENT_CODE", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_STATUS", Integer.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_IS_ACTIVE", Integer.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_IS_DISPLAY", Integer.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_EFFECTIVE_DATE", Date.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_END_EFFECTIVE_DATE", Date.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_ID", Long.class, ParameterMode.OUT);

        sp.setParameter("P_PARAM_NAME", req.paramName());
        sp.setParameter("P_PARAM_VALUE", req.paramValue());
        sp.setParameter("P_PARAM_TYPE", req.paramType());
        sp.setParameter("P_DESCRIPTION", req.description());
        sp.setParameter("P_COMPONENT_CODE", req.componentCode());
        sp.setParameter("P_STATUS", req.status());
        sp.setParameter("P_IS_ACTIVE", req.isActive());
        sp.setParameter("P_IS_DISPLAY", req.isDisplay());
        sp.setParameter("P_EFFECTIVE_DATE", req.effectiveDate() == null ? null : Date.valueOf(req.effectiveDate()));
        sp.setParameter("P_END_EFFECTIVE_DATE", req.endEffectiveDate() == null ? null : Date.valueOf(req.endEffectiveDate()));

        sp.execute();

        Object outId = sp.getOutputParameterValue("P_ID");
        return outId == null ? null : ((Number) outId).longValue();
    }

    @SuppressWarnings("unchecked")
    public PageResponse<GroupCategory> getAll(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : Math.min(size, 100);

        StoredProcedureQuery sp= em.createStoredProcedureQuery("LND_PRC_GC_GET_ALL_PAGE");

        sp.registerStoredProcedureParameter("P_PAGE", Integer.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_SIZE", Integer.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_TOTAL", Long.class, ParameterMode.OUT);
        sp.registerStoredProcedureParameter("P_CURSOR", void.class, ParameterMode.REF_CURSOR);

        sp.setParameter("P_PAGE", safePage);
        sp.setParameter("P_SIZE", safeSize);

        List<Object[]> rows = sp.getResultList();
        List<GroupCategory> result = new ArrayList<>();
        for(Object[] row : rows){
            result.add(mapRow(row));
        }

        long total = ((Number) sp.getOutputParameterValue("P_TOTAL")).longValue();

        return PageResponse.fromNative(result, safePage, safeSize, total, "effectiveDate", "desc", item -> item);
    }

    @SuppressWarnings("unchecked")
    public GroupCategory getById(Long id){
        StoredProcedureQuery sp = em.createStoredProcedureQuery("LND_PRC_GC_GET_BY_ID");

        sp.registerStoredProcedureParameter("P_ID", Long.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_CURSOR", void.class, ParameterMode.REF_CURSOR);

        sp.setParameter("P_ID", id);
        sp.execute();

        List<Object[]> rows = sp.getResultList();
        if(rows == null || rows.isEmpty()){
            throw new RuntimeException("Không tìm thấy bản ghi với id = " + id);
        }

        return mapRow(rows.get(0));
    }

    public Long update (Long id, GroupCategoryUpdateReq req){
        StoredProcedureQuery sp = em.createStoredProcedureQuery("LND_PRC_GC_UPDATE");

        sp.registerStoredProcedureParameter("P_ID", Long.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_PARAM_NAME", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_PARAM_VALUE", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_PARAM_TYPE", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_DESCRIPTION", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_COMPONENT_CODE", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_STATUS", Integer.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_IS_ACTIVE", Integer.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_IS_DISPLAY", Integer.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_EFFECTIVE_DATE", Date.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_END_EFFECTIVE_DATE", Date.class, ParameterMode.IN);

        sp.setParameter("P_ID", id);
        sp.setParameter("P_PARAM_NAME", req.paramName());
        sp.setParameter("P_PARAM_VALUE", req.paramValue());
        sp.setParameter("P_PARAM_TYPE", req.paramType());
        sp.setParameter("P_DESCRIPTION", req.description());
        sp.setParameter("P_COMPONENT_CODE", req.componentCode());
        sp.setParameter("P_STATUS", req.status());
        sp.setParameter("P_IS_ACTIVE", req.isActive());
        sp.setParameter("P_IS_DISPLAY", req.isDisplay());
        sp.setParameter("P_EFFECTIVE_DATE", req.effectiveDate() == null ? null : Date.valueOf(req.effectiveDate()));
        sp.setParameter("P_END_EFFECTIVE_DATE", req.endEffectiveDate() == null ? null : Date.valueOf(req.endEffectiveDate()));

        sp.execute();
        return id;
    }

    public void delete(Long id){
        StoredProcedureQuery sp = em.createStoredProcedureQuery("LND_PRC_GC_DELETE");

        sp.registerStoredProcedureParameter("P_ID", Long.class, ParameterMode.IN);
        sp.setParameter("P_ID", id);
        sp.execute();
    }

    public PageResponse<GroupCategory> search(GroupCategorySearchReq req) {
        int safePage = req.page() == null || req.page() < 0 ? 0 : req.page();
        int safeSize = req.size() == null || req.size() <= 0 ? 10 : Math.min(req.size(), 100);

        StoredProcedureQuery sp = em.createStoredProcedureQuery("LND_PRC_GC_SEARCH_PAGE");

        sp.registerStoredProcedureParameter("P_PARAM_NAME", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_PARAM_VALUE", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_PARAM_TYPE", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_STATUS", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_IS_ACTIVE", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_PAGE", Integer.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_SIZE", Integer.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_TOTAL", Long.class, ParameterMode.OUT);
        sp.registerStoredProcedureParameter("P_CURSOR", void.class, ParameterMode.REF_CURSOR);

        sp.setParameter("P_PARAM_NAME", emptyToNull(req.paramName()));
        sp.setParameter("P_PARAM_VALUE", emptyToNull(req.paramValue()));
        sp.setParameter("P_PARAM_TYPE", emptyToNull(req.paramType()));
        sp.setParameter("P_STATUS", joinIntegerList(req.status()));
        sp.setParameter("P_IS_ACTIVE", joinIntegerList(req.isActive()));
        sp.setParameter("P_PAGE", safePage);
        sp.setParameter("P_SIZE", safeSize);

        sp.execute();

        List<Object[]> rows = sp.getResultList();
        List<GroupCategory> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(mapRow(row));
        }

        long total = ((Number) sp.getOutputParameterValue("P_TOTAL")).longValue();

        return PageResponse.fromNative(result, safePage, safeSize, total, "effectiveDate", "desc", item -> item);
    }

    private GroupCategory mapRow(Object[] row){
        GroupCategory entity = new GroupCategory();
        entity.setId(toLong(row[0]));
        entity.setParamName((String) row[1]);
        entity.setParamValue((String) row[2]);
        entity.setParamType((String) row[3]);
        entity.setDescription((String) row[4]);
        entity.setComponentCode((String) row[5]);
        entity.setStatus(toInteger(row[6]));
        entity.setIsActive(toInteger(row[7]));
        entity.setIsDisplay(toInteger(row[8]));
        entity.setEffectiveDate(toLocalDate(row[9]));
        entity.setEndEffectiveDate(toLocalDate(row[10]));
        return entity;
    }

    private Long toLong(Object value){
        if(value == null) return null;
        return ((Number) value).longValue();
    }

    private Integer toInteger(Object value){
        if(value == null) return null;
        return ((Number) value).intValue();
    }

    private java.time.LocalDate toLocalDate(Object value){
        if (value == null) return null;
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime().toLocalDate();
        }
        return null;
    }

    private String joinIntegerList(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse(null);
    }

    private String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
