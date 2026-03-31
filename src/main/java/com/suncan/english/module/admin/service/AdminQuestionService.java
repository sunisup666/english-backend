package com.suncan.english.module.admin.service;

import com.suncan.english.module.admin.dto.AdminQuestionQueryDTO;
import com.suncan.english.module.admin.dto.AdminQuestionSaveDTO;
import com.suncan.english.module.admin.vo.AdminQuestionPageVO;
import com.suncan.english.module.admin.vo.AdminQuestionVO;

public interface AdminQuestionService {

    AdminQuestionPageVO page(AdminQuestionQueryDTO queryDTO);

    AdminQuestionVO detail(Long id);

    void create(AdminQuestionSaveDTO dto);

    void update(AdminQuestionSaveDTO dto);

    void delete(Long id);
}