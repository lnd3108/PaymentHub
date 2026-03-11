package com.example.demo.repository;

import com.example.demo.entity.GroupCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GroupCategoryRepository extends JpaRepository<GroupCategory, Long>, JpaSpecificationExecutor<GroupCategory> {
    GroupCategory findByParamName(String paramName);
}

