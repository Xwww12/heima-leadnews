package com.heima.schedule.service;

import com.heima.model.schedule.dtos.Task;

/**
 * 对外访问接口
 */
public interface TaskService {

    /**
     * 添加任务
     * @param task   任务对象
     * @return       任务id
     */
    public long addTask(Task task);

    /**
     * 删除任务
     * @param taskId
     * @return
     */
    public boolean cancelTask(long taskId);

    /**
     * 消费任务
     * @param type
     * @param priority
     * @return
     */
    public Task poll(int type, int priority);

}