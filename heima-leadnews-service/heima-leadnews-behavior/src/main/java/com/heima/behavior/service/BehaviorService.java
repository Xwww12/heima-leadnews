package com.heima.behavior.service;

import com.heima.model.behavior.dto.LikesBehaviorDto;
import com.heima.model.behavior.dto.ReadBehaviorDto;
import com.heima.model.behavior.dto.UnLikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;

public interface BehaviorService {
    /**
     * 文章点赞
     * @param dto
     * @return
     */
    ResponseResult like(LikesBehaviorDto dto);

    /**
     * 记录文章阅读数
     * @param dto
     * @return
     */
    ResponseResult read(ReadBehaviorDto dto);

    /**
     * 文章不喜欢
     * @param dto
     * @return
     */
    ResponseResult unLike(UnLikesBehaviorDto dto);
}
