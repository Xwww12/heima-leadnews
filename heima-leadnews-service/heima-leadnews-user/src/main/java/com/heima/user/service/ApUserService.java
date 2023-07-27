package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.dtos.UserAuthDto;
import com.heima.model.user.dtos.UserListDto;
import com.heima.model.user.pojos.ApUser;

public interface ApUserService extends IService<ApUser> {
    ResponseResult login(LoginDto loginDto);

    /**
     * 查询用户列表
     * @param dto
     */
    ResponseResult userList(UserListDto dto);

    /**
     * 用户认证通过
     * @param dto
     * @return
     */
    ResponseResult authPass(UserAuthDto dto);

    /**
     * 用户认证失败
     * @param dto
     * @return
     */
    ResponseResult authFail(UserAuthDto dto);
}
