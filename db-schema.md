# 数据库设计规范

> 适用范围：所有微服务的 MySQL 数据库表设计、索引设计、分表策略

---

## 一、命名规范

### 1.1 表名
- 格式：`edu_{svc}_{biz}`
- 全小写，下划线分隔
- 禁止使用复数形式（统一用单数名词）

示例：
- `edu_exam_paper` — 试卷表
- `edu_exam_question` — 题目表
- `edu_exam_question_option` — 题目选项表
- `edu_course_chapter` — 课程章节表
- `edu_learning_record` — 学习记录表
- `edu_point_account` — 积分账户表

### 1.2 字段名
- 全小写，下划线分隔
- 禁止使用 MySQL 保留字（如 `order`、`status`、`key` 等，即使可用也避免）
- 布尔字段用 `is_` 前缀：`is_deleted`、`is_published`、`is_required`
- 时间字段统一后缀：`create_time`、`update_time`、`start_time`、`end_time`
- 人员字段后缀：`create_by`、`update_by`、`assign_to`
- 外键字段格式：`{关联表}_{主键}`，如 `paper_id`、`course_id`

### 1.3 索引名
- 普通索引：`idx_{table}_{fields}`
- 唯一索引：`uk_{table}_{fields}`
- 示例：`idx_exam_paper_creator_id`、`uk_exam_question_bank_code`

---

## 二、通用字段（所有表必须有）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT / VARCHAR(64) | 主键。简单场景用自增 BIGINT，分布式或需要对外暴露时用雪花 ID VARCHAR |
| `create_time` | DATETIME(3) | 创建时间，默认 CURRENT_TIMESTAMP(3) |
| `update_time` | DATETIME(3) | 更新时间，默认 CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) |
| `create_by` | BIGINT | 创建人用户ID |
| `update_by` | BIGINT | 更新人用户ID |
| `is_deleted` | TINYINT(1) | 逻辑删除：0-未删除 1-已删除，默认 0。MyBatis-Plus 全局配置 |

> 注意：逻辑删除字段统一命名为 `is_deleted`，与 MyBatis-Plus 的 `@TableLogic` 默认配置对齐。

---

## 三、字段类型规范

| 数据类型 | 适用场景 | 说明 |
|---------|---------|------|
| `BIGINT` | 主键、用户ID、部门ID | 自增或雪花ID |
| `VARCHAR(n)` | 名称、标题、编码 | n 根据业务定，标题 200，描述 500，URL 1000 |
| `TEXT` | 长文本、富文本内容 | 如题目题干、帖子正文 |
| `LONGTEXT` | 超大文本 | JSON 配置、日志内容 |
| `TINYINT(1)` | 布尔值、状态枚举 | 0/1 布尔；0-5 小范围状态 |
| `INT` | 数量、时长、分数 | 如考试时长（分钟）、题目分数 |
| `DECIMAL(10,2)` | 金额 | 积分金额、商品价格 |
| `DATETIME(3)` | 时间戳 | 带毫秒精度，统一 +8 时区存储 |
| `DATE` | 仅日期 | 生日、入职日期 |
| `JSON` | 灵活结构 | 扩展配置、答案选项（MySQL 5.7+） |
| `ENUM` | 极少变化的状态 | 谨慎使用，推荐用 TINYINT + 代码枚举 |

---

## 四、主键策略

### 4.1 自增 ID
- 适用于：单表、无分库分表、不需要对外暴露 ID 的场景
- 优点：简单、有序、存储小
- 缺点：分库分表后冲突、易被爬虫遍历

### 4.2 雪花 ID（推荐）
- 适用于：所有需要对外暴露 ID 的表、分库分表场景
- 实现：基于 MyBatis-Plus `IdType.ASSIGN_ID`（封装雪花算法）
- 格式：19 位 Long，或转为 String 避免前端精度丢失
- 注意：前端接收 Long 类型必须转为 String，JS Number 精度只有 53 位

### 4.3 业务编码
- 适用于：需要人工可读、带业务前缀的编码
- 示例：`EP202401150001`（试卷编码 = EP + 日期 + 4位序号）
- 实现：独立编码表或 Redis 自增，应用层组装

