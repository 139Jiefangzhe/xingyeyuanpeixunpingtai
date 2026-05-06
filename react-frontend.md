# React 前端开发规范

> 适用范围：edu-pc（学员端）、edu-admin（管理后台）、edu-lecturer（讲师工作台）

---

## 一、技术栈与版本约束

| 技术 | 版本 | 用途 |
|------|------|------|
| React | 18.x | UI 框架 |
| TypeScript | 5.x | 类型安全 |
| Vite | 5.x | 构建工具 |
| Ant Design | 5.x | 组件库 |
| Zustand | 4.x | 客户端全局状态 |
| React Query (TanStack Query) | 5.x | 服务端状态管理（缓存/刷新/重试） |
| Axios | 1.x | HTTP 请求 |
| React Router | 6.x | 路由 |
| Zod | 3.x | 表单校验 Schema |
| ECharts | 5.x | 图表（管理后台） |
| React DnD | 16.x | 拖拽（题目排序/课程编排） |
| DOMPurify | 3.x | XSS 过滤 |

---

## 二、项目目录结构

```
edu-{app}/                    # edu-pc / edu-admin / edu-lecturer
├── src/
│   ├── api/                   # 后端接口层（按服务分文件）
│   │   ├── userApi.ts
│   │   ├── courseApi.ts
│   │   ├── examApi.ts
│   │   ├── liveApi.ts
│   │   ├── trainApi.ts
│   │   ├── communityApi.ts
│   │   ├── pointApi.ts
│   │   └── fileApi.ts
│   │
│   ├── components/            # 业务公共组件
│   │   ├── StandardTable/     # 标准表格（分页/筛选/刷新/批量操作）
│   │   ├── UploadVideo/       # 视频上传（分片/断点续传/转码进度）
│   │   ├── RichTextEditor/    # 富文本编辑器（题目题干/帖子正文）
│   │   ├── UserSelector/      # 人员选择器（弹窗/部门树/多选）
│   │   ├── DeptTree/          # 部门树选择
│   │   ├── CourseSelector/    # 课程选择器
│   │   ├── ExamPaperPicker/   # 试卷选择器
│   │   ├── PermissionWrapper/ # 权限包裹组件（控制按钮/菜单显隐）
│   │   └── DataEmpty/         # 空状态统一组件
│   │
│   ├── hooks/                 # 自定义 Hooks
│   │   ├── useTable.ts        # 标准列表页 Hook（分页/筛选/刷新）
│   │   ├── useAuth.ts         # 权限检查 Hook
│   │   ├── useUpload.ts       # 上传逻辑 Hook
│   │   ├── useExamTimer.ts    # 考试倒计时 Hook
│   │   └── useLivePlayer.ts   # 直播播放器 Hook
│   │
│   ├── pages/                 # 页面组件（按模块划分）
│   │   ├── login/
│   │   ├── dashboard/
│   │   ├── course/
│   │   ├── exam/
│   │   ├── live/
│   │   ├── train/
│   │   ├── community/
│   │   ├── point/
│   │   ├── talent/
│   │   ├── stats/
│   │   └── settings/
│   │
│   ├── stores/                # Zustand 全局状态
│   │   ├── userStore.ts       # 用户信息/登录态
│   │   ├── appStore.ts        # 全局配置（主题/侧边栏/未读消息）
│   │   └── permissionStore.ts # 权限菜单/按钮权限
│   │
│   ├── types/                 # TypeScript 类型定义
│   │   ├── api.ts             # 后端接口通用类型（Result/PageResult）
│   │   ├── user.ts
│   │   ├── course.ts
│   │   └── exam.ts
│   │
│   ├── utils/                 # 工具函数
│   │   ├── request.ts         # Axios 封装实例
│   │   ├── format.ts          # 日期/金额/时长格式化
│   │   ├── validate.ts        # 通用校验函数
│   │   ├── mask.ts            # 脱敏工具
│   │   └── enumMap.ts         # 枚举映射（前后端枚举值统一）
│   │
│   ├── App.tsx
│   ├── main.tsx
│   └── router.tsx             # 路由定义
│
├── public/
├── index.html
├── vite.config.ts
├── tsconfig.json
└── package.json
```

---

## 三、代码命名规范

### 3.1 文件命名
- 组件文件：**大写驼峰**，如 `ExamPaperList.tsx`
- 非组件文件（hooks/utils/api）：**小写驼峰**，如 `useExamData.ts`
- 类型文件：与被描述对象同名，如 `exam.ts` 定义考试相关类型
- 样式文件：与组件同名，如 `ExamPaperList.module.less`

