package net.strocamp.hugo.wbfscoringscreen.scheduler;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    private ConcurrentHashMap<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();
    private ScheduledThreadPoolExecutor executor;

    public void onCreate() {
        executor = new ScheduledThreadPoolExecutor(2);
    }

    public void onDestroy() {
        executor.shutdownNow();
    }

    public String submit(Runnable runnable, int time, TimeUnit timeUnit) {
        String taskId = UUID.randomUUID().toString();

        ScheduledFuture<?> future = executor.scheduleWithFixedDelay(runnable, 0, time, timeUnit);
        tasks.put(taskId, future);

        return taskId;
    }

    public void remove(String taskId) {
        if (tasks.contains(taskId)) {
            ScheduledFuture<?> task = tasks.get(taskId);
            task.cancel(false);
        }
        tasks.remove(taskId);
    }
}
