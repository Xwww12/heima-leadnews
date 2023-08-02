package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.constants.ArticleConstants;
import com.heima.common.constants.BehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.article.vos.HotArticleVo;
import com.heima.model.behavior.dto.ArticleInfoDto;
import com.heima.model.behavior.dto.CollectionBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mess.ArticleVisitStreamMess;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.nntp.Article;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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

    @Resource
    private CacheService cacheService;

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

    @Override
    public ResponseResult loadBehavior(ArticleInfoDto dto) {
        Integer userId = getCurUserId();
        if (userId == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        // 校验参数
        if (dto == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        Long articleId = dto.getArticleId();
        if (articleId == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        Integer authorId = dto.getAuthorId();
        if (authorId == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        // 查询redis
        String key = BehaviorConstants.CACHE_BEHAVIOR_LIKE + articleId;
        Boolean isLike = cacheService.sIsMember(key, userId.toString());
        key = BehaviorConstants.CACHE_BEHAVIOR_UNLIKE + articleId;
        Boolean isUnlike = cacheService.sIsMember(key, userId.toString());
        key = BehaviorConstants.CACHE_BEHAVIOR_READ + articleId;
        String viewCount = (String) cacheService.hGet(key, articleId.toString());
        key = BehaviorConstants.CACHE_BEHAVIOR_COLLECTION + articleId;
        Boolean isCollection = cacheService.sIsMember(key, userId.toString());
        key = BehaviorConstants.CACHE_BEHAVIOR_FOLLOW + authorId;
        Boolean isFollow = cacheService.sIsMember(key, userId.toString());

        // 返回
        HashMap<String, Object> res = new HashMap<>();
        res.put("islike", isLike);      // 点赞
        res.put("isunlike", isUnlike);  // 不喜欢
        res.put("iscollection", isCollection); // 收藏
        res.put("isfollow", isFollow);     // 关注
        res.put("viewCount", Integer.valueOf(viewCount == null ? "0" : viewCount));// 阅读数
        return ResponseResult.okResult(res);
    }

    @Override
    public ResponseResult collection(CollectionBehaviorDto dto) {
        Integer userId = getCurUserId();
        if (userId == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        // 校验参数
        if (dto == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        Long articleId = dto.getEntryId();  // 文章id
        Short operation = dto.getOperation();   // 0 收藏  1 取消收藏
        Date publishedTime = dto.getPublishedTime();    // 发布时间
        Short type = dto.getType();         // 0 文章  1 动态
        if (articleId == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        if (operation == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        if (publishedTime == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        if (type == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        // 保存到数据库
        Boolean res = false;
        if (operation == 0) {
            res = apArticleMapper.saveCollection(userId, articleId, publishedTime, type);
            String key = BehaviorConstants.CACHE_BEHAVIOR_COLLECTION + articleId;
            cacheService.sAdd(key, userId.toString());
        } else if (operation == 1) {
            res = apArticleMapper.cancelCollection(userId, articleId, type);
            String key = BehaviorConstants.CACHE_BEHAVIOR_COLLECTION + articleId;
            cacheService.sRemove(key, userId.toString());
        }
        return ResponseResult.okResult(res);
    }

    @Override
    public ResponseResult load2(ArticleHomeDto dto, Short type, Boolean firstPage) {
        String jsonStr = cacheService.get(ArticleConstants.HOT_ARTICLE_FIRST_PAGE + dto.getTag());

        if (StringUtils.isNotBlank(jsonStr)) {
            HotArticleVo hotArticleVo = JSON.parseObject(jsonStr, HotArticleVo.class);
            return ResponseResult.okResult(hotArticleVo);
        }

        // 没有缓存时
        return load(dto, type);
    }

    @Override
    public void updateScore(ArticleVisitStreamMess mess) {
        // 更新文章用户行为数据
        ApArticle apArticle = updateArticle(mess);

        Integer score = computeScore(apArticle) * 3;
        // 替换文章对应频道的热点数据
        replaceDataToRedis(apArticle, score, ArticleConstants.HOT_ARTICLE_FIRST_PAGE + apArticle.getChannelId());

        // 替换推荐对应的热点数据
        replaceDataToRedis(apArticle, score, ArticleConstants.HOT_ARTICLE_FIRST_PAGE + ArticleConstants.DEFAULT_TAG);

    }

    private void replaceDataToRedis(ApArticle apArticle, Integer score, String key) {
        String articleListStr = cacheService.get(key);
        if (StringUtils.isNotBlank(articleListStr)) {
            List<HotArticleVo> hotArticleVoList = JSON.parseArray(articleListStr, HotArticleVo.class);

            boolean flag = true;

            //如果缓存中存在该文章，只更新分值
            for (HotArticleVo hotArticleVo : hotArticleVoList) {
                if (hotArticleVo.getId().equals(apArticle.getId())) {
                    hotArticleVo.setScore(score);
                    flag = false;
                    break;
                }
            }

            //如果缓存中不存在，查询缓存中分值最小的一条数据，进行分值的比较，如果当前文章的分值大于缓存中的数据，就替换
            if (flag) {
                if (hotArticleVoList.size() >= 30) {
                    hotArticleVoList = hotArticleVoList.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed()).collect(Collectors.toList());
                    HotArticleVo lastHot = hotArticleVoList.get(hotArticleVoList.size() - 1);
                    if (lastHot.getScore() < score) {
                        hotArticleVoList.remove(lastHot);
                        HotArticleVo hot = new HotArticleVo();
                        BeanUtils.copyProperties(apArticle, hot);
                        hot.setScore(score);
                        hotArticleVoList.add(hot);
                    }


                } else {
                    HotArticleVo hot = new HotArticleVo();
                    BeanUtils.copyProperties(apArticle, hot);
                    hot.setScore(score);
                    hotArticleVoList.add(hot);
                }
            }
            //缓存到redis
            hotArticleVoList = hotArticleVoList.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed()).collect(Collectors.toList());
            cacheService.set(key, JSON.toJSONString(hotArticleVoList));

        }
    }

    private ApArticle updateArticle(ArticleVisitStreamMess mess) {
        ApArticle apArticle = getById(mess.getArticleId());
        // 累加用户行为数据
        apArticle.setCollection(apArticle.getCollection() == null ? 0 : apArticle.getCollection() + mess.getCollect());
        apArticle.setComment(apArticle.getComment() == null ? 0 : apArticle.getComment() + mess.getComment());
        apArticle.setLikes(apArticle.getLikes() == null ? 0 : apArticle.getLikes() + mess.getLike());
        apArticle.setViews(apArticle.getViews() == null ? 0 : apArticle.getViews() + mess.getView());

        updateById(apArticle);
        return apArticle;

    }

    private Integer computeScore(ApArticle apArticle) {
        Integer score = 0;
        if(apArticle.getLikes() != null){
            score += apArticle.getLikes() * ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
        }
        if(apArticle.getViews() != null){
            score += apArticle.getViews();
        }
        if(apArticle.getComment() != null){
            score += apArticle.getComment() * ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
        }
        if(apArticle.getCollection() != null){
            score += apArticle.getCollection() * ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
        }

        return score;
    }

    private Integer getCurUserId() {
        // 获取登录用户
        ApUser user = AppThreadLocalUtil.getUser();
        return user == null ? null : user.getId();
    }
}
