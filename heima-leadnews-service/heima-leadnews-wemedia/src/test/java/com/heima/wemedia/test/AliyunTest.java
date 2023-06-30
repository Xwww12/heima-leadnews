package com.heima.wemedia.test;

import com.heima.common.aliyun.GreenTextScan;
import com.heima.common.aliyun.ImageAutoRoute;
import com.heima.common.aliyun.TextModeration;
import com.heima.wemedia.WemediaApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Map;

@SpringBootTest(classes = WemediaApplication.class)
@RunWith(SpringRunner.class)
public class AliyunTest {

    @Resource
    private TextModeration textModeration;

    @Resource
    private ImageAutoRoute imageAutoRoute;

    /**
     * 测试文本内容
     */
    @Test
    public void testScanText() throws Exception {
        Map map = textModeration.greenTextScan("你好，冰毒");
        System.out.println(map);
    }

    /**
     * 测试图片审核
     */
    @Test
    public void testScanImage() throws Exception {
        // imageAutoRoute.imageScan("http://192.168.200.130:9000/leadnews/2023/06/27/3bd2e04ed8cc405896ca68b29d7c0428.png");
    }
}
