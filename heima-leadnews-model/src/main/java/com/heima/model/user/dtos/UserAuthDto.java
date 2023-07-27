package com.heima.model.user.dtos;

import lombok.Data;

/**
 * 用户审核通过/驳回Dto
 */
@Data
public class UserAuthDto {
    private Integer id;

    private String msg;
}
