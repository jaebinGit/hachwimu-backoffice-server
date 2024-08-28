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
        // 기존에 같은 taskId로 예약된 작업이 있는지 확인하고 제거
        cancelTask(taskId);

        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(task, triggerContext -> {
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