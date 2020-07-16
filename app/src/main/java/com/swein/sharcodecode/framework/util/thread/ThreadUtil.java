package com.swein.sharcodecode.framework.util.thread;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtil {

    // fixed number thread pool, can reuse
    static private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    // only one thread can run at one time
    static private ExecutorService executorSequential = Executors.newSingleThreadExecutor();

    // sync UI
    private static Handler handle = new Handler(Looper.getMainLooper());

    @Override
    protected void finalize() throws Throwable {
        if (executor != null && executor.isShutdown()) {
            executor.shutdown();
        }
        if (executorSequential != null && !executorSequential.isShutdown()) {
            executorSequential.shutdown();
        }
        super.finalize();
    }

    public static void startUIThread(int delayMillis, Runnable runnable) {
        handle.postDelayed(runnable, delayMillis);
    }

    public static void startThread(final Runnable runnable) {
        executor.submit(runnable);
    }

    public static void startSingleThread(final Runnable runnable) {
        executorSequential.submit(runnable);
    }
}
