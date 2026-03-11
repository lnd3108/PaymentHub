package com.example.demo.repository;

import com.example.demo.dto.GroupCategoryCreateReq;
import com.example.demo.dto.GroupCategorySearchReq;
import com.example.demo.dto.GroupCategoryUpdateReq;
import com.example.demo.entity.GroupCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional
public class GroupCategoryNativeRepository {

    @PersistenceContext
    private EntityManager em;

    public Long create(GroupCategoryCreateReq req) {
        Long id = getNextId();

        String sql = """
                INSERT INTO PMH_GROUP_CATEGORY (
                    ID,
                    PARAM_NAME,
                    PARAM_VALUE,
                    PARAM_TYPE,
                    DESCRIPTION,
                    COMPONENT_CODE,
                    STATUS,
                    IS_ACTIVE,
                    IS_DISPLAY,
                    EFFECTIVE_DATE,
                    END_EFFECTIVE_DATE
                ) VALUES (
                    :id,
                    :paramName,
                    :paramValue,
                    :paramType,
                    :description,
                    :componentCode,
                    :status,
                    :isActive,
                    :isDisplay,
                    :effectiveDate,
                    :endEffectiveDate
                )
                """;

        Query query = em.createNativeQuery(sql);
        query.setParameter("id", id);
        query.setParameter("paramName", req.paramName());
        query.setParameter("paramValue", req.paramValue());
        query.setParameter("paramType", req.paramType());
        query.setParameter("description", req.description());
        query.setParameter("componentCode", req.componentCode());
        query.setParameter("status", req.status());
        query.setParameter("isActive", req.isActive());
        query.setParameter("isDisplay", req.isDisplay());
        query.setParameter("effectiveDate", req.effectiveDate());
        query.setParameter("endEffectiveDate", req.endEffectiveDate());

        query.executeUpdate();
        return id;
    }

    public List<GroupCategory> getAll(){
        String sql = """
                    SELECT
                        ID, PARAM_NAME, PARAM_VALUE, PARAM_TYPE, DESCRIPTION,
                        COMPONENT_CODE, STATUS, IS_ACTIVE, IS_DISPLAY, EFFECTIVE_DATE, END_EFFECTIVE_DATE
                    FROM PMH_GROUP_CATEGORY
                    ORDER BY ID DESC
                """;

        List<Object[]> rows = em.createNativeQuery(sql).getResultList();
        List<GroupCategory> result = new ArrayList<>();

        for(Object[] row : rows){
            result.add(mapRow(row));
        }

        return result;
    }

    public GroupCategory getById(Long id) {
        String sql = """
                SELECT
                    ID,
                    PARAM_NAME,
                    PARAM_VALUE,
                    PARAM_TYPE,
                    DESCRIPTION,
                    COMPONENT_CODE,
                    STATUS,
                    IS_ACTIVE,
                    IS_DISPLAY,
                    EFFECTIVE_DATE,
                    END_EFFECTIVE_DATE
                FROM PMH_GROUP_CATEGORY
                WHERE ID = :id
                """;

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("id", id)
                .getResultList();

        if (rows.isEmpty()) {
            throw new RuntimeException("Không tìm thấy bản ghi với id = " + id);
        }

        return mapRow(rows.get(0));
    }

