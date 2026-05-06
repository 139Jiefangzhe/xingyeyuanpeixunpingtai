CREATE TABLE IF NOT EXISTS `courses` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `thumb` int DEFAULT NULL,
  `charge` int NOT NULL DEFAULT 0,
  `short_desc` varchar(500) DEFAULT NULL,
  `is_required` tinyint NOT NULL DEFAULT 0,
  `class_hour` int NOT NULL DEFAULT 0,
  `is_show` tinyint NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `sort_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` datetime DEFAULT NULL,
  `extra` text,
  `admin_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_courses_is_show` (`is_show`),
  KEY `idx_courses_is_required` (`is_required`),
  KEY `idx_courses_admin_id` (`admin_id`),
  KEY `idx_courses_sort_at` (`sort_at`),
  KEY `idx_courses_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `course_chapters` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `course_id` int unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_course_chapters_course_id` (`course_id`),
  KEY `idx_course_chapters_course_sort` (`course_id`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `course_hour` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `course_id` int unsigned NOT NULL,
  `chapter_id` int unsigned NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `title` varchar(255) NOT NULL,
  `type` varchar(64) NOT NULL,
  `rid` int DEFAULT NULL,
  `duration` int NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_course_hour_course_id` (`course_id`),
  KEY `idx_course_hour_chapter_id` (`chapter_id`),
  KEY `idx_course_hour_course_sort` (`course_id`, `chapter_id`, `sort`),
  KEY `idx_course_hour_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `resource_course_category` (
  `course_id` int unsigned NOT NULL,
  `category_id` int unsigned NOT NULL,
  UNIQUE KEY `uk_resource_course_category` (`course_id`, `category_id`),
  KEY `idx_resource_course_category_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
