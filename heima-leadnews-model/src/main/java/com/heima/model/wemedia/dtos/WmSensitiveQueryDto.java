package com.heima.model.wemedia.dtos;

import lombok.Data;

/**
 * 敏感词查询dto
 */
@Data
public class WmSensitiveQueryDto {
    private String name;

    private int page;

    private int size;
}