### 3.2 变量与函数命名
- 组件：`PascalCase`，如 `ExamPaperList`
- Hooks：`camelCase` + `use` 前缀，如 `useExamData`
- 普通函数：`camelCase`，如 `handleSubmit`
- 常量：`UPPER_SNAKE_CASE`，如 `MAX_UPLOAD_SIZE`
- 布尔变量：`is`/`has`/`can` 前缀，如 `isLoading`、`hasPermission`
- 事件处理：`handle` + 动作，如 `handleClick`、`handleFormSubmit`

### 3.3 TypeScript 类型命名
- 接口/类型：`PascalCase`，如 `ExamPaperRespDTO`
- 枚举：对象枚举（非 TS enum），如 `const ExamStatus = { DRAFT: 1, PUBLISHED: 2 } as const`
- 泛型参数：`T` / `K` / `V`，复杂场景用描述性名称如 `TData`

---

## 四、API 层规范

### 4.1 Axios 实例封装

```typescript
// utils/request.ts
import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';
import { useUserStore } from '@/stores/userStore';
import { message } from 'antd';

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
});

// 请求拦截器：注入 Token
request.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = useUserStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  // 注入 Trace-ID
  config.headers['X-Trace-Id'] = generateTraceId();
  return config;
});

// 响应拦截器：统一错误处理 + Token 刷新
request.interceptors.response.use(
  (response) => response.data,
  async (error: AxiosError<Result<any>>) => {
    const status = error.response?.status;
    const code = error.response?.data?.code;
    
    if (status === 401) {
      // Token 过期，尝试刷新
      const refreshed = await refreshToken();
      if (!refreshed) {
        useUserStore.getState().logout();
        window.location.href = '/login';
      }
    } else if (status === 403) {
      message.error('无操作权限');
    } else if (status === 429) {
      message.error('请求过于频繁，请稍后再试');
    } else if (code && code !== 0) {
      message.error(error.response?.data?.msg || '业务处理失败');
    } else {
      message.error('网络异常，请稍后重试');
    }
    return Promise.reject(error);
  }
);
```

### 4.2 API 文件组织

每个服务一个 API 文件，函数命名与后端接口语义一致：

```typescript
// api/examApi.ts
import request from '@/utils/request';
import type { ExamPaperRespDTO, ExamPaperCreateReqDTO, Result, PageResult } from '@/types';

export const examApi = {
  // 分页查询试卷列表
  listPapers: (params: ExamPaperQueryDTO) =>
    request.get<Result<PageResult<ExamPaperRespDTO>>>('/exam-svc/api/v1/exam-papers', { params }),
  
  // 获取试卷详情
  getPaper: (id: string) =>
    request.get<Result<ExamPaperRespDTO>>(`/exam-svc/api/v1/exam-papers/${id}`),
  
  // 创建试卷
  createPaper: (data: ExamPaperCreateReqDTO) =>
    request.post<Result<ExamPaperRespDTO>>('/exam-svc/api/v1/exam-papers', data),
  
  // 发布试卷
  publishPaper: (id: string) =>
    request.post<Result<void>>(`/exam-svc/api/v1/exam-papers/${id}/publish`),
  
  // 删除试卷
  deletePaper: (id: string) =>
    request.delete<Result<void>>(`/exam-svc/api/v1/exam-papers/${id}`),
};
```

### 4.3 React Query 使用规范

```typescript
// 列表页查询
const { data, isLoading, refetch } = useQuery({
  queryKey: ['examPapers', queryParams],
  queryFn: () => examApi.listPapers(queryParams),
  staleTime: 30 * 1000,        // 30秒内不重新请求
  retry: 1,                    // 失败只重试1次
});

// 提交后自动刷新列表
const mutation = useMutation({
  mutationFn: examApi.createPaper,
  onSuccess: () => {
    message.success('创建成功');
    queryClient.invalidateQueries({ queryKey: ['examPapers'] });
  },
});
```

---

## 五、页面开发规范

### 5.1 列表页标准模式

所有管理后台列表页必须使用 `useTable` Hook 封装，保持交互一致：

```typescript
// hooks/useTable.ts
export function useTable<T, Q extends Record<string, any>>(
  fetchFn: (params: Q) => Promise<Result<PageResult<T>>>,
  defaultQuery: Q
) {
  const [query, setQuery] = useState<Q>(defaultQuery);
  const [selectedRows, setSelectedRows] = useState<T[]>([]);
  
  const { data, isLoading } = useQuery({
    queryKey: ['table', fetchFn.name, query],
    queryFn: () => fetchFn(query),
  });
  
  const pagination = {
    current: query.pageNum,
    pageSize: query.pageSize,
    total: data?.data?.total || 0,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  };
  
  return { data, isLoading, query, setQuery, pagination, selectedRows, setSelectedRows };
}
```