---

## 五、索引设计规范

### 5.1 必须建索引的场景
1. 主键（聚簇索引，默认）
2. 外键/关联字段（如 `paper_id`、`user_id`）
3. 查询条件字段（如 `status`、`type`、`create_time`）
4. 排序字段（如 `sort_order`、`create_time`）
5. 组合查询的最左前缀字段

### 5.2 禁止建索引的场景
1. 区分度极低的字段（如性别 `gender`，仅 0/1）
2. 大字段（TEXT/BLOB/LONGTEXT）
3. 频繁更新的字段（索引维护成本高）
4. 从未用于查询的字段

### 5.3 联合索引设计

遵循最左前缀原则，把区分度高的、查询频率高的放左边：

```sql
-- 查询场景：WHERE user_id = ? AND course_id = ? AND create_time BETWEEN ? AND ?
CREATE INDEX idx_learning_record_user_course_time 
ON edu_learning_record(user_id, course_id, create_time);
```

### 5.4 索引数量控制
- 单表索引不超过 5 个
- 单表字段数超过 20 个时，审慎评估表拆分

### 5.5 执行计划确认
所有涉及查询的新表/新索引，上线前必须用 EXPLAIN 确认执行计划：

```sql
EXPLAIN SELECT * FROM edu_exam_paper 
WHERE creator_id = 10001 AND status = 1 
ORDER BY create_time DESC LIMIT 20;
```

确认项：
- `type` 至少达到 `range`，理想是 `ref` 或 `eq_ref`
- `key` 使用了期望的索引
- `Extra` 不出现 `Using filesort`、`Using temporary`

---

## 六、大表分表策略

### 6.1 需要分表的表

| 表名 | 分表策略 | 理由 |
|------|---------|------|
| `edu_learning_record` | 按 `user_id` 取模 或 按月分区 | 学习记录量大，每人多条 |
| `edu_exam_record` | 按 `exam_id` 取模 或 按年分表 | 考试记录增长快 |
| `edu_point_record` | 按季度分表 | 积分流水增长快 |
| `edu_community_post` | 按月归档 + 冷热分离 | 帖子数据量大，老数据访问少 |
| `edu_msg_record` | 按月分表 | 消息记录量大 |

### 6.2 分表实现方式

**方案A：MyBatis-Plus 动态表名（简单场景）**

```java
// 在 Mapper 中动态拼接表名
@Select("SELECT * FROM edu_learning_record_${tableSuffix} WHERE user_id = #{userId}")
List<LearningRecord> selectByUserId(@Param("tableSuffix") String suffix, @Param("userId") Long userId);
```

**方案B：ShardingSphere（推荐）**

配置分片规则，对业务代码透明：
```yaml
spring:
  shardingsphere:
    rules:
      sharding:
        tables:
          edu_learning_record:
            actual-data-nodes: ds0.edu_learning_record_$->{0..15}
            table-strategy:
              standard:
                sharding-column: user_id
                sharding-algorithm-name: record-inline
```

### 6.3 分表后的查询原则
1. 带分片键的查询：直接路由到目标表
2. 不带分片键的查询：广播到所有分片，禁止高频使用
3. 分页查询：必须在分片键范围内分页，禁止全表聚合分页

---

## 七、关联关系设计

### 7.1 一对一
- 主表 + 扩展表，扩展表主键即外键
- 例：`edu_user` + `edu_user_profile`

### 7.2 一对多
- 外键放在多的一方
- 例：`edu_exam_paper`（1）↔ `edu_exam_paper_question`（N），`paper_id` 在关联表中

### 7.3 多对多
- 必须拆中间关联表
- 例：试卷与考生多对多 → `edu_exam_paper_assignee`（`paper_id` + `user_id` + `status`）
- 关联表必须有自己的主键，禁止用联合主键

### 7.4 外键约束
- **禁止数据库级外键约束**（FOREIGN KEY），用逻辑外键 + 应用层校验
- 理由：性能损耗、分库分表后无法使用、级联删除风险

---

## 八、JSON 字段使用规范

MySQL JSON 类型适用于结构灵活、查询频率低的扩展字段：

