# API 设计与接口契约规范

> 适用范围：所有后端微服务的 RESTful API 设计

---

## 一、URL 设计规范

### 1.1 基础格式

```
/{svc-prefix}/api/{version}/{resource}/{sub-resource}
```

示例：
- `POST /exam-svc/api/v1/questions` — 创建题目
- `GET /exam-svc/api/v1/exam-papers/{paperId}/questions` — 获取试卷题目列表
- `POST /exam-svc/api/v1/exam-sessions/{sessionId}/submit` — 提交考试
- `GET /user-svc/api/v1/users/{userId}` — 获取用户详情（跨服务调用）

### 1.2 HTTP Method 语义

| Method | 用途 | 幂等性 |
|--------|------|--------|
| GET | 查询资源或列表 | 是 |
| POST | 创建资源 / 执行操作 | 否 |
| PUT | 全量更新资源 | 是 |
| PATCH | 部分更新资源 | 否（视实现）|
| DELETE | 删除资源 | 是 |

### 1.3 特殊操作命名

当标准 Method 无法表达业务动作时，URL 末尾加动作标识：
- `POST /exam-svc/api/v1/exam-papers/{id}/publish` — 发布试卷
- `POST /exam-svc/api/v1/exam-papers/{id}/copy` — 复制试卷
- `POST /exam-svc/api/v1/exam-sessions/{id}/submit` — 提交考试
- `POST /train-svc/api/v1/train-projects/{id}/assign` — 派发培训任务
- `GET /course-svc/api/v1/courses/{id}/learning-progress` — 学习进度

禁止在 URL 中使用动词作为资源名，如 `/getExamPaper`、`/createQuestion`。

---

## 二、请求规范

### 2.1 请求头（必须）

| Header | 说明 | 示例 |
|--------|------|------|
| Authorization | Bearer Token | `Bearer eyJhbGciOiJIUzI1NiJ9...` |
| X-Trace-Id | 链路追踪 ID | `a1b2c3d4e5f6` |
| X-User-Id | 当前用户 ID（Gateway 注入） | `10001` |
| X-Dept-Id | 当前部门 ID（Gateway 注入） | `20001` |
| Content-Type | 请求体格式 | `application/json` |
| Accept | 响应格式 | `application/json` |

### 2.2 查询参数（GET 请求）

分页参数统一命名：
- `pageNum` — 页码，从 1 开始，默认 1
- `pageSize` — 每页大小，默认 10，最大 100
- `sortField` — 排序字段，如 `createTime`
- `sortOrder` — 排序方向，`asc` 或 `desc`

筛选参数命名：
- 精确匹配：`{field}`，如 `courseId=abc123`
- 模糊匹配：`{field}Like`，如 `titleLike=Java`
- 范围查询：`{field}Begin`、`{field}End`，如 `createTimeBegin=2024-01-01`
- 多值匹配：`{field}List`，如 `statusList=1,2,3`

示例：
```
GET /exam-svc/api/v1/exam-papers?pageNum=1&pageSize=20&titleLike=期末&statusList=1,2&sortField=createTime&sortOrder=desc
```

### 2.3 请求体（POST/PUT/PATCH）

- 必须是 JSON 格式
- 日期时间统一用 ISO-8601 字符串：`2024-01-15T14:30:00+08:00`
- 金额用整数（分），禁止用浮点数
- 枚举用整数或字符串（String 更利于可读性，Integer 更省空间），必须在 Swagger 标注 `@Schema(description="状态：1-草稿 2-已发布")`

---

## 三、响应规范

### 3.1 成功响应

**单对象**：
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": "ep-001",
    "title": "Java基础考试",
    "totalScore": 100,
    "duration": 60
  },
  "version": "v1",
  "timestamp": 1705312800000
}
```

**列表（分页）**：
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "list": [
      {"id": "ep-001", "title": "Java基础"},
      {"id": "ep-002", "title": "Spring进阶"}
    ],
    "total": 156,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 16
  },
  "version": "v1",
  "timestamp": 1705312800000
}
```

**无数据**：
```json
{
  "code": 0,
  "msg": "success",
  "data": null,
  "version": "v1",
  "timestamp": 1705312800000
}
```

### 3.2 错误响应

```json
{
  "code": 600001,
  "msg": "考试已结束，不可提交",
  "data": null,
  "version": "v1",
  "timestamp": 1705312800000
}
```

### 3.3 HTTP 状态码映射

| HTTP Code | 含义 |
|-----------|------|
| 200 | 业务成功（含 code=0 和 code!=0） |
| 400 | HTTP 参数格式错误（框架层校验失败） |
| 401 | Token 缺失或无效 |
| 403 | 无接口权限 |
| 404 | 接口不存在 |
| 429 | 触发限流 |
| 500 | 系统异常（未捕获异常） |

> 注意：业务逻辑错误（如考试已结束）返回 HTTP 200，通过 `code` 字段表达。

---

## 四、DTO 设计规范

### 4.1 请求 DTO 命名

