package com.heima.user;

import com.heima.user.mapper.UserFollowMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MapperTest {
    @Resource
    private UserFollowMapper mapper;

    @Test
    public void test() {
        mapper.follow(1, 2);
    }
}
