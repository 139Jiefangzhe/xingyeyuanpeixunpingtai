INSERT INTO edu_train_project (
  id, title, description, type, status, start_time, end_time, assignee_scope, target_dept_ids,
  create_by, create_time, update_by, update_time, is_deleted
) VALUES (
  'train-local-001',
  'Java 全栈新员工训练营',
  '本地联调用培训项目，包含课程与考试任务',
  1,
  2,
  TIMESTAMP '2026-05-08 09:00:00',
  TIMESTAMP '2026-05-31 18:00:00',
  1,
  NULL,
  1,
  TIMESTAMP '2026-05-05 09:00:00',
  1,
  TIMESTAMP '2026-05-05 09:00:00',
  0
);

INSERT INTO edu_train_task (
  id, project_id, name, type, ref_id, sort, is_required, pass_rule,
  create_by, create_time, update_by, update_time, is_deleted
) VALUES
  (
    'train-task-local-001',
    'train-local-001',
    'Java 基础线上课',
    1,
    '101',
    1,
    1,
    1,
    1,
    TIMESTAMP '2026-05-05 09:10:00',
    1,
    TIMESTAMP '2026-05-05 09:10:00',
    0
  ),
  (
    'train-task-local-002',
    'train-local-001',
    'Java 基础测试',
    2,
    'paper-local-001',
    2,
    1,
    2,
    1,
    TIMESTAMP '2026-05-05 09:15:00',
    1,
    TIMESTAMP '2026-05-05 09:15:00',
    0
  );
