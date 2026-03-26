package com.seckill.service;

import com.seckill.dto.UserAddDTO;
import com.seckill.dto.UserInfoDTO;
import com.seckill.dto.UserLoginDTO;
import com.seckill.dto.UserRegisterDTO;
import com.seckill.entity.User;

import java.util.List;

public interface UserService {
    UserInfoDTO register(UserRegisterDTO dto);
    UserInfoDTO login(UserLoginDTO dto);
    UserInfoDTO getUserInfo(Long userId);
    boolean checkUsernameExists(String username);
    
    List<User> getUserList();
    UserInfoDTO addUser(UserAddDTO dto);
    boolean deleteUser(Long userId);
    User getUserById(Long userId);
}
