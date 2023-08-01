package com.heima.apis.wemedia;

import com.heima.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("leadnews-wemedia")
public interface IWemediaClient {

    /**
     * 获取所有频道
     * @return
     */
    @GetMapping("/api/v1/channel/list")
    public ResponseResult getChannels();
}
