package com.suncan.english.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suncan.english.enums.EnglishLevelEnum;
import com.suncan.english.dto.user.LoginDTO;
import com.suncan.english.dto.user.RegisterDTO;
import com.suncan.english.dto.user.UpdatePasswordDTO;
import com.suncan.english.dto.user.UpdateUserDTO;
import com.suncan.english.entity.User;
import com.suncan.english.exception.BusinessException;
import com.suncan.english.mapper.UserMapper;
import com.suncan.english.service.UserService;

import com.suncan.english.util.Md5Util;
import com.suncan.english.util.TokenUtil;
import com.suncan.english.vo.user.UserInfoVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户业务实现。
 *
 * 设计说明：
 * - 等级在数据库中只存数字编码（1/2/3）；
 * - 展示给前端时再转换中文名称，避免业务判断依赖文案字符串；
 * - entity 只做持久化映射，展示扩展信息放到 VO。
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final TokenUtil tokenUtil;

    public UserServiceImpl(TokenUtil tokenUtil) {
        this.tokenUtil = tokenUtil;
    }

    @Override
    public void register(RegisterDTO dto) {
        String username = normalizeRequired(dto.getUsername(), "用户名不能为空");
        long duplicateCount = this.lambdaQuery().eq(User::getUsername, username).count();
        if (duplicateCount > 0) {
            throw new BusinessException("用户名已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(username);
        user.setPassword(Md5Util.md5(dto.getPassword()));
        user.setNickname(normalizeOptional(dto.getNickname()));
        user.setEmail(normalizeOptional(dto.getEmail()));
        user.setPhone(normalizeOptional(dto.getPhone()));
        // 新用户默认初级：后续可由测试模块自动更新。
        user.setEnglishLevel(EnglishLevelEnum.BEGINNER.getCode());
        user.setCreateTime(now);
        user.setUpdateTime(now);
        this.save(user);
    }

    @Override
    public String login(LoginDTO dto) {
        String username = normalizeRequired(dto.getUsername(), "用户名不能为空");
        User user = this.lambdaQuery().eq(User::getUsername, username).one();
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!Md5Util.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        return tokenUtil.createToken(user.getId(), user.getUsername());
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // VO 组装：同时返回编码和值名称，前端展示更直观、联调更方便。
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setEnglishLevel(user.getEnglishLevel());
        vo.setEnglishLevelName(EnglishLevelEnum.getNameByCode(user.getEnglishLevel()));
        return vo;
    }

    @Override
    public void updateUser(Long userId, UpdateUserDTO dto) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String nickname = normalizeOptional(dto.getNickname());
        String email = normalizeOptional(dto.getEmail());
        String phone = normalizeOptional(dto.getPhone());
        if (nickname == null && email == null && phone == null) {
            throw new BusinessException("至少传一个要更新的字段");
        }

        this.lambdaUpdate()
                .eq(User::getId, userId)
                .set(nickname != null, User::getNickname, nickname)
                .set(email != null, User::getEmail, email)
                .set(phone != null, User::getPhone, phone)
                .set(User::getUpdateTime, LocalDateTime.now())
                .update();
    }

    @Override
    public void updatePassword(Long userId, UpdatePasswordDTO dto) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String oldPassword = dto.getOldPassword();
        String newPassword = dto.getNewPassword();
        if (!Md5Util.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        if (Md5Util.matches(newPassword, user.getPassword())) {
            throw new BusinessException("新密码不能和旧密码一样");
        }

        this.lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getPassword, Md5Util.md5(newPassword))
                .set(User::getUpdateTime, LocalDateTime.now())
                .update();
    }

    @Override
    public void updateEnglishLevel(Long userId, Integer englishLevel) {
        // 统一做编码合法性校验，避免非法等级写入数据库。
        if (!EnglishLevelEnum.containsCode(englishLevel)) {
            throw new BusinessException("英语等级编码不合法");
        }
        this.lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getEnglishLevel, englishLevel)
                .set(User::getUpdateTime, LocalDateTime.now())
                .update();
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

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
