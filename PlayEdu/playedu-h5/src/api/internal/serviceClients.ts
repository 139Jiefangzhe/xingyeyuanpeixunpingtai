import { HttpClient } from "./httpClient";

const EXAM_APP_URL =
  import.meta.env.VITE_EXAM_API_URL || "http://127.0.0.1:8081";
const TRAIN_APP_URL =
  import.meta.env.VITE_TRAIN_API_URL || "http://127.0.0.1:8082";
const POINT_APP_URL =
  import.meta.env.VITE_POINT_API_URL || "http://127.0.0.1:8086";

export const examClient = new HttpClient(EXAM_APP_URL);
export const trainClient = new HttpClient(TRAIN_APP_URL);
export const pointClient = new HttpClient(POINT_APP_URL);
