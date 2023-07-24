package com.heima.wemedia.controller.v1;


import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmChannelQueryDto;
import com.heima.model.wemedia.dtos.WmChannelUpdateDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.wemedia.service.WmChannelService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/channel")
public class WmChannelController {

    @Resource
    private WmChannelService wmChannelService;

    @GetMapping("/channels")
    public ResponseResult findAll() {
        return wmChannelService.findAll();
    }

    @PostMapping("/save")
    public ResponseResult save(@RequestBody WmChannel wmChannel) {
        return wmChannelService.saveChannel(wmChannel);
    }

    @PostMapping("/list")
    public ResponseResult list(@RequestBody WmChannelQueryDto dto) {
        return wmChannelService.channelList(dto);
    }

    @PostMapping("/update")
    public ResponseResult update(@RequestBody WmChannelUpdateDto dto) {
        return wmChannelService.channelUpdate(dto);
    }

    @GetMapping("/del/{id}")
    public ResponseResult del(@PathVariable("id") Integer id) {
        return wmChannelService.channelDel(id);
    }
}
