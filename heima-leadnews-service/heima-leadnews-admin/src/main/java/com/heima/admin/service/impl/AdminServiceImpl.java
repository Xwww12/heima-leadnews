package com.heima.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.admin.mapper.AdUserMapper;
import com.heima.admin.service.AdminService;
import com.heima.model.admin.dtos.LoginDto;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.AppJwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.HashMap;

@Service
@Transactional
@Slf4j
public class AdminServiceImpl extends ServiceImpl<AdUserMapper, AdUser> implements AdminService {
    @Override
    public ResponseResult login(LoginDto dto) {
        if (dto == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        String name = dto.getName();
        String password = dto.getPassword();
        if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(password)) {
            // 判断用户名密码
            AdUser adUser = getOne(Wrappers.<AdUser>lambdaQuery().eq(AdUser::getName, name));
            if (adUser == null)
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "用户不存在");
            String pwd = adUser.getPassword();
            String salt = adUser.getSalt();
            String passwords = DigestUtils.md5DigestAsHex((password + salt).getBytes());
            if (!passwords.equals(pwd))
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            // 返回用户数据
            adUser.setSalt("");
            adUser.setPassword("");
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", AppJwtUtil.getToken(adUser.getId().longValue()));
            map.put("user", adUser);
            return ResponseResult.okResult(map);
        }
        return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
    }
}
