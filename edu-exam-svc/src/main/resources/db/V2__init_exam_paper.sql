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

CREATE TABLE `edu_exam_paper_question` (
  `id` VARCHAR(64) NOT NULL COMMENT '雪花ID',
  `paper_id` VARCHAR(64) NOT NULL COMMENT '试卷ID',
  `question_id` VARCHAR(64) NOT NULL COMMENT '题目ID',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `score` INT NOT NULL DEFAULT 0 COMMENT '题目分值',
  `create_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人',
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_by` BIGINT NOT NULL DEFAULT 0 COMMENT '更新人',
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_paper_question_paper_id` (`paper_id`),
  KEY `idx_paper_question_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷题目关联表';
