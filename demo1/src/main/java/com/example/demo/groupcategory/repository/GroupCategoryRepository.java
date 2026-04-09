package com.example.demo.groupcategory.repository;

import com.example.demo.groupcategory.entity.GroupCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GroupCategoryRepository extends JpaRepository<GroupCategory, Long>, JpaSpecificationExecutor<GroupCategory> {

    boolean existsByParamNameAndParamValueAndParamType(
            String paramName,
            String paramValue,
            String paramType
    );

    boolean existsByParamNameAndParamValueAndParamTypeAndIdNot(
            String paramName,
            String paramValue,
            String paramType,
            Long id
    );
}