package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmLoginDto;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.AppJwtUtil;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.HashMap;

@Service
@Transactional
@Slf4j
public class WmUserServiceImpl extends ServiceImpl<WmUserMapper, WmUser> implements WmUserService {
    @Override
    public ResponseResult login(WmLoginDto wmLoginDto) {
        if (wmLoginDto == null)
            ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        String name = wmLoginDto.getName();
        String password = wmLoginDto.getPassword();
        if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(password)) {
            // 判断用户名密码
            WmUser wmUser = getOne(Wrappers.<WmUser>lambdaQuery().eq(WmUser::getName, name));
            if (wmUser == null)
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "用户不存在");
            String pwd = wmUser.getPassword();
            String salt = wmUser.getSalt();
            String passwords = DigestUtils.md5DigestAsHex((password + salt).getBytes());
            if (!passwords.equals(pwd))
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            // 返回用户数据
            wmUser.setSalt("");
            wmUser.setPassword("");
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", AppJwtUtil.getToken(wmUser.getId().longValue()));
            map.put("user", wmUser);
            return ResponseResult.okResult(map);
        }
        return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
    }
}
