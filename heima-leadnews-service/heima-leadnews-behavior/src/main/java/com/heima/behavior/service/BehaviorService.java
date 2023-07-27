package com.heima.behavior.service;

import com.heima.model.behavior.ArticleInfoDto;
import com.heima.model.behavior.LikesBehaviorDto;
import com.heima.model.behavior.ReadBehaviorDto;
import com.heima.model.behavior.UnLikesBehaviorDto;
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
