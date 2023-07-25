package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmChannelQueryDto;
import com.heima.model.wemedia.dtos.WmChannelUpdateDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.service.WmChannelService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class WmChannelServiceImpl extends ServiceImpl<WmChannelMapper, WmChannel> implements WmChannelService {
    @Resource
    private WmChannelMapper wmChannelMapper;

    @Override
    public ResponseResult findAll() {
        return ResponseResult.okResult(list());
    }

    @Override
    public ResponseResult saveChannel(WmChannel wmChannel) {
        // 校验参数
        if (wmChannel == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        String name = wmChannel.getName();
        Boolean status = wmChannel.getStatus();
        wmChannel.setCreatedTime(new Date());
        if (StringUtils.isBlank(name) ||
            status == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        // 判断频道名称是否已存在
        LambdaQueryWrapper<WmChannel> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(WmChannel::getName).eq(WmChannel::getName, name);
        if (wmChannelMapper.selectOne(wrapper) != null)
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST, "频道名称已存在");

        // 保存频道
        boolean res = save(wmChannel);
        return ResponseResult.okResult(res);
    }

    @Override
    public ResponseResult channelList(WmChannelQueryDto dto) {
        // 参数校验
        if (dto == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        String name = dto.getName();
        int page = dto.getPage();
        int size = dto.getSize();
        Boolean status = dto.getStatus();
        page = Math.max(page, 0);
        size = Math.max(size, 10);

        // 条件查询
        LambdaQueryWrapper<WmChannel> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(WmChannel::getName, name)
                // .eq(WmChannel::getStatus, status)
                .orderByDesc(WmChannel::getCreatedTime);
        // 分页查询
        Page<WmChannel> pageWrapper = new Page<>(page, size);

        // 查询
        page(pageWrapper, wrapper);

        return ResponseResult.okResult(pageWrapper.getRecords());
    }

    @Override
    public ResponseResult channelUpdate(WmChannelUpdateDto dto) {
        // 校验参数
        if (dto == null || dto.getId() == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        Integer id = dto.getId();
        Integer ord = dto.getOrd();
        WmChannel wmChannel = wmChannelMapper.selectById(id);
        if (wmChannel == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        ord = Math.max(ord, 0);

        // 更新
        BeanUtils.copyProperties(dto, wmChannel);
        boolean res = updateById(wmChannel);

        return ResponseResult.okResult(res);
    }

    @Override
    public ResponseResult channelDel(Integer id) {
        // 参数校验
        if (id == null || id < 0)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        WmChannel wmChannel = wmChannelMapper.selectOne(Wrappers.<WmChannel>query().eq("id", id));
        if (wmChannel == null || wmChannel.getStatus())
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "频道不存在或正在启用");

        // 删除
        boolean res = removeById(id);
        return ResponseResult.okResult(res);
    }
}
