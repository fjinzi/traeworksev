package com.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seckill.dto.UserAddDTO;
import com.seckill.dto.UserInfoDTO;
import com.seckill.dto.UserLoginDTO;
import com.seckill.dto.UserRegisterDTO;
import com.seckill.entity.User;
import com.seckill.mapper.UserMapper;
import com.seckill.service.UserService;
import com.seckill.util.JwtUtil;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserInfoDTO register(UserRegisterDTO dto) {
        log.info("开始注册用户：username={}", dto.getUsername());
        
        if (checkUsernameExists(dto.getUsername())) {
            log.warn("用户名已存在：username={}", dto.getUsername());
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        user.setPassword(encodedPassword);
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRoleType(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setIsDeleted(0);

        log.info("准备插入用户数据到数据库");
        userMapper.insert(user);
        log.info("用户插入成功，userId={}", user.getId());

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRoleType());
        log.info("用户注册成功，userId={}, token={}", user.getId(), token.substring(0, Math.min(20, token.length())) + "...");

        return buildUserInfoDTO(user, token);
    }

    @Override
    public UserInfoDTO login(UserLoginDTO dto) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, dto.getUsername());
        queryWrapper.eq(User::getIsDeleted, 0);

        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRoleType());

        return buildUserInfoDTO(user, token);
    }

    @Override
    public UserInfoDTO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            return null;
        }
        return buildUserInfoDTO(user, null);
    }

    @Override
    public boolean checkUsernameExists(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        queryWrapper.eq(User::getIsDeleted, 0);
        return userMapper.selectCount(queryWrapper) > 0;
    }

    private UserInfoDTO buildUserInfoDTO(User user, String token) {
        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRoleType(user.getRoleType());
        dto.setToken(token);
        return dto;
    }

    @Override
    public List<User> getUserList() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getIsDeleted, 0);
        queryWrapper.orderByDesc(User::getCreateTime);
        return userMapper.selectList(queryWrapper);
    }

    @Override
    public UserInfoDTO addUser(UserAddDTO dto) {
        log.info("管理员添加用户：username={}", dto.getUsername());
        
        if (checkUsernameExists(dto.getUsername())) {
            log.warn("用户名已存在：username={}", dto.getUsername());
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        user.setPassword(encodedPassword);
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRoleType(dto.getRoleType() != null ? dto.getRoleType() : 0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setIsDeleted(0);

        userMapper.insert(user);
        log.info("管理员添加用户成功，userId={}", user.getId());

        return buildUserInfoDTO(user, null);
    }

    @Override
    public boolean deleteUser(Long userId) {
        log.info("逻辑删除用户：userId={}", userId);
        
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            log.warn("用户不存在或已删除：userId={}", userId);
            return false;
        }

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId);
        updateWrapper.set(User::getIsDeleted, 1);
        updateWrapper.set(User::getUpdateTime, LocalDateTime.now());
        
        int result = userMapper.update(null, updateWrapper);
        log.info("用户逻辑删除结果：userId={}, result={}", userId, result);
        return result > 0;
    }

    @Override
    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null && user.getIsDeleted() == 0) {
            return user;
        }
        return null;
    }
}
