package com.example.demo.groupcategory.controller;

import com.example.demo.common.paging.PageResponse;
import com.example.demo.common.paging.PagingRequest;
import com.example.demo.common.response.ApiResponse;
import com.example.demo.groupcategory.dto.request.GroupCategoryRejectReq;
import com.example.demo.groupcategory.dto.request.GroupCategorySearchReq;
import com.example.demo.groupcategory.dto.request.GroupCategoryUpsertReq;
import com.example.demo.groupcategory.dto.response.GroupCategoryResponse;
import com.example.demo.groupcategory.service.GroupCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jpa/categories")
public class GroupCategoryController {

    private final GroupCategoryService service;

    @PreAuthorize("hasAuthority('CREATE') or hasAuthority('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GroupCategoryResponse> create(@Valid @RequestBody GroupCategoryUpsertReq req) {
        return ApiResponse.success("Create success", GroupCategoryResponse.from(service.create(req)));
    }

    @PreAuthorize("hasAuthority('CREATE') or hasAuthority('SUBMIT') or hasAuthority('ADMIN')")
    @PostMapping("/submit-create")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GroupCategoryResponse> createAndSubmit(@Valid @RequestBody GroupCategoryUpsertReq req) {
        return ApiResponse.success("Create and submit success", GroupCategoryResponse.from(service.createAndSubmit(req)));
    }

    @PreAuthorize("hasAuthority('LIST') or hasAuthority('ADMIN')")
    @GetMapping
    public ApiResponse<PageResponse<GroupCategoryResponse>> getAll(PagingRequest pagingRequest) {
        return ApiResponse.success(service.getCategory(pagingRequest));
    }

    @PreAuthorize("hasAuthority('LIST') or hasAuthority('ADMIN')")
    @GetMapping("/{id}")
    public ApiResponse<GroupCategoryResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(GroupCategoryResponse.from(service.getCategoryById(id)));
    }

    @PreAuthorize("hasAuthority('UPDATE') or hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<GroupCategoryResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody GroupCategoryUpsertReq req) {
        return ApiResponse.success("Update success", GroupCategoryResponse.from(service.update(id, req)));
    }

    @PreAuthorize("hasAuthority('SUBMIT') or hasAuthority('ADMIN')")
    @PostMapping("/{id}/submit")
    public ApiResponse<GroupCategoryResponse> submit(@PathVariable Long id) {
        return ApiResponse.success("Submit success", GroupCategoryResponse.from(service.submit(id)));
    }

    @PreAuthorize("hasAuthority('APPROVE') or hasAuthority('ADMIN')")
    @PostMapping("/{id}/approve")
    public ApiResponse<GroupCategoryResponse> approve(@PathVariable Long id) {
        return ApiResponse.success("Approve success", GroupCategoryResponse.from(service.approve(id)));
    }

    @PreAuthorize("hasAuthority('REJECT') or hasAuthority('ADMIN')")
    @PostMapping("/{id}/reject")
    public ApiResponse<GroupCategoryResponse> reject(@PathVariable Long id,
                                                     @Valid @RequestBody GroupCategoryRejectReq req) {
        return ApiResponse.success("Reject success", GroupCategoryResponse.from(service.reject(id, req.reason())));
    }

    @PreAuthorize("hasAuthority('CANCEL_APPROVE') or hasAuthority('ADMIN')")
    @PostMapping("/{id}/cancel-approve")
    public ApiResponse<GroupCategoryResponse> cancelApprove(@PathVariable Long id) {
        return ApiResponse.success("Cancel approve success", GroupCategoryResponse.from(service.cancelApprove(id)));
    }

    @PreAuthorize("hasAuthority('DELETE') or hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success("Delete success", "Deleted successfully");
    }

    @PreAuthorize("hasAuthority('SEARCH') or hasAuthority('ADMIN')")
    @PostMapping("/search")
    public ApiResponse<PageResponse<GroupCategoryResponse>> search(@RequestBody GroupCategorySearchReq req,
                                                                   PagingRequest pagingRequest) {
        return ApiResponse.success(service.search(req, pagingRequest));
    }
}