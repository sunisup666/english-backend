package com.suncan.english.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.suncan.english.dto.user.LoginDTO;
import com.suncan.english.dto.user.RegisterDTO;
import com.suncan.english.dto.user.UpdatePasswordDTO;
import com.suncan.english.dto.user.UpdateUserDTO;
import com.suncan.english.entity.User;
import com.suncan.english.vo.user.UserInfoVO;

/**
 * 用户业务接口。
 */
public interface UserService extends IService<User> {

    void register(RegisterDTO dto);

    String login(LoginDTO dto);

    /**
     * 查询用户信息（返回 VO，而不是 entity）。
     *
     * 说明：
     * - entity 只负责数据库字段；
     * - VO 负责“编码 + 中文名称”等展示字段。
     */
    UserInfoVO getUserInfo(Long userId);

    void updateUser(Long userId, UpdateUserDTO dto);

    void updatePassword(Long userId, UpdatePasswordDTO dto);

    /**
     * 同步更新用户英语等级（1初级 2中级 3高级）。
     */
    void updateEnglishLevel(Long userId, Integer englishLevel);
}