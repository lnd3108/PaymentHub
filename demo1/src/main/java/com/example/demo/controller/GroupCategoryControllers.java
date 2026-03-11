package com.example.demo.controller;

import com.example.demo.dto.GroupCategorySearchReq;
import com.example.demo.service.GroupCategoryService;
import com.example.demo.entity.GroupCategory;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/jpa/categories")
public class GroupCategoryControllers {

    private final GroupCategoryService service;

    public GroupCategoryControllers(GroupCategoryService service){
        this.service = service;
    }

    @PostMapping("/created")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupCategory create(@RequestBody GroupCategory req){
        return service.create(req);
    }

    @GetMapping
    public List<GroupCategory> getAll() {
        return service.getCategory();
    }

    @GetMapping("/{id}")
    public GroupCategory getById(@PathVariable Long id){
        return service.getCategoryById(id);
    }

    @PutMapping("/{id}")
    public GroupCategory update(@PathVariable Long id, @RequestBody GroupCategory req){
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        service.delete(id);
    }

    @PostMapping("/search")
    public Page<GroupCategory> search(
            @RequestBody GroupCategorySearchReq req,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)Pageable pageable
            ){
        return service.search(req, pageable);
    }
}
