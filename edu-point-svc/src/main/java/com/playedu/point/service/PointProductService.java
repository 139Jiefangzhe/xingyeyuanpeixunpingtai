package com.playedu.point.service;

import com.playedu.point.dto.req.PointProductSaveReq;
import com.playedu.point.dto.resp.PointProductResp;
import java.util.List;

public interface PointProductService {
    List<PointProductResp> listProducts(boolean includeDisabled);

    PointProductResp getProductById(String id);

    String createProduct(PointProductSaveReq req);

    void updateProduct(String id, PointProductSaveReq req);

    void deleteProduct(String id);
}
