package com.heima.admin.controller.v1;

import com.heima.admin.service.AdminService;
import com.heima.model.admin.dtos.LoginDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/login")
public class AdminLoginController {

    @Resource
    private AdminService adminService;

    @PostMapping("/in")
    public ResponseResult login(@RequestBody LoginDto dto) {
        return adminService.login(dto);
    }
}
