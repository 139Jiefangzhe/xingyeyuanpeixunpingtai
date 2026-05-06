DROP TABLE IF EXISTS edu_train_task;
DROP TABLE IF EXISTS edu_train_project;

CREATE TABLE edu_train_project (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  description VARCHAR(500),
  type TINYINT NOT NULL DEFAULT 1,
  status TINYINT NOT NULL DEFAULT 1,
  start_time TIMESTAMP,
  end_time TIMESTAMP,
  assignee_scope TINYINT NOT NULL DEFAULT 1,
  target_dept_ids VARCHAR(1000),
  create_by BIGINT NOT NULL DEFAULT 0,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT NOT NULL DEFAULT 0,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE edu_train_task (
  id VARCHAR(64) PRIMARY KEY,
  project_id VARCHAR(64) NOT NULL,
  name VARCHAR(200) NOT NULL,
  type TINYINT NOT NULL,
  ref_id VARCHAR(64) NOT NULL,
  sort INT NOT NULL DEFAULT 1,
  is_required TINYINT NOT NULL DEFAULT 1,
  pass_rule TINYINT NOT NULL DEFAULT 1,
  create_by BIGINT NOT NULL DEFAULT 0,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT NOT NULL DEFAULT 0,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_train_project_status ON edu_train_project(status);
CREATE INDEX idx_train_project_type ON edu_train_project(type);
CREATE INDEX idx_train_project_create_time ON edu_train_project(create_time);
CREATE INDEX idx_train_task_project_id ON edu_train_task(project_id);
CREATE INDEX idx_train_task_type ON edu_train_task(type);
CREATE INDEX idx_train_task_sort ON edu_train_task(sort);
