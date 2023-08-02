package com.heima.behavior.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.common.constants.BehaviorConstants;
import com.heima.behavior.service.BehaviorService;
import com.heima.common.redis.CacheService;
import com.heima.model.behavior.dto.LikesBehaviorDto;
import com.heima.model.behavior.dto.ReadBehaviorDto;
import com.heima.model.behavior.dto.UnLikesBehaviorDto;
import com.heima.model.common.constancts.HotArticleConstants;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mess.UpdateArticleMess;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class BehaviorServiceImpl implements BehaviorService {
    @Resource
    private CacheService cacheService;

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public ResponseResult like(LikesBehaviorDto dto) {
        Integer userId = getCurUserId();
        if (userId == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        // 校验参数
        if (dto == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        Long articleId = dto.getArticleId();
        Short operation = dto.getOperation();
        Short type = dto.getType();
        if (articleId == null || operation == null || type == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        // 根据参数判断是点赞还是取消点赞
        if (operation == 0) {
            // 点赞，点赞人里把当前用户添加进去，将点赞数+1
            String key = BehaviorConstants.CACHE_BEHAVIOR_LIKE + articleId;
            cacheService.sAdd(key, userId.toString());
            key = BehaviorConstants.CACHE_BEHAVIOR_LIKE_COUNT + articleId;
            cacheService.incrBy(key, 1);
        } else {
            // 取消点赞，点赞人里吧当前用户移除，将点赞数-1
            String key = BehaviorConstants.CACHE_BEHAVIOR_LIKE + articleId;
            cacheService.sRemove(key, userId.toString());
            key = BehaviorConstants.CACHE_BEHAVIOR_LIKE_COUNT + articleId;
            cacheService.incrBy(key, -1);
        }

        // 发送消息
        UpdateArticleMess mess = new UpdateArticleMess();
        mess.setArticleId(articleId);
        mess.setType(UpdateArticleMess.UpdateArticleType.LIKES);
        mess.setAdd(operation == 0 ? 1 : -1);
        kafkaTemplate.send(HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC, JSON.toJSONString(mess));

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult read(ReadBehaviorDto dto) {
        Integer userId = getCurUserId();
        if (userId == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        // 校验参数
        if (dto == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        Long articleId = dto.getArticleId();
        Integer count = dto.getCount();
        if (articleId == null || count == null)
            return  ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        // 将文章的阅读数+1
        String key = BehaviorConstants.CACHE_BEHAVIOR_READ;
        cacheService.hIncrBy(key, articleId.toString(), 1);

        // 发送消息
        UpdateArticleMess mess = new UpdateArticleMess();
        mess.setArticleId(articleId);
        mess.setType(UpdateArticleMess.UpdateArticleType.VIEWS);
        mess.setAdd(1);
        kafkaTemplate.send(HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC, JSON.toJSONString(mess));

        return ResponseResult.okResult(true);
    }

    @Override
    public ResponseResult unLike(UnLikesBehaviorDto dto) {
        Integer userId = getCurUserId();
        if (userId == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        // 校验参数
        if (dto == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        Long articleId = dto.getArticleId();
        Short type = dto.getType();

        // 根据参数判断是不喜欢还是取消不喜欢
        if (type == 0) {
            // 不喜欢
            String key = BehaviorConstants.CACHE_BEHAVIOR_UNLIKE + articleId;
            cacheService.sAdd(key, userId.toString());
        } else {
            // 取消不喜欢
            String key = BehaviorConstants.CACHE_BEHAVIOR_UNLIKE + articleId;
            cacheService.sRemove(key, userId.toString());
        }

        return ResponseResult.okResult(true);
    }

    private Integer getCurUserId() {
        // 获取登录用户
        ApUser user = AppThreadLocalUtil.getUser();
        return user == null ? null : user.getId();
    }
}
