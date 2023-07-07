package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@Slf4j
public class TaskServiceImpl implements TaskService {
    @Resource
    private TaskinfoMapper taskinfoMapper;

    @Resource
    private TaskinfoLogsMapper taskinfoLogsMapper;

    @Resource
    private CacheService cacheService;

    @Override
    public long addTask(Task task) {
        // 把任务保存到数据库中
        boolean isSuccess = addTaskToDb(task);

        // 任务保存到redis中
        if (isSuccess)
            addTaskToCache(task);
        else
            throw new RuntimeException("保存任务到数据库出错");       // 抛出异常用来回滚数据

        return task.getTaskId();
    }

    @Override
    public boolean cancelTask(long taskId) {
        // 从db中删除任务，更新日志
        Task task = updateDb(taskId, ScheduleConstants.CANCELLED);

        // 删除redis中任务
        if (task != null)
            removeTaskFromCache(task);
        else
            return false;

        return true;
    }

    @Override
    public Task poll(int type, int priority) {
        // 删除redis、db中任务，更新日志，返回任务
        Task task = null;
        try {
            String key = type + "_" + priority;
            String taskJson = cacheService.lRightPop(ScheduleConstants.TOPIC + key);
            if (StringUtils.isNotBlank(taskJson)) {
                task = JSON.parseObject(taskJson, Task.class);
                updateDb(task.getTaskId(), ScheduleConstants.EXECUTED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return task;
    }

    private void removeTaskFromCache(Task task) {
        String key = task.getTaskType() + "_" + task.getPriority();

        if (task.getExecuteTime() <= System.currentTimeMillis())
            cacheService.lRemove(ScheduleConstants.TOPIC + key, 0, JSON.toJSONString(task));
        else
            cacheService.lRemove(ScheduleConstants.FUTURE + key, 0, JSON.toJSONString(task));
    }

    private Task updateDb(long taskId, int status) {
        Task task = null;
        try {
            // 删除任务
            taskinfoMapper.deleteById(taskId);

            // 更新日志
            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
            taskinfoLogs.setStatus(status);
            taskinfoLogsMapper.updateById(taskinfoLogs);

            // 返回任务
            task = new Task();
            BeanUtils.copyProperties(taskinfoLogs, task);
            task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());
        } catch (BeansException e) {
            e.printStackTrace();
        }
        return task;
    }

    // 任务执行时间 <= 当前时间的任务保存到list中，
    // 任务执行时间 <= 当前时间 + 5分钟的任务保存到zSet中
    // 任务执行时间 > 当前时间 + 5分钟的任务先不保存
    private void addTaskToCache(Task task) {
        String key = task.getTaskType() + "_" + task.getPriority();
        long executeTime = task.getExecuteTime();

        // 后5分钟的毫秒值
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        long next5minTime = calendar.getTimeInMillis();

        if (executeTime <= System.currentTimeMillis())
            cacheService.lLeftPush(ScheduleConstants.TOPIC + key, JSON.toJSONString(task));
        else if (executeTime <= next5minTime)
            cacheService.zAdd(ScheduleConstants.FUTURE + key, JSON.toJSONString(task), task.getExecuteTime());
        // 大于5分钟的先不保存
    }

    // 保存任务信息和任务日志到db
    private boolean addTaskToDb(Task task) {
        try {
            Taskinfo taskinfo = new Taskinfo();
            BeanUtils.copyProperties(task, taskinfo);
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
            taskinfoMapper.insert(taskinfo);

            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            BeanUtils.copyProperties(taskinfo, taskinfoLogs);
            taskinfoLogs.setVersion(1);     // 乐观锁版本号
            taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);    // 任务状态为未执行
            taskinfoLogsMapper.insert(taskinfoLogs);

            //设置taskID，用于返回
            task.setTaskId(taskinfo.getTaskId());

            return true;
        } catch (BeansException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Scheduled(cron = "0 */1 * * * ?")
    public void refresh() {
        // 获取分布式锁
        String token = cacheService.tryLock("FUTURE_TASK_SYNC", 1000 * 30);
        if (StringUtils.isNotBlank(token)) {
            System.out.println(System.currentTimeMillis() / 1000 + "执行了任务");
            // 获取zSet中的任务的key
            Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
            System.out.println(futureKeys);
            for (String futureKey : futureKeys) {
                String topicKey = ScheduleConstants.TOPIC + futureKey.split(ScheduleConstants.FUTURE)[1];
                System.out.println(topicKey);
                // 获取key下所有的任务
                Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());

                if (!tasks.isEmpty()) {
                    // 放到对应的list中
                    cacheService.refreshWithPipeline(futureKey, topicKey, tasks);
                    System.out.println("成功将" + futureKey + "下的任务刷新到" + topicKey + "下");
                }
            }
        }
    }

    @Scheduled(cron = "0 */5 * * * ?")
    @PostConstruct
    public void reloadData() {
        // 清理缓存
        clearCache();
        log.info("数据库数据同步到缓存");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);

        //查看小于未来5分钟的所有任务
        List<Taskinfo> allTasks = taskinfoMapper.selectList(Wrappers.<Taskinfo>lambdaQuery().lt(Taskinfo::getExecuteTime, calendar.getTime()));
        if (allTasks != null && allTasks.size() > 0) {
            for (Taskinfo taskinfo : allTasks) {
                Task task = new Task();
                BeanUtils.copyProperties(taskinfo, task);
                task.setExecuteTime(taskinfo.getExecuteTime().getTime());
                addTaskToCache(task);
            }
        }
    }

    private void clearCache(){
        // 删除缓存中未来数据集合和当前消费者队列的所有key
        Set<String> futurekeys = cacheService.scan(ScheduleConstants.FUTURE + "*");// future_
        Set<String> topickeys = cacheService.scan(ScheduleConstants.TOPIC + "*");// topic_
        cacheService.delete(futurekeys);
        cacheService.delete(topickeys);
    }
}
