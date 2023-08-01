package com.heima.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface ApArticleMapper extends BaseMapper<ApArticle> {
    /**
     *
     * @param dto
     * @param type 1 加载更多，2 加载最新
     * @return
     */
    List<ApArticle> loadArticleList(ArticleHomeDto dto, Short type);

    /**
     * 保存文章收藏
     * @param userId
     * @param articleId
     * @param publishedTime
     * @param type
     * @return
     */
    Boolean saveCollection(Integer userId, Long articleId, Date publishedTime, Short type);

    /**
     * 取消收藏
     * @param userId
     * @param articleId
     * @param type
     */
    Boolean cancelCollection(Integer userId, Long articleId, Short type);

    /**
     * 获取5天内的文章数据
     * @return
     */
    List<ApArticle> findArticleListBy5Days(@Param("before5Day") Date before5Day);
}
