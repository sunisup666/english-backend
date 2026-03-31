package com.suncan.english.module.learning.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 瀛︿範浠诲姟鍒楄〃鏌ヨ鍙傛暟銆? *
 * 璇存槑锛? * 1. 璇?DTO 鍙礋璐ｆ壙杞芥煡璇㈡潯浠讹紝涓嶅仛澶嶆潅涓氬姟閫昏緫锛? * 2. 閫氳繃涓€涓?DTO 缁熶竴鎵胯浇绛涢€夊弬鏁帮紝鍚庣画鏂板绛涢€夐」鏃跺彧闇€澧炲瓧娈碉紝鎺ュ彛鎵╁睍鎴愭湰浣庯紱
 * 3. current/size 榛樿鍊煎湪 service 灞傜粺涓€鍏滃簳锛岄伩鍏嶆帶鍒跺眰鍜屼笟鍔″眰鍑虹幇閲嶅鍒ゆ柇銆? */
@Data
public class StudyTaskQueryDTO {

    /**
     * 瀛︿範璁″垝 ID锛堝繀浼狅級銆?     */
    private Long planId;

    /**
     * 浠诲姟鏃ユ湡锛堝彲閫夛級銆?     * 鍓嶇浼犱粖澶╁嵆鍙疄鐜扳€滃綋澶╀换鍔¤鍥锯€濓紝涓嶄紶鍒欐煡鐪嬪叏閮ㄤ换鍔°€?     */
    private LocalDate taskDate;

    /**
     * 浠诲姟鐘舵€侊紙鍙€夛級锛? 鏈畬鎴愶紝1 宸插畬鎴愩€?     */
    private Integer status;

    /**
     * 浠诲姟绫诲瀷锛堝彲閫夛級锛? 璇嶆眹锛? 璇硶锛? 鍚姏锛? 鍙ｈ銆?     */
    private Integer taskType;

    /**
     * 褰撳墠椤碉紙鍙€夛級銆?     */
    private Long current;

    /**
     * 姣忛〉鏉℃暟锛堝彲閫夛級銆?     */
    private Long size;
}


