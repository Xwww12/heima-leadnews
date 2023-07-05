package com.heima.schedule.test;

import com.heima.common.redis.CacheService;
import com.heima.schedule.ScheduleApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Set;

@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class RedisTest {

    @Resource
    private CacheService cacheService;

    @Test
    public void test1() {
        cacheService.lLeftPush("list_001", "hello ");
        cacheService.lRightPush("list_001", "world");
    }

    @Test
    public void test2() {
        cacheService.zAdd("zset_001", "a", 1000);
        cacheService.zAdd("zset_001", "b", 2000);
        cacheService.zAdd("zset_001", "c", 3000);

        Set<String> set = cacheService.zRangeByScore("zset_001", 1000, 2000);
        System.out.println(set);    // [a, b]
    }
}
