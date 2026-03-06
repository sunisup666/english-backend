package com.suncan.english.service;

import com.suncan.english.dto.LoginDTO;
import com.suncan.english.dto.RegisterDTO;
import com.suncan.english.dto.UpdatePasswordDTO;
import com.suncan.english.dto.UpdateUserDTO;
import com.suncan.english.entity.User;

/**
 * 用户业务接口。
 */
public interface UserService {
    /**
     * 用户注册。
     */
    void register(RegisterDTO dto);

    /**
     * 用户登录，校验通过后返回 token。
     */
    String login(LoginDTO dto);

    /**
     * 根据用户 ID 获取个人信息。
     */
    User getUserInfo(Long userId);

    /**
     * 更新用户基础资料。
     */
    void updateUser(Long userId, UpdateUserDTO dto);

    /**
     * 修改用户密码。
     */
    void updatePassword(Long userId, UpdatePasswordDTO dto);
}
