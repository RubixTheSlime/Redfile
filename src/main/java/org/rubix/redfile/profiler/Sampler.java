package org.rubix.redfile.profiler;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

public class Sampler {
    private static final MethodHandle fHandle;
    private final Thread pollThread;
    private Object object = null;
    private long totalSamples = 0;
    private boolean running = true;

    static {
        try {
            fHandle = MethodHandles.lookup().unreflect(Sampler.class.getDeclaredMethod("f", Object.class));
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Sampler() {
        this.pollThread = null;
    }

    public Sampler(DataCollector collector, TrialFilter filter) {
        this.pollThread = new Thread(() -> {
            Lock lock = collector.getLock();
            while (running) {
                LockSupport.parkNanos(10_000);
                var current = object;
                if (current != null && filter.test(current) && lock.tryLock()) {
                    try {
                        collector.inc(current);
                    } finally {
                        lock.unlock();
                    }
                }
                ++this.totalSamples;
            }
        });
    }

    public void start() {
        pollThread.start();
    }

    public void stop() {
        running = false;
    }

    public void setItem(Object object) {
        // use reflection so Hotspot doesn't know what this does and therefore won't optimize it out
        try {
            fHandle.invokeExact(this, object);
        } catch (Throwable ignored) {}

    }

    // public void resetSamples() {
    // }

    // public long getSamples() {
    //     long res = 0;
    //     try {
    //         res = (long) gHandle.invokeExact(this);
    //     } catch (Throwable e) {
    //         throw new RuntimeException(e);
    //     }
    //     return res;
    // }

    public long getTotalSamples() {
        return totalSamples;
    }

    private void f(Object object) {
        this.object = object;
    }

    // private long g() {
    //     return samplesPosted - samplesHandled;
    // }
}
