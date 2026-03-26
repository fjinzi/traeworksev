package com.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckill.dto.PageResult;
import com.seckill.dto.UserAddDTO;
import com.seckill.dto.UserInfoDTO;
import com.seckill.dto.UserListDTO;
import com.seckill.dto.UserLoginDTO;
import com.seckill.dto.UserQueryDTO;
import com.seckill.dto.UserRegisterDTO;
import com.seckill.entity.User;
import com.seckill.mapper.UserMapper;
import com.seckill.service.UserService;
import com.seckill.util.JwtUtil;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public PageResult<UserListDTO> getUserList(UserQueryDTO query) {
        log.info("查询用户列表，条件：{}", query);
        
        Page<User> page = new Page<>(query.getPage(), query.getPageSize());
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getIsDeleted, 0);
        
        if (StringUtils.hasText(query.getUsername())) {
            queryWrapper.like(User::getUsername, query.getUsername());
        }
        if (StringUtils.hasText(query.getNickname())) {
            queryWrapper.like(User::getNickname, query.getNickname());
        }
        if (query.getRoleType() != null) {
            queryWrapper.eq(User::getRoleType, query.getRoleType());
        }
        if (query.getStatus() != null) {
            queryWrapper.eq(User::getStatus, query.getStatus());
        }
        
        queryWrapper.orderByDesc(User::getCreateTime);
        
        Page<User> result = userMapper.selectPage(page, queryWrapper);
        
        List<UserListDTO> dtoList = result.getRecords().stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
        
        return PageResult.of(dtoList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public UserListDTO addUser(UserAddDTO dto) {
        log.info("管理员添加用户：username={}", dto.getUsername());
        
        if (checkUsernameExists(dto.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname() : dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRoleType(dto.getRoleType() != null ? dto.getRoleType() : 0);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setIsDeleted(0);

        userMapper.insert(user);
        log.info("用户添加成功，userId={}", user.getId());

        return convertToListDTO(user);
    }

    @Override
    public boolean deleteUser(Long userId) {
        log.info("逻辑删除用户：userId={}", userId);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (user.getRoleType() == 1) {
            throw new RuntimeException("不能删除管理员用户");
        }
        
        user.setIsDeleted(1);
        user.setUpdateTime(LocalDateTime.now());
        
        int result = userMapper.updateById(user);
        log.info("用户删除{}，userId={}", result > 0 ? "成功" : "失败", userId);
        
        return result > 0;
    }

    @Override
    public UserListDTO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            return null;
        }
        return convertToListDTO(user);
    }

    @Override
    public void initAdminUser() {
        log.info("开始初始化管理员用户...");
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, "admin");
        queryWrapper.eq(User::getIsDeleted, 0);
        
        User existAdmin = userMapper.selectOne(queryWrapper);
        
        if (existAdmin != null) {
            log.info("管理员用户已存在，跳过初始化");
            return;
        }
        
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setNickname("系统管理员");
        admin.setRoleType(1);
        admin.setStatus(1);
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        admin.setIsDeleted(0);
        
        userMapper.insert(admin);
        log.info("管理员用户初始化成功，userId={}", admin.getId());
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

    private UserListDTO convertToListDTO(User user) {
        UserListDTO dto = new UserListDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
}
