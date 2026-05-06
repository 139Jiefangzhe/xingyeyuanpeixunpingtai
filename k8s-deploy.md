# Kubernetes 部署与运维规范

> 适用范围：所有微服务的容器化构建、K8s 部署、Helm 发布、运维排障

---

## 一、Docker 镜像构建规范

### 1.1 Dockerfile 模板（多阶段构建）

每个服务根目录必须包含 Dockerfile：

```dockerfile
# Stage 1: 构建
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
# 仅当 pom.xml 变化时才下载依赖
RUN mvn dependency:go-offline -B
RUN mvn clean package -DskipTests -B

# Stage 2: 运行
FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="playedu-team"
LABEL service="edu-exam-svc"

# 安装必要的工具（时区、字体）
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 创建非 root 用户运行
RUN addgroup -S playedu && adduser -S playedu -G playedu
WORKDIR /app

# 复制构建产物
COPY --from=builder /build/target/*.jar app.jar
RUN chown -R playedu:playedu /app
USER playedu

# JVM 参数（容器感知）
ENV JAVA_OPTS="-Xmx512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
ENV SPRING_PROFILES_ACTIVE="prod"

# 健康检查
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health/liveness || exit 1

EXPOSE 8080

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
```

### 1.2 镜像标签策略

| 场景 | 标签格式 | 示例 |
|------|---------|------|
| 开发构建 | `{service}:dev-{git-sha}` | `edu-exam-svc:dev-a1b2c3d` |
| 测试构建 | `{service}:test-{git-sha}` | `edu-exam-svc:test-a1b2c3d` |
| 生产构建 | `{service}:{git-sha}` | `edu-exam-svc:a1b2c3d` |
| 最新稳定 | `{service}:latest` | `edu-exam-svc:latest` |

> 生产环境禁止用 `latest`，必须用精确 SHA 标签，确保可回滚。

---

## 二、K8s 资源配置

### 2.1 Deployment 模板

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: edu-exam-svc
  namespace: edu-prod
  labels:
    app: edu-exam-svc
    version: "{{ .Values.image.tag }}"
spec:
  replicas: {{ .Values.replicaCount }}
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 25%
      maxUnavailable: 0
  selector:
    matchLabels:
      app: edu-exam-svc
  template:
    metadata:
      labels:
        app: edu-exam-svc
        version: "{{ .Values.image.tag }}"
      annotations:
        # 强制滚动更新（配置变更时）
        checksum/config: "{{ include (print $.Template.BasePath "/configmap.yaml") . | sha256sum }}"
    spec:
      serviceAccountName: edu-sa
      containers:
        - name: edu-exam-svc
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
          imagePullPolicy: IfNotPresent
          ports:
            - name: http
              containerPort: 8080
              protocol: TCP
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "{{ .Values.springProfile }}"
            - name: NACOS_SERVER_ADDR
              valueFrom:
                configMapKeyRef:
                  name: edu-common-config
                  key: nacos.server.addr
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: edu-db-secret
                  key: password
          resources:
            requests:
              memory: "1Gi"
              cpu: "500m"
            limits:
              memory: "2Gi"
              cpu: "2000m"
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: http
            initialDelaySeconds: 60
            periodSeconds: 30
            timeoutSeconds: 5
            failureThreshold: 3
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: http
            initialDelaySeconds: 30
            periodSeconds: 10
            timeoutSeconds: 3
            failureThreshold: 3
          volumeMounts:
            - name: logs
              mountPath: /app/logs
            - name: skywalking-agent
              mountPath: /skywalking
          env:
            - name: JAVA_TOOL_OPTIONS
              value: "-javaagent:/skywalking/skywalking-agent.jar"
      volumes:
        - name: logs
          emptyDir: {}
        - name: skywalking-agent
          emptyDir:
            medium: Memory
      initContainers:
        - name: init-skywalking
          image: apache/skywalking-java-agent:9.1.0-java17
          command: ['sh', '-c', 'cp -r /skywalking/agent/* /skywalking/']
          volumeMounts:
            - name: skywalking-agent
              mountPath: /skywalking
```

### 2.2 Service 模板

```yaml
apiVersion: v1
kind: Service
metadata:
  name: edu-exam-svc
  namespace: edu-prod
  labels:
    app: edu-exam-svc
spec:
  type: ClusterIP
  ports:
    - port: 8080
      targetPort: http
      protocol: TCP
      name: http
  selector:
    app: edu-exam-svc
```

### 2.3 HPA（水平自动伸缩）

核心服务配置 HPA：

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: edu-exam-svc
  namespace: edu-prod
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: edu-exam-svc
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
  behavior:
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
        - type: Percent
          value: 100
          periodSeconds: 60
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
        - type: Percent
          value: 10
          periodSeconds: 60
```

