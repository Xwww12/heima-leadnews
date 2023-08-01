package com.heima.model.behavior.dto;

import lombok.Data;

/**
 * 阅读行为dto
 */
@Data
public class ReadBehaviorDto {
    /**
     * 文章id
     */
    private Long articleId;

    /**
     * 阅读次数
     */
    private Integer count;
}
