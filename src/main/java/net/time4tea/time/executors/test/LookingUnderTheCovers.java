package net.time4tea.time.executors.test;

import java.util.ArrayList;
import java.util.List;

public class LookingUnderTheCovers implements UnderTheCovers {

    private final List<Throwable> exceptions = new ArrayList<>();
    private int taskCount;
    private int tasksExecuted;

    @Override
    public void exceptionThrown(Throwable e) {
        exceptions.add(e);
    }

    @Override
    public void taskExecuted() {
        tasksExecuted++;
    }

    @Override
    public void taskCount(int tasksInQueue) {
        this.taskCount = tasksInQueue;
    }

    public int tasksExecuted() {
        return tasksExecuted;
    }

    public int taskCount() {
        return taskCount;
    }

    public List<Throwable> exceptions() {
        return exceptions;
    }
}
