package com.heima.model.admin.dtos;

import lombok.Data;

@Data
public class LoginDto {

    /**
     * 登录名
     */
    private String name;

    /**
     * 密码
     */
    private String password;
}
