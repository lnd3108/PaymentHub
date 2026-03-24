package com.example.demo.common.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Common
    INVALID_REQUEST("CM_400", HttpStatus.BAD_REQUEST, "Dữ liệu đầu vào không hợp lệ"),
    NOT_FOUND("CM_404", HttpStatus.NOT_FOUND, "Không tìm thấy dữ liệu"),
    CONFLICT("CM_409", HttpStatus.CONFLICT, "Dữ liệu đã tồn tại"),
    INVALID_STATE("CM_422", HttpStatus.UNPROCESSABLE_ENTITY, "Trạng thái dữ liệu không hợp lệ"),
    INTERNAL_ERROR("CM_500", HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống"),

    // Group Category
    GC_NOT_FOUND("GC_404", HttpStatus.NOT_FOUND, "Không tìm thấy nhóm danh mục"),
    GC_DUPLICATE("GC_409", HttpStatus.CONFLICT, "Bản ghi đã tồn tại"),
    GC_NO_CHANGES("GC_422", HttpStatus.UNPROCESSABLE_ENTITY, "Không có dữ liệu thay đổi"),
    GC_PENDING_CANNOT_DELETE("GC_422", HttpStatus.UNPROCESSABLE_ENTITY, "Bản ghi đang chờ duyệt thì không được xóa"),
    GC_APPROVED_CANNOT_DELETE("GC_422", HttpStatus.UNPROCESSABLE_ENTITY, "Bản ghi đã duyệt / đang hiển thị thì không được xóa"),
    GC_ONLY_PENDING_CAN_APPROVE("GC_422", HttpStatus.UNPROCESSABLE_ENTITY, "Chỉ bản ghi chờ phê duyệt mới được duyệt"),
    GC_ONLY_PENDING_CAN_REJECT("GC_422", HttpStatus.UNPROCESSABLE_ENTITY, "Chỉ bản ghi chờ phê duyệt mới được từ chối"),
    GC_ONLY_APPROVED_CAN_CANCEL("GC_422", HttpStatus.UNPROCESSABLE_ENTITY, "Chỉ bản ghi đã phê duyệt mới được hủy duyệt"),
    GC_ALREADY_PENDING("GC_422", HttpStatus.UNPROCESSABLE_ENTITY, "Bản ghi đang ở trạng thái chờ phê duyệt"),
    GC_INVALID_DATE_RANGE("GC_400", HttpStatus.BAD_REQUEST, "endEffectiveDate phải lớn hơn hoặc bằng effectiveDate"),
    GC_INVALID_NEW_DATA("GC_500", HttpStatus.INTERNAL_SERVER_ERROR, "NEW_DATA không đúng định dạng JSON"),
    GC_SERIALIZE_NEW_DATA_FAILED("GC_500", HttpStatus.INTERNAL_SERVER_ERROR, "Không serialize được NEW_DATA");


    private final String code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }
}
