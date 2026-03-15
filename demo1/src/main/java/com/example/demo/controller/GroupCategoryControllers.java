package com.example.demo.controller;

import com.example.demo.dto.GroupCategoryResponse;
import com.example.demo.dto.GroupCategorySearchReq;
import com.example.demo.dto.GroupCategoryUpsertReq;
import com.example.demo.dto.RejectReq;
import com.example.demo.entity.GroupCategory;
import com.example.demo.service.GroupCategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/jpa/categories")
public class GroupCategoryControllers {

    private final GroupCategoryService service;

    public GroupCategoryControllers(GroupCategoryService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupCategoryResponse create(@RequestBody GroupCategoryUpsertReq req) {
        return GroupCategoryResponse.from(service.create(req));
    }

    @PostMapping("/submit-create")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupCategoryResponse createAndSubmit(@RequestBody GroupCategoryUpsertReq req) {
        return GroupCategoryResponse.from(service.createAndSubmit(req));
    }

    @GetMapping
    public Page<GroupCategory> getAll(@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)Pageable pageable) {
        return service.getCategory(pageable);
    }

    @GetMapping("/{id}")
    public GroupCategory getById(@PathVariable Long id) {
        return service.getCategoryById(id);
    }

    @PutMapping("/{id}")
    public GroupCategoryResponse update(@PathVariable Long id, @RequestBody GroupCategoryUpsertReq req) {
        return GroupCategoryResponse.from(service.update(id, req));
    }

    @PostMapping("/{id}/submit")
    public GroupCategoryResponse submit(@PathVariable Long id) {
        return GroupCategoryResponse.from(service.submit(id));
    }

    @PostMapping("/{id}/approve")
    public GroupCategoryResponse approve(@PathVariable Long id) {
        return GroupCategoryResponse.from(service.approve(id));
    }

    @PostMapping("/{id}/reject")
    public GroupCategoryResponse reject(@PathVariable Long id, @RequestBody RejectReq req) {
        return GroupCategoryResponse.from(service.reject(id, req.reason()));
    }

    @PostMapping("/{id}/cancel-approve")
    public GroupCategoryResponse cancelApprove(@PathVariable Long id) {
        return GroupCategoryResponse.from(service.cancelApprove(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/search")
    public Page<GroupCategory> search(
            @RequestBody GroupCategorySearchReq req,
            @PageableDefault(size = 2000, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return service.search(req, pageable);
    }
}