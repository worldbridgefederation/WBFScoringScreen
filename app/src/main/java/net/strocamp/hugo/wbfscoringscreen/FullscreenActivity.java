package net.strocamp.hugo.wbfscoringscreen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import net.strocamp.hugo.wbfscoringscreen.domain.ServerDetails;
import net.strocamp.hugo.wbfscoringscreen.domain.Status;
import net.strocamp.hugo.wbfscoringscreen.domain.StatusTaskDetails;
import net.strocamp.hugo.wbfscoringscreen.nds.NsdHelper;
import net.strocamp.hugo.wbfscoringscreen.nds.StatusUpdateTask;
import net.strocamp.hugo.wbfscoringscreen.scheduler.Scheduler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static net.strocamp.hugo.wbfscoringscreen.Toast.showMessage;

public class FullscreenActivity extends AppCompatActivity implements WatchDogListener {
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 300;
    private static final int UI_ANIMATION_DELAY = 300;

    private static final String BASE_URL = "http://10.100.200.99";

    private final Handler mHideHandler = new Handler();
    private final Handler mLoadurlHandler = new Handler();

    private View mContentView;
    private View mControlsView;
    private NsdHelper nsdHelper;

    private Scheduler scheduler = new Scheduler();
    private ServerDetails serverDetails = null;
    private String statusTaskId = null;

    private String deviceId = null;

    private volatile String currentUrl;

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
            if (AUTO_HIDE) {
                showMessage(getApplicationContext(), R.string.toast_fullscreen_enable);
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        NsdManager nsdManager = (NsdManager)getApplicationContext().getSystemService(Context.NSD_SERVICE);
        nsdHelper = new NsdHelper(nsdManager, mNsdEventHandler);

        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        mContentView.setOnTouchListener(mDelayHideTouchListener);

        WebView mWebView = (WebView) mContentView;
        mWebView.setWebViewClient(new MonitoringWebViewClient());

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

        WebView mWebView = (WebView) mContentView;
        mWebView.loadUrl(BASE_URL);
    }

    @Override
    protected void onStop() {
        super.onStop();

        nsdHelper.discoveryStop();
        WatchDog.getInstance().shutdown();
    }

    @Override
    protected void onStart() {
        super.onStart();

        WatchDog.getInstance().setListener(this);
        nsdHelper.discoveryStart();
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

                myWebView.loadUrl(BASE_URL);
            }
        });
    }

    private void serviceDiscoveryCallback(Message msg) {
        String message = msg.getData().getString("messageid");
        String name = msg.getData().getString("name");
        String type = msg.getData().getString("type");

        String logMessage = "Got a " + message + " for service " + type + " on " + name;
        Toast.showMessage(this.getApplicationContext(), logMessage);

        NsdHelper.MessageType messageType = NsdHelper.MessageType.valueOf(message);

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

        Status status = new Status();
        status.setDeviceId(deviceId);
        status.setCurrentUrl(getCurrentUrlFromOtherThread());

        StatusTaskDetails details = new StatusTaskDetails(status, serverDetails);
        new StatusUpdateTask().execute(details);
    }

    private String getCurrentUrlFromOtherThread() {
        final CountDownLatch latch = new CountDownLatch(1);
        mNsdEventHandler.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                WebView myWebView = (WebView) mContentView;
                currentUrl = myWebView.getUrl();
                latch.countDown();
            }
        });
        try {
            boolean done = latch.await(250, TimeUnit.MILLISECONDS);
            if (done) {
                return currentUrl;
            }
        } catch (InterruptedException e) {
            Log.e("FullscreenActivity", "Latch wait got interrupted");
        }
        return null;
    }
}
