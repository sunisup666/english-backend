package com.suncan.english.module.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suncan.english.module.admin.dto.AdminLoginDTO;
import com.suncan.english.module.admin.entity.Admin;
import com.suncan.english.module.admin.mapper.AdminMapper;
import com.suncan.english.module.admin.service.AdminService;
import com.suncan.english.module.admin.vo.AdminInfoVO;
import com.suncan.english.shared.exception.BusinessException;
import com.suncan.english.shared.util.Md5Util;
import com.suncan.english.shared.util.TokenUtil;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    private static final int STATUS_ENABLED = 1;

    private final TokenUtil tokenUtil;

    public AdminServiceImpl(TokenUtil tokenUtil) {
        this.tokenUtil = tokenUtil;
    }

    @Override
    public String login(AdminLoginDTO dto) {
        String username = normalizeRequired(dto.getUsername(), "管理员账号不能为空");
        Admin admin = this.lambdaQuery().eq(Admin::getUsername, username).one();
        if (admin == null) {
            throw new BusinessException("管理员账号或密码错误");
        }
        if (!Md5Util.matches(dto.getPassword(), admin.getPassword())) {
            throw new BusinessException("管理员账号或密码错误");
        }
        if (!STATUS_ENABLED_EQUALS(admin.getStatus())) {
            throw new BusinessException("管理员账号已禁用");
        }
        return tokenUtil.createAdminToken(admin.getId(), admin.getUsername());
    }

    @Override
    public AdminInfoVO getAdminInfo(Long adminId) {
        Admin admin = this.getById(adminId);
        if (admin == null) {
            throw new BusinessException("管理员不存在");
        }
        AdminInfoVO vo = new AdminInfoVO();
        vo.setId(admin.getId());
        vo.setUsername(admin.getUsername());
        vo.setNickname(admin.getNickname());
        vo.setStatus(admin.getStatus());
        return vo;
    }

    private boolean STATUS_ENABLED_EQUALS(Integer status) {
        return status != null && status == STATUS_ENABLED;
    }

    private String normalizeRequired(String value, String message) {
        if (value == null) {
            throw new BusinessException(message);
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException(message);
        }
        return trimmed;
    }
}