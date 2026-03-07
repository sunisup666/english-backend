package com.suncan.english.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户业务实现。
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
        user.setEnglishLevel("初级");
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
    public User getUserInfo(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        return user;
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