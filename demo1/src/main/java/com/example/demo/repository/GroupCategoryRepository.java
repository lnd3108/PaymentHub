package com.example.demo.repository;

import com.example.demo.entity.GroupCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface GroupCategoryRepository extends JpaRepository<GroupCategory, Long>, JpaSpecificationExecutor<GroupCategory> {

    Optional<GroupCategory> findByParamNameAndParamValueAndParamType(
            String paramName,
            String paramValue,
            String paramType
    );

    boolean existsByParamNameAndParamValueAndParamType(
            String paramName,
            String paramValue,
            String paramType
    );
}