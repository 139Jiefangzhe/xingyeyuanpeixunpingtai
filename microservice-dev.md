# 微服务开发专项规范

> 适用范围：Spring Cloud Alibaba 微服务架构下的服务注册、配置管理、服务调用、熔断限流、分布式事务

---

## 一、服务注册与发现

### 1.1 Nacos 服务注册配置

```yaml
# bootstrap.yml（必须放在 bootstrap，优先于 application 加载）
spring:
  application:
    name: edu-exam-svc
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:nacos.edu-infra.svc.cluster.local:8848}
        namespace: ${NACOS_NAMESPACE:edu-prod}
        group: DEFAULT_GROUP
        metadata:
          version: "1.0.0"
          region: "beijing"
      config:
        server-addr: ${NACOS_SERVER_ADDR:nacos.edu-infra.svc.cluster.local:8848}
        namespace: ${NACOS_NAMESPACE:edu-prod}
        group: DEFAULT_GROUP
        file-extension: yml
        shared-configs:
          - data-id: shared-config.yml
            group: DEFAULT_GROUP
            refresh: true
        extension-configs:
          - data-id: edu-exam-svc.yml
            group: DEFAULT_GROUP
            refresh: true
```

### 1.2 服务命名规范

- `spring.application.name` 必须使用短横线连接的小写
- 名称全局唯一，不可重复：`edu-user-svc`、`edu-course-svc`
- Gateway 路由名与服务名保持一致

---

## 二、统一配置管理

### 2.1 配置分层

| 层级 | Data ID | 说明 | 刷新 |
|------|---------|------|------|
| 共享配置 | `shared-config.yml` | 所有服务共用：Redis、MySQL、RocketMQ、日志级别 | 是 |
| 服务配置 | `{service-name}.yml` | 单个服务私有：业务参数、线程池大小 | 是 |
| 扩展配置 | `{service-name}-dev.yml` | 环境特定：开发环境降级某些校验 | 否 |

### 2.2 动态刷新使用

需要热更新的 Bean 加 `@RefreshScope`：

```java
@Component
@RefreshScope
public class ExamConfig {
    @Value("${exam.default-duration:60}")
    private Integer defaultDuration;
    
    @Value("${exam.max-redo-count:3}")
    private Integer maxRedoCount;
}
```

> 注意：`@RefreshScope` 会创建代理，避免滥用；无状态工具类不加。

---

## 三、OpenFeign 服务间调用

### 3.1 Feign 客户端定义

```java
@FeignClient(
    name = "edu-user-svc",
    contextId = "userFeignClient",  // 防止不同包中同名 Bean 冲突
    fallbackFactory = UserFeignClientFallbackFactory.class,
    configuration = FeignConfig.class
)
public interface UserFeignClient {
    
    @GetMapping("/edu-user-svc/api/v1/users/{userId}")
    Result<UserInternalDTO> getUserById(@PathVariable("userId") Long userId);
    
    @PostMapping("/edu-user-svc/api/v1/users/batch")
    Result<List<UserInternalDTO>> batchGetUsers(@RequestBody List<Long> userIds);
    
    @GetMapping("/edu-user-svc/api/v1/users/departments/{deptId}/members")
    Result<PageResult<UserInternalDTO>> getDeptMembers(
        @PathVariable("deptId") Long deptId,
        @SpringQueryMap UserQueryDTO query
    );
}
```

### 3.2 全局 Feign 配置

```java
@Configuration
public class FeignConfig {
    
    // 传递 Trace-ID 和 User-ID
    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return requestTemplate -> {
            String traceId = MDC.get("traceId");
            if (StrUtil.isNotBlank(traceId)) {
                requestTemplate.header("X-Trace-Id", traceId);
            }
            String userId = RequestContextHolder.getUserId();
            if (StrUtil.isNotBlank(userId)) {
                requestTemplate.header("X-User-Id", userId);
            }
        };
    }
    
    // 超时配置
    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(10, TimeUnit.SECONDS, 30, TimeUnit.SECONDS, true);
    }
    
    // 重试策略（Sentinel 降级后不要重试，避免雪崩）
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, 1000, 2);  // 初始间隔100ms，最大间隔1s，最多2次
    }
}
```

### 3.3 Fallback 降级工厂

