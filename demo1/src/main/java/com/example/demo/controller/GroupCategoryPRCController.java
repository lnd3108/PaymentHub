package com.example.demo.controller;

import com.example.demo.dto.GroupCategoryCreateReq;
import com.example.demo.dto.GroupCategorySearchReq;
import com.example.demo.dto.GroupCategoryUpdateReq;
import com.example.demo.entity.GroupCategory;
import com.example.demo.service.GroupCategoryPrcService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("http://localhost:4200/")
@RestController
@RequestMapping("/api/prc/categories")
public class GroupCategoryPRCController {

    private final GroupCategoryPrcService service;

    public GroupCategoryPRCController(GroupCategoryPrcService service){
        this.service = service;
    }

    @PostMapping
    public Long create(@RequestBody GroupCategoryCreateReq req){
        return service.create(req);
    }

    @GetMapping
    public List<GroupCategory> getAll(){
        return service.getAll();
    }

    @GetMapping("/{id}")
    public GroupCategory getById(@PathVariable Long id){
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
    public List<GroupCategory> search(@RequestBody GroupCategorySearchReq req) {
        return service.search(req);
    }


}
