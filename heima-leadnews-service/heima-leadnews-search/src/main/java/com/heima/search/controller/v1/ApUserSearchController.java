package com.heima.search.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.HistorySearchDto;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.service.ApUserSearchService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/history")
public class ApUserSearchController {

    @Resource
    private ApUserSearchService apUserSearchService;

    /**
     * 查找用户搜索历史
     * @return
     */
    @PostMapping("/load")
    public ResponseResult findUserSearch() {
        return apUserSearchService.findUserSearch();
    }


    /**
     * 删除搜索历史
     * @return
     */
    @PostMapping("/del")
    public ResponseResult delUserSearch(@RequestBody HistorySearchDto dto) {
        return apUserSearchService.delUserSearch(dto);
    }
}
