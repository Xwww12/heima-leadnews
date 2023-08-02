package com.heima.model.mess;

import lombok.Data;

@Data
public class UpdateArticleMess {

    // 修改文章的字段类型
    private UpdateArticleType type;

    // 文章id
    private Long articleId;

    // 增量
    private Integer add;

    public enum UpdateArticleType {
        COLLECTION,     // 收藏
        COMMENT,        // 评论
        LIKES,          // 点赞
        VIEWS           // 观看
    }
}
