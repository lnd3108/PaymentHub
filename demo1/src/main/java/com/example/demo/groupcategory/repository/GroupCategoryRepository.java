package com.example.demo.groupcategory.repository;

import com.example.demo.groupcategory.entity.GroupCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

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

    @Query("""
        select g from GroupCategory g
        where upper(trim(g.paramName)) in :paramNames
          and upper(trim(g.paramValue)) in :paramValues
          and upper(trim(g.paramType)) in :paramTypes
    """)
    List<GroupCategory> findForImportDuplicateCheck(
            Set<String> paramNames,
            Set<String> paramValues,
            Set<String> paramTypes
    );
}