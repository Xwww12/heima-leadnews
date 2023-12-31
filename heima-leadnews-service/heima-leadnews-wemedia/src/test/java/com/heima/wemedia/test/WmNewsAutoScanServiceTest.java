package com.heima.wemedia.test;

import com.heima.wemedia.WemediaApplication;
import com.heima.wemedia.service.WmNewsAutoScanService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest(classes = WemediaApplication.class)
@RunWith(SpringRunner.class)
public class WmNewsAutoScanServiceTest {

    @Resource
    private WmNewsAutoScanService wmNewsAutoScanService;

    @Test
    public void test() throws Exception {
        wmNewsAutoScanService.autoScanWmNews(6234);
    }
}
