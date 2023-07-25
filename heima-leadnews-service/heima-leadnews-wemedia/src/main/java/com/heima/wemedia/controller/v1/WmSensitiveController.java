package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmChannelQueryDto;
import com.heima.model.wemedia.dtos.WmChannelUpdateDto;
import com.heima.model.wemedia.dtos.WmSensitiveQueryDto;
import com.heima.model.wemedia.dtos.WmSensitiveUpdateDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.wemedia.service.WmChannelService;
import com.heima.wemedia.service.WmSensitiveService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/sensitive")
public class WmSensitiveController {
    @Resource
    private WmSensitiveService wmSensitiveService;

    @PostMapping("/save")
    public ResponseResult save(@RequestBody WmSensitive wmSensitive) {
        return wmSensitiveService.saveSensitive(wmSensitive);
    }

    @PostMapping("/list")
    public ResponseResult list(@RequestBody WmSensitiveQueryDto dto) {
        return wmSensitiveService.sensitiveList(dto);
    }

    @PostMapping("/update")
    public ResponseResult update(@RequestBody WmSensitiveUpdateDto dto) {
        return wmSensitiveService.sensitiveUpdate(dto);
    }

    @DeleteMapping("/del/{id}")
    public ResponseResult del(@PathVariable("id") Integer id) {
        return wmSensitiveService.sensitiveDel(id);
    }
}
