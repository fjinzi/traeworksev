package com.seckill.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserListDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String phone;

    private Integer roleType;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
