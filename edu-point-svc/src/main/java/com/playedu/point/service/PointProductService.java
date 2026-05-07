package com.playedu.point.service;

import com.playedu.point.dto.resp.PointProductResp;
import java.util.List;

public interface PointProductService {
    List<PointProductResp> listProducts();

    PointProductResp getProductById(String id);
}

