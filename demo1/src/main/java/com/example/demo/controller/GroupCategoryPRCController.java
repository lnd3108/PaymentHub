package com.example.demo.controller;

import com.example.demo.common.response.ApiResponse;
import com.example.demo.common.response.PageResponse;
import com.example.demo.dto.GroupCategoryCreateReq;
import com.example.demo.dto.GroupCategorySearchReq;
import com.example.demo.dto.GroupCategoryUpdateReq;
import com.example.demo.entity.GroupCategory;
import com.example.demo.service.GroupCategoryPrcService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("http://localhost:4200/")
@RestController
@RequestMapping("/api/prc/categories")
public class GroupCategoryPRCController {

    private final GroupCategoryPrcService service;

    public GroupCategoryPRCController(GroupCategoryPrcService service){
        this.service = service;
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody GroupCategoryCreateReq req){
        return ApiResponse.success(service.create(req));
    }

    @GetMapping
    public ApiResponse<PageResponse<GroupCategory>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ApiResponse.success(service.getAll(page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<GroupCategory> getById(@PathVariable Long id){
        return ApiResponse.success(service.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<Long> update(@PathVariable Long id, @RequestBody GroupCategoryUpdateReq req) {
        return ApiResponse.success(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success("Deleted successfully", "OK");
    }

    @PostMapping("/search")
    public ApiResponse<PageResponse<GroupCategory>> search(@RequestBody GroupCategorySearchReq req) {
        return ApiResponse.success(service.search(req));
    }


}
