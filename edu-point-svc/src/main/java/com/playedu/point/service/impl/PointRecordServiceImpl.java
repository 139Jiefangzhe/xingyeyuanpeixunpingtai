package com.playedu.point.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.playedu.point.domain.PointRecord;
import com.playedu.point.dto.resp.PointRecordResp;
import com.playedu.point.mapper.PointRecordMapper;
import com.playedu.point.service.PointRecordService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PointRecordServiceImpl implements PointRecordService {
    private final PointRecordMapper pointRecordMapper;

    public PointRecordServiceImpl(PointRecordMapper pointRecordMapper) {
        this.pointRecordMapper = pointRecordMapper;
    }

    @Override
    public List<PointRecordResp> listMyRecords(Long userId) {
        validateUserId(userId);
        return pointRecordMapper.selectList(
                        new LambdaQueryWrapper<PointRecord>()
                                .eq(PointRecord::getUserId, userId)
                                .orderByDesc(PointRecord::getCreateTime)
                                .orderByDesc(PointRecord::getId))
                .stream()
                .map(PointRecordResp::fromEntity)
                .toList();
    }

    @Override
    public Integer getBalance(Long userId) {
        validateUserId(userId);
        PointRecord latest =
                pointRecordMapper.selectOne(
                        new LambdaQueryWrapper<PointRecord>()
                                .eq(PointRecord::getUserId, userId)
                                .orderByDesc(PointRecord::getCreateTime)
                                .orderByDesc(PointRecord::getId)
                                .last("LIMIT 1"));
        return latest == null ? 0 : latest.getBalance();
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0L) {
            throw new IllegalArgumentException("用户ID不合法");
        }
    }
}

