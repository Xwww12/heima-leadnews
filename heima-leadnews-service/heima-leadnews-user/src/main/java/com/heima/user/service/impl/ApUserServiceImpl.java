package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.dtos.UserAuthDto;
import com.heima.model.user.dtos.UserListDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.ApUserRealname;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.mapper.ApUserRealnameMapper;
import com.heima.user.service.ApUserService;
import com.heima.utils.common.AppJwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService {
    @Resource
    private ApUserRealnameMapper apUserRealnameMapper;

    @Override
    public ResponseResult login(LoginDto loginDto) {
        String phone = loginDto.getPhone();
        String password = loginDto.getPassword();
        // 如果有手机号和密码则为用户登录，否则为游客登录
        if (StringUtils.isNotBlank(phone) && StringUtils.isNotBlank(password)) {
            // 判断用户是否存在
            ApUser apUser = getOne(Wrappers.<ApUser>lambdaQuery().eq(ApUser::getPhone, phone));
            if (apUser == null)
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "用户不存在");
            // 判断密码是否正确
            String pwd = apUser.getPassword();
            String salt = apUser.getSalt();
            String passwords = DigestUtils.md5DigestAsHex((password + salt).getBytes());
            if (!passwords.equals(pwd))
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            // 返回用户信息
            apUser.setSalt("");
            apUser.setPassword("");
            Map<String, Object> map = new HashMap<>();
            map.put("token", AppJwtUtil.getToken(apUser.getId().longValue()));
            map.put("user", apUser);
            return ResponseResult.okResult(map);
        } else {
            Map<String, Object> map = new HashMap<>();
            // 没有手机号，默认使用0来生成token
            map.put("token", AppJwtUtil.getToken(0L));
            return ResponseResult.okResult(map);
        }
    }

    @Override
    public ResponseResult userList(UserListDto dto) {
        // 校验参数
        if (dto == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        Integer page = dto.getPage();
        Integer size = dto.getSize();
        Integer status = dto.getStatus();
        if (page == null) page = 0;
        if (size == null) size = 0;
        page = Math.max(page, 0);
        size = Math.max(size, 10);

        // 条件
        LambdaQueryWrapper<ApUserRealname> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, ApUserRealname::getStatus, status);
        // 分页
        Page<ApUserRealname> pageWrapper = new Page<>(page, size);
        // 查询
        Page<ApUserRealname> apUserRealnamePage = apUserRealnameMapper.selectPage(pageWrapper, wrapper);

        return ResponseResult.okResult(apUserRealnamePage.getRecords());
    }

    @Override
    public ResponseResult authPass(UserAuthDto dto) {
        // 参数校验
        if (dto == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        Integer id = dto.getId();
        if (id == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        // 查找用户是否存在
        ApUserRealname apUserRealname = apUserRealnameMapper.selectOne(Wrappers.<ApUserRealname>query().eq("id", id));
        if (apUserRealname == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "用户不存在");

        // 修改审核状态为通过
        apUserRealname.setStatus((short) 9);
        int count = apUserRealnameMapper.updateById(apUserRealname);

        return ResponseResult.okResult(count == 1);
    }

    @Override
    public ResponseResult authFail(UserAuthDto dto) {
        // 参数校验
        if (dto == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        Integer id = dto.getId();
        String msg = dto.getMsg();      // 驳回理由，暂时没用到
        if (id == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        // 查找用户是否存在
        ApUserRealname apUserRealname = apUserRealnameMapper.selectOne(Wrappers.<ApUserRealname>query().eq("id", id));
        if (apUserRealname == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "用户不存在");

        // 修改审核状态为失败
        apUserRealname.setStatus((short) 2);
        int count = apUserRealnameMapper.updateById(apUserRealname);

        return ResponseResult.okResult(count == 1);
    }
}
