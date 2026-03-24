package com.example.demo.service;

import com.example.demo.common.paging.PageResponse;
import com.example.demo.dto.request.GroupCategoryCreateReq;
import com.example.demo.dto.request.GroupCategorySearchReq;
import com.example.demo.dto.request.GroupCategoryUpdateReq;
import com.example.demo.entity.GroupCategory;
import com.example.demo.repository.GroupCategoryProcedureRepo;
import org.springframework.stereotype.Service;

@Service
public class GroupCategoryPrcService {

    private final GroupCategoryProcedureRepo repository;

    public GroupCategoryPrcService(GroupCategoryProcedureRepo repository){
        this.repository = repository;
    }

    public Long create(GroupCategoryCreateReq req){
        return repository.create(req);
    }

    public PageResponse<GroupCategory> getAll(int page, int size){
        return repository.getAll(page, size);
    }

    public GroupCategory getById(Long id){
        return repository.getById(id);
    }

    public Long update(Long id, GroupCategoryUpdateReq req){
        return repository.update(id, req);
    }

    public void delete(Long id){
        repository.delete(id);
    }

    public PageResponse<GroupCategory> search(GroupCategorySearchReq req) {
        return repository.search(req);
    }
}
