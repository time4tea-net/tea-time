package net.time4tea.time.executors.test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;

public interface SimpleScheduledExecutorService {

    boolean isShutdown();

    <T> ScheduledFuture<T> schedule(Callable<T> callable, Duration delay);

    ScheduledFuture<?> schedule(Runnable runnable, Duration delay);

    ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, Duration initialDelay, Duration delay);

    ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, Duration initialDelay, Duration period);
}
