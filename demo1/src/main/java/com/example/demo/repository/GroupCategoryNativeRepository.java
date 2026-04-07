package com.example.demo.repository;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.ErrorCode;
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
import java.sql.Timestamp;
import java.util.List;

@Repository
@Transactional // các method được bao bọc bởi transactional nếu có lỗi thì có thể rollback
public class GroupCategoryNativeRepository {

    @PersistenceContext //inject entity mânger vào đây
    private EntityManager em; // đối tượng chính để chạy native query như SELECT, INSERT, UPDATE, ...

    //Chuỗi select cố định
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
                NEW_DATA,
                EFFECTIVE_DATE,
                END_EFFECTIVE_DATE
            FROM PMH_GROUP_CATEGORY
            """;

    //Sql nền đếm tôngt bản ghi
    private static final String BASE_COUNT = """
            SELECT COUNT(1)
            FROM PMH_GROUP_CATEGORY
            """;

    //hàm tạo mới bản ghi
    public Long create(GroupCategoryCreateReq req) {
        //tạo id mới tự động
        Long id = getNextId();

        //insert bản ghi mới vào bảng
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
                    NEW_DATA,
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
                    :newData,
                    :effectiveDate,
                    :endEffectiveDate
                )
                """;

        //gán giá trị vào field
        em.createNativeQuery(sql) //khởi tạo querry native SQL
                .setParameter("id", id)//gán giá trị vào từng placeholder trong câu lệnh sql
                .setParameter("paramName", req.paramName()) // req.paramName lấy dữ liệu từ req
                .setParameter("paramValue", req.paramValue())
                .setParameter("paramType", req.paramType())
                .setParameter("description", req.description())
                .setParameter("componentCode", req.componentCode())
                .setParameter("status", req.status())
                .setParameter("isActive", req.isActive())
                .setParameter("isDisplay", req.isDisplay())
                .setParameter("newData", req.newData())
                .setParameter("effectiveDate", req.effectiveDate())
                .setParameter("endEffectiveDate", req.endEffectiveDate())
                .executeUpdate();

