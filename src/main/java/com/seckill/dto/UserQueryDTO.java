package com.seckill.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;

    private String nickname;

    private Integer roleType;

    private Integer status;

    private Integer page = 1;

    private Integer pageSize = 10;
}
