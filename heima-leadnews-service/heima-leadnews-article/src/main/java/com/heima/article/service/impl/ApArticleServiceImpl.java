package com.heima.article.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    // 最大分页数
    private static final short MAX_PAGE_SIZE = 50;

    @Resource
    private ApArticleMapper apArticleMapper;

    @Override
    public ResponseResult load(ArticleHomeDto dto, Short type) {
        // 校验参数
        Integer size = dto.getSize();
        Date maxBehotTime = dto.getMaxBehotTime();
        Date minBehotTime = dto.getMinBehotTime();
        String tag = dto.getTag();
        if (size == null || size == 0)
            size = 10;
        size = Math.min(size, MAX_PAGE_SIZE);
        if (type == null || (!type.equals(ArticleConstants.LOADTYPE_LOAD_MORE) && !type.equals(ArticleConstants.LOADTYPE_LOAD_NEW)))
            type = ArticleConstants.LOADTYPE_LOAD_MORE;
        if (maxBehotTime == null)
            maxBehotTime = new Date();
        if (minBehotTime == null)
            minBehotTime = new Date();
        if (StringUtils.isBlank(tag))
            tag = ArticleConstants.DEFAULT_TAG;
        // 把参数填回dto
        dto.setSize(size);
        dto.setMaxBehotTime(maxBehotTime);
        dto.setMinBehotTime(minBehotTime);
        dto.setTag(tag);
        // 返回结果
        List<ApArticle> apArticles = apArticleMapper.loadArticleList(dto, type);
        return ResponseResult.okResult(apArticles);
    }
}
