package net.time4tea.time.executors.test;

import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ControllableSimpleScheduledExecutorServiceTest {

    private ControllableSimpleScheduledExecutorService service = new ControllableSimpleScheduledExecutorService();
    private AtomicInteger counter = new AtomicInteger();

    @Test
    public void isntShutdownUntilItIs() throws Exception {
        assertThat(service.isShutdown(), equalTo(false));
        service.shutdown();
        assertThat(service.isShutdown(), equalTo(true));
    }

    @Test
    public void tasksSubmittedAfterShutdownAreIgnored() throws Exception {
        service.shutdown();
        service.schedule(() -> counter.incrementAndGet(), Duration.ofSeconds(1));
        service.timePasses(Duration.ofSeconds(2));
        assertThat(counter.get(), equalTo(0));
    }

    @Test
    public void runsScheduledTasksThatAreSubmittedAtTheCorrectTime() throws Exception {
        assertThat(counter.get(), equalTo(0));
        service.schedule(() -> counter.incrementAndGet(), Duration.ofSeconds(1));
        assertThat(counter.get(), equalTo(0));
        service.timePasses(Duration.ofSeconds(1));
        assertThat(counter.get(), equalTo(1));
    }

    @Test
    public void cancellingAScheduledTaskBeforeItRunsWillCancelIt() throws Exception {
        ScheduledFuture<Integer> future = service.schedule(() -> counter.incrementAndGet(), Duration.ofSeconds(1));
        future.cancel(true);
        service.timePasses(Duration.ofSeconds(1));
        assertThat(counter.get(), equalTo(0));
        assertThat(future.isCancelled(), equalTo(true));
    }

    @Test(expected = CancellationException.class)
    public void gettingCancelledFuture() throws Exception {
        ScheduledFuture<Integer> future = service.schedule(() -> counter.incrementAndGet(), Duration.ofSeconds(1));
        future.cancel(true);
        future.get();
    }

    @Test(expected = CancellationException.class)
    public void gettingCancelledFutureWithTimeout() throws Exception {
        ScheduledFuture<Integer> future = service.schedule(() -> counter.incrementAndGet(), Duration.ofSeconds(1));
        future.cancel(true);
        future.get(1, TimeUnit.MILLISECONDS);
    }

    @Test
    public void canRetrieveTheResultOnceTheTaskHasRun() throws Exception {
        ScheduledFuture<Integer> future = service.schedule(() -> counter.incrementAndGet(), Duration.ofSeconds(1));
        service.timePasses(Duration.ofSeconds(1));
        assertThat(counter.get(), equalTo(1));
        assertThat(future.get(), equalTo(1));
        assertThat(future.get(1, TimeUnit.MILLISECONDS), equalTo(1));
        assertThat(future.isDone(), equalTo(true));
    }

    @Test(expected = IllegalStateException.class)
    public void retrievingResultBeforeTaskHasRunThrows() throws Exception {
        ScheduledFuture<Integer> future = service.schedule(() -> counter.incrementAndGet(), Duration.ofSeconds(1));
        future.get();
    }

    @Test(expected = TimeoutException.class)
    public void retrievingResultWithTimeoutBeforeTaskRuns() throws Exception {
        ScheduledFuture<Integer> future = service.schedule(() -> counter.incrementAndGet(), Duration.ofSeconds(1));
        future.get(1, TimeUnit.MILLISECONDS);
    }

    @Test
    public void taskThrowingExceptionStillRunsOtherTasks() throws Exception {
        service.schedule(() -> {throw new NullPointerException();}, Duration.ofSeconds(1));
        service.schedule(() -> counter.incrementAndGet(), Duration.ofSeconds(1));
        service.timePasses(Duration.ofSeconds(1));
        assertThat(counter.get(), equalTo(1));
    }

    @Test(expected = ExecutionException.class)
    public void retrievingResultOfFailedTask() throws Exception {
        ScheduledFuture<?> future = service.schedule(() -> {
            throw new NullPointerException();
        }, Duration.ofSeconds(1));
        service.timePasses(Duration.ofSeconds(1));

        future.get();
    }

    @Test(expected = ExecutionException.class)
    public void retrievingResultOfFailedTaskWithTimeout() throws Exception {
        ScheduledFuture<?> future = service.schedule(() -> {
            throw new NullPointerException();
        }, Duration.ofSeconds(1));
        service.timePasses(Duration.ofSeconds(1));

        future.get(1, TimeUnit.MILLISECONDS);
    }

    public static class ControllableSimpleScheduledExecutorService implements SimpleScheduledExecutorService {

        private long clock = 0L;
        private boolean isShutdown = false;
        private List<SimpleScheduleTask> tasks = new ArrayList<>();

        private class SimpleScheduleTask<T> implements ScheduledFuture<T> {
            private final Callable<T> callable;
            private boolean isCancelled = false;
            private boolean isDone = false;
            private final Duration delay;
            private final long timeToRun;
            private T result;
            private Throwable error;

            public SimpleScheduleTask(Callable<T> callable, Duration delay, long timeToRun) {
                this.callable = callable;
                this.delay = delay;
                this.timeToRun = timeToRun;
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
                if ( isCancelled ) throw new CancellationException("get() on cancelled task");
                if ( error != null ) throw new ExecutionException(error);
                if ( isDone ) return result;
                throw new IllegalStateException("get() before task runs");
            }

            @Override
            public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                if ( isCancelled ) throw new CancellationException("get(timeout) on cancelled task");
                if ( error != null ) throw new ExecutionException(error);
                if ( isDone ) return result;
                throw new TimeoutException("task not scheduled to run for another " + Duration.ofMillis(
                        timeToRun - clock
                ));
            }

            public void execute() {
                try {
                    if (! isCancelled) {
                        result = callable.call();
                    }
                } catch (Exception e) {
                    error = e;
                }
                finally {
                    isDone = true;
                }
            }
        }

        @Override
        public boolean isShutdown() {
            return isShutdown;
        }

        @Override
        public <T> ScheduledFuture<T> schedule(Callable<T> callable, Duration delay) {
            return enqueue(new SimpleScheduleTask<>(
                    callable, delay, clock + delay.toMillis()
            ));
        }

        private <T> ScheduledFuture<T> enqueue(SimpleScheduleTask<T> task) {
            if ( ! isShutdown) tasks.add(task);
            return task;
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable runnable, Duration delay) {
            return null;
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, Duration initialDelay, Duration delay) {
            throw new UnsupportedOperationException("james didn't write");
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, Duration initialDelay, Duration period) {
            throw new UnsupportedOperationException("james didn't write");
        }

        public void shutdown() {
            isShutdown = true;
        }

        public void timePasses(Duration duration) {
            clock += duration.toMillis();
            runPendingTasks();
        }

        private void runPendingTasks() {
            for (SimpleScheduleTask task : tasks) {
                if (task.timeToRun <= clock) {
                    try {
                        task.execute();
                    } catch (Exception e) {

                    }
                }
            }
        }
    }
}
