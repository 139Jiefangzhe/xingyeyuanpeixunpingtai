package com.playedu.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.playedu.user.domain.entity.Department;
import com.playedu.user.dto.resp.DepartmentTreeResp;
import com.playedu.user.mapper.DepartmentMapper;
import com.playedu.user.service.DepartmentService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentMapper departmentMapper;

    public DepartmentServiceImpl(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    @Override
    public List<DepartmentTreeResp> getDeptTree() {
        LambdaQueryWrapper<Department> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Department::getSort).orderByAsc(Department::getId);
        List<Department> departments = departmentMapper.selectList(queryWrapper);
        if (departments.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, List<Department>> grouped = new LinkedHashMap<>();
        for (Department department : departments) {
            grouped.computeIfAbsent(normalizeParentId(department.getParentId()), key -> new ArrayList<>()).add(department);
        }
        return buildTree(0, grouped);
    }

    private List<DepartmentTreeResp> buildTree(Integer parentId, Map<Integer, List<Department>> grouped) {
        List<Department> children = grouped.getOrDefault(parentId, Collections.emptyList());
        if (children.isEmpty()) {
            return Collections.emptyList();
        }

        List<DepartmentTreeResp> result = new ArrayList<>(children.size());
        for (Department child : children) {
            DepartmentTreeResp resp = DepartmentTreeResp.from(child);
            resp.setChildren(buildTree(child.getId(), grouped));
            result.add(resp);
        }
        return result;
    }

    private Integer normalizeParentId(Integer parentId) {
        return parentId == null ? 0 : parentId;
    }
}
