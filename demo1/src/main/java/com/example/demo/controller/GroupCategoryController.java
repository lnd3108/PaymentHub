package com.example.demo.controller;

import com.example.demo.common.paging.PagingRequest;
import com.example.demo.common.response.ApiResponse;
import com.example.demo.common.paging.PageResponse;
import com.example.demo.dto.response.GroupCategoryResponse;
import com.example.demo.dto.request.GroupCategorySearchReq;
import com.example.demo.dto.request.GroupCategoryUpsertReq;
import com.example.demo.dto.request.GroupCategoryRejectReq;
import com.example.demo.service.GroupCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jpa/categories")
public class GroupCategoryController {

    private final GroupCategoryService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GroupCategoryResponse> create(@RequestBody GroupCategoryUpsertReq req) {
        return ApiResponse.success("Create success", GroupCategoryResponse.from(service.create(req)));
    }

    @PostMapping("/submit-create")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GroupCategoryResponse> createAndSubmit(@RequestBody GroupCategoryUpsertReq req) {
        return ApiResponse.success("Create and submit success", GroupCategoryResponse.from(service.createAndSubmit(req)));
    }

    @GetMapping
    public ApiResponse<PageResponse<GroupCategoryResponse>> getAll(PagingRequest pagingRequest) {
        return ApiResponse.success(service.getCategory(pagingRequest));
    }

    @GetMapping("/{id}")
    public ApiResponse<GroupCategoryResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(GroupCategoryResponse.from(service.getCategoryById(id)));
    }

    @PutMapping("/{id}")
    public ApiResponse<GroupCategoryResponse> update(@PathVariable Long id, @RequestBody GroupCategoryUpsertReq req) {
        return ApiResponse.success("Update success", GroupCategoryResponse.from(service.update(id, req)));
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<GroupCategoryResponse> submit(@PathVariable Long id) {
        return ApiResponse.success("Submit success", GroupCategoryResponse.from(service.submit(id)));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<GroupCategoryResponse> approve(@PathVariable Long id) {
        return ApiResponse.success("Approve success", GroupCategoryResponse.from(service.approve(id)));
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<GroupCategoryResponse> reject(@PathVariable Long id, @RequestBody GroupCategoryRejectReq req) {
        return ApiResponse.success("Reject success", GroupCategoryResponse.from(service.reject(id, req.reason())));
    }

    @PostMapping("/{id}/cancel-approve")
    public ApiResponse<GroupCategoryResponse> cancelApprove(@PathVariable Long id) {
        return ApiResponse.success("Cancel approve success", GroupCategoryResponse.from(service.cancelApprove(id)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success("Delete success", "Deleted successfully");
    }

    @PostMapping("/search")
    public ApiResponse<PageResponse<GroupCategoryResponse>> search(
            @RequestBody GroupCategorySearchReq req,
            PagingRequest pagingRequest
    ) {
        return ApiResponse.success(service.search(req, pagingRequest));
    }
}