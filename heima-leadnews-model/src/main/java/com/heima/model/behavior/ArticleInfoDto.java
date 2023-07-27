package com.heima.model.behavior;

import lombok.Data;

/**
 * 回显文章信息Dto
 */
@Data
public class ArticleInfoDto {
    /**
     * 文章id
     */
    private Long articleId;

    /**
     * 作者id
     */
    private Integer authorId;
}
