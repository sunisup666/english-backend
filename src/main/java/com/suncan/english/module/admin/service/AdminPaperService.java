package com.suncan.english.module.admin.service;

import com.suncan.english.module.admin.dto.AdminPaperQueryDTO;
import com.suncan.english.module.admin.dto.AdminPaperQuestionAssignRequestDTO;
import com.suncan.english.module.admin.dto.AdminPaperSaveDTO;
import com.suncan.english.module.admin.vo.AdminPaperPageVO;
import com.suncan.english.module.admin.vo.AdminPaperQuestionVO;
import com.suncan.english.module.admin.vo.AdminPaperVO;

import java.util.List;

public interface AdminPaperService {

    AdminPaperPageVO page(AdminPaperQueryDTO queryDTO);

    AdminPaperVO detail(Long id);

    void create(AdminPaperSaveDTO dto);

    void update(AdminPaperSaveDTO dto);

    void delete(Long id);

    List<AdminPaperQuestionVO> listQuestions(Long paperId);

    void saveQuestions(Long paperId, AdminPaperQuestionAssignRequestDTO dto);
}