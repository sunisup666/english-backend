package com.suncan.english.module.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.suncan.english.module.admin.dto.AdminLoginDTO;
import com.suncan.english.module.admin.entity.Admin;
import com.suncan.english.module.admin.vo.AdminInfoVO;

public interface AdminService extends IService<Admin> {

    String login(AdminLoginDTO dto);

    AdminInfoVO getAdminInfo(Long adminId);
}