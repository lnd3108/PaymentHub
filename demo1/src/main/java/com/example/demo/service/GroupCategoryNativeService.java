package com.example.demo.service;

import com.example.demo.dto.GroupCategoryCreateReq;
import com.example.demo.dto.GroupCategorySearchReq;
import com.example.demo.dto.GroupCategoryUpdateReq;
import com.example.demo.entity.GroupCategory;
import com.example.demo.repository.GroupCategoryNativeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupCategoryNativeService {

    private final GroupCategoryNativeRepository repository;

    public GroupCategoryNativeService(GroupCategoryNativeRepository repository) {
        this.repository = repository;
    }

    public Long create(GroupCategoryCreateReq req) {
        return repository.create(req);
    }

    public List<GroupCategory> getAll(){
        return repository.getAll();
    }

    public GroupCategory getById(Long id) {
        return repository.getById(id);
    }

    public Long update(Long id, GroupCategoryUpdateReq req) {
        return repository.update(id, req);
    }

    public void delete(Long id) {
        repository.delete(id);
    }

    public List<GroupCategory> search(GroupCategorySearchReq req) {
        return repository.search(req);
    }
}