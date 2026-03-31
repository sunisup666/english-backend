-- 学习任务执行与练习记录模块：基础建表脚本
--
-- 设计说明：
-- 1) 练习记录(user_practice_record)与能力测试记录(user_test_record)分开存储，
--    避免“任务训练数据”和“阶段评估数据”混用导致统计口径混乱。
-- 2) 主表记录一次任务练习的整体结果；明细表记录逐题作答，便于后续复盘与错题分析。
-- 3) status 当前阶段先约定 1=已提交，满足基础闭环；后续可扩展进行中/中断等状态。

CREATE TABLE IF NOT EXISTS `user_practice_record` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `plan_id` BIGINT UNSIGNED NOT NULL COMMENT '学习计划ID',
  `task_id` BIGINT UNSIGNED NOT NULL COMMENT '学习任务ID',
  `task_type` TINYINT NOT NULL COMMENT '任务类型编码：1词汇 2语法 3听力 4口语',
  `question_type` TINYINT DEFAULT NULL COMMENT '题型编码',
  `scene_type` TINYINT DEFAULT NULL COMMENT '场景编码',
  `total_count` INT NOT NULL DEFAULT 0 COMMENT '总题数',
  `correct_count` INT NOT NULL DEFAULT 0 COMMENT '正确题数',
  `total_score` INT NOT NULL DEFAULT 0 COMMENT '总分',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '记录状态：1已提交',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `submit_time` DATETIME DEFAULT NULL COMMENT '提交时间',
  `duration_seconds` INT NOT NULL DEFAULT 0 COMMENT '作答时长（秒）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_practice_record_user_id` (`user_id`),
  KEY `idx_practice_record_task_id` (`task_id`),
  KEY `idx_practice_record_plan_id` (`plan_id`),
  KEY `idx_practice_record_submit_time` (`submit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户学习任务练习记录主表';

CREATE TABLE IF NOT EXISTS `user_practice_answer` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `practice_record_id` BIGINT UNSIGNED NOT NULL COMMENT '练习记录ID',
  `question_id` BIGINT UNSIGNED NOT NULL COMMENT '题目ID',
  `user_answer` VARCHAR(255) DEFAULT NULL COMMENT '客观题作答答案',
  `answer_text` TEXT COMMENT '主观题文本答案',
  `audio_answer_url` VARCHAR(500) DEFAULT NULL COMMENT '口语题音频答案地址',
  `is_correct` TINYINT NOT NULL DEFAULT 0 COMMENT '是否正确：0否 1是',
  `score` INT NOT NULL DEFAULT 0 COMMENT '本题得分',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_practice_answer_record_id` (`practice_record_id`),
  KEY `idx_practice_answer_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户学习任务练习作答明细表';
