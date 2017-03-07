package net.time4tea.time.executors.test;

import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

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
    public void tasksAreRunAtTheRightTime() throws Exception {
        service.schedule(() -> counter.incrementAndGet(), Duration.ofSeconds(1));
        assertThat(counter.get(), equalTo(0));
        service.timePasses(Duration.ofMillis(999));
        assertThat(counter.get(), equalTo(0));
        service.timePasses(Duration.ofMillis(1));
        assertThat(counter.get(), equalTo(1));
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
    public void schedulingARunnableRunsItAtTheRightTime() throws Exception {
        Runnable runnable = () -> counter.incrementAndGet();
        ScheduledFuture<?> future = service.schedule(runnable, Duration.ofDays(1));
        assertThat(counter.get(), equalTo(0));
        service.timePasses(Duration.ofDays(1));
        assertThat(counter.get(), equalTo(1));
        assertThat(future.get(), nullValue());
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

    @Test
    public void schedulingATaskAtFixedRateWillRunItAfterInitialDuration() throws Exception {
        service.scheduleAtFixedRate(() -> counter.incrementAndGet(), Duration.ofSeconds(1), Duration.ofHours(1));
        assertThat(counter.get(), equalTo(0));
        service.timePasses(Duration.ofMillis(999));
        assertThat(counter.get(), equalTo(0));
        service.timePasses(Duration.ofMillis(1));
        assertThat(counter.get(), equalTo(1));
    }

    @Test
    public void schedulingATaskAtFixedRateWillRunItPeriodically() throws Exception {
        service.scheduleAtFixedRate(() -> counter.incrementAndGet(), Duration.ofSeconds(1), Duration.ofHours(1));
        service.timePasses(Duration.ofSeconds(1));
        assertThat(counter.get(), equalTo(1));
        service.timePasses(Duration.ofMinutes(1));
        assertThat(counter.get(), equalTo(1));
        service.timePasses(Duration.ofMinutes(59));
        assertThat(counter.get(), equalTo(2));
        service.timePasses(Duration.ofHours(1));
        assertThat(counter.get(), equalTo(3));
    }

    @Test
    public void schedulingAtFixedRateWillRunItEveryTimePeriod() throws Exception {
        service.scheduleAtFixedRate(() -> counter.incrementAndGet(), Duration.ofSeconds(1), Duration.ofHours(1));
        service.timePasses(Duration.ofSeconds(1));
        assertThat(counter.get(), equalTo(1));
        service.timePasses(Duration.ofHours(5));
        assertThat(counter.get(), equalTo(6));
    }

    @Test
    public void schedulingATaskAtFixedDelayWorksJustTheSameAsFixedRate() throws Exception {
        service.scheduleWithFixedDelay(() -> counter.incrementAndGet(), Duration.ofSeconds(1), Duration.ofHours(1));
        service.timePasses(Duration.ofSeconds(1));
        assertThat(counter.get(), equalTo(1));
        service.timePasses(Duration.ofMinutes(1));
        assertThat(counter.get(), equalTo(1));
        service.timePasses(Duration.ofMinutes(59));
        assertThat(counter.get(), equalTo(2));
        service.timePasses(Duration.ofHours(1));
        assertThat(counter.get(), equalTo(3));
    }


}
