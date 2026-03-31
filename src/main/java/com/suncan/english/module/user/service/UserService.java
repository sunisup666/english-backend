package com.suncan.english.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.suncan.english.module.user.dto.LoginDTO;
import com.suncan.english.module.user.dto.RegisterDTO;
import com.suncan.english.module.user.dto.UpdatePasswordDTO;
import com.suncan.english.module.user.dto.UpdateUserDTO;
import com.suncan.english.module.user.entity.User;
import com.suncan.english.module.user.vo.UserDashboardVO;
import com.suncan.english.module.user.vo.UserInfoVO;

/**
 * 用户业务接口。
 */
public interface UserService extends IService<User> {

    void register(RegisterDTO dto);

    String login(LoginDTO dto);

    /**
     * 查询用户信息，返回展示用 VO。
     */
    UserInfoVO getUserInfo(Long userId);

    /**
     * 查询当前登录用户首页概览。
     */
    UserDashboardVO getDashboard(Long userId);

    void updateUser(Long userId, UpdateUserDTO dto);

    void updatePassword(Long userId, UpdatePasswordDTO dto);

    /**
     * 同步更新用户英语等级编码。
     */
    void updateEnglishLevel(Long userId, Integer englishLevel);
}
