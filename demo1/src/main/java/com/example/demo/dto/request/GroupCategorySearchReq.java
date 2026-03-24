package com.example.demo.dto.request;

import java.util.List;

public record GroupCategorySearchReq(
        String paramName,
        String paramValue,
        String paramType,
        List<Integer> status,
        List<Integer> isActive,
        Integer page,
        Integer size
){
}
