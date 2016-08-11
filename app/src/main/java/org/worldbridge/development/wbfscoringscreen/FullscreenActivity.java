package org.worldbridge.development.wbfscoringscreen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.nsd.NsdManager;
import android.os.*;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import org.worldbridge.development.wbfscoringscreen.domain.*;
import org.worldbridge.development.wbfscoringscreen.nds.NsdHelper;
import org.worldbridge.development.wbfscoringscreen.scheduler.Scheduler;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.worldbridge.development.wbfscoringscreen.Toast.showMessage;

public class FullscreenActivity extends AppCompatActivity implements WatchDogListener {
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 300;
    private static final int UI_ANIMATION_DELAY = 300;

    private static final boolean USE_DISCOVERY = false;
    public static final int MAX_URL_SIZE = 1024;

    private final Handler mHideHandler = new Handler();
    private final Handler mLoadurlHandler = new Handler();

    private View mContentView;
    private View mControlsView;
    private NsdHelper nsdHelper;

    private Scheduler scheduler = new Scheduler();
    private ServerDetails serverDetails = null;
    private String statusTaskId = null;

    private String deviceId = null;

    private volatile Status status;
    private Configuration configuration = null;

    private Notification activeNotification = null;

    private Handler mNsdEventHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            serviceDiscoveryCallback(message);
            return true;
        }
    });

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        configuration = new Configuration(preferences);

        setContentView(R.layout.activity_fullscreen);

        NsdManager nsdManager = (NsdManager)getApplicationContext().getSystemService(Context.NSD_SERVICE);
        nsdHelper = new NsdHelper(nsdManager, mNsdEventHandler, configuration.getServiceType());

        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        mContentView.setOnTouchListener(mDelayHideTouchListener);

        WebView mWebView = (WebView) mContentView;
        mWebView.setWebViewClient(new MonitoringWebViewClient());
        mWebView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AUTO_HIDE) {
                    showMessage(getApplicationContext(), R.string.toast_fullscreen_enable);
                    delayedHide(AUTO_HIDE_DELAY_MILLIS);
                }
            }
        });

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        scheduler.onCreate();

        deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);

        WatchDog.getInstance().setListener(this);

        final WebView mWebView = (WebView) mContentView;
        mWebView.loadUrl(configuration.getDefaultUrl());
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (USE_DISCOVERY) {
            nsdHelper.discoveryStop();
        } else {
            scheduler.remove(statusTaskId);
        }
        WatchDog.getInstance().shutdown();
    }

    @Override
    protected void onStart() {
        super.onStart();

        WatchDog.getInstance().setListener(this);

        if (USE_DISCOVERY) {
            // Start looking for a published service after some time
            // Gives the NsdManager some time to listen to the network
            mNsdEventHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    nsdHelper.discoveryStart();
                }
            }, 10000);
        } else {
            serverDetails = new ServerDetails("10.100.200.10", "8080");
            statusTaskId = scheduler.submit(new Runnable() {
                @Override
                public void run() {
                    sendStatusUpdate();
                }
            }, 15, TimeUnit.SECONDS);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        nsdHelper.cleanup();
        scheduler.onDestroy();
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onWatchDogExpired() {
        mLoadurlHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.showMessage(mContentView.getContext(), "WatchDog Timer expiring, forcing reload");
                WebView myWebView = (WebView) mContentView;
                myWebView.stopLoading();

                myWebView.loadUrl(configuration.getDefaultUrl());
            }
        });
    }

    private void serviceDiscoveryCallback(Message msg) {
        String message = msg.getData().getString("messageid");
        String name = msg.getData().getString("name");
        String type = msg.getData().getString("type");

        NsdHelper.MessageType messageType = NsdHelper.MessageType.valueOf(message);

        if (NsdHelper.MessageType.MESSAGE_INFO == messageType) {
            Toast.showMessage(this.getApplicationContext(), msg.getData().getString("message"));
        } else {
            String logMessage = "Got a " + message + " for service " + type + " on " + name;
            Toast.showMessage(this.getApplicationContext(), logMessage);
        }

        if (NsdHelper.MessageType.MESSAGE_SRVFOUND.equals(messageType)) {
            if (!msg.getData().containsKey("host")) {
                Toast.showMessage(getBaseContext(), "Server found, but needs resolving");
            } else {
                serverDetails = new ServerDetails(msg.getData().getString("host"), msg.getData().getString("port"));

                // Remove existing task if needed
                if (statusTaskId != null) {
                    scheduler.remove(statusTaskId);
                }

                statusTaskId = scheduler.submit(new Runnable() {
                    @Override
                    public void run() {
                        sendStatusUpdate();
                    }
                }, 15, TimeUnit.SECONDS);
            }
        } else if (NsdHelper.MessageType.MESSAGE_SRVLOST.equals(messageType)) {
            if (statusTaskId != null) {
                scheduler.remove(statusTaskId);
            }
            serverDetails = null;
        }
    }

    private void sendStatusUpdate() {
        Log.d("FullScreenActivity", "Sending status update to " + serverDetails.getHost());

        Status status = getStatusFromMainThread();

        StatusTaskDetails details = new StatusTaskDetails(status, serverDetails);
        new AsyncTask<StatusTaskDetails, String, StatusResponse>() {
            @Override
            protected StatusResponse doInBackground(StatusTaskDetails... data) {
                StatusTaskDetails taskDetails = data[0];
                StatusTaskHelper statusTaskHelper = new StatusTaskHelper();
                return statusTaskHelper.doStatusUpdate(taskDetails);
            }

            @Override
            protected void onPostExecute(StatusResponse s) {
                if (s == null) {
                    Log.w("FullscreenActivity", "Server didn't send a status response");
                    return;
                }
                Log.i("FullscreenActivity", "Received Statusresponse with show:" + s.getShowNotitification());
                if (Boolean.TRUE.equals(s.getShowNotitification())) {
                    if (s.getNotification() == null) {
                        Log.e("FullscreenActivity", "Asked to show notification but no content");
                        return;
                    }
                    if (!s.getNotification().equals(activeNotification)) {
                        activeNotification = s.getNotification();
                        final WebView mWebView = (WebView) mContentView;
                        try {
                            mWebView.loadData(
                                NotificationHelper.getNotificationContent(getAssets(),
                                        s.getNotification().getTitle(), s.getNotification().getMessage()),
                                "text/html", "UTF-8");
                        } catch (IOException e) {
                            Log.e("FullscreenActivity", "Failed to load notification");
                        }
                    }

                    // Reset the watchDog now we are showing a notification
                    WatchDog.getInstance().resetWatchDog();
                } else if (activeNotification != null) {
                    // We need to get back to the regular program
                    activeNotification = null;
                    final WebView mWebView = (WebView) mContentView;
                    mWebView.loadUrl(configuration.getDefaultUrl());
                }

            }
        }.execute(details);
    }

    private Status getStatusFromMainThread() {
        final CountDownLatch latch = new CountDownLatch(1);
        mNsdEventHandler.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                WebView myWebView = (WebView) mContentView;

                status = new Status();
                status.setDeviceId(deviceId);
                String urlString = myWebView.getUrl();
                if (urlString != null && urlString.length() > MAX_URL_SIZE) {
                    urlString = urlString.substring(0, MAX_URL_SIZE);

                }
                status.setCurrentUrl(urlString);
                status.setScreenDetails(StatusHelper.getScreenDetails(getResources().getDisplayMetrics()));
                status.setHardwareDetails(StatusHelper.getHardwareDetails());
                status.setVersionDetails(StatusHelper.getVersionDetails());

                latch.countDown();
            }
        });
        try {
            boolean done = latch.await(250, TimeUnit.MILLISECONDS);
            if (done) {
                return status;
            }
        } catch (InterruptedException e) {
            Log.e("FullscreenActivity", "Latch wait got interrupted");
        }
        return null;
    }

}
