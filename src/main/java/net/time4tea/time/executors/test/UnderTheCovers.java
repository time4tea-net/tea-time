package net.time4tea.time.executors.test;

/**
 * not really recommended to use, but sometimes it is hard to check only by side effects of tasks
 */
public interface UnderTheCovers {
    void exceptionThrown(Throwable e);

    void taskExecuted();

    void taskCount(int tasksInQueue);
}