### 5.2 表单页标准模式

```typescript
// 使用 Ant Design Form + Zod 校验
import { Form, Input, Button, message } from 'antd';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';

const schema = z.object({
  title: z.string().min(1, '标题不能为空').max(200, '标题不超过200字符'),
  duration: z.number().min(1, '至少1分钟').max(300, '不超过300分钟'),
  passScore: z.number().min(0).max(100, '不超过100分'),
});

type FormData = z.infer<typeof schema>;

export function ExamPaperForm() {
  const { control, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });
  
  const onSubmit = async (data: FormData) => {
    await examApi.createPaper(data);
    message.success('保存成功');
  };
  
  return (
    <Form onFinish={handleSubmit(onSubmit)}>
      {/* 表单字段 */}
    </Form>
  );
}
```

### 5.3 详情/抽屉页

- 新增/编辑共用同一组件，通过 `id` 是否存在判断模式
- 详情用 Drawer 或 Modal，宽度 800px 起步
- 表单提交中禁用按钮并显示 Loading

---

## 六、状态管理规范

### 6.1 Zustand Store 模板

```typescript
// stores/userStore.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface UserState {
  token: string | null;
  userInfo: UserRespDTO | null;
  permissions: string[];
  setToken: (token: string) => void;
  setUserInfo: (info: UserRespDTO) => void;
  logout: () => void;
}

export const useUserStore = create<UserState>()(
  persist(
    (set) => ({
      token: null,
      userInfo: null,
      permissions: [],
      setToken: (token) => set({ token }),
      setUserInfo: (info) => set({ userInfo: info, permissions: info.permissions }),
      logout: () => set({ token: null, userInfo: null, permissions: [] }),
    }),
    { name: 'user-storage' }  // localStorage 持久化
  )
);
```

### 6.2 权限控制

**按钮级权限**：
```typescript
// components/PermissionWrapper.tsx
export function PermissionWrapper({ 
  code, 
  children, 
  fallback = null 
}: { 
  code: string; 
  children: React.ReactNode; 
  fallback?: React.ReactNode;
}) {
  const permissions = useUserStore((s) => s.permissions);
  return permissions.includes(code) ? children : fallback;
}

// 使用
<PermissionWrapper code="exam:paper:create">
  <Button type="primary">创建试卷</Button>
</PermissionWrapper>
```

**路由级权限**：在路由配置中定义 `permission` 字段，路由守卫校验。

---

## 七、路由规范

```typescript
// router.tsx
import { createBrowserRouter, Navigate } from 'react-router-dom';

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/',
    element: <Layout />,
    children: [
      { index: true, element: <Navigate to="/dashboard" replace /> },
      { path: 'dashboard', element: <DashboardPage />, permission: 'dashboard:view' },
      { path: 'course/list', element: <CourseListPage />, permission: 'course:list' },
      { path: 'course/:id', element: <CourseDetailPage /> },
      { path: 'exam/papers', element: <ExamPaperListPage />, permission: 'exam:paper:list' },
      { path: 'exam/papers/create', element: <ExamPaperFormPage />, permission: 'exam:paper:create' },
      { path: 'exam/exams', element: <ExamListPage />, permission: 'exam:exam:list' },
      { path: 'exam/exams/:id', element: <ExamSessionPage /> },
      { path: 'live/rooms', element: <LiveRoomListPage />, permission: 'live:room:list' },
      { path: 'live/rooms/:id', element: <LiveRoomPage /> },
      { path: 'train/projects', element: <TrainProjectListPage />, permission: 'train:project:list' },
      { path: 'community/posts', element: <PostListPage /> },
      { path: 'community/posts/:id', element: <PostDetailPage /> },
      { path: 'point/mall', element: <PointMallPage /> },
      { path: 'talent/map', element: <TalentMapPage />, permission: 'talent:map:view' },
      { path: 'stats/dashboard', element: <StatsDashboardPage />, permission: 'stats:dashboard' },
    ],
  },
  { path: '*', element: <NotFoundPage /> },
]);
```

---

## 八、样式规范

### 8.1 技术选择
- 全局样式：`src/styles/global.less`
- 组件级样式：CSS Modules，`Component.module.less`
- 主题定制：Ant Design ConfigProvider + `theme` 配置
- 禁止使用 CSS-in-JS（如 styled-components），保持与 Ant Design 风格一致

### 8.2 变量定义

