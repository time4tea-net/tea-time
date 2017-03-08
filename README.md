# tea-time

[ ![Codeship Status for time4tea-net/tea-time](https://app.codeship.com/projects/dddf5cb0-e3f8-0134-b021-7a2224107979/status?branch=master)](https://app.codeship.com/projects/206081)

## What is it

A collection of test friendly implementations of time for Java 8

* Executor
* ScheduledExecutorService

* Clock as an interface

This allows the developer to test code that uses ScheduledExecutorService without starting up threads,
and control the passage of time properly.

It doesn't just execute the Runnables synchronously, as this gives a false impression
of the ordering of events in the system - if you do this, a get() right after a submit() will always
work in tests, but never under a real ScheduledExecutorService

# Clock

## How to use 

Don't use the java 8 clock in your code, you cannot control the passage of time properly

Use a tea-time Clock.

## How to use in tests

```java
Clock clock = TickingClock.atUTC("2017/10/02 10:23:44.000")

MyClass myClass = new MyClass(clock);

myClass.recordTimestamp();

clock.timePasses(Duration.ofSeconds(23));

myClass.recordTimestamp();

```


# SimpleExecutorServices

You can pretty much replace any usage of ScheduledExecutorService with a SimpleScheduledExecutorService,
then use a ControllableSimpleScheduledExecutorService in your code like this

## How to use in tests


```java

ControllableSimpleScheduledExecutorService service = new ControllableSimpleScheduledExecutorService();
MyClass myClass = new MyClass(service);

myClass.doSomething(); // schedules a task...

// assert nothing happened yet

service.timePasses(Duration.ofHours(1));

// assert what should have happened.

```

## How to use for real

```java
ScheduledExecutorService service = Executors....();
MyClass myClass = new MyClass(SimpleExecutorServices.wrapping(service));
```



