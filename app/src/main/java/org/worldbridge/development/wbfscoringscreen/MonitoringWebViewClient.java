package org.worldbridge.development.wbfscoringscreen;

import android.app.Activity;
import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.webkit.*;

import java.util.concurrent.ConcurrentHashMap;

public class MonitoringWebViewClient extends WebViewClient {

    private WatchDog watchDog = WatchDog.getInstance();
    private ConcurrentHashMap<String, RetryStatus> retryStatus = new ConcurrentHashMap<>();

    @Override
    public void onReceivedError(final WebView view, int errorCode, String description, final String failingUrl) {
        handleError(view, failingUrl);
    }

    @VisibleForTesting
    void handleError(final WebView view, final String failingUrl) {
        view.loadData("<html><body><h1>Error </h1><p>Loading " + failingUrl + " failed...</body></html>", "text/html", "UTF-8");

        if (!retryStatus.containsKey(failingUrl)) {
            retryStatus.put(failingUrl, new RetryStatus());
        }
        boolean retry = retryStatus.get(failingUrl).shouldRetry();

        if (retry) {
            Toast.showMessage(view.getContext(), "Load failed, retrying in 3 seconds");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        // nothing
                    }
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            view.stopLoading();
                            view.loadUrl(failingUrl);
                        }
                    });
                }
            }).start();
        } else {
            Toast.showMessage(view.getContext(), "Load failed multiple times, leaving resolution to the watchdog");
        }
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        showSpinner(view);
        watchDog.resetWatchDog();
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        hideSpinner(view);
        watchDog.resetWatchDog();
    }

    private void showSpinner(WebView view) {
        View progressBar = getProgressBarView(view);
        if (progressBar.getVisibility() == View.INVISIBLE) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideSpinner(WebView view) {
        View progressBar = getProgressBarView(view);
        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private View getProgressBarView(WebView view) {
        Activity host = (Activity) view.getContext();
        return host.findViewById(R.id.progressBar);
    }

    private class RetryStatus {
        private volatile int tries;
        private volatile long firstFailureTimestamp;

        private RetryStatus() {
            this.tries = 0;
            this.firstFailureTimestamp = System.currentTimeMillis();
        }

        boolean shouldRetry() {
            long deltaTime = System.currentTimeMillis() - firstFailureTimestamp;
            if (tries > 5 || deltaTime > 30000) {
                return false;
            }
            tries++;
            return true;
        }
    }
}
