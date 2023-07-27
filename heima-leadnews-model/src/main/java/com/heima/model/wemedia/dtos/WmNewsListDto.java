package com.heima.model.wemedia.dtos;

import lombok.Data;

/**
 * 文章查询dto
 */
@Data
public class WmNewsListDto {
    private Integer page;

    private Integer size;

    private Integer status;

    private String title;
}
