package com.heima.model.user.dtos;

import lombok.Data;

/**
 * 用户查询dto
 */
@Data
public class UserListDto {
    private Integer page;

    private Integer size;

    private Integer status;
}
