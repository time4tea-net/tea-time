package net.time4tea.time.executors.test;

import net.time4tea.time.executors.SimpleScheduledExecutorService;

import java.time.Duration;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.*;

import static java.util.Comparator.comparingLong;

public class ControllableSimpleScheduledExecutorService implements SimpleScheduledExecutorService {

    private long clock = 0L;
    private boolean isShutdown = false;
    private Queue<SimpleScheduleTask> tasks = emptyTaskList();

    private Queue<SimpleScheduleTask> emptyTaskList() {
        return new PriorityQueue<>(comparingLong(tasks -> tasks.getDelay(TimeUnit.MILLISECONDS)));
    }

    private class SimpleScheduleTask<T> implements ScheduledFuture<T> {
        private final Callable<T> callable;
        private boolean isCancelled = false;
        private boolean isDone = false;
        private final Duration period;
        private final long timeToRun;
        private T result;
        private Throwable error;

        public SimpleScheduleTask(Callable<T> callable, Duration period, long timeToRun) {
            this.callable = callable;
            this.period = period;
            this.timeToRun = timeToRun;
        }

        public SimpleScheduleTask(Callable<T> callable, long timeToRun) {
            this(callable, null, timeToRun);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(timeToRun - clock, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            throw new UnsupportedOperationException("james didn't write");
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            isCancelled = true;
            return true;
        }

        @Override
        public boolean isCancelled() {
            return isCancelled;
        }

        @Override
        public boolean isDone() {
            return isDone;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            if (isCancelled) throw new CancellationException("get() on cancelled task");
            if (error != null) throw new ExecutionException(error);
            if (isDone) return result;
            throw new IllegalStateException("get() before task runs");
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            if (isCancelled) throw new CancellationException("get(timeout) on cancelled task");
            if (error != null) throw new ExecutionException(error);
            if (isDone) return result;
            throw new TimeoutException("task not scheduled to run for another " + Duration.ofMillis(
                    timeToRun - clock
            ));
        }

        private void execute() {
            try {
                if (!isCancelled) {
                    result = callable.call();
                }
            } catch (Exception e) {
                error = e;
            } finally {
                isDone = true;
            }
        }

        public boolean isPeriodic() {
            return period != null;
        }

        public SimpleScheduleTask<T> atNextExecutionTimeAfter(long clock) {
            return new SimpleScheduleTask<>(
                    callable, period, clock + period.toMillis()
            );
        }
    }

    @Override
    public boolean isShutdown() {
        return isShutdown;
    }

    @Override
    public <T> ScheduledFuture<T> schedule(Callable<T> callable, Duration delay) {
        return enqueue(new SimpleScheduleTask<>(
                callable, clock + delay.toMillis()
        ));
    }

    private <T> ScheduledFuture<T> enqueue(SimpleScheduleTask<T> task) {
        if (!isShutdown) tasks.add(task);
        return task;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable runnable, Duration delay) {
        return enqueue(new SimpleScheduleTask<>(
                () -> {
                    runnable.run();
                    return null;
                }, clock + delay.toMillis()
        ));
    }

    /**
     * this doesn't really work quite the same as real life, as we don't know how much to add for the delay...
     */
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, Duration initialDelay, Duration delay) {
        return enqueue(new SimpleScheduleTask<>(
                () -> {
                    runnable.run();
                    return null;
                }, delay, clock + initialDelay.toMillis()
        ));
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, Duration initialDelay, Duration period) {
        return enqueue(new SimpleScheduleTask<>(
                () -> {
                    runnable.run();
                    return null;
                }, period, clock + initialDelay.toMillis()
        ));
    }

    public void shutdown() {
        isShutdown = true;
    }

    public void timePasses(Duration duration) {

        long durationMillis = duration.toMillis();

        while (true) {
            if (!(runNextTask(clock + durationMillis))) break;
        }

        clock += durationMillis;
    }

    private boolean runNextTask(long endOfPeriod) {

        Queue<SimpleScheduleTask> nextTasks = emptyTaskList();
        boolean ranSomething = false;

        for (SimpleScheduleTask task : tasks) {
            if ( task.isCancelled( ) ) continue;
            long executionTimeOfTask = task.timeToRun;
            if ( executionTimeOfTask <= endOfPeriod) {
                ranSomething = true;
                task.execute();
                if (task.isPeriodic()) {
                    nextTasks.add(task.atNextExecutionTimeAfter(executionTimeOfTask));
                }
            }
            else {
                nextTasks.add(task);
            }
        }
        tasks = nextTasks;
        return ranSomething;
    }
}
