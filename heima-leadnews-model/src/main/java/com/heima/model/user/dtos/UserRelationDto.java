package com.heima.model.user.dtos;

import lombok.Data;

/**
 * 作者关注/全关dto
 */
@Data
public class UserRelationDto {
    /**
     * 文章id
     */
    private Long articleId;

    /**
     * 作者id
     */
    private Integer authorId;

    /**
     * 0 关注   1 取消
     */
    private Short operation;
}
