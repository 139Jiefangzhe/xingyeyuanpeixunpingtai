package com.playedu.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.playedu.common.exception.BizException;
import com.playedu.user.domain.entity.User;
import com.playedu.user.domain.entity.UserDepartment;
import com.playedu.user.dto.resp.UserResp;
import com.playedu.user.mapper.UserDepartmentMapper;
import com.playedu.user.mapper.UserMapper;
import com.playedu.user.service.UserService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserDepartmentMapper userDepartmentMapper;

    public UserServiceImpl(UserMapper userMapper, UserDepartmentMapper userDepartmentMapper) {
        this.userMapper = userMapper;
        this.userDepartmentMapper = userDepartmentMapper;
    }

    @Override
    public UserResp getUserById(Long id) {
        Integer userId = toIntId(id);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("404001", "用户不存在");
        }
        List<Long> deptIds = loadDeptIds(List.of(userId)).getOrDefault(userId, Collections.emptyList());
        return UserResp.from(user, deptIds);
    }

    @Override
    public List<UserResp> batchGetUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> normalizedIds =
                userIds.stream()
                        .filter(Objects::nonNull)
                        .map(this::toIntId)
                        .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), ArrayList::new));
        if (normalizedIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, User> userMap =
                userMapper.selectBatchIds(normalizedIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Integer, List<Long>> deptIdMap = loadDeptIds(normalizedIds);

        List<UserResp> result = new ArrayList<>();
        for (Integer userId : normalizedIds) {
            User user = userMap.get(userId);
            if (user != null) {
                result.add(UserResp.from(user, deptIdMap.getOrDefault(userId, Collections.emptyList())));
            }
        }
        return result;
    }

    private Map<Integer, List<Long>> loadDeptIds(Collection<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<UserDepartment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(UserDepartment::getUserId, userIds);
        return userDepartmentMapper.selectList(queryWrapper).stream()
                .collect(
                        Collectors.groupingBy(
                                UserDepartment::getUserId,
                                Collectors.mapping(item -> item.getDepId().longValue(), Collectors.toList())));
    }

    private Integer toIntId(Long id) {
        if (id == null || id <= 0L) {
            throw new IllegalArgumentException("用户ID不合法");
        }
        if (id > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("用户ID超出旧表范围");
        }
        return id.intValue();
    }
}
