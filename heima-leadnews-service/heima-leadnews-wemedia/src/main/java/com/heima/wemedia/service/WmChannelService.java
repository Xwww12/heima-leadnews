package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmChannelQueryDto;
import com.heima.model.wemedia.dtos.WmChannelUpdateDto;
import com.heima.model.wemedia.pojos.WmChannel;

public interface WmChannelService extends IService<WmChannel> {

    /**
     * 查询所有频道
     * @return
     */
    public ResponseResult findAll();

    /**
     * 保存频道
     * @param wmChannel
     * @return
     */
    ResponseResult saveChannel(WmChannel wmChannel);

    /**
     * 条件查询
     * @param dto
     * @return
     */
    ResponseResult channelList(WmChannelQueryDto dto);

    /**
     * 修改
     * @param dto
     * @return
     */
    ResponseResult channelUpdate(WmChannelUpdateDto dto);

    /**
     * 删除
     * @param id
     * @return
     */
    ResponseResult channelDel(Integer id);
}