```less
// styles/variables.less
@primary-color: #1890ff;
@success-color: #52c41a;
@warning-color: #faad14;
@error-color: #f5222d;
@text-primary: rgba(0, 0, 0, 0.85);
@text-secondary: rgba(0, 0, 0, 0.45);
@border-radius-base: 4px;
@spacing-base: 8px;
```

### 8.3 响应式断点

| 设备 | 宽度 | 布局调整 |
|------|------|---------|
| 桌面端 | >= 1280px | 标准侧边栏 + 内容区 |
| 笔记本 | >= 1024px | 紧凑侧边栏 |
| 平板 | >= 768px | 折叠侧边栏 |
| 手机 | < 768px | 底部导航 + 全宽内容 |

---

## 九、表单与交互规范

### 9.1 表单字段对齐
- 标签右对齐，宽度 120px
- 输入框宽度根据内容定：短文本 200px，长文本 400px，描述 600px
- 必填项红色星号标注
- 提交按钮文案：创建/保存/发布，禁用笼统的"确定"

### 9.2 操作反馈
- 成功操作：message.success，自动消失
- 失败操作：message.error，显示后端返回的 msg
- 加载状态：表格 skeleton，按钮 loading，页面 spinning
- 删除操作：Modal.confirm 二次确认，显示删除对象名称

### 9.3 考试场景特殊交互
- 考试页面：全屏模式，禁止右键/F12（前端层面，配合后端防作弊）
- 倒计时：顶部固定显示，最后 5 分钟变红闪烁
- 题目导航：左侧题号面板，已答/未答/标记状态
- 自动保存：每 30 秒自动保存答案到 localStorage，防刷新丢失

### 9.4 直播场景特殊交互
- 播放器：HLS.js 或 TCPlayer，多线路切换
- 弹幕：底部输入框，发送后右侧弹幕列表显示
- 签到：弹出签到二维码或按钮，倒计时 30 秒
- 问答：学员提问 → 讲师端显示 → 讲师回答 → 全员可见

---

## 十、性能优化规范

1. **路由懒加载**：`React.lazy(() => import('@/pages/exam/ExamList'))`
2. **组件懒加载**：Modal/Drawer 内容组件懒加载
3. **图片优化**：课程封面用 WebP，带懒加载 `loading="lazy"`
4. **虚拟滚动**：列表超过 100 条用 Ant Design 的 `VirtualTable`
5. **请求防抖**：搜索输入 300ms debounce
6. **避免重复渲染**：列表项用 `React.memo`，复杂对象用 `useMemo`
7. **状态拆分**：Zustand Store 按模块拆分，避免无关状态变更引起重渲染

---

## 十一、枚举值管理

前后端枚举值必须保持一致，前端从后端 `/api/v1/enums` 动态拉取映射表，禁止硬编码：

```typescript
// utils/enumMap.ts
let enumMap: Record<string, Record<string, string>> = {};

export async function loadEnums() {
  const res = await userApi.getEnums();
  enumMap = res.data || {};
}

export function getEnumLabel(type: string, value: number | string): string {
  return enumMap[type]?.[String(value)] || String(value);
}

// 使用
getEnumLabel('ExamStatus', 1); // "草稿"
getEnumLabel('QuestionType', 2); // "多选题"
```

---

## 十二、类型定义规范

```typescript
// types/api.ts — 通用响应类型
export interface Result<T> {
  code: number;
  msg: string;
  data: T;
  version: string;
  timestamp: number;
}

export interface PageResult<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
  pages: number;
}

// types/exam.ts — 业务类型
export interface ExamPaperRespDTO {
  id: string;
  title: string;
  totalScore: number;
  duration: number;
  passScore: number;
  status: number;
  statusLabel: string;
  createTime: string;
  creatorName: string;
}

export interface ExamPaperCreateReqDTO {
  title: string;
  description?: string;
  totalScore: number;
  duration: number;
  passScore: number;
  questions: PaperQuestionReqDTO[];
}
```

---

## 十三、代码审查清单（前端）

- [ ] 所有 API 调用使用封装后的 request，禁止裸写 axios
- [ ] 表单有 Zod/Yup 校验，提交前做最终校验
- [ ] 列表页使用 useTable Hook，包含分页/刷新/加载状态
- [ ] 权限控制使用 PermissionWrapper，不直接判断权限字符串
- [ ] 敏感信息（手机号/邮箱）使用 mask 工具脱敏显示
- [ ] 路由新增后更新菜单配置和权限枚举
- [ ] 类型定义与后端 DTO 字段名保持一致
- [ ] 图片/视频使用懒加载，大列表用虚拟滚动
- [ ] 考试/直播等长页面有异常恢复机制（localStorage 缓存）
- [ ] 无 console.log 残留，无 debugger 残留
