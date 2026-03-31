-- 英语等级数字化改造：最终结构整理版
--
-- 目标：三张表最终只保留正式字段
-- 1) user.english_level
-- 2) study_plan.current_level
-- 3) user_test_record.level_result
--
-- 编码含义：
-- 1 = 初级
-- 2 = 中级
-- 3 = 高级

-- ===============================
-- 一、把正式字段中的历史字符串数据转换为数字编码
-- 说明：若字段当前已是数字，不会受影响。
-- ===============================
UPDATE `user`
SET `english_level` = CASE
    WHEN `english_level` IN ('初级', '1') THEN 1
    WHEN `english_level` IN ('中级', '2') THEN 2
    WHEN `english_level` IN ('高级', '3') THEN 3
    ELSE 1
END;

UPDATE `study_plan`
SET `current_level` = CASE
    WHEN `current_level` IN ('初级', '1') THEN 1
    WHEN `current_level` IN ('中级', '2') THEN 2
    WHEN `current_level` IN ('高级', '3') THEN 3
    ELSE 1
END;

UPDATE `user_test_record`
SET `level_result` = CASE
    WHEN `level_result` IN ('初级', '1') THEN 1
    WHEN `level_result` IN ('中级', '2') THEN 2
    WHEN `level_result` IN ('高级', '3') THEN 3
    ELSE 1
END;

-- ===============================
-- 二、统一正式字段类型与注释（最终设计）
-- ===============================
ALTER TABLE `user`
    MODIFY COLUMN `english_level` TINYINT NOT NULL DEFAULT 1 COMMENT '英语等级：1初级 2中级 3高级';

ALTER TABLE `study_plan`
    MODIFY COLUMN `current_level` TINYINT NOT NULL DEFAULT 1 COMMENT '计划快照等级：1初级 2中级 3高级';

ALTER TABLE `user_test_record`
    MODIFY COLUMN `level_result` TINYINT NOT NULL DEFAULT 1 COMMENT '测试结果等级：1初级 2中级 3高级';

-- ===============================
-- 三、清理过渡字段痕迹（若存在）
-- ===============================
ALTER TABLE `user` DROP COLUMN IF EXISTS `english_level_old`;
ALTER TABLE `user` DROP COLUMN IF EXISTS `english_level_new`;

ALTER TABLE `study_plan` DROP COLUMN IF EXISTS `current_level_old`;
ALTER TABLE `study_plan` DROP COLUMN IF EXISTS `current_level_new`;

ALTER TABLE `user_test_record` DROP COLUMN IF EXISTS `level_result_old`;
ALTER TABLE `user_test_record` DROP COLUMN IF EXISTS `level_result_new`;