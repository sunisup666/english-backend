-- 闁活潿鍔嶉崺娑氭偘椤帞绀勯柡鍜佸櫍閳ь剚姘ㄩ弫銈夊箣閸戙倗绀?CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '濞戞挸顭烽弫鐠塂',
  `username` VARCHAR(50) NOT NULL COMMENT '闁活潿鍔嶉崺娑㈠触瀹ュ繒绀夐柣褑顕х紞宥夊船椤栨瑧顏遍柡宥呮穿閻?,
  `password` VARCHAR(64) NOT NULL COMMENT 'MD5闁告艾娴峰▓鎴犫偓闈涙閻?,
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '闁哄嫮鏁歌ⅷ',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '闂侇収鍠氶?,
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '闁归潧顑嗗┃鈧柛?,
  `english_level` VARCHAR(20) NOT NULL DEFAULT '闁告帗绻勬? COMMENT '闁兼槒绮鹃銏㈢驳婢跺矂鐛?,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '闁告帗绋戠紓鎾诲籍閸洘锛?,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '闁哄洤鐡ㄩ弻濠囧籍閸洘锛?,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='闁活潿鍔嶉崺娑氭偘?;

-- =============================================
-- 妤犵偠娅曠划锕傚础閸モ晠鐛撻柨娑欎亢閻︻垶宕￠摎鍌滅憿濡増顭囧ú浼村礂鐎圭姳绮撳☉鎿冨弮濡法鎮伴…鎺旂paper_question闁?-- 閻犲洤鐡ㄥΣ鎴︽晬?-- 1) 闁哄牜鍓氶鍏肩閸涱喗鐓€濠⒀呭仒閼垫垿姊荤壕瀣ㄢ偓鍐晬鐏炶偐鐟濋柛鎺斿█濞?question.paper_id
-- 2) 濞戞挻鑹炬慨鐔哥閿濆洨鍨冲ù鍏济崢娑氭導?paper_question闁挎稑顔巙estion.paper_id 濞寸姴鎳庢禒娑㈠礂閻撳寒鍟囬柛蹇旂矊缁?-- =============================================

CREATE TABLE IF NOT EXISTS `paper_question` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '濞戞挸顭烽弫鐠塂',
  `paper_id` BIGINT UNSIGNED NOT NULL COMMENT '閻犲洦娲栧畵宥狣',
  `question_id` BIGINT UNSIGNED NOT NULL COMMENT '濡増顭囧ú鐧怐',
  `score` INT DEFAULT NULL COMMENT '閻犲洢鍎撮惁顖炲础閾氬倻鐟撻悹鍥ュ劦椤ｄ粙宕氶崱妞诲亾?,
  `sort_order` INT DEFAULT NULL COMMENT '閻犲洢鍎撮惁顖炲础閾氬倻鐟撻悹鍥ュ劦椤ｄ粙骞掗幒鎴犵',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '闁告帗绋戠紓鎾诲籍閸洘锛?,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_paper_question` (`paper_id`, `question_id`),
  KEY `idx_paper_question_paper_id` (`paper_id`),
  KEY `idx_paper_question_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='閻犲洦娲栧畵搴紣濡吋绐楅柛蹇撶枃娴犲牏鎮?;

-- 闁告ê妫楄ぐ鍫曞极閻楀牆绁﹂弶鈺€鑳朵簺闁挎稒鑹鹃惃?question 閻炴稏鍔嬮懙鎴︽儍?paper_id/score/sort_order 閻庣數鍘ч崣?paper_question
-- 婵炲鍔嶉崜浼存晬濮橆偆鐭岄弶鈺€鑳朵簺 paper_id 濞戞挸绉崇拹鐔虹矚閾忚鐣遍柡浣哄瀹撲線鏁嶅畝鍕級闁稿繐绉烽崜浼村极閻楀牆绁﹂弶鈺傜☉閸欏棝宕楃€圭姳绮撻悶?INSERT INTO `paper_question` (`paper_id`, `question_id`, `score`, `sort_order`, `create_time`)
SELECT q.`paper_id`, q.`id`, q.`score`, q.`sort_order`, NOW()
FROM `question` q
WHERE q.`paper_id` IS NOT NULL
ON DUPLICATE KEY UPDATE
  `score` = VALUES(`score`),
  `sort_order` = VALUES(`sort_order`);
