package com.suncan.english.module.progress.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TaskTypeWeekCorrectRateVO {

    private String weekLabel;

    private Integer taskType;

    private BigDecimal correctRate;
}

