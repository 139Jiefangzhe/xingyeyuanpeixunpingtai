import type { ApiResult } from "../types/api";
import request from "../utils/request";

export interface PointRule {
  id: string;
  name: string;
  ruleType: string;
  points: number;
  description?: string;
  status: number;
}

export interface PointProduct {
  id: string;
  name: string;
  description?: string;
  imageUrl?: string;
  pointsPrice: number;
  stock: number;
  status: number;
  sort: number;
  createTime?: string;
  updateTime?: string;
}

export interface PointOrder {
  id: string;
  userId: number;
  productId: string;
  productName: string;
  pointsPrice: number;
  quantity: number;
  totalPoints: number;
  status: string;
  address?: string;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface PointProductSaveReq {
  name: string;
  description?: string;
  imageUrl?: string;
  pointsPrice: number;
  stock: number;
  status?: number;
  sort?: number;
}

export const pointApi = {
  listRules() {
    return request.get<PointRule[]>("/api/v1/point-rules");
  },
  getRule(id: string) {
    return request.get<PointRule>(`/api/v1/point-rules/${id}`);
  },
  updateRuleStatus(id: string, status: number) {
    return request.put<void, { status: number }>(
      `/api/v1/point-rules/${id}/status`,
      { status }
    );
  },

  listProducts() {
    return request.get<PointProduct[]>("/api/v1/point-products", {
      params: {
        all: true,
      },
    });
  },
  getProduct(id: string) {
    return request.get<PointProduct>(`/api/v1/point-products/${id}`);
  },
  createProduct(data: PointProductSaveReq) {
    return request.post<string, PointProductSaveReq>("/api/v1/point-products", data);
  },
  updateProduct(id: string, data: PointProductSaveReq) {
    return request.put<void, PointProductSaveReq>(`/api/v1/point-products/${id}`, data);
  },
  deleteProduct(id: string) {
    return request.delete<void>(`/api/v1/point-products/${id}`);
  },

  listOrders(params?: Record<string, unknown>) {
    return request.get<PointOrder[]>("/api/v1/point-orders", {
      params,
    });
  },
  getOrder(id: string) {
    return request.get<PointOrder>(`/api/v1/point-orders/${id}`);
  },
  updateOrderStatus(id: string, status: string) {
    return request.put<void, { status: string }>(
      `/api/v1/point-orders/${id}/status`,
      { status }
    );
  },
} satisfies Record<string, (...args: any[]) => Promise<ApiResult<any>>>;
