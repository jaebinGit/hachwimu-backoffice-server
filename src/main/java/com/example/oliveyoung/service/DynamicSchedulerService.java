package com.example.oliveyoung.service;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class DynamicSchedulerService {

    private final TaskScheduler taskScheduler;
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public DynamicSchedulerService() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.initialize();
        this.taskScheduler = scheduler;
    }

    public void scheduleTask(String taskId, Runnable task, long delayInMillis) {
        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(task, triggerContext -> {
            // 현재 시간에서 지정된 지연을 추가하여 다음 실행 시간 계산
            return new Date(System.currentTimeMillis() + delayInMillis).toInstant();
        });
        scheduledTasks.put(taskId, scheduledFuture);
    }

    public void cancelTask(String taskId) {
        ScheduledFuture<?> scheduledTask = scheduledTasks.remove(taskId);
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }
    }
}