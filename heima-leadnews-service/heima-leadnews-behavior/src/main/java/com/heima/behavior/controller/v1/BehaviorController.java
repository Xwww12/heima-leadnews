package com.heima.behavior.controller.v1;

import com.heima.behavior.service.BehaviorService;
import com.heima.model.behavior.dto.LikesBehaviorDto;
import com.heima.model.behavior.dto.ReadBehaviorDto;
import com.heima.model.behavior.dto.UnLikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1")
public class BehaviorController {
    @Resource
    private BehaviorService behaviorService;

    /**
     * 点赞/取消点赞
     * @param dto
     * @return
     */
    @PostMapping("/likes_behavior")
    public ResponseResult like(@RequestBody LikesBehaviorDto dto) {
        return behaviorService.like(dto);
    }

    /**
     * 增加文章阅读数
     * @param dto
     * @return
     */
    @PostMapping("/read_behavior")
    public ResponseResult read(@RequestBody ReadBehaviorDto dto) {
        return behaviorService.read(dto);
    }

    /**
     * 不喜欢/取消不喜欢文章
     * @param dto
     * @return
     */
    @PostMapping("/un_likes_behavior")
    public ResponseResult unLike(@RequestBody UnLikesBehaviorDto dto) {
        return behaviorService.unLike(dto);
    }
}
