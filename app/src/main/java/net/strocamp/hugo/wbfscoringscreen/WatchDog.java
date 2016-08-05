package net.strocamp.hugo.wbfscoringscreen;

import java.util.Date;

public class WatchDog {
    private static final int WATCHDOG_TIMEOUT = 300000;
    private final static Object instanceLock = new Object();
    private static WatchDog instance = null;

    private volatile Date lastUpdate = null;

    private volatile WatchDogListener listener = null;

    private void notifyListeners() {
        if (listener != null) {
            listener.onWatchDogExpired();
        }
    }

    private WatchDog() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
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

            }
        }).start();
    }

    public static WatchDog getInstance() {
        synchronized (instanceLock) {
            if (instance == null) {
                instance = new WatchDog();
            }
        }
        return instance;
    }

    public void resetWatchDog() {
        this.lastUpdate = new Date();
    }

    public void setListener(WatchDogListener watchDogListener) {
        this.listener = watchDogListener;
    }

}
