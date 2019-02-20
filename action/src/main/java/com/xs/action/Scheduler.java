package com.xs.action;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

/**
 * Created by xs on 2019/2/20.
 */

public enum  Scheduler {

    INSTANCE;

    /**
     * main thread
     */
    public static final int MAIN = 1001;
    /**
     * single thread
     */
    public static final int IO = 1002;
    /**
     * thread pool
     */
    public static final int POOL= 1003;

    /**
     * main handle
     */
    private Handler main;

    /**
     * io handle
     */
    private Handler io;

    /**
     * init
     */
    private void init() {
        if (main == null) {
            main = new Handler(Looper.getMainLooper());
        }
        if (io == null) {
            HandlerThread ioThread = new HandlerThread("action_io");
            ioThread.start();
            io = new Handler(ioThread.getLooper());
        }
    }

    /**
     * execute runnable
     *
     * @param thread
     * @param runnable
     */
    public void execute(int thread,Runnable runnable) {
        init();
        if (thread == MAIN) {
            main.post(runnable);
        } else if(thread == IO) {
            io.post(runnable);
        } else if(thread == POOL) {
            ThreadPool.INSTANCE.execute(runnable);
        }
    }

}
