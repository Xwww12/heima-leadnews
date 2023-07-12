package com.heima.kafka.controller;

import com.alibaba.fastjson.JSON;
import com.heima.kafka.pojo.User;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class HelloController {

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @GetMapping("/hello")
    public String hello() {
        // 发送消息到'test-topic'
        User user = new User();
        user.setName("zs");
        user.setAge(20);
        kafkaTemplate.send("test-topic", JSON.toJSONString(user));
        return "ok";
    }
}
