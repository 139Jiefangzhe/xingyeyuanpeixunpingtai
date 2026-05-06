DROP TABLE IF EXISTS edu_live_room;

CREATE TABLE edu_live_room (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  course_id INT,
  start_time TIMESTAMP,
  end_time TIMESTAMP,
  status TINYINT NOT NULL DEFAULT 1,
  push_url VARCHAR(500),
  play_url VARCHAR(500),
  record_url VARCHAR(500),
  creator_id BIGINT NOT NULL DEFAULT 0,
  create_by BIGINT NOT NULL DEFAULT 0,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT NOT NULL DEFAULT 0,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_live_room_status ON edu_live_room(status);
CREATE INDEX idx_live_room_course_id ON edu_live_room(course_id);
CREATE INDEX idx_live_room_start_time ON edu_live_room(start_time);
