package com.suncan.english.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suncan.english.dto.LoginDTO;
import com.suncan.english.dto.RegisterDTO;
import com.suncan.english.dto.UpdatePasswordDTO;
import com.suncan.english.dto.UpdateUserDTO;
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
 * 这里主要使用 MP 的 lambdaQuery / lambdaUpdate，逻辑保持直观，方便答辩讲解。
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final TokenUtil tokenUtil;

    public UserServiceImpl(TokenUtil tokenUtil) {
        this.tokenUtil = tokenUtil;
    }

    /**
     * 注册逻辑：
     * 1. 处理用户名空白
     * 2. 用 MP 链式查询做判重
     * 3. 密码做 MD5 后落库
     */
    @Override
    public void register(RegisterDTO dto) {
        String username = normalizeRequired(dto.getUsername(), "用户名不能为空");
        long duplicateCount = this.lambdaQuery()
                .eq(User::getUsername, username)
                .count();
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

    /**
     * 登录逻辑：
     * 1. 按用户名查用户
     * 2. 校验 MD5 密码
     * 3. 生成 token
     */
    @Override
    public String login(LoginDTO dto) {
        String username = normalizeRequired(dto.getUsername(), "用户名不能为空");
        User user = this.lambdaQuery()
                .eq(User::getUsername, username)
                .one();
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!Md5Util.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        return tokenUtil.createToken(user.getId(), user.getUsername());
    }

    /**
     * 查询个人信息，返回前隐藏密码字段。
     */
    @Override
    public User getUserInfo(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    /**
     * 修改个人信息：
     * - 至少更新一个字段
     * - 使用 MP 的 lambdaUpdate 按字段更新，避免整实体回写
     */
    @Override
    public void updateUser(Long userId, UpdateUserDTO dto) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String nickname = normalizeOptional(dto.getNickname());
        String email = normalizeOptional(dto.getEmail());
        String phone = normalizeOptional(dto.getPhone());

        boolean changed = nickname != null || email != null || phone != null;
        if (!changed) {
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

    /**
     * 修改密码：
     * - 先校验旧密码
     * - 新旧密码不能相同
     * - 使用 lambdaUpdate 更新密码和更新时间
     */
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
