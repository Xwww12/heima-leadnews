package com.heima.kafka.sample;

import org.apache.kafka.clients.producer.*;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

// 生产者
public class ProducerQuickStart {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //1.kafka的配置信息
        Properties properties = new Properties();
        //kafka的连接地址
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"192.168.200.130:9092");

        //发送失败，失败的重试次数
        properties.put(ProducerConfig.RETRIES_CONFIG,5);
        // 应答方式 0： 不需要应答 1： 主节点收到后应答（默认） 2： 同步给所有从节点后应答
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        // 压缩方式
        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        //消息key的序列化器
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        //消息value的序列化器
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");

        // 生产者
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // 消息
        ProducerRecord<String, String> record = new ProducerRecord<>("topic001","key001","hello kafka");

        // 发送消息
        producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if (e != null)
                    e.printStackTrace();
                System.out.println("偏移量：" + recordMetadata.offset());
            }
        });


        // 关闭消息通道，消息才能发送出去
        producer.close();


    }
}
