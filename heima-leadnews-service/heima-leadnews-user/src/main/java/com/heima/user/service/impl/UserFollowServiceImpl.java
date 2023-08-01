package com.heima.user.service.impl;

import com.heima.common.constants.BehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.UserRelationDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.mapper.UserFollowMapper;
import com.heima.user.service.UserFollowService;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
public class UserFollowServiceImpl implements UserFollowService {
    @Resource
    private UserFollowMapper userFollowMapper;

    @Resource
    private CacheService cacheService;

    @Override
    public ResponseResult follow(UserRelationDto dto) throws Exception {
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        Integer userId = user.getId();
        // 校验参数
        if (dto == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        Integer authorId = dto.getAuthorId();
        if (authorId == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        Short operation = dto.getOperation();

        Boolean res = null;
        if (operation == 0) {
            // 关注
            res = userFollowMapper.fan(userId, authorId) &&
                    userFollowMapper.follow(userId, authorId);
            if (!res)
                throw new Exception("关注失败");
            String key = BehaviorConstants.CACHE_BEHAVIOR_FOLLOW + authorId;
            cacheService.sAdd(key, userId.toString());
        } else {
            // 取关
            res = userFollowMapper.cancelFan(userId, authorId) &&
                    userFollowMapper.cancelFollow(userId, authorId);
            if (!res)
                throw new Exception("取关失败");
            String key = BehaviorConstants.CACHE_BEHAVIOR_FOLLOW + authorId;
            cacheService.sRemove(key, userId.toString());

        }

        return ResponseResult.okResult(res);
    }
}
