package com.suncan.english.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import java.util.Objects;

/**
 * 用户业务实现，逻辑尽量保持简单，方便毕业设计讲解。
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final TokenUtil tokenUtil;

    public UserServiceImpl(UserMapper userMapper, TokenUtil tokenUtil) {
        this.userMapper = userMapper;
        this.tokenUtil = tokenUtil;
    }

    @Override
    public void register(RegisterDTO dto) {
        String username = dto.getUsername().trim();
        User exist = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (exist != null) {
            throw new BusinessException("用户名已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(username);
        // 这里按要求用 MD5，实际生产建议用 bcrypt/argon2
        user.setPassword(Md5Util.md5(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setEnglishLevel("\u521d\u7ea7");
        user.setCreateTime(now);
        user.setUpdateTime(now);
        userMapper.insert(user);
    }

    @Override
    public String login(LoginDTO dto) {
        String username = dto.getUsername().trim();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!Md5Util.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        return tokenUtil.createToken(user.getId(), user.getUsername());
    }

    @Override
    public User getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public void updateUser(Long userId, UpdateUserDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        boolean changed = false;
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname().trim());
            changed = true;
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail().trim());
            changed = true;
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone().trim());
            changed = true;
        }
        if (!changed) {
            throw new BusinessException("至少传一个要更新的字段");
        }

        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void updatePassword(Long userId, UpdatePasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!Md5Util.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        if (Objects.equals(dto.getOldPassword(), dto.getNewPassword())) {
            throw new BusinessException("新密码不能和旧密码一样");
        }

        user.setPassword(Md5Util.md5(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }
}
