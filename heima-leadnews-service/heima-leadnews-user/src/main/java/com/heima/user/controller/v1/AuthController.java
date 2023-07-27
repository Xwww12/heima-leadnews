package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.UserAuthDto;
import com.heima.model.user.dtos.UserListDto;
import com.heima.user.service.ApUserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户认证
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Resource
    private ApUserService apUserService;

    @PostMapping("/list")
    public ResponseResult list(@RequestBody UserListDto dto) {
        return apUserService.userList(dto);
    }

    @PostMapping("/authPass")
    public ResponseResult authPass(@RequestBody UserAuthDto dto) {
        return apUserService.authPass(dto);
    }

    @PostMapping("/authFail")
    public ResponseResult authFail(@RequestBody UserAuthDto dto) {
        return apUserService.authFail(dto);
    }
}
