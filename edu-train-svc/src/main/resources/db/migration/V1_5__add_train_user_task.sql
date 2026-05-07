CREATE TABLE IF NOT EXISTS edu_train_user_task (
    id              VARCHAR(64) PRIMARY KEY,
    project_id      VARCHAR(64) NOT NULL COMMENT '培训项目ID',
    task_id         VARCHAR(64) NOT NULL COMMENT '任务ID',
    user_id         BIGINT NOT NULL COMMENT '学员用户ID',
    status          VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED' COMMENT 'NOT_STARTED/IN_PROGRESS/COMPLETED/OVERDUE',
    completed_at    DATETIME NULL COMMENT '完成时间',
    create_by       BIGINT NOT NULL DEFAULT 0 COMMENT '创建人',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by       BIGINT NOT NULL DEFAULT 0 COMMENT '更新人',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_project_task_user (project_id, task_id, user_id),
    INDEX idx_user_project (user_id, project_id),
    INDEX idx_project_user_status (project_id, user_id, status)
) COMMENT='学员培训任务完成记录';