```sql
-- 试卷的扩展配置（不同题型规则不同）
config JSON DEFAULT NULL
```

```java
// Java 中用 String 存 JSON，或用 Map/Object 配合 Jackson
@TableField(typeHandler = JacksonTypeHandler.class)
private ExamConfig config;
```

注意：
- 不要在 JSON 字段上建索引（MySQL 8.0 支持虚拟列索引，但性能一般）
- 不要存储超大 JSON（> 1MB），应放对象存储
- 不要将 JSON 作为查询条件的主要字段

---

## 九、审计与版本控制

### 9.1 数据库变更管理
- 所有 DDL 必须通过 Flyway / Liquibase 管理
- 脚本命名：`V{version}__{description}.sql`
- 禁止直接在生产环境执行 DDL
- 加字段必须允许 NULL 或有默认值，避免锁表

### 9.2 数据备份
- 每日全量备份（mysqldump / xtrabackup）
- Binlog 保留 7 天以上，用于时间点恢复
- 敏感操作（删除课程/试卷）做软删除，禁止物理删除

---

## 十、实体类与表映射示例

```java
@Data
@TableName("edu_exam_paper")
public class ExamPaper {
    
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    private String title;
    private String description;
    private Integer totalScore;
    private Integer duration;          // 分钟
    private Integer passScore;         // 及格线
    private Integer status;            // 1-草稿 2-已发布 3-已归档
    private Integer type;              // 1-普通考试 2-随机抽题
    private Integer allowRedo;         // 0-不可重考 1-可重考
    private String knowledgeConfig;    // JSON：知识点权重配置
    
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDeleted;
}
```

对应的建表语句：

```sql
CREATE TABLE `edu_exam_paper` (
  `id` VARCHAR(64) NOT NULL COMMENT '雪花ID',
  `title` VARCHAR(200) NOT NULL COMMENT '试卷标题',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '试卷描述',
  `total_score` INT NOT NULL DEFAULT 100 COMMENT '总分',
  `duration` INT NOT NULL DEFAULT 60 COMMENT '考试时长(分钟)',
  `pass_score` INT NOT NULL DEFAULT 60 COMMENT '及格分数线',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-草稿 2-已发布 3-已归档',
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型：1-普通考试 2-随机抽题',
  `allow_redo` TINYINT NOT NULL DEFAULT 0 COMMENT '是否允许重考：0-否 1-是',
  `knowledge_config` JSON DEFAULT NULL COMMENT '知识点权重配置',
  `create_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人',
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_by` BIGINT NOT NULL DEFAULT 0 COMMENT '更新人',
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_exam_paper_status` (`status`),
  KEY `idx_exam_paper_creator_id` (`create_by`),
  KEY `idx_exam_paper_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';
```

---

## 十一、常见业务表设计模板

### 题库题目表（含多题型扩展）

```sql
CREATE TABLE `edu_exam_question` (
  `id` VARCHAR(64) NOT NULL,
  `bank_id` VARCHAR(64) NOT NULL COMMENT '所属题库',
  `type` TINYINT NOT NULL COMMENT '题型：1-单选 2-多选 3-判断 4-填空 5-问答 6-组合',
  `content` TEXT NOT NULL COMMENT '题干',
  `options` JSON DEFAULT NULL COMMENT '选项（单选/多选用）',
  `answer` TEXT NOT NULL COMMENT '正确答案',
  `analysis` TEXT DEFAULT NULL COMMENT '解析',
  `difficulty` TINYINT NOT NULL DEFAULT 3 COMMENT '难度：1-5',
  `knowledge_point` VARCHAR(200) DEFAULT NULL COMMENT '知识点标签',
  `score` INT NOT NULL DEFAULT 5 COMMENT '默认分值',
  `create_by` BIGINT NOT NULL DEFAULT 0,
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_by` BIGINT NOT NULL DEFAULT 0,
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_question_bank_id` (`bank_id`),
  KEY `idx_question_type` (`type`),
  KEY `idx_question_difficulty` (`difficulty`),
  KEY `idx_question_knowledge` (`knowledge_point`),
  KEY `idx_question_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库题目表';
