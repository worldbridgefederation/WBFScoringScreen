package net.strocamp.hugo.wbfscoringscreen;

import android.util.Log;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class WatchDog {
    private static final int WATCHDOG_TIMEOUT = 300000;
    private final static Object instanceLock = new Object();
    private static WatchDog instance = null;

    private volatile Date lastUpdate = null;
    private volatile WatchDogListener listener = null;
    private volatile Thread worker;

    private AtomicBoolean running;

    private void notifyListeners() {
        if (listener != null) {
            listener.onWatchDogExpired();
        }
    }

    private WatchDog() {
        running = new AtomicBoolean(true);
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("WatchDog", "Starting WatchDog");
                while (running.get()) {
                    long now = System.currentTimeMillis();
                    if (lastUpdate != null && now - lastUpdate.getTime() > WATCHDOG_TIMEOUT) {
                        notifyListeners();
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
                Log.i("WatchDog", "Closing WatchDog");
            }
        });
        worker.start();
    }

    public static WatchDog getInstance() {
        synchronized (instanceLock) {
            if (instance == null) {
                instance = new WatchDog();
            }
        }
        return instance;
    }

    public void shutdown() {
        running.set(false);
        try {
            worker.join(1000);
        } catch (InterruptedException e) {
            Log.e("WatchDog", "Failed to shutdown", e);
        }
        synchronized (instanceLock) {
            instance = null;
        }
    }

    public void resetWatchDog() {
        this.lastUpdate = new Date();
    }

    public void setListener(WatchDogListener watchDogListener) {
        this.listener = watchDogListener;
    }

}
