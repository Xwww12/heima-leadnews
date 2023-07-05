package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    // 最大分页数
    private static final short MAX_PAGE_SIZE = 50;

    @Resource
    private ApArticleMapper apArticleMapper;

    @Resource
    private ApArticleConfigMapper apArticleConfigMapper;

    @Resource
    private ApArticleContentMapper apArticleContentMapper;

    @Resource
    private ArticleFreemarkerService articleFreemarkerService;

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

    @Override
    @Transactional
    public ResponseResult saveArticle(ArticleDto dto) {
        // 校验参数
        if (dto == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        ApArticle apArticle = new ApArticle();
        BeanUtils.copyProperties(dto, apArticle);
        // 判断是新增文章还是更新文章
        if (apArticle.getId() == null) {
            // 新增文章，保存文章信息、配置、内容
            save(apArticle);

            ApArticleConfig config = new ApArticleConfig(apArticle.getId());
            apArticleConfigMapper.insert(config);

            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(apArticle.getId());
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.insert(apArticleContent);
        } else {
            // 更新文章，更新文章信息、内容
            updateById(apArticle);

            ApArticleContent apArticleContent = apArticleContentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId, dto.getId()));
            if (apArticleContent == null)
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.updateById(apArticleContent);
        }

        // 保存静态模板文件到MinIO
        articleFreemarkerService.buildArticleToMinIO(apArticle, dto.getContent());

        return ResponseResult.okResult(apArticle.getId());
    }
}
