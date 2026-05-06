DROP TABLE IF EXISTS edu_exam_session;
DROP TABLE IF EXISTS edu_exam_record;
DROP TABLE IF EXISTS edu_exam_paper_question;
DROP TABLE IF EXISTS edu_exam_paper;
DROP TABLE IF EXISTS edu_exam_question;

CREATE TABLE edu_exam_question (
  id VARCHAR(64) PRIMARY KEY,
  bank_id VARCHAR(64) NOT NULL,
  type TINYINT NOT NULL,
  content CLOB NOT NULL,
  options VARCHAR(4000),
  answer VARCHAR(1000) NOT NULL,
  analysis CLOB,
  difficulty TINYINT NOT NULL DEFAULT 3,
  knowledge_point VARCHAR(200),
  score INT NOT NULL DEFAULT 5,
  create_by BIGINT NOT NULL DEFAULT 0,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT NOT NULL DEFAULT 0,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE edu_exam_paper (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  description VARCHAR(500),
  total_score INT NOT NULL DEFAULT 100,
  duration INT NOT NULL DEFAULT 60,
  pass_score INT NOT NULL DEFAULT 60,
  status TINYINT NOT NULL DEFAULT 1,
  type TINYINT NOT NULL DEFAULT 1,
  allow_redo TINYINT NOT NULL DEFAULT 0,
  knowledge_config VARCHAR(4000),
  create_by BIGINT NOT NULL DEFAULT 0,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT NOT NULL DEFAULT 0,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE edu_exam_paper_question (
  id VARCHAR(64) PRIMARY KEY,
  paper_id VARCHAR(64) NOT NULL,
  question_id VARCHAR(64) NOT NULL,
  sort INT NOT NULL DEFAULT 0,
  score INT NOT NULL DEFAULT 5,
  create_by BIGINT NOT NULL DEFAULT 0,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT NOT NULL DEFAULT 0,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE edu_exam_record (
  id VARCHAR(64) PRIMARY KEY,
  exam_id VARCHAR(64) NOT NULL,
  paper_id VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL DEFAULT 0,
  total_score INT NOT NULL DEFAULT 0,
  obtain_score INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  start_time TIMESTAMP NOT NULL,
  submit_time TIMESTAMP,
  ip_address VARCHAR(50),
  switch_count INT NOT NULL DEFAULT 0,
  answers CLOB,
  create_by BIGINT NOT NULL DEFAULT 0,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT NOT NULL DEFAULT 0,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE edu_exam_session (
  id VARCHAR(64) PRIMARY KEY,
  record_id VARCHAR(64) NOT NULL,
  exam_id VARCHAR(64) NOT NULL,
  paper_id VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  start_time TIMESTAMP NOT NULL,
  end_time TIMESTAMP NOT NULL,
  submit_time TIMESTAMP,
  last_active_time TIMESTAMP,
  ip_address VARCHAR(50),
  create_by BIGINT NOT NULL DEFAULT 0,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT NOT NULL DEFAULT 0,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_exam_session_record_id ON edu_exam_session(record_id);
CREATE INDEX idx_question_bank_id ON edu_exam_question(bank_id);
CREATE INDEX idx_question_type ON edu_exam_question(type);
CREATE INDEX idx_question_difficulty ON edu_exam_question(difficulty);
CREATE INDEX idx_question_knowledge ON edu_exam_question(knowledge_point);
CREATE INDEX idx_question_create_time ON edu_exam_question(create_time);
CREATE INDEX idx_exam_paper_status ON edu_exam_paper(status);
CREATE INDEX idx_exam_paper_creator_id ON edu_exam_paper(create_by);
CREATE INDEX idx_exam_paper_create_time ON edu_exam_paper(create_time);
CREATE INDEX idx_paper_question_paper_id ON edu_exam_paper_question(paper_id);
CREATE INDEX idx_paper_question_question_id ON edu_exam_paper_question(question_id);
CREATE INDEX idx_record_exam_id ON edu_exam_record(exam_id);
CREATE INDEX idx_record_user_id ON edu_exam_record(user_id);
CREATE INDEX idx_record_dept_id ON edu_exam_record(dept_id);
CREATE INDEX idx_record_status ON edu_exam_record(status);
CREATE INDEX idx_session_exam_id ON edu_exam_session(exam_id);
CREATE INDEX idx_session_user_id ON edu_exam_session(user_id);
CREATE INDEX idx_session_status ON edu_exam_session(status);
CREATE INDEX idx_session_end_time ON edu_exam_session(end_time);
