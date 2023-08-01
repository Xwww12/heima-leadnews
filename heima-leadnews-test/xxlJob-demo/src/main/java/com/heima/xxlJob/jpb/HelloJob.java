package com.heima.xxlJob.jpb;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HelloJob {

    @Value("${xxl.port}")
    private String val;

    @XxlJob("demoJobHandler")
    public void hello() {
        System.out.println(val);
    }

    @XxlJob("demoSharding")
    public void shardingJobHandler() {
        // 分片的参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        //业务逻辑
        List<Integer> list = getList();
        for (Integer integer : list) {
            if (integer % shardTotal == shardIndex) {
                System.out.println("当前第" + shardIndex + "分片执行了，任务项为：" + integer);
            }
        }
    }

    public List<Integer> getList() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            list.add(i);
        }
        return list;
    }
}
