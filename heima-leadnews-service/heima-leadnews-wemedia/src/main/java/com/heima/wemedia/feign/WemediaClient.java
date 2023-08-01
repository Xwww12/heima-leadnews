package com.heima.wemedia.feign;

import com.heima.apis.wemedia.IWemediaClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.service.WmChannelService;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class WemediaClient implements IWemediaClient {
    @Resource
    private WmChannelService wmChannelService;

    @Override
    public ResponseResult getChannels() {
        return wmChannelService.findAll();
    }
}
