package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.behavior.ArticleInfoDto;
import com.heima.model.common.dtos.ResponseResult;

public interface ApArticleService extends IService<ApArticle> {
    ResponseResult load(ArticleHomeDto dto, Short type);

    /**
     * 保存/更新文章
     * @param dto
     * @return
     */
    ResponseResult saveArticle(ArticleDto dto);

    /**
     * 文章回显
     * @param dto
     * @return
     */
    ResponseResult loadBehavior(ArticleInfoDto dto);
}
