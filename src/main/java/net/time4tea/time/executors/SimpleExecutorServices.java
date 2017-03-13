package net.time4tea.time.executors;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SimpleExecutorServices {
    public static SimpleScheduledExecutorService wrapping(ScheduledExecutorService service) {
        return new SimpleScheduledExecutorService() {
            @Override
            public boolean isShutdown() {
                return service.isShutdown();
            }

            @Override
            public <T> ScheduledFuture<T> schedule(Callable<T> callable, Duration delay) {
                return service.schedule(callable, delay.toMillis(), TimeUnit.MILLISECONDS);
            }

            @Override
            public ScheduledFuture<?> schedule(Runnable runnable, Duration delay) {
                return service.schedule(runnable, delay.toMillis(), TimeUnit.MILLISECONDS);
            }

            @Override
            public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, Duration initialDelay, Duration delay) {
                return service.scheduleWithFixedDelay(runnable, initialDelay.toMillis(), delay.toMillis(), TimeUnit.MILLISECONDS);
            }

            @Override
            public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, Duration initialDelay, Duration period) {
                return service.scheduleAtFixedRate(runnable, initialDelay.toMillis(), period.toMillis(), TimeUnit.MILLISECONDS);
            }
        };
    }
}
