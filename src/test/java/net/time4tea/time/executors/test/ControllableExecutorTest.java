package net.time4tea.time.executors.test;

import net.time4tea.time.executors.ControllableExecutor;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ControllableExecutorTest {

    private final ControllableExecutor executor = new ControllableExecutor();

    @Test
    public void doesntRunTheRunnableSynchronously() throws Exception {
        AtomicInteger value = new AtomicInteger(0);
        executor.execute(value::incrementAndGet);
        assertThat(value.get(), equalTo(0));
    }

    @Test
    public void executesTheRunnableWhenTold() throws Exception {
        AtomicInteger value = new AtomicInteger(0);
        executor.execute(value::incrementAndGet);
        assertThat(value.get(), equalTo(0));
        executor.runPendingTasks();
        assertThat(value.get(), equalTo(1));
    }

    @Test
    public void executesMultipleTasks() throws Exception {
        AtomicInteger value1 = new AtomicInteger(0);
        AtomicInteger value2 = new AtomicInteger(0);
        executor.execute(value1::incrementAndGet);
        executor.execute(value2::incrementAndGet);
        executor.runPendingTasks();
        assertThat(value1.get(), equalTo(1));
        assertThat(value2.get(), equalTo(1));
    }

    @Test
    public void executesMultipleTasksEvenIfOneThrowsAnException() throws Exception {
        AtomicInteger value1 = new AtomicInteger(0);
        AtomicInteger value2 = new AtomicInteger(0);
        executor.execute(() -> { throw new NullPointerException(); });
        executor.execute(value2::incrementAndGet);
        executor.runPendingTasks();
        assertThat(value1.get(), equalTo(0));
        assertThat(value2.get(), equalTo(1));
    }

    @Test
    public void onlyRunsTasksOnce() throws Exception {
        AtomicInteger value = new AtomicInteger(0);
        executor.execute(value::incrementAndGet);
        executor.runPendingTasks();
        executor.runPendingTasks();
        executor.runPendingTasks();
        assertThat(value.get(), equalTo(1));
    }
}