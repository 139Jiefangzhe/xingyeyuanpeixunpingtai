package com.playedu.point.service;

import com.playedu.point.dto.resp.PointRecordResp;
import java.util.List;

public interface PointRecordService {
    List<PointRecordResp> listMyRecords(Long userId);

    Integer getBalance(Long userId);
}

