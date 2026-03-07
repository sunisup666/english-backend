package com.suncan.english.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.suncan.english.dto.user.LoginDTO;
import com.suncan.english.dto.user.RegisterDTO;
import com.suncan.english.dto.user.UpdatePasswordDTO;
import com.suncan.english.dto.user.UpdateUserDTO;
import com.suncan.english.entity.User;

/**
 * 用户业务接口。
 */
public interface UserService extends IService<User> {

    void register(RegisterDTO dto);

    String login(LoginDTO dto);

    User getUserInfo(Long userId);

    void updateUser(Long userId, UpdateUserDTO dto);

    void updatePassword(Long userId, UpdatePasswordDTO dto);
}