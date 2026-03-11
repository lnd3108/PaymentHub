package com.example.demo.service;

import com.example.demo.dto.GroupCategorySearchReq;
import com.example.demo.repository.GroupCategorySpecs;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.entity.GroupCategory;
import com.example.demo.repository.GroupCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class GroupCategoryService {
    private final GroupCategoryRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GroupCategoryService(GroupCategoryRepository repository){
        this.repository = repository;
    }

    public GroupCategory create(GroupCategory req){
        req.setId(null);
        req.setStatus(1);
        req.setIsDisplay(1);
        req.setNewData(null);

        if(req.getIsActive() == null) req.setIsActive(1);
        return repository.save(req);
    }

    public GroupCategory update(Long id, GroupCategory req){
        GroupCategory cur = repository.findById(id).orElseThrow(() -> new RuntimeException("Not found: " + id));

        if (Objects.equals(cur.getStatus(), 4)){
            cur.setNewData(toJson(req));
            cur.setStatus(3);
            return repository.save(cur);
        }
        cur.setParamName(req.getParamName());
        cur.setParamValue(req.getParamValue());
        cur.setParamType(req.getParamType());
        cur.setDescription(req.getDescription());
        cur.setComponentCode(req.getComponentCode());
        cur.setIsActive(req.getIsActive() == null ? cur.getIsActive() : req.getIsActive());
        cur.setEffectiveDate(req.getEffectiveDate());
        cur.setEndEffectiveDate(req.getEndEffectiveDate());
        return repository.save(cur);
    }

    public  void delete(Long id){
        GroupCategory cur = repository.findById(id).orElseThrow(() -> new RuntimeException("Not found: " + id));
        if (Objects.equals(cur.getIsDisplay(), 2) || Objects.equals(cur.getStatus(), 4)) {
            throw new RuntimeException("Đã duyệt -> Không được xóa");
        }
        repository.delete(cur);
    }

    public Page<GroupCategory> search(GroupCategorySearchReq req, Pageable pageable){
        return repository.findAll(GroupCategorySpecs.search(req), pageable);
    }

    public List<GroupCategory> getCategory(){
        return repository.findAll();
    }

    public GroupCategory getCategoryById(Long id){
        return repository.findById(id).orElse(null);
    }

    public GroupCategory submit(Long id){
        GroupCategory cur = repository.findById(id).orElseThrow(() -> new RuntimeException("Not found: " + id));
        cur.setStatus(3);
        return repository.save(cur);
    }

    private String toJson(GroupCategory req){
        try{
            return objectMapper.writeValueAsString(req);

        }catch (JsonProcessingException e){
            throw new RuntimeException("Cannot serialize NEW_DATE", e);
        }
    }
}
