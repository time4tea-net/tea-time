package net.time4tea.time.executors;

import org.junit.After;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class SimpleExecutorServicesTest {

    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    @After
    public void tearDown() throws Exception {
        scheduledExecutorService.shutdown();
        scheduledExecutorService.awaitTermination(1, TimeUnit.SECONDS);
    }

    @Test
    public void wrapsAScheduledExecutorService() throws Exception {

        SimpleScheduledExecutorService service = SimpleExecutorServices.wrapping(scheduledExecutorService);

        CountDownLatch latch = new CountDownLatch(1);

        service.schedule(latch::countDown, Duration.ofMillis(1));

        boolean got = latch.await(100, TimeUnit.MILLISECONDS);
        assertThat("expected to have counted down", got, equalTo(true));
    }
}
