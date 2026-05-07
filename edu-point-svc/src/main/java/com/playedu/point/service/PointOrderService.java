package com.playedu.point.service;

import com.playedu.point.dto.resp.PointOrderResp;
import java.util.List;

public interface PointOrderService {
    List<PointOrderResp> listMyOrders(Long userId);

    PointOrderResp getOrderById(String id);
}

