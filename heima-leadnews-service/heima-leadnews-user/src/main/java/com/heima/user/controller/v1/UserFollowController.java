package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.UserRelationDto;
import com.heima.user.service.UserFollowService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/user")
public class UserFollowController {

    @Resource
    private UserFollowService userFollowService;

    @PostMapping("/user_follow")
    public ResponseResult follow(@RequestBody UserRelationDto dto) throws Exception {
        return userFollowService.follow(dto);
    }
}
