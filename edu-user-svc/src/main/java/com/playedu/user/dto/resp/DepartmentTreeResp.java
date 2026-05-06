package com.playedu.user.dto.resp;

import com.playedu.user.domain.entity.Department;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class DepartmentTreeResp {
    private Long id;

    private String name;

    private Long parentId;

    private Integer sort;

    private List<DepartmentTreeResp> children = new ArrayList<>();

    public static DepartmentTreeResp from(Department department) {
        DepartmentTreeResp resp = new DepartmentTreeResp();
        resp.setId(department.getId() == null ? null : department.getId().longValue());
        resp.setName(department.getName());
        resp.setParentId(department.getParentId() == null ? 0L : department.getParentId().longValue());
        resp.setSort(department.getSort());
        return resp;
    }
}