```java
@Slf4j
@Component
public class UserFeignClientFallbackFactory implements FallbackFactory<UserFeignClient> {
    
    @Override
    public UserFeignClient create(Throwable cause) {
        log.error("edu-user-svc 调用失败, reason: {}", cause.getMessage());
        
        return new UserFeignClient() {
            @Override
            public Result<UserInternalDTO> getUserById(Long userId) {
                return Result.fail("用户服务暂不可用，请稍后重试");
            }
            
            @Override
            public Result<List<UserInternalDTO>> batchGetUsers(List<Long> userIds) {
                return Result.success(Collections.emptyList());
            }
            
            @Override
            public Result<PageResult<UserInternalDTO>> getDeptMembers(Long deptId, UserQueryDTO query) {
                return Result.success(PageResult.empty());
            }
        };
    }
}
```

### 3.4 批量调用原则

```java
@Service
public class ExamResultService {
    
    @Autowired
    private UserFeignClient userFeignClient;
    
    // ❌ 错误：循环调用 Feign
    public void notifyUsersWrong(List<Long> userIds) {
        for (Long userId : userIds) {
            Result<UserInternalDTO> result = userFeignClient.getUserById(userId);
            // ... 发送通知
        }
    }
    
    // ✅ 正确：批量查询
    public void notifyUsersRight(List<Long> userIds) {
        Result<List<UserInternalDTO>> result = userFeignClient.batchGetUsers(userIds);
        if (result.isSuccess()) {
            List<UserInternalDTO> users = result.getData();
            // ... 批量发送通知
        }
    }
}
```

---

## 四、Sentinel 限流熔断

### 4.1 网关层流控规则

```yaml
# gateway 配置
spring:
  cloud:
    gateway:
      routes:
        - id: exam-svc
          uri: lb://edu-exam-svc
          predicates:
            - Path=/exam-svc/**
          filters:
            - name: Sentinel
              args:
                resource: exam_route
                fallbackUri: forward:/fallback/sentinel
```

### 4.2 代码定义流控规则（推荐，可版本控制）

```java
@Component
public class SentinelConfig {
    
    @PostConstruct
    public void initRules() {
        List<FlowRule> rules = new ArrayList<>();
        
        // 考试提交接口：QPS 限流 200
        FlowRule submitRule = new FlowRule("examSubmit");
        submitRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        submitRule.setCount(200);
        submitRule.setLimitApp("default");
        submitRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        rules.add(submitRule);
        
        // 智能组卷接口：热点参数限流（按 paperId）
        ParamFlowRule paramRule = new ParamFlowRule("generatePaper");
        paramRule.setParamIdx(0);  // 第1个参数
        paramRule.setCount(50);    // 单 paperId 50 QPS
        paramRule.setDurationInSec(60);
        
        // 熔断规则：慢调用比例
        DegradeRule degradeRule = new DegradeRule("examQuery");
        degradeRule.setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO);
        degradeRule.setCount(0.5);     // 慢调用比例 > 50%
        degradeRule.setTimeWindow(30);   // 熔断 30s
        degradeRule.setSlowRatioThreshold(500);  // 慢调用阈值 500ms
        degradeRule.setMinRequestAmount(10);
        rules.add(degradeRule);
        
        FlowRuleManager.loadRules(rules);
    }
}
```

### 4.3 @SentinelResource 注解

```java
@Service
public class ExamPaperService {
    
    @SentinelResource(
        value = "generatePaper",
        blockHandler = "generatePaperBlock",
        fallback = "generatePaperFallback"
    )
    public ExamPaper generatePaper(PaperGenerateReqDTO req) {
        // 组卷逻辑...
    }
    
    // 限流时的处理方法
    public ExamPaper generatePaperBlock(PaperGenerateReqDTO req, BlockException ex) {
        log.warn("组卷被限流: {}", req);
        throw new BizException(ErrorCode.REQUEST_LIMITED, "系统繁忙，请稍后重试");
    }
    
    // 异常降级时的处理方法
    public ExamPaper generatePaperFallback(PaperGenerateReqDTO req, Throwable ex) {
        log.error("组卷异常降级: {}", req, ex);
        throw new BizException(ErrorCode.SYSTEM_ERROR, "组卷服务暂不可用");
    }
}
```

---

## 五、分布式事务策略

### 5.1 原则

- **能不用就不用**：微服务优先通过业务设计避免分布式事务
- **最终一致性优先**：使用 RocketMQ 异步消息保证最终一致
- **强一致性最后手段**：Seata AT 模式（性能损耗大，仅在金额/库存类场景使用）

### 5.2 事件驱动最终一致（推荐）