    public Long update(Long id, GroupCategoryUpdateReq req) {
        String sql = """
                UPDATE PMH_GROUP_CATEGORY
                SET
                    PARAM_NAME = :paramName,
                    PARAM_VALUE = :paramValue,
                    PARAM_TYPE = :paramType,
                    DESCRIPTION = :description,
                    COMPONENT_CODE = :componentCode,
                    STATUS = :status,
                    IS_ACTIVE = :isActive,
                    IS_DISPLAY = :isDisplay,
                    EFFECTIVE_DATE = :effectiveDate,
                    END_EFFECTIVE_DATE = :endEffectiveDate
                WHERE ID = :id
                """;

        Query query = em.createNativeQuery(sql);
        query.setParameter("id", id);
        query.setParameter("paramName", req.paramName());
        query.setParameter("paramValue", req.paramValue());
        query.setParameter("paramType", req.paramType());
        query.setParameter("description", req.description());
        query.setParameter("componentCode", req.componentCode());
        query.setParameter("status", req.status());
        query.setParameter("isActive", req.isActive());
        query.setParameter("isDisplay", req.isDisplay());
        query.setParameter("effectiveDate", req.effectiveDate());
        query.setParameter("endEffectiveDate", req.endEffectiveDate());

        int updated = query.executeUpdate();

        if (updated == 0) {
            throw new RuntimeException("Không tìm thấy bản ghi để update, id = " + id);
        }

        return id;
    }

    public void delete(Long id) {
        String sql = """
                DELETE FROM PMH_GROUP_CATEGORY
                WHERE ID = :id
                """;

        int deleted = em.createNativeQuery(sql)
                .setParameter("id", id)
                .executeUpdate();

        if (deleted == 0) {
            throw new RuntimeException("Không tìm thấy bản ghi để xóa, id = " + id);
        }
    }

    public List<GroupCategory> search(GroupCategorySearchReq req) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    ID,
                    PARAM_NAME,
                    PARAM_VALUE,
                    PARAM_TYPE,
                    DESCRIPTION,
                    COMPONENT_CODE,
                    STATUS,
                    IS_ACTIVE,
                    IS_DISPLAY,
                    EFFECTIVE_DATE,
                    END_EFFECTIVE_DATE
                FROM PMH_GROUP_CATEGORY
                WHERE 1 = 1
                """);

        if (hasText(req.paramName())) {
            sql.append(" AND UPPER(PARAM_NAME) LIKE UPPER(:paramName)");
        }

        if (hasText(req.paramValue())) {
            sql.append(" AND UPPER(PARAM_VALUE) LIKE UPPER(:paramValue)");
        }

        if (hasText(req.paramType())) {
            sql.append(" AND UPPER(PARAM_TYPE) LIKE UPPER(:paramType)");
        }

        if (req.status() != null && !req.status().isEmpty()) {
            sql.append(" AND STATUS IN (:status)");
        }

        if (req.isActive() != null && !req.isActive().isEmpty()) {
            sql.append(" AND IS_ACTIVE IN (:isActive)");
        }

        sql.append(" ORDER BY ID DESC");

        Query query = em.createNativeQuery(sql.toString());

        if (hasText(req.paramName())) {
            query.setParameter("paramName", "%" + req.paramName().trim() + "%");
        }

        if (hasText(req.paramValue())) {
            query.setParameter("paramValue", "%" + req.paramValue().trim() + "%");
        }

        if (hasText(req.paramType())) {
            query.setParameter("paramType", "%" + req.paramType().trim() + "%");
        }

        if (req.status() != null && !req.status().isEmpty()) {
            query.setParameter("status", req.status());
        }

        if (req.isActive() != null && !req.isActive().isEmpty()) {
            query.setParameter("isActive", req.isActive());
        }

        List<Object[]> rows = query.getResultList();
        List<GroupCategory> result = new ArrayList<>();

        for (Object[] row : rows) {
            result.add(mapRow(row));
        }

        return result;
    }

    private Long getNextId() {
        String sql = "SELECT PMH_GROUP_CATEGORY_SEQ.NEXTVAL FROM dual";
        Number number = (Number) em.createNativeQuery(sql).getSingleResult();
        return number.longValue();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private GroupCategory mapRow(Object[] row) {
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

    private Long toLong(Object value) {
        if (value == null) return null;
        return ((Number) value).longValue();
    }

    private Integer toInteger(Object value) {
        if (value == null) return null;
        return ((Number) value).intValue();
    }

    private java.time.LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        return null;
    }
}