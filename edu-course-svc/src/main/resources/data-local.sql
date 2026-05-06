INSERT INTO courses (
  id, title, thumb, charge, short_desc, is_required, class_hour, is_show,
  created_at, sort_at, updated_at, deleted_at, extra, admin_id
) VALUES
  (
    101,
    'Java 基础线上课',
    1001,
    0,
    '面向新员工的 Java 语法与面向对象基础',
    1,
    8,
    1,
    TIMESTAMP '2026-05-05 10:00:00',
    TIMESTAMP '2026-05-05 10:00:00',
    TIMESTAMP '2026-05-05 10:00:00',
    NULL,
    '{"level":"basic","courseType":1,"coverUrl":"https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=960&q=80"}',
    1
  ),
  (
    102,
    'Spring Boot 实战营',
    1002,
    0,
    '覆盖配置管理、IOC、Web 开发与持久层集成',
    1,
    12,
    1,
    TIMESTAMP '2026-05-05 10:05:00',
    TIMESTAMP '2026-05-05 10:05:00',
    TIMESTAMP '2026-05-05 10:05:00',
    NULL,
    '{"level":"intermediate","courseType":1,"coverUrl":"https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=960&q=80"}',
    1
  ),
  (
    103,
    'React 管理后台开发',
    1003,
    0,
    '聚焦 React + TypeScript + Ant Design 管理端交互',
    0,
    10,
    1,
    TIMESTAMP '2026-05-05 10:10:00',
    TIMESTAMP '2026-05-05 10:10:00',
    TIMESTAMP '2026-05-05 10:10:00',
    NULL,
    '{"level":"frontend","courseType":2,"coverUrl":"https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=960&q=80"}',
    1
  );

INSERT INTO course_chapters (id, course_id, name, sort, created_at, updated_at) VALUES
  (10001, 101, 'Java 入门', 1, TIMESTAMP '2026-05-05 10:20:00', TIMESTAMP '2026-05-05 10:20:00'),
  (10002, 101, '面向对象', 2, TIMESTAMP '2026-05-05 10:25:00', TIMESTAMP '2026-05-05 10:25:00'),
  (10003, 102, 'Spring Boot 基础', 1, TIMESTAMP '2026-05-05 10:30:00', TIMESTAMP '2026-05-05 10:30:00'),
  (10004, 103, 'React 页面搭建', 1, TIMESTAMP '2026-05-05 10:35:00', TIMESTAMP '2026-05-05 10:35:00');

INSERT INTO course_hour (
  id, course_id, chapter_id, sort, title, type, rid, duration, created_at, deleted
) VALUES
  (20001, 101, 10001, 1, 'Java 开发环境与 main 方法', 'video', 1, 900, TIMESTAMP '2026-05-05 10:40:00', 0),
  (20002, 101, 10002, 1, '类与对象', 'video', 2, 1200, TIMESTAMP '2026-05-05 10:45:00', 0),
  (20003, 102, 10003, 1, 'Spring Boot 自动配置', 'video', 3, 1500, TIMESTAMP '2026-05-05 10:50:00', 0),
  (20004, 103, 10004, 1, 'Ant Design 列表与表单', 'video', 4, 1100, TIMESTAMP '2026-05-05 10:55:00', 0);

INSERT INTO resource_course_category (course_id, category_id) VALUES
  (101, 1),
  (102, 1),
  (103, 2);
