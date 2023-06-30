package com.heima.wemedia.service;

import org.springframework.stereotype.Service;

public interface WmNewsAutoScanService {

    /**
     * 自动审核文章内容
     * @param id
     */
    void autoScanWmNews(Integer id);
}
