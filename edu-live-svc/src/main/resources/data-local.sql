INSERT INTO edu_live_room (
  id, title, course_id, start_time, end_time, status, push_url, play_url, record_url, creator_id,
  create_by, create_time, update_by, update_time, is_deleted
) VALUES
  (
    'live-local-001',
    'Spring Boot 线上直播答疑',
    102,
    TIMESTAMP '2026-05-10 19:30:00',
    TIMESTAMP '2026-05-10 21:00:00',
    1,
    'rtmp://local/push/live-local-001',
    'http://127.0.0.1/live/live-local-001.m3u8',
    '',
    1,
    1,
    TIMESTAMP '2026-05-05 11:00:00',
    1,
    TIMESTAMP '2026-05-05 11:00:00',
    0
  ),
  (
    'live-local-002',
    'React 管理后台实践直播',
    103,
    TIMESTAMP '2026-05-12 20:00:00',
    TIMESTAMP '2026-05-12 21:30:00',
    2,
    'rtmp://local/push/live-local-002',
    'http://127.0.0.1/live/live-local-002.m3u8',
    'http://127.0.0.1/live/record/live-local-002.mp4',
    1,
    1,
    TIMESTAMP '2026-05-05 11:10:00',
    1,
    TIMESTAMP '2026-05-05 11:10:00',
    0
  );
