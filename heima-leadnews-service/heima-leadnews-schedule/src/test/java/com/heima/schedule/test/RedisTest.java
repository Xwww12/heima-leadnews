package com.heima.schedule.test;

import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.ScheduleApplication;
import com.heima.schedule.service.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Set;

@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class RedisTest {

    @Resource
    private CacheService cacheService;

    @Resource
    private TaskService taskService;

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

    @Test
    public void test3() {
        for (int i = 1; i <= 5; i++) {
            Task task = new Task();
            task.setTaskType(100);
            task.setPriority(50 + i);
            task.setParameters("asdad".getBytes());
            task.setExecuteTime(new Date().getTime() + 500 * i);

            long taskId = taskService.addTask(task);
        }
    }

    @Test
    public void test4() {
        boolean result = taskService.cancelTask(1676856100115775490L);
        System.out.println(result);
    }

    @Test
    public void test5() {
        Task task = taskService.poll(100, 50);
        System.out.println(task);
    }
}
