package com.example.demo.repository;

import com.example.demo.common.paging.PageResponse;
import com.example.demo.dto.request.GroupCategoryCreateReq;
import com.example.demo.dto.request.GroupCategorySearchReq;
import com.example.demo.dto.request.GroupCategoryUpdateReq;
import com.example.demo.entity.GroupCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;

@Repository
@Transactional
public class GroupCategoryNativeRepository {

    @PersistenceContext
    private EntityManager em;

    private static final String TABLE_NAME = "PMH_GROUP_CATEGORY";

    private static final String BASE_SELECT = """
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
            """;

    private static final String BASE_COUNT = """
                SELECT COUNT(1)
                FROM PMH_GROUP_CATEGORY
            """;

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

    public PageResponse<GroupCategory> getAll(int page, int size){
        String sql = BASE_SELECT + " ORDER BY ID DESC";
        String countSql = BASE_COUNT;

        Query dataQuery = em.createNativeQuery(sql);
        dataQuery.setFirstResult(page * size);
        dataQuery.setMaxResults(size);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();

        long total = ((Number) em.createNativeQuery(countSql).getSingleResult()).longValue();

        return buildPageResponse(rows, page, size, total, "id", "desc");
    }

    public GroupCategory getById(Long id) {
        String sql = BASE_SELECT + " WHERE ID = :id";

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("id", id)
                .getResultList();

        if(rows.isEmpty()){
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

    public PageResponse<GroupCategory> search(GroupCategorySearchReq req, int page, int size) {
        StringBuilder Where = new StringBuilder(" WHERE 1 = 1");
        appendLikeCondition(Where, req.paramName(), "PARAM_NAME", "paramName");
        appendLikeCondition(Where, req.paramValue(), "PARAM_VALUE", "paramValue");
        appendLikeCondition(Where, req.paramType(), "PARAM_TYPE", "paramType");
        appendInCondition(Where, req.status(), "STATUS", "status");
        appendInCondition(Where, req.isActive(), "IS_ACTIVE", "isActive");

        String sql = BASE_SELECT + Where + " ORDER BY ID DESC";
        String countSql = BASE_COUNT + Where;

        Query dataQuery = em.createNativeQuery(sql);
        Query countQuery = em.createNativeQuery(countSql);

        setSearchParams(dataQuery, req);
        setSearchParams(countQuery, req);

        dataQuery.setFirstResult(page * size);

        @SuppressWarnings("unchecked")
                List<Object[]> rows = dataQuery.getResultList();

        long total = ((Number) countQuery.getSingleResult()).longValue();

        return buildPageResponse(rows, page, size, total, "id", "desc");
    }

    //============================HELPER==========================

    private Long getNextId() {
        String sql = "SELECT PMH_GROUP_CATEGORY_SEQ.NEXTVAL FROM dual";
        Number number = (Number) em.createNativeQuery(sql).getSingleResult();
        return number.longValue();
    }

    private PageResponse<GroupCategory> buildPageResponse(
            @NonNull List<Object[]> rows,
            int page,
            int size,
            long total,
            String sortBy,
            String sortDir
    ){
        List<GroupCategory> content = rows.stream()
                .map(this::mapRow)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);

        return PageResponse.<GroupCategory>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page + 1 >= totalPages)
                .empty(content.isEmpty())
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();
    }

    private void setSearchParams(Query query, GroupCategorySearchReq req){
        setLikeParametter(query, req.paramName(), "paramName");
        setLikeParametter(query, req.paramValue(), "paramValue");
        setLikeParametter(query, req.paramType(), "paramType");
        setInParametter(query, req.status(), "status");
        setInParametter(query, req.isActive(), "isActive");
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

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void appendLikeCondition(StringBuilder sql, String value, String column, String param){
        if(hasText(value)){
            sql.append(" AND UPPER(")
                    .append(column)
                    .append(") LIKE UPPER(:")
                    .append(param)
                    .append(")");
        }
    }

    private void appendInCondition(StringBuilder sql, List<?> values, String column, String param){
        if(values != null && !values.isEmpty()){
            sql.append(" AND ")
                    .append(column)
                    .append(" IN (:")
                    .append(param)
                    .append(")");
        }
    }

    private void setLikeParametter(Query query, String value, String param){
        if(hasText(value)){
            query.setParameter(param, "%" + value.trim() + "%");
        }
    }

    private void setInParametter(Query query, List<?> values, String param){
        if(values != null && !values.isEmpty()){
            query.setParameter(param, values);
        }
    }

    private Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private Integer toInteger(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }

    private java.time.LocalDate toLocalDate(Object value) {
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        return null;
    }
}