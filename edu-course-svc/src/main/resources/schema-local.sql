DROP TABLE IF EXISTS resource_course_category;
DROP TABLE IF EXISTS course_hour;
DROP TABLE IF EXISTS course_chapters;
DROP TABLE IF EXISTS courses;

CREATE TABLE courses (
  id INT PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  thumb INT NOT NULL DEFAULT 0,
  charge INT NOT NULL DEFAULT 0,
  short_desc VARCHAR(500),
  is_required TINYINT NOT NULL DEFAULT 0,
  class_hour INT NOT NULL DEFAULT 0,
  is_show TINYINT NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  sort_at TIMESTAMP,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP,
  extra CLOB,
  admin_id INT NOT NULL DEFAULT 0
);

CREATE TABLE course_chapters (
  id INT PRIMARY KEY,
  course_id INT NOT NULL,
  name VARCHAR(200) NOT NULL,
  sort INT NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE course_hour (
  id INT PRIMARY KEY,
  course_id INT NOT NULL,
  chapter_id INT NOT NULL,
  sort INT NOT NULL DEFAULT 1,
  title VARCHAR(200) NOT NULL,
  type VARCHAR(32) NOT NULL,
  rid INT NOT NULL DEFAULT 0,
  duration INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE resource_course_category (
  course_id INT NOT NULL,
  category_id INT NOT NULL
);

CREATE INDEX idx_courses_is_show ON courses(is_show);
CREATE INDEX idx_courses_created_at ON courses(created_at);
CREATE INDEX idx_course_chapters_course_id ON course_chapters(course_id);
CREATE INDEX idx_course_hour_course_id ON course_hour(course_id);
CREATE INDEX idx_course_hour_chapter_id ON course_hour(chapter_id);
CREATE INDEX idx_resource_course_category_course_id ON resource_course_category(course_id);
CREATE INDEX idx_resource_course_category_category_id ON resource_course_category(category_id);