### 2.4 各服务资源规格参考

| 服务 | replicas | Request CPU | Request Mem | Limit CPU | Limit Mem | HPA |
|------|----------|-------------|-------------|-----------|-----------|-----|
| Gateway | 2 | 1000m | 2Gi | 2000m | 4Gi | 2-5 |
| user-svc | 2 | 500m | 1Gi | 2000m | 2Gi | 2-6 |
| course-svc | 2 | 500m | 1Gi | 2000m | 2Gi | 2-8 |
| exam-svc | 2 | 1000m | 2Gi | 2000m | 4Gi | 2-10（考试高峰）|
| live-svc | 2 | 500m | 1Gi | 2000m | 2Gi | 2-6 |
| train-svc | 2 | 500m | 1Gi | 2000m | 2Gi | 2-4 |
| community-svc | 2 | 500m | 1Gi | 2000m | 2Gi | 2-6 |
| point-svc | 2 | 500m | 1Gi | 2000m | 2Gi | 2-4 |
| talent-svc | 1 | 250m | 512Mi | 1000m | 1Gi | 无 |
| stats-svc | 1 | 500m | 1Gi | 2000m | 2Gi | 无 |
| file-svc | 2 | 500m | 1Gi | 2000m | 2Gi | 2-4 |
| msg-svc | 2 | 500m | 1Gi | 2000m | 2Gi | 2-4 |

---

## 三、Helm Chart 结构

```
edu-chart/
├── Chart.yaml
├── values.yaml                    # 默认配置
├── values-dev.yaml                # 开发环境覆盖
├── values-test.yaml               # 测试环境覆盖
├── values-prod.yaml               # 生产环境覆盖
├── templates/
│   ├── _helpers.tpl               # 模板辅助函数
│   ├── configmap.yaml             # 公共 ConfigMap
│   ├── secret.yaml                # 敏感信息 Secret
│   ├── gateway-deployment.yaml
│   ├── gateway-service.yaml
│   ├── gateway-ingress.yaml
│   ├── svc-deployment.yaml          # 通用服务 Deployment（循环渲染）
│   ├── svc-service.yaml
│   ├── svc-hpa.yaml
│   └── ingress.yaml
```

### 3.1 values.yaml 核心配置

```yaml
# values.yaml
namespace: edu-prod
springProfile: prod

services:
  gateway:
    enabled: true
    image: registry.internal/playedu/gateway
    tag: latest
    replicas: 2
    port: 8080
    resources:
      requests: { memory: "2Gi", cpu: "1000m" }
      limits: { memory: "4Gi", cpu: "2000m" }
    hpa: { enabled: true, min: 2, max: 5, cpuTarget: 70 }
    ingress: { enabled: true, host: api.playedu.internal }
  
  user-svc:
    enabled: true
    image: registry.internal/playedu/user-svc
    tag: latest
    replicas: 2
    port: 8080
    resources:
      requests: { memory: "1Gi", cpu: "500m" }
      limits: { memory: "2Gi", cpu: "2000m" }
    hpa: { enabled: true, min: 2, max: 6, cpuTarget: 70 }
  
  exam-svc:
    enabled: true
    image: registry.internal/playedu/exam-svc
    tag: latest
    replicas: 2
    port: 8080
    resources:
      requests: { memory: "2Gi", cpu: "1000m" }
      limits: { memory: "4Gi", cpu: "2000m" }
    hpa: { enabled: true, min: 2, max: 10, cpuTarget: 60, memTarget: 75 }

# 中间件配置（指向已部署的中间件）
infra:
  nacos:
    serverAddr: "nacos.edu-infra.svc.cluster.local:8848"
    namespace: "edu-prod"
  redis:
    cluster: "redis-cluster.edu-infra.svc.cluster.local:6379"
    password: "${REDIS_PASSWORD}"  # 从 Secret 注入
  mysql:
    host: "mysql-master.edu-infra.svc.cluster.local"
    port: 3306
    database: "edu_{svc}"
  rocketmq:
    nameserver: "rocketmq-namesrv.edu-infra.svc.cluster.local:9876"
  elasticsearch:
    hosts: "es-master.edu-infra.svc.cluster.local:9200"
  minio:
    endpoint: "minio.edu-infra.svc.cluster.local:9000"
    bucket: "playedu"
```

### 3.2 部署命令

```bash
# 开发环境
helm upgrade --install edu-dev ./edu-chart \
  -f ./edu-chart/values.yaml \
  -f ./edu-chart/values-dev.yaml \
  --namespace edu-dev --create-namespace

# 生产环境
helm upgrade --install edu-prod ./edu-chart \
  -f ./edu-chart/values.yaml \
  -f ./edu-chart/values-prod.yaml \
  --namespace edu-prod \
  --set image.tag=a1b2c3d  # 精确指定镜像版本
```

