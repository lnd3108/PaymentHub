package com.example.demo.groupcategory.service;

import com.example.demo.groupcategory.dto.excel.GroupCatExcelImportResult;
import com.example.demo.groupcategory.dto.request.GroupCategorySearchReq;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

public interface GroupCategoryExcelService {
    byte[] exportExcel(GroupCategorySearchReq req);
    GroupCatExcelImportResult importExcel(MultipartFile file, boolean submitAfterImport);


}
