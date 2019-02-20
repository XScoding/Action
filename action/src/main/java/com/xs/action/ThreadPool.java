package com.xs.action;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by xs on 2019/2/20.
 */

public enum ThreadPool {

    INSTANCE;

    /**
     * cpu count
     */
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    /**
     * core pool size
     */
    private static final int corePoolSize = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    /**
     * maximum pool size
     */
    private static final int maximumPoolSize = CPU_COUNT * 2 + 1;
    /**
     * keep alive time
     */
    private static final int keepAliveTime = 3000;

    /**
     * ThreadPoolExecutor
     */
    private ThreadPoolExecutor mExecutor;

    /**
     * init threadPoolExectuter
     */
    private void initThreadPoolExecutor() {
        if (mExecutor == null || mExecutor.isShutdown() || mExecutor.isTerminated()) {
            synchronized (ThreadPool.class) {
                if (mExecutor == null || mExecutor.isShutdown() || mExecutor.isTerminated()) {
                    TimeUnit unit = TimeUnit.MILLISECONDS;
                    BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<>();
                    ThreadFactory threadFactory = Executors.defaultThreadFactory();
                    RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();

                    mExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                            threadFactory, handler);
                }
            }
        }
    }


    /**
     * execute runnable
     */
    public void execute(Runnable task) {
        initThreadPoolExecutor();
        mExecutor.execute(task);
    }

    /**
     * submit runnable
     */
    public Future<?> submit(Runnable task) {
        initThreadPoolExecutor();
        return mExecutor.submit(task);
    }

    /**
     * remove runnable
     */
    public void remove(Runnable task) {
        initThreadPoolExecutor();
        mExecutor.remove(task);
    }

}
