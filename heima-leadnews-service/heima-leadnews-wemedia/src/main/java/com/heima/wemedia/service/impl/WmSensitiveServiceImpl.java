package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmSensitiveQueryDto;
import com.heima.model.wemedia.dtos.WmSensitiveUpdateDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.service.WmSensitiveService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class WmSensitiveServiceImpl extends ServiceImpl<WmSensitiveMapper, WmSensitive> implements WmSensitiveService {
    @Resource
    private WmSensitiveMapper wmSensitiveMapper;

    @Override
    public ResponseResult saveSensitive(WmSensitive wmSensitive) {
        // 校验参数
        if (wmSensitive == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        String sensitives = wmSensitive.getSensitives();
        wmSensitive.setCreatedTime(new Date());
        if (StringUtils.isBlank(sensitives))
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        // 判断敏感词是否已存在
        LambdaQueryWrapper<WmSensitive> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(WmSensitive::getSensitives).eq(WmSensitive::getSensitives, sensitives);
        if (wmSensitiveMapper.selectOne(wrapper) != null)
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST, "敏感词已存在");

        // 保存频道
        boolean res = save(wmSensitive);
        return ResponseResult.okResult(res);
    }

    @Override
    public ResponseResult sensitiveList(WmSensitiveQueryDto dto) {
        // 参数校验
        if (dto == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        String name = dto.getName();
        int page = dto.getPage();
        int size = dto.getSize();
        page = Math.max(page, 0);
        size = Math.max(size, 10);

        // 条件查询
        LambdaQueryWrapper<WmSensitive> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(WmSensitive::getSensitives, name)
                .orderByDesc(WmSensitive::getCreatedTime);
        // 分页查询
        Page<WmSensitive> pageWrapper = new Page<>(page, size);

        // 查询
        page(pageWrapper, wrapper);

        return ResponseResult.okResult(pageWrapper.getRecords());
    }

    @Override
    public ResponseResult sensitiveUpdate(WmSensitiveUpdateDto dto) {
        // 校验参数
        if (dto == null || dto.getId() == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        Integer id = dto.getId();
        WmSensitive wmSensitive = wmSensitiveMapper.selectById(id);
        if (wmSensitive == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);

        // 更新
        BeanUtils.copyProperties(dto, wmSensitive);
        boolean res = updateById(wmSensitive);

        return ResponseResult.okResult(res);
    }

    @Override
    public ResponseResult sensitiveDel(Integer id) {
        // 参数校验
        if (id == null || id < 0)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        WmSensitive wmSensitive = wmSensitiveMapper.selectOne(Wrappers.<WmSensitive>query().eq("id", id));
        if (wmSensitive == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "敏感词不存在");

        // 删除
        boolean res = removeById(id);
        return ResponseResult.okResult(res);
    }
}