```java
@Service
public class ExamSubmitService {
    
    @Autowired
    private ExamRecordMapper examRecordMapper;
    
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    
    @Transactional(rollbackFor = Exception.class)
    public void submitExam(ExamSubmitReqDTO req) {
        // 1. 本地事务：保存考试记录、答案
        ExamRecord record = saveRecord(req);
        
        // 2. 发送 MQ 事件（本地事务提交后执行）
        ExamCompletedEvent event = new ExamCompletedEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setExamId(record.getExamId());
        event.setUserId(record.getUserId());
        event.setScore(record.getObtainScore());
        event.setPass(record.getObtainScore() >= record.getPassScore());
        event.setTimestamp(System.currentTimeMillis());
        
        // Spring 事务同步器：事务成功提交后才发消息
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    rocketMQTemplate.asyncSend("exam-result-topic", event, new SendCallback() {
                        @Override
                        public void onSuccess(SendResult sendResult) {
                            log.info("考试完成事件发送成功: {}", event.getEventId());
                        }
                        @Override
                        public void onException(Throwable e) {
                            log.error("考试完成事件发送失败: {}", event.getEventId(), e);
                            // 入失败消息表，定时重试
                        }
                    });
                }
            }
        );
    }
}
```

### 5.3 MQ 消费者幂等

```java
@Service
@RocketMQMessageListener(
    topic = "exam-result-topic",
    consumerGroup = "point-consumer-group"
)
public class PointConsumer implements RocketMQListener<ExamCompletedEvent> {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private PointRecordService pointRecordService;
    
    @Override
    public void onMessage(ExamCompletedEvent event) {
        String idempotencyKey = "mq:idempotency:" + event.getEventId();
        
        // 幂等校验：SETNX 原子操作
        Boolean isNew = redisTemplate.opsForValue()
            .setIfAbsent(idempotencyKey, "1", Duration.ofHours(24));
        
        if (Boolean.FALSE.equals(isNew)) {
            log.warn("重复消费消息: {}", event.getEventId());
            return;
        }
        
        try {
            // 发放积分
            pointRecordService.awardExamPoints(event);
        } catch (Exception e) {
            log.error("积分发放失败: {}", event.getEventId(), e);
            // 删除幂等键，允许重试（或入死信队列）
            redisTemplate.delete(idempotencyKey);
            throw e;  // 抛异常触发 MQ 重试
        }
    }
}
```

### 5.4 Seata AT 模式（备用）

仅在强一致性场景使用（如积分兑换实物商品时的库存扣减 + 积分扣减）：

```java
@GlobalTransactional(name = "point-exchange-tx", rollbackFor = Exception.class)
public void exchangeGoods(PointExchangeReqDTO req) {
    // 1. point-svc: 扣减积分
    pointService.deductPoints(req.getUserId(), req.getPoints());
    
    // 2. order-svc: 创建兑换订单（Feign 调用）
    orderService.createExchangeOrder(req);
    
    // 3. inventory-svc: 扣减库存（Feign 调用）
    inventoryService.deductStock(req.getGoodsId(), 1);
}
```

> 注意：Seata 需部署 TC 服务，且对性能有 10%-30% 损耗，非必要不启用。

---

## 六、链路追踪与日志

### 6.1 Trace-ID 传递

```java
// Gateway Filter：入口生成 Trace-ID
@Component
public class TraceIdGatewayFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst("X-Trace-Id");
        if (StrUtil.isBlank(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        final String finalTraceId = traceId;
        
        MDC.put("traceId", finalTraceId);
        
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
            .header("X-Trace-Id", finalTraceId)
            .build();
        
        return chain.filter(exchange.mutate().request(mutatedRequest).build())
            .doFinally(signalType -> MDC.clear());
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}
```

```java
// Feign 拦截器：向下游传递
public class FeignTraceInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        String traceId = MDC.get("traceId");
        if (StrUtil.isNotBlank(traceId)) {
            template.header("X-Trace-Id", traceId);
        }
    }
}
```

### 6.2 日志格式统一

```xml
<!-- logback-spring.xml -->
<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <includeMdcKeyName>traceId</includeMdcKeyName>
        <includeMdcKeyName>userId</includeMdcKeyName>
        <includeMdcKeyName>serviceName</includeMdcKeyName>
    </encoder>
</appender>
```

---

## 七、公共 Starter 封装

### 7.1 edu-common-starter 结构

