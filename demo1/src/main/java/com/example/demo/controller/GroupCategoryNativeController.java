package com.example.demo.controller;

import com.example.demo.common.paging.PageResponse;
import com.example.demo.dto.request.GroupCategoryCreateReq;
import com.example.demo.dto.request.GroupCategorySearchReq;
import com.example.demo.dto.request.GroupCategoryUpdateReq;
import com.example.demo.entity.GroupCategory;
import com.example.demo.service.GroupCategoryNativeService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("http://localhost:4200/")
@RestController
@RequestMapping("/api/native/categories")
public class GroupCategoryNativeController {

    private final GroupCategoryNativeService service;

    public GroupCategoryNativeController(GroupCategoryNativeService service) {
        this.service = service;
    }

    @PostMapping
    public Long create(@RequestBody GroupCategoryCreateReq req) {
        return service.create(req);
    }

    @GetMapping
    public PageResponse<GroupCategory> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.getAll(page, size);
    }

    @GetMapping("/{id}")
    public GroupCategory getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Long update(@PathVariable Long id, @RequestBody GroupCategoryUpdateReq req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "Deleted successfully";
    }

    @PostMapping("/search")
    public PageResponse<GroupCategory> search(
            @RequestBody GroupCategorySearchReq req,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.search(req, page, size);
    }
}