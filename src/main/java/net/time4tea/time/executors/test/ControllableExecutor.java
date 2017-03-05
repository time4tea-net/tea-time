package net.time4tea.time.executors.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class ControllableExecutor implements Executor {

    private final List<Runnable> pending = new ArrayList<>();

    @Override
    public void execute(Runnable command) {
        pending.add(command);
    }

    public void runPendingTasks() {
        for (Runnable runnable : pending) {
            try {
                runnable.run();
            } catch (RuntimeException e) {

            }
        }
    }
}
