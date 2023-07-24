package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.HistorySearchDto;
import com.heima.model.search.dtos.UserSearchDto;

public interface ApUserSearchService {

    /**
     * 保存用户搜索历史记录
     * @param keyword
     * @param userId
     */
    void insert(String keyword,Integer userId);

    /**
     * 获取用户搜索历史
     * @return
     */
    ResponseResult findUserSearch();

    /**
     * 删除用户搜索历史
     * @return
     */
    ResponseResult delUserSearch(HistorySearchDto dto);
}