- `XxxReqDTO` — 创建/更新请求
- `XxxQueryDTO` — 分页查询请求（继承或包含 PageParam）
- `XxxUpdateReqDTO` — 部分更新请求（字段全部可选）
- `XxxBatchReqDTO` — 批量操作请求

### 4.2 响应 DTO 命名

- `XxxRespDTO` — 单对象响应
- `XxxListRespDTO` — 列表响应（内含 List<XxxRespDTO>）
- `XxxPageRespDTO` — 分页响应（继承 PageResult）
- `XxxSimpleRespDTO` — 精简响应（列表项，含少量字段）
- `XxxDetailRespDTO` — 详情响应（完整字段）

### 4.3 内部 DTO（Feign 调用）

- `XxxInternalDTO` — 仅服务间调用，字段按需精简
- `XxxInternalQueryDTO` — 批量查询参数
- 内部 DTO 放在 `dto/internal/` 包下

### 4.4 校验注解

使用 Jakarta Validation：
```java
public class ExamPaperCreateReqDTO {
    @NotBlank(message = "试卷标题不能为空")
    @Size(max = 200, message = "标题长度不超过200字符")
    private String title;
    
    @NotNull(message = "考试时长不能为空")
    @Min(value = 1, message = "考试时长至少1分钟")
    @Max(value = 300, message = "考试时长不超过300分钟")
    private Integer duration;
    
    @NotEmpty(message = "题目列表不能为空")
    private List<PaperQuestionReqDTO> questions;
}
```

---

## 五、Swagger / Knife4j 注解规范

每个 Controller 方法必须包含：

```java
@Operation(summary = "创建试卷", description = "根据题目列表创建新试卷")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "成功"),
    @ApiResponse(responseCode = "400", description = "参数校验失败"),
    @ApiResponse(responseCode = "403", description = "无权限")
})
@PostMapping("/exam-papers")
public Result<ExamPaperRespDTO> createPaper(
    @Valid @RequestBody ExamPaperCreateReqDTO reqDTO
) { ... }
```

Controller 类级注解：
```java
@Tag(name = "试卷管理", description = "考试中心-试卷相关接口")
@RestController
@RequestMapping("/api/v1")
public class ExamPaperController { ... }
```

---

## 六、批量与导入导出接口

### 6.1 批量操作

批量接口统一用 `POST`，请求体传 ID 列表：
```java
@PostMapping("/exam-papers/batch-delete")
public Result<Void> batchDelete(@RequestBody BatchDeleteReqDTO dto) {
    // dto.getIds() -> List<String>
}
```

### 6.2 导入

```java
@PostMapping(value = "/questions/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public Result<ImportResultRespDTO> importQuestions(
    @RequestPart("file") MultipartFile file,
    @RequestParam(defaultValue = "xlsx") String fileType
) { ... }
```

返回导入结果：成功数、失败数、失败明细列表。

### 6.3 导出

```java
@GetMapping("/exam-records/export")
public void exportRecords(
    ExamRecordQueryDTO query,
    HttpServletResponse response
) { ... }
```

- 小数据量（<1万）：同步导出，直接写 response
- 大数据量：异步导出，提交任务 → 生成文件 → file-svc 存储 → MQ 通知下载链接

---

## 七、Feign 接口规范

### 7.1 接口定义

```java
@FeignClient(
    name = "user-svc",
    fallbackFactory = UserFeignClientFallbackFactory.class,
    configuration = FeignConfig.class
)
public interface UserFeignClient {
    
    @GetMapping("/user-svc/api/v1/users/{userId}")
    Result<UserInternalDTO> getUserById(@PathVariable("userId") Long userId);
    
    @PostMapping("/user-svc/api/v1/users/batch")
    Result<List<UserInternalDTO>> batchGetUsers(@RequestBody List<Long> userIds);
}
```

### 7.2 Fallback 降级

```java
@Component
@Slf4j
public class UserFeignClientFallbackFactory implements FallbackFactory<UserFeignClient> {
    @Override
    public UserFeignClient create(Throwable cause) {
        log.error("user-svc 调用失败", cause);
        return new UserFeignClient() {
            @Override
            public Result<UserInternalDTO> getUserById(Long userId) {
                return Result.fail("用户服务暂不可用");
            }
            @Override
            public Result<List<UserInternalDTO>> batchGetUsers(List<Long> userIds) {
                return Result.success(Collections.emptyList());
            }
        };
    }
}
```

### 7.3 批量优先原则

禁止在循环中调用 Feign：

```java
// ❌ 错误
for (Long userId : userIds) {
    userFeignClient.getUserById(userId);  // N 次网络往返
}

// ✅ 正确
userFeignClient.batchGetUsers(userIds);  // 1 次网络往返
```

---

## 八、接口版本管理

- URL 版本：`/api/v1/`、`/api/v2/`
- 跨版本兼容期：旧版本保留至少 2 个迭代周期
- 版本切换通过 Gateway 路由配置，或 Controller 同时挂载多个 `@RequestMapping`
- 重大不兼容变更才升版本，日常迭代保持 v1