---

## 四、Ingress 配置

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: edu-ingress
  namespace: edu-prod
  annotations:
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/proxy-body-size: "500m"      # 允许大文件上传
    nginx.ingress.kubernetes.io/proxy-read-timeout: "600"      # 长连接（直播/WebSocket）
    nginx.ingress.kubernetes.io/proxy-send-timeout: "600"
    nginx.ingress.kubernetes.io/rate-limit: "1000"             # 限频
    cert-manager.io/cluster-issuer: "letsencrypt-prod"         # 自动 SSL
spec:
  ingressClassName: nginx
  tls:
    - hosts:
        - api.playedu.internal
        - admin.playedu.internal
        - pc.playedu.internal
      secretName: playedu-tls
  rules:
    - host: api.playedu.internal
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: gateway
                port:
                  number: 8080
    - host: admin.playedu.internal
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: edu-admin-static  # 前端静态资源服务
                port:
                  number: 80
```

---

## 五、配置管理策略

### 5.1 Nacos 配置分层

```
shared-config.yml          # 所有服务共享（中间件地址、公共常量）
├── user-svc.yml           # user-svc 私有
├── course-svc.yml         # course-svc 私有
├── exam-svc.yml           # exam-svc 私有
└── gateway.yml            # 网关路由/限流规则
```

### 5.2 敏感信息（K8s Secret）

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: edu-db-secret
  namespace: edu-prod
type: Opaque
stringData:
  username: "playedu"
  password: "YourStrongPassword123!"
---
apiVersion: v1
kind: Secret
metadata:
  name: edu-jwt-secret
  namespace: edu-prod
type: Opaque
stringData:
  secret-key: "your-256-bit-secret-key-here-must-be-long-enough"
```

### 5.3 配置热更新

- Nacos 配置变更 → 服务自动刷新（`@RefreshScope`）
- Sentinel 规则 → 通过 Sentinel 控制台实时调整，持久化到 Nacos
- 日志级别 → Spring Boot Actuator `loggers` 端点动态调整

---

## 六、日志与可观测性

### 6.1 日志输出

所有服务统一输出 JSON 格式到 stdout，由 Filebeat/Fluentd 采集：

```yaml
# application-logging.yml
logging:
  pattern:
    console: "{\"timestamp\":\"%d{yyyy-MM-dd HH:mm:ss.SSS}\",\"level\":\"%p\",\"traceId\":\"%X{traceId}\",\"service\":\"%X{serviceName}\",\"thread\":\"%t\",\"class\":\"%c{1}\",\"message\":\"%m\"}%n"
  level:
    root: INFO
    com.playedu: DEBUG
```

### 6.2 SkyWalking Agent 配置

```properties
# skywalking-agent/config/agent.config
agent.service_name=${SW_AGENT_NAME:edu-exam-svc}
agent.namespace=${SW_AGENT_NAMESPACE:edu-prod}
collector.backend_service=${SW_BACKEND_ADDR:skywalking-oap.edu-infra.svc.cluster.local:11800}
logging.output=CONSOLE
plugin.toolkit.log.transmit_formatted=false
```

### 6.3 监控大盘关键指标

| 指标 | PromQL | 告警阈值 |
|------|--------|---------|
| JVM 堆内存使用率 | `jvm_memory_used_bytes / jvm_memory_max_bytes` | > 80% |
| 接口 P99 延迟 | `histogram_quantile(0.99, http_server_requests_seconds_bucket)` | > 1s |
| 错误率 | `rate(http_server_requests_seconds_count{status=~"5.."}[5m])` | > 1% |
| Pod CPU 使用率 | `rate(container_cpu_usage_seconds_total[5m])` | > 80% |
| Pod 内存使用率 | `container_memory_working_set_bytes / container_spec_memory_limit_bytes` | > 85% |
| MQ 消费堆积 | `rocketmq_consumer_offset{group=~".*exam.*"}` 差值 | > 10000 |
| MySQL 慢查询 | `mysql_global_status_slow_queries` 增长率 | > 10/min |

---

## 七、运维操作手册

### 7.1 查看服务状态

```bash
# 查看所有服务 Pod
kubectl get pods -n edu-prod -l app.kubernetes.io/part-of=playedu

# 查看单个服务日志
kubectl logs -f edu-exam-svc-7d9f4b2c5-a1b2c -n edu-prod --tail=200

# 进入 Pod 排查
kubectl exec -it edu-exam-svc-7d9f4b2c5-a1b2c -n edu-prod -- /bin/sh

# 查看服务事件
kubectl get events -n edu-prod --sort-by='.lastTimestamp' | grep edu-exam
```

### 7.2 扩容操作

