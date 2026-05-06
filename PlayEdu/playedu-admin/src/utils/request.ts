import axios, {
  AxiosError,
  AxiosInstance,
  AxiosRequestConfig,
  InternalAxiosRequestConfig,
} from "axios";
import { message } from "antd";
import type { ApiResult } from "../types/api";
import { clearToken, getToken, setToken } from "./index";

type RetryableConfig = InternalAxiosRequestConfig & {
  _retry?: boolean;
};

const defaultBaseURL =
  import.meta.env.VITE_API_BASE_URL ||
  import.meta.env.VITE_GATEWAY_URL ||
  import.meta.env.VITE_APP_URL ||
  "";

const localServiceBaseURLs = {
  exam: import.meta.env.VITE_EXAM_API_BASE_URL || "http://127.0.0.1:8081",
  train: import.meta.env.VITE_TRAIN_API_BASE_URL || "http://127.0.0.1:8082",
  course: import.meta.env.VITE_COURSE_API_BASE_URL || "http://127.0.0.1:8083",
  live: import.meta.env.VITE_LIVE_API_BASE_URL || "http://127.0.0.1:8084",
  user: import.meta.env.VITE_USER_API_BASE_URL || "http://127.0.0.1:8085",
};

const serviceRoutes: Array<{ prefix: string; baseURL: string }> = [
  { prefix: "/api/v1/train-projects", baseURL: localServiceBaseURLs.train },
  { prefix: "/api/v1/courses", baseURL: localServiceBaseURLs.course },
  { prefix: "/api/v1/live-rooms", baseURL: localServiceBaseURLs.live },
  { prefix: "/api/v1/exam-papers", baseURL: localServiceBaseURLs.exam },
  { prefix: "/api/v1/questions", baseURL: localServiceBaseURLs.exam },
  { prefix: "/api/v1/exam-records", baseURL: localServiceBaseURLs.exam },
  { prefix: "/api/v1/exam-sessions", baseURL: localServiceBaseURLs.exam },
  { prefix: "/api/v1/enums", baseURL: localServiceBaseURLs.exam },
  { prefix: "/api/v1/users", baseURL: localServiceBaseURLs.user },
  { prefix: "/api/v1/departments", baseURL: localServiceBaseURLs.user },
];

const isAbsoluteUrl = (url?: string) => Boolean(url && /^https?:\/\//i.test(url));

const resolveBaseURL = (url?: string) => {
  if (!url || isAbsoluteUrl(url)) {
    return undefined;
  }
  const matched = serviceRoutes.find((item) => url.startsWith(item.prefix));
  return matched?.baseURL || defaultBaseURL;
};

const requestClient: AxiosInstance = axios.create({
  baseURL: defaultBaseURL,
  timeout: 30000,
  headers: {
    Accept: "application/json",
    "Content-Type": "application/json",
  },
});

const getRefreshToken = () =>
  window.localStorage.getItem("playedu-backend-refresh-token") || "";

const setRefreshToken = (value: string) => {
  window.localStorage.setItem("playedu-backend-refresh-token", value);
};

const clearRefreshToken = () => {
  window.localStorage.removeItem("playedu-backend-refresh-token");
};

const goLogin = () => {
  clearToken();
  clearRefreshToken();
  window.location.href = "/login";
};

const createTraceId = () =>
  `trace-${Date.now()}-${Math.random().toString(16).slice(2, 10)}`;

let refreshingPromise: Promise<boolean> | null = null;

const tryRefreshToken = async (): Promise<boolean> => {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    return false;
  }

  if (!refreshingPromise) {
    const authBaseURL = resolveBaseURL("/api/v1/auth/refresh") || defaultBaseURL;
    refreshingPromise = axios
      .post<
        ApiResult<{ token: string; refreshToken?: string }>
      >(`${authBaseURL}/api/v1/auth/refresh`, {
        refreshToken,
      })
      .then((response) => {
        const payload = response.data;
        if (
          (payload.code === "0" || payload.code === 0) &&
          payload.data?.token
        ) {
          setToken(payload.data.token);
          if (payload.data.refreshToken) {
            setRefreshToken(payload.data.refreshToken);
          }
          return true;
        }
        return false;
      })
      .catch((error: AxiosError) => {
        if (error.response?.status === 404) {
          goLogin();
        }
        return false;
      })
      .finally(() => {
        refreshingPromise = null;
      });
  }

  return refreshingPromise;
};

requestClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const routedBaseURL = resolveBaseURL(config.url);
    if (routedBaseURL) {
      config.baseURL = routedBaseURL;
    }
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    config.headers["X-Trace-Id"] = createTraceId();
    return config;
  },
  (error) => Promise.reject(error)
);

requestClient.interceptors.response.use(
  (response) => {
    const payload = response.data as ApiResult<unknown> | undefined;
    if (!payload || typeof payload !== "object" || !("code" in payload)) {
      return response.data;
    }
    if (payload.code === 0 || payload.code === "0") {
      return payload;
    }
    if (payload.msg) {
      message.error(payload.msg);
    }
    return Promise.reject(
      new AxiosError(
        payload.msg || "业务请求失败",
        "ERR_BUSINESS",
        response.config,
        response.request,
        response
      )
    );
  },
  async (error: AxiosError<ApiResult<unknown>>) => {
    const responseStatus = error.response?.status;
    const config = error.config as RetryableConfig | undefined;

    if (
      responseStatus === 404 &&
      config?.url?.includes("/api/v1/auth/refresh")
    ) {
      goLogin();
      return Promise.reject(error);
    }

    if (responseStatus === 401 && config && !config._retry) {
      config._retry = true;
      const refreshed = await tryRefreshToken();
      if (refreshed) {
        const token = getToken();
        if (token) {
          config.headers = config.headers || {};
          config.headers.Authorization = `Bearer ${token}`;
        }
        return requestClient.request(config);
      }
      message.error("登录状态已失效，请重新登录");
      goLogin();
      return Promise.reject(error);
    }

    if (responseStatus === 403) {
      message.error("无操作权限");
    } else if (responseStatus === 429) {
      message.error("请求过于频繁，请稍后再试");
    } else if (error.response?.data?.msg) {
      message.error(error.response.data.msg);
    } else {
      message.error("网络异常，请稍后重试");
    }
    return Promise.reject(error);
  }
);

const request = {
  get<T>(url: string, config?: AxiosRequestConfig) {
    return requestClient.get<ApiResult<T>, ApiResult<T>>(url, config);
  },
  post<T, D = unknown>(url: string, data?: D, config?: AxiosRequestConfig<D>) {
    return requestClient.post<ApiResult<T>, ApiResult<T>, D>(
      url,
      data,
      config
    );
  },
  put<T, D = unknown>(url: string, data?: D, config?: AxiosRequestConfig<D>) {
    return requestClient.put<ApiResult<T>, ApiResult<T>, D>(
      url,
      data,
      config
    );
  },
  delete<T>(url: string, config?: AxiosRequestConfig) {
    return requestClient.delete<ApiResult<T>, ApiResult<T>>(url, config);
  },
};

export default request;
