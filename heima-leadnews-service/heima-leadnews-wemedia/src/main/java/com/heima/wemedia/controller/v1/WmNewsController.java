package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.UserAuthDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsListDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.wemedia.service.WmNewsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController {
    @Resource
    private WmNewsService wmNewsService;

    @PostMapping("/list")
    public ResponseResult findAll(@RequestBody WmNewsPageReqDto dto){
        return  wmNewsService.findAll(dto);
    }

    @PostMapping("/submit")
    public ResponseResult submitNews(@RequestBody WmNewsDto dto) {
        return wmNewsService.submitNews(dto);
    }

    @GetMapping("/one/{id}")
    public ResponseResult getOne(@PathVariable("id") Integer id) {
        return wmNewsService.getDetail(id);
    }

    @GetMapping("/del_news/{id}")
    public ResponseResult deleteNews(@PathVariable("id") Integer id) {
        return wmNewsService.deleteNews(id);
    }

    @PostMapping("/down_or_up")
    public ResponseResult downOrUpNews(@RequestBody WmNewsDto dto) {
        return wmNewsService.downOrUpNews(dto);
    }

    @PostMapping("list_vo")
    public ResponseResult listVo(@RequestBody WmNewsListDto dto) {
        return wmNewsService.listVo(dto);
    }

    @GetMapping("one_vo/{id}")
    public ResponseResult oneVo(@PathVariable("id") Integer id) {
        return wmNewsService.getDetail(id);
    }

    @PostMapping("/auth_pass")
    public ResponseResult authPass(@RequestBody UserAuthDto dto) {
        return wmNewsService.authPass(dto);
    }

    @PostMapping("/auth_fail")
    public ResponseResult authFail(@RequestBody UserAuthDto dto) {
        return wmNewsService.authFail(dto);
    }
}