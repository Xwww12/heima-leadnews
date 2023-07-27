package com.heima.user.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.UserRelationDto;

public interface UserFollowService {
    /**
     * 关注/取关
     * @param dto
     * @return
     */
    ResponseResult follow(UserRelationDto dto) throws Exception;
}