```
edu-common-starter/
├── pom.xml
└── src/main/java/com/playedu/common/
    ├── config/
    │   ├── MybatisPlusConfig.java          # MyBatis-Plus 分页插件、逻辑删除、自动填充
    │   ├── RedisConfig.java                # RedisTemplate 序列化配置
    │   ├── RedissonConfig.java             # Redisson 分布式锁
    │   ├── RocketMQConfig.java             # MQ 生产者配置
    │   ├── Knife4jConfig.java              # Swagger 文档配置
    │   └── WebConfig.java                  # 全局 Cors、Interceptor
    ├── entity/
    │   └── BaseEntity.java                 # 通用字段基类（id/createTime/updateTime/createBy/updateBy/isDeleted）
    ├── result/
    │   ├── Result.java                     # 统一响应体
    │   └── PageResult.java                 # 分页响应体
    ├── exception/
    │   ├── BizException.java               # 业务异常
    │   └── GlobalExceptionHandler.java     # 全局异常处理器
    ├── enums/
    │   └── ErrorCode.java                  # 系统级错误码
    ├── util/
    │   ├── JsonUtil.java                   # Jackson 工具
    │   ├── JwtUtil.java                    # JWT 生成/解析
    │   ├── MaskUtil.java                   # 脱敏工具
    │   └── IpUtil.java                     # IP 获取工具
    └── annotation/
        ├── MaskField.java                  # 脱敏注解
        └── EnumValid.java                  # 枚举校验注解
```

### 7.2 自动填充配置

```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        Long userId = getCurrentUserId();
        this.strictInsertFill(metaObject, "createBy", Long.class, userId);
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateBy", Long.class, userId);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "isDeleted", Integer.class, 0);
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        Long userId = getCurrentUserId();
        this.strictUpdateFill(metaObject, "updateBy", Long.class, userId);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
    
    private Long getCurrentUserId() {
        // 从 RequestContextHolder 或 SecurityContext 获取
        return RequestContextHolder.getUserIdOrDefault(0L);
    }
}
```

---

## 八、定时任务与异步处理

### 8.1 XXL-Job 分布式任务

```java
@JobHandler(value = "examAutoSubmitJob")
@Component
public class ExamAutoSubmitJob extends IJobHandler {
    
    @Autowired
    private ExamSessionService examSessionService;
    
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        // 自动提交已超时未交的考试
        int count = examSessionService.autoSubmitTimeoutExams();
        XxlJobHelper.log("自动提交超时考试 {} 条", count);
        return ReturnT.SUCCESS;
    }
}
```

任务配置：每 5 分钟执行一次，路由策略：分片广播（按考试 ID 取模分片执行）。

### 8.2 Spring @Async 异步线程池

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("edu-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

---

## 九、服务启动自检清单

每个服务启动时必须完成以下自检，否则启动失败：

```java
@Component
public class StartupHealthChecker implements ApplicationRunner {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 1. 检查数据库连接
        try (Connection conn = dataSource.getConnection()) {
            log.info("数据库连接正常: {}", conn.getMetaData().getURL());
        }
        
        // 2. 检查 Redis
        redisTemplate.opsForValue().set("health:check", "ok", Duration.ofSeconds(10));
        
        // 3. 检查 Nacos 注册
        // Spring Cloud 自动处理，可通过 /actuator/health 验证
        
        log.info("服务启动自检通过: {}", SpringUtil.getApplicationName());
    }
}
```

---

## 十、微服务开发自检清单

开发完一个微服务后，逐项检查：

- [ ] `bootstrap.yml` 正确配置了 Nacos 注册和配置中心
- [ ] `spring.application.name` 与服务代码目录名一致
- [ ] 所有接口有 Swagger `@Operation` 注解
- [ ] Feign 客户端有 `fallbackFactory` 降级配置
- [ ] Feign 调用有 `X-Trace-Id` 和 `X-User-Id` 传递
- [ ] 对外 API 返回 `Result<T>` 统一包装体
- [ ] 数据库操作使用 MyBatis-Plus，无手写 JDBC
- [ ] 所有表有 `id/create_time/update_time/create_by/update_by/is_deleted` 字段
- [ ] 缓存 Key 遵循 `{svc}:{biz}:{id}` 规范
- [ ] MQ 生产者发送消息后记录 eventId 日志
- [ ] MQ 消费者有幂等校验（eventId + Redis SETNX）
- [ ] 敏感配置（密码/密钥）走 Nacos 加密或 K8s Secret
- [ ] Dockerfile 多阶段构建，镜像 < 200MB
- [ ] Liveness/Readiness Probe 配置正确
- [ ] 健康检查接口 `/actuator/health` 可正常访问
- [ ] 单元测试覆盖率 > 60%（核心 Service 层）
