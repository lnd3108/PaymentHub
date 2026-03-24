package com.example.demo.specification;

import com.example.demo.dto.GroupCategorySearchReq;
import com.example.demo.entity.GroupCategory;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class GroupCategorySpecification {

    public static Specification<GroupCategory> search(GroupCategorySearchReq req) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            if (hasText(req.paramType())) {
                ps.add(cb.like(cb.upper(root.get("paramType")), "%" + req.paramType().trim().toUpperCase() + "%"));
            }
            if (hasText(req.paramValue())) {
                ps.add(cb.like(cb.upper(root.get("paramValue")), "%" + req.paramValue().trim().toUpperCase() + "%"));
            }
            if (hasText(req.paramName())) {
                ps.add(cb.like(cb.upper(root.get("paramName")), "%" + req.paramName().trim().toUpperCase() + "%"));
            }

            if (req.status() != null && !req.status().isEmpty()) {
                ps.add(root.get("status").in(req.status()));
            }

                if (req.isActive() != null && !req.isActive().isEmpty()) {
                ps.add(root.get("isActive").in(req.isActive()));
            }

            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}