```bash
# 手动扩容（临时）
kubectl scale deployment edu-exam-svc --replicas=5 -n edu-prod

# 查看 HPA 状态
kubectl get hpa edu-exam-svc -n edu-prod

# 查看资源使用
kubectl top pods -n edu-prod
```

### 7.3 回滚操作

```bash
# 查看历史版本
kubectl rollout history deployment/edu-exam-svc -n edu-prod

# 回滚到上一个版本
kubectl rollout undo deployment/edu-exam-svc -n edu-prod

# 回滚到指定版本
kubectl rollout undo deployment/edu-exam-svc --to-revision=3 -n edu-prod
```

### 7.4 配置刷新

```bash
# 刷新 Nacos 配置（触发配置监听）
curl -X POST http://edu-exam-svc:8080/actuator/refresh

# 调整日志级别
curl -X POST http://edu-exam-svc:8080/actuator/loggers/com.playedu.exam \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'
```

---

## 八、CI/CD Pipeline 模板

```yaml
# .gitlab-ci.yml
stages:
  - build
  - test
  - package
  - deploy

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository"
  DOCKER_REGISTRY: "registry.internal/playedu"

build:
  stage: build
  image: maven:3.9-eclipse-temurin-17
  script:
    - mvn compile -B
  cache:
    paths:
      - .m2/repository/

test:
  stage: test
  image: maven:3.9-eclipse-temurin-17
  script:
    - mvn test -B
  artifacts:
    reports:
      junit: target/surefire-reports/*.xml
    paths:
      - target/site/jacoco/

sonar:
  stage: test
  image: sonarsource/sonar-scanner-cli
  script:
    - sonar-scanner
      -Dsonar.projectKey=playedu-${CI_PROJECT_NAME}
      -Dsonar.sources=src/main/java
      -Dsonar.tests=src/test/java
      -Dsonar.java.binaries=target/classes

package:
  stage: package
  image: docker:24
  services:
    - docker:24-dind
  script:
    - docker build -t $DOCKER_REGISTRY/${CI_PROJECT_NAME}:${CI_COMMIT_SHORT_SHA} .
    - docker push $DOCKER_REGISTRY/${CI_PROJECT_NAME}:${CI_COMMIT_SHORT_SHA}
    - docker tag $DOCKER_REGISTRY/${CI_PROJECT_NAME}:${CI_COMMIT_SHORT_SHA} $DOCKER_REGISTRY/${CI_PROJECT_NAME}:latest
    - docker push $DOCKER_REGISTRY/${CI_PROJECT_NAME}:latest

deploy-dev:
  stage: deploy
  image: bitnami/kubectl:latest
  script:
    - helm upgrade --install edu-dev ./edu-chart
        -f ./edu-chart/values.yaml
        -f ./edu-chart/values-dev.yaml
        --namespace edu-dev
        --set services.${CI_PROJECT_NAME}.tag=${CI_COMMIT_SHORT_SHA}
  environment:
    name: development
    url: https://api-dev.playedu.internal
  only:
    - develop

deploy-prod:
  stage: deploy
  image: bitnami/kubectl:latest
  script:
    - helm upgrade --install edu-prod ./edu-chart
        -f ./edu-chart/values.yaml
        -f ./edu-chart/values-prod.yaml
        --namespace edu-prod
        --set services.${CI_PROJECT_NAME}.tag=${CI_COMMIT_SHORT_SHA}
  environment:
    name: production
    url: https://api.playedu.internal
  when: manual
  only:
    - main
```

---

## 九、故障排查速查表

| 现象 | 排查步骤 | 解决方式 |
|------|---------|---------|
| Pod 频繁重启 | `kubectl describe pod` → Events → 查看 OOMKilled/Error | 增加内存 Limit；检查内存泄漏 |
| 服务注册不上 Nacos | 检查 Pod 网络 → `curl nacos:8848` → 查看命名空间配置 | 确认 Nacos 地址和 namespace ID 正确 |
| MQ 消费堆积 | RocketMQ Console 查看消费 TPS → 消费者日志 | 增加消费者线程数/实例数；检查消费逻辑是否阻塞 |
| 接口响应慢 | SkyWalking 链路 → 定位慢 Span → 数据库/Redis/Feign | SQL 加索引；加缓存；Feign 降级 |
| 数据库连接池耗尽 | 查看 Druid 监控 → 活跃连接数 | 检查慢 SQL；降低连接池 maxSize；增加连接池 |
| Redis 内存告警 | `redis-cli info memory` → 查 bigkey | bigkey 拆分；调整过期策略；扩容 |
| 前端 502 | Ingress 日志 → 后端服务 Pod 状态 | Pod 是否 Ready； readinessProbe 是否通过 |
| 文件上传失败 | Ingress proxy-body-size → file-svc 日志 → MinIO 状态 | 调大 Ingress body-size；检查 MinIO 签名 |
