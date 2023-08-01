package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.wemedia.IWemediaClient;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.HotArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.vos.HotArticleVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class HotArticleServiceImpl implements HotArticleService {

    @Resource
    private ApArticleMapper apArticleMapper;

    @Resource
    private IWemediaClient iWemediaClient;

    @Resource
    private CacheService cacheService;

    @Override
    public void computeHotArticle() {
        // 查询前5天的文章
        Date before5Day = DateTime.now().minusDays(5).toDate();
        List<ApArticle> articleList = apArticleMapper.findArticleListBy5Days(before5Day);

        // 计算文章的热度
        List<HotArticleVo> hotArticleVoList = computeHot(articleList);

        // 根据频道缓存30条文章数据
        cacheTagToRedis(hotArticleVoList);
    }

    /**
     * 缓存热门文章数据到redis
     * @param hotArticleVoList
     */
    private void cacheTagToRedis(List<HotArticleVo> hotArticleVoList) {
        // 获取频道
        ResponseResult responseResult = iWemediaClient.getChannels();
        if (responseResult.getCode() == 200) {
            String channelJson = JSON.toJSONString(responseResult.getData());
            List<WmChannel> wmChannels = JSON.parseArray(channelJson, WmChannel.class);

            // 检索出每个频道的文章
            if (wmChannels != null && wmChannels.size() > 0) {
                for (WmChannel wmChannel : wmChannels) {
                    List<HotArticleVo> hotArticleVos = hotArticleVoList.stream().filter(x -> x.getChannelId().equals(wmChannel.getId()))
                            .collect(Collectors.toList());
                    // 缓存热度最高的30条文章
                    sortAndCache(hotArticleVos, ArticleConstants.HOT_ARTICLE_FIRST_PAGE + wmChannel.getId());
                }
            }
        }
    }

    private void sortAndCache(List<HotArticleVo> hotArticleVos, String key) {
        hotArticleVos = hotArticleVos.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed())
                .collect(Collectors.toList());
        if (hotArticleVos.size() > 30) {
            hotArticleVos = hotArticleVos.subList(0, 30);
        }
        cacheService.set(key, JSON.toJSONString(hotArticleVos));
    }

    /**
     * 计算文章热度
     * @param articleList
     * @return
     */
    private List<HotArticleVo> computeHot(List<ApArticle> articleList) {
        List<HotArticleVo> res = new ArrayList<>();
        if (articleList != null && articleList.size() > 0) {
            for (ApArticle apArticle : articleList) {
                HotArticleVo hotArticleVo = new HotArticleVo();
                BeanUtils.copyProperties(apArticle, hotArticleVo);
                // 加权计算热度
                Integer score = 0;
                if (hotArticleVo.getLikes() != null)
                    score += hotArticleVo.getLikes() * ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
                if (hotArticleVo.getComment() != null)
                    score += hotArticleVo.getComment() * ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
                if (hotArticleVo.getCollection() != null)
                    score += hotArticleVo.getCollection() * ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
                if (hotArticleVo.getViews() != null)
                    score += hotArticleVo.getViews();
                hotArticleVo.setScore(score);
                res.add(hotArticleVo);
            }
        }
        return res;
    }
}