```

### 考试记录表（大表，需分表）

```sql
CREATE TABLE `edu_exam_record` (
  `id` VARCHAR(64) NOT NULL,
  `exam_id` VARCHAR(64) NOT NULL COMMENT '考试ID',
  `paper_id` VARCHAR(64) NOT NULL COMMENT '试卷ID',
  `user_id` BIGINT NOT NULL COMMENT '考生ID',
  `dept_id` BIGINT NOT NULL COMMENT '部门ID',
  `total_score` INT NOT NULL DEFAULT 0 COMMENT '试卷总分',
  `obtain_score` INT NOT NULL DEFAULT 0 COMMENT '获得分数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-进行中 2-已提交 3-已评分',
  `start_time` DATETIME(3) NOT NULL COMMENT '开始时间',
  `submit_time` DATETIME(3) DEFAULT NULL COMMENT '提交时间',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT '考试IP',
  `switch_count` INT NOT NULL DEFAULT 0 COMMENT '切屏次数',
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_record_exam_id` (`exam_id`),
  KEY `idx_record_user_id` (`user_id`),
  KEY `idx_record_dept_id` (`dept_id`),
  KEY `idx_record_status` (`status`),
  KEY `idx_record_submit_time` (`submit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录表';
```

### 学习记录表（大表，按 user_id 分片）

```sql
CREATE TABLE `edu_learning_record` (
  `id` VARCHAR(64) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `course_id` VARCHAR(64) NOT NULL,
  `chapter_id` VARCHAR(64) DEFAULT NULL,
  `lesson_id` VARCHAR(64) DEFAULT NULL,
  `progress` INT NOT NULL DEFAULT 0 COMMENT '进度百分比',
  `learned_duration` INT NOT NULL DEFAULT 0 COMMENT '已学习时长(秒)',
  `total_duration` INT NOT NULL DEFAULT 0 COMMENT '总时长(秒)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-未开始 2-学习中 3-已完成',
  `last_position` INT NOT NULL DEFAULT 0 COMMENT '视频上次观看位置(秒)',
  `complete_time` DATETIME(3) DEFAULT NULL,
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_learn_user_course_lesson` (`user_id`, `course_id`, `lesson_id`),
  KEY `idx_learn_user_id` (`user_id`),
  KEY `idx_learn_course_id` (`course_id`),
  KEY `idx_learn_status` (`status`),
  KEY `idx_learn_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习记录表';
```

### 积分账户表

```sql
CREATE TABLE `edu_point_account` (
  `id` VARCHAR(64) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `total_points` INT NOT NULL DEFAULT 0 COMMENT '总积分',
  `available_points` INT NOT NULL DEFAULT 0 COMMENT '可用积分',
  `frozen_points` INT NOT NULL DEFAULT 0 COMMENT '冻结积分',
  `total_earned` INT NOT NULL DEFAULT 0 COMMENT '累计获取',
  `total_consumed` INT NOT NULL DEFAULT 0 COMMENT '累计消耗',
  `version` INT NOT NULL DEFAULT 1 COMMENT '乐观锁版本号',
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_point_user` (`user_id`),
  KEY `idx_point_available` (`available_points`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分账户表';
```

### 积分流水表（大表，按季度分表）

```sql
CREATE TABLE `edu_point_record` (
  `id` VARCHAR(64) NOT NULL,
  `account_id` VARCHAR(64) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `type` TINYINT NOT NULL COMMENT '类型：1-收入 2-支出',
  `action` VARCHAR(50) NOT NULL COMMENT '动作：course_complete / exam_pass / exchange / signin',
  `points` INT NOT NULL COMMENT '变动积分数',
  `balance` INT NOT NULL COMMENT '变动后余额',
  `source_id` VARCHAR(64) DEFAULT NULL COMMENT '来源业务ID',
  `source_type` VARCHAR(50) DEFAULT NULL COMMENT '来源业务类型',
  `remark` VARCHAR(500) DEFAULT NULL,
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_record_account_id` (`account_id`),
  KEY `idx_record_user_id` (`user_id`),
  KEY `idx_record_action` (`action`),
  KEY `idx_record_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水表';
```
