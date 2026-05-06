package com.playedu.user.service;

import com.playedu.user.dto.resp.DepartmentTreeResp;
import java.util.List;

public interface DepartmentService {
    List<DepartmentTreeResp> getDeptTree();
}
