package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.service.ApArticleConfigService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class ApArticleConfigServiceImpl extends ServiceImpl<ApArticleConfigMapper, ApArticleConfig> implements ApArticleConfigService {
    @Override
    public void updateByMap(Map<String, Object> map) {
        Object enable = map.get("enable");
        boolean isDown = !enable.equals(1);

        update(Wrappers.<ApArticleConfig>lambdaUpdate()
                .eq(ApArticleConfig::getArticleId, map.get("articleId"))
                .set(ApArticleConfig::getIsDown, isDown));
    }
}
