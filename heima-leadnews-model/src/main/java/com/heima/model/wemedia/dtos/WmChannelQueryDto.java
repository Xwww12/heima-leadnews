package com.heima.model.wemedia.dtos;

import lombok.Data;

/**
 * 频道查询dto
 */
@Data
public class WmChannelQueryDto {
    private String name;

    private int page;

    private int size;

    private Boolean status;
}
