package com.example.demo.dto;

import java.util.List;

public record GroupCategorySearchReq(
        String paramName,
        String paramValue,
        String paramType,
        List<Integer> status,
        List<Integer> isActive
){
}
