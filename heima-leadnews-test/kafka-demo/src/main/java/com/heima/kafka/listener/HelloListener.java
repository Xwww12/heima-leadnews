package com.heima.kafka.listener;

import com.alibaba.fastjson.JSON;
import com.heima.kafka.pojo.User;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class HelloListener {

    @KafkaListener(topics = {"test-topic"})
    public void onMessage(String message) {
        System.out.println(JSON.parseObject(message, User.class));
    }
}
