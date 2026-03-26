package com.seckill.service;

import com.seckill.dto.PageResult;
import com.seckill.dto.UserAddDTO;
import com.seckill.dto.UserInfoDTO;
import com.seckill.dto.UserListDTO;
import com.seckill.dto.UserLoginDTO;
import com.seckill.dto.UserQueryDTO;
import com.seckill.dto.UserRegisterDTO;

public interface UserService {
    UserInfoDTO register(UserRegisterDTO dto);
    UserInfoDTO login(UserLoginDTO dto);
    UserInfoDTO getUserInfo(Long userId);
    boolean checkUsernameExists(String username);

    PageResult<UserListDTO> getUserList(UserQueryDTO query);
    UserListDTO addUser(UserAddDTO dto);
    boolean deleteUser(Long userId);
    UserListDTO getUserById(Long userId);
    void initAdminUser();
}