        return id; // trả ra id sau khi tạo mới bản ghi
    }

    //Hàm lấy tất cả bản ghi
    public PageResponse<GroupCategory> getAll(int page, int size) {
        //lấy danh sách tất cả bản ghi có phân trang
        String sql = BASE_SELECT + " ORDER BY ID DESC";
        Query dataQuery = em.createNativeQuery(sql); // tạo querry lấy dữ liệu
        dataQuery.setFirstResult(page * size); //bỏ qua số dòng của trang trước
        dataQuery.setMaxResults(size); // lấy tối đa size cho trang hiện tại

        //chạy querry để lấy dữ liệu
        @SuppressWarnings("unchecked")
                // kết quả: native sql không tự map sang entity
        List<Object[]> rows = dataQuery.getResultList();

        //querry đếm tổng banr ghi
        //trả đúng 1 giá trị
        long total = ((Number) em.createNativeQuery(BASE_COUNT).getSingleResult()).longValue();
        //tính tổng số trang
        //đóng gói thành pageResponse
        return buildPageResponse(rows, page, size, total, "id", "desc");
    }

    public GroupCategory getById(Long id) {
        String sql = BASE_SELECT + " WHERE ID = :id";

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("id", id)
                .getResultList(); //Dùng getResultList() thay vì getSingleResult() để tránh lỗi nếu không có dữ liệu.

        if (rows.isEmpty()) {
            throw new BusinessException(ErrorCode.GC_NOT_FOUND, "Khong tim thay ban ghi voi id = " + id);
        }
        //lấy dòng đầu tiên trong kết quả
        //gọi mapRow để convert từ Object[] sang groupCategory
        return mapRow(rows.get(0));
    }

    //hàm cập nhật bản ghi
    public Long update(Long id, GroupCategoryUpdateReq req) {
        //khởi tạo câu lệnh update
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
                    NEW_DATA = :newData,
                    EFFECTIVE_DATE = :effectiveDate,
                    END_EFFECTIVE_DATE = :endEffectiveDate
                WHERE ID = :id
                """;

        int updated = em.createNativeQuery(sql) //khởi tạo native sql
                //gán giá trị cho từng placehoder trong câu lệnh sql
                .setParameter("id", id)
                .setParameter("paramName", req.paramName())
                .setParameter("paramValue", req.paramValue())
                .setParameter("paramType", req.paramType())
                .setParameter("description", req.description())
                .setParameter("componentCode", req.componentCode())
                .setParameter("status", req.status())
                .setParameter("isActive", req.isActive())
                .setParameter("isDisplay", req.isDisplay())
                .setParameter("newData", req.newData())
                .setParameter("effectiveDate", req.effectiveDate())
                .setParameter("endEffectiveDate", req.endEffectiveDate())
                .executeUpdate();// thưcj thi câu lệnh

        if (updated == 0) {
            throw new BusinessException(ErrorCode.GC_NOT_FOUND, "Khong tim thay ban ghi de update, id = " + id);
        }

        return id;
    }

    //xóa bản ghi
    public void delete(Long id) {
        String sql = """
                DELETE FROM PMH_GROUP_CATEGORY
                WHERE ID = :id
                """;

        int deleted = em.createNativeQuery(sql) // khơi tạo querry native sql
                .setParameter("id", id) //gán gia trị của id
                .executeUpdate(); //thưcj thi câu lệnh

        if (deleted == 0) {
            throw new BusinessException(ErrorCode.GC_NOT_FOUND, "Khong tim thay ban ghi de xoa, id = " + id);
        }
    }

    public PageResponse<GroupCategory> search(GroupCategorySearchReq req, int page, int size) {
        //tạo hàm search có phân trang
        //tạo where 1 = 1 đẻr nối thêm điều kiện and cho dêx
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        //nếu request có giá trị thì nối điều kiện vào where
        appendLikeCondition(where, req.paramName(), "PARAM_NAME", "paramName");
        appendLikeCondition(where, req.paramValue(), "PARAM_VALUE", "paramValue");
        appendLikeCondition(where, req.paramType(), "PARAM_TYPE", "paramType");
        appendInCondition(where, req.status(), "STATUS", "status");
        appendInCondition(where, req.isActive(), "IS_ACTIVE", "isActive");

        //querry lấy dữ liệu thật
        String sql = BASE_SELECT + where + " ORDER BY ID DESC";
        //querry đếm tổng bản ghi thoa mãn điều kiện
        String countSql = BASE_COUNT + where;

        //khởi tạo 1 query lấy danh sách
        //1 querry đếm tổng
        Query dataQuery = em.createNativeQuery(sql);
        Query countQuery = em.createNativeQuery(countSql);
        setSearchParams(dataQuery, req); //bind parametter cho cả data và count
        setSearchParams(countQuery, req);
        dataQuery.setFirstResult(page * size); //phân trang cho querru data
        dataQuery.setMaxResults(size); // count không cần phân trang vì chỉ trả ra 1 giá trị

        //lấy data trang hiện tại
        //lấy tổng bản ghi
        //build data phân trang
        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();

        long total = ((Number) countQuery.getSingleResult()).longValue();
        return buildPageResponse(rows, page, size, total, "id", "desc");
    }

    //hàm update riêng cho workflow
    public void updateWorkflowState(Long id, Integer status, Integer isDisplay, String newData) {
        String sql = """
                UPDATE PMH_GROUP_CATEGORY
                SET
                    STATUS = :status,
                    IS_DISPLAY = :isDisplay,
                    NEW_DATA = :newData
                WHERE ID = :id
                """;

        int updated = em.createNativeQuery(sql)
                .setParameter("id", id)
                .setParameter("status", status)
                .setParameter("isDisplay", isDisplay)
                .setParameter("newData", newData)
                .executeUpdate();
        //gán giá trị rồi chạy update
        if (updated == 0) {
            throw new BusinessException(ErrorCode.GC_NOT_FOUND, "Khong tim thay ban ghi voi id = " + id);
        }
    }

    //hàm duyêt thành công ghi dữ liệu đã được approve vào DB
    public void applyApprovedData(GroupCategory entity) {
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
                    NEW_DATA = :newData,
                    EFFECTIVE_DATE = :effectiveDate,
                    END_EFFECTIVE_DATE = :endEffectiveDate
                WHERE ID = :id
                """;

        int updated = em.createNativeQuery(sql)
                .setParameter("id", entity.getId())
                .setParameter("paramName", entity.getParamName())
                .setParameter("paramValue", entity.getParamValue())
                .setParameter("paramType", entity.getParamType())
                .setParameter("description", entity.getDescription())
                .setParameter("componentCode", entity.getComponentCode())
                .setParameter("status", entity.getStatus())
                .setParameter("isActive", entity.getIsActive())
                .setParameter("isDisplay", entity.getIsDisplay())
                .setParameter("newData", entity.getNewData())
                .setParameter("effectiveDate", entity.getEffectiveDate())
                .setParameter("endEffectiveDate", entity.getEndEffectiveDate())
                .executeUpdate();

        if (updated == 0) {
            throw new BusinessException(ErrorCode.GC_NOT_FOUND, "Khong tim thay ban ghi voi id = " + entity.getId());
        }
    }

    /*
    Query sequence Oracle để lấy id tiếp theo.
    dual là bảng giả trong Oracle.
    NEXTVAL tăng sequence lên 1 đơn vị.
    Ép kết quả sang long.
     */
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
    ) {
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

    private void setSearchParams(Query query, GroupCategorySearchReq req) {
        setLikeParameter(query, req.paramName(), "paramName");
        setLikeParameter(query, req.paramValue(), "paramValue");
        setLikeParameter(query, req.paramType(), "paramType");
        setInParameter(query, req.status(), "status");
        setInParameter(query, req.isActive(), "isActive");
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
        entity.setNewData((String) row[9]);
        entity.setEffectiveDate(toLocalDate(row[10]));
        entity.setEndEffectiveDate(toLocalDate(row[11]));
        return entity;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void appendLikeCondition(StringBuilder sql, String value, String column, String param) {
        if (hasText(value)) {
            sql.append(" AND UPPER(")
                    .append(column)
                    .append(") LIKE UPPER(:")
                    .append(param)
                    .append(")");
        }
    }

    private void appendInCondition(StringBuilder sql, List<?> values, String column, String param) {
        if (values != null && !values.isEmpty()) {
            sql.append(" AND ")
                    .append(column)
                    .append(" IN (:")
                    .append(param)
                    .append(")");
        }
    }

    private void setLikeParameter(Query query, String value, String param) {
        if (hasText(value)) {
            query.setParameter(param, "%" + value.trim() + "%");
        }
    }

    private void setInParameter(Query query, List<?> values, String param) {
        if (values != null && !values.isEmpty()) {
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
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        return null;
    }
}
