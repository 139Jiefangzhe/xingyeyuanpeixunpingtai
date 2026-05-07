import { pointClient } from "./internal/serviceClients";

const LOCAL_USER_ID = String(import.meta.env.VITE_LOCAL_USER_ID || "10005");

export interface PointProduct {
  id: string;
  name: string;
  description: string;
  imageUrl?: string;
  pointsPrice: number;
  stock: number;
  status: number;
  sort: number;
  createTime?: string;
  updateTime?: string;
}

export interface PointRecord {
  id: number;
  userId: number;
  ruleType?: string;
  points: number;
  balance: number;
  sourceId?: string;
  sourceType?: string;
  remark?: string;
  createTime: string;
}

export const getBalance = () =>
  pointClient.request({
    url: "/api/v1/point-records/balance",
    method: "get",
    headers: {
      "X-User-Id": LOCAL_USER_ID,
    },
  });

export const listProducts = () => pointClient.get("/api/v1/point-products", {});

export const listRecords = () =>
  pointClient.request({
    url: "/api/v1/point-records/my",
    method: "get",
    headers: {
      "X-User-Id": LOCAL_USER_ID,
    },
  });
