package com.example.demo.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_REQUEST("CM_400", HttpStatus.BAD_REQUEST, "Du lieu dau vao khong hop le"),
    UNAUTHORIZED("CM_401", HttpStatus.UNAUTHORIZED, "Ban chua dang nhap"),
    FORBIDDEN("CM_403", HttpStatus.FORBIDDEN, "Ban khong co quyen thuc hien thao tac nay"),
    NOT_FOUND("CM_404", HttpStatus.NOT_FOUND, "Khong tim thay du lieu"),
    CONFLICT("CM_409", HttpStatus.CONFLICT, "Du lieu da ton tai"),
    INVALID_STATE("CM_422", HttpStatus.UNPROCESSABLE_ENTITY, "Trang thai du lieu khong hop le"),
    INTERNAL_ERROR("CM_500", HttpStatus.INTERNAL_SERVER_ERROR, "Loi he thong"),

    GC_INVALID_ID("GC_400_01", HttpStatus.BAD_REQUEST, "Id khong hop le"),
    GC_INVALID_PAGE("GC_400_02", HttpStatus.BAD_REQUEST, "page phai >= 0 va size phai trong khoang 1-100"),
    GC_PARAM_NAME_REQUIRED("GC_400_03", HttpStatus.BAD_REQUEST, "paramName khong duoc de trong"),
    GC_PARAM_VALUE_REQUIRED("GC_400_04", HttpStatus.BAD_REQUEST, "paramValue khong duoc de trong"),
    GC_PARAM_TYPE_REQUIRED("GC_400_05", HttpStatus.BAD_REQUEST, "paramType khong duoc de trong"),

    GC_NOT_FOUND("GC_404", HttpStatus.NOT_FOUND, "Khong tim thay nhom danh muc"),
    GC_DUPLICATE("GC_409", HttpStatus.CONFLICT, "Ban ghi da ton tai"),
    GC_NO_CHANGES("GC_422", HttpStatus.UNPROCESSABLE_ENTITY, "Khong co du lieu thay doi"),
    GC_PENDING_CANNOT_DELETE("GC_422_01", HttpStatus.UNPROCESSABLE_ENTITY, "Ban ghi dang cho duyet thi khong duoc xoa"),
    GC_APPROVED_CANNOT_DELETE("GC_422_02", HttpStatus.UNPROCESSABLE_ENTITY, "Ban ghi da duyet / dang hien thi thi khong duoc xoa"),
    GC_ONLY_PENDING_CAN_APPROVE("GC_422_03", HttpStatus.UNPROCESSABLE_ENTITY, "Chi ban ghi cho phe duyet moi duoc duyet"),
    GC_ONLY_PENDING_CAN_REJECT("GC_422_04", HttpStatus.UNPROCESSABLE_ENTITY, "Chi ban ghi cho phe duyet moi duoc tu choi"),
    GC_ONLY_APPROVED_CAN_CANCEL("GC_422_05", HttpStatus.UNPROCESSABLE_ENTITY, "Chi ban ghi da phe duyet moi duoc huy duyet"),
    GC_ALREADY_PENDING("GC_422_06", HttpStatus.UNPROCESSABLE_ENTITY, "Ban ghi dang o trang thai cho phe duyet"),

    GC_INVALID_DATE_RANGE("GC_400_06", HttpStatus.BAD_REQUEST, "endEffectiveDate phai lon hon hoac bang effectiveDate"),
    GC_INVALID_NEW_DATA("GC_500_01", HttpStatus.INTERNAL_SERVER_ERROR, "NEW_DATA khong dung dinh dang JSON"),
    GC_SERIALIZE_NEW_DATA_FAILED("GC_500_02", HttpStatus.INTERNAL_SERVER_ERROR, "Khong serialize duoc NEW_DATA"),

    GC_CREATE_FAILED("GC_500_03", HttpStatus.INTERNAL_SERVER_ERROR, "Tao nhom danh muc that bai"),
    GC_UPDATE_FAILED("GC_500_04", HttpStatus.INTERNAL_SERVER_ERROR, "Cap nhat nhom danh muc that bai"),
    GC_DELETE_FAILED("GC_500_05", HttpStatus.INTERNAL_SERVER_ERROR, "Xoa nhom danh muc that bai"),
    GC_SEARCH_FAILED("GC_500_06", HttpStatus.INTERNAL_SERVER_ERROR, "Tim kiem nhom danh muc that bai"),
    GC_GET_ALL_FAILED("GC_500_07", HttpStatus.INTERNAL_SERVER_ERROR, "Lay danh sach nhom danh muc that bai");

    private final String code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }
}
