package net.strocamp.hugo.wbfscoringscreen;

import android.app.Activity;
import android.view.View;
import android.webkit.*;

public class MonitoringWebViewClient extends WebViewClient {

    private WatchDog watchDog = WatchDog.getInstance();

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
    }

    @Override
    public void onReceivedError(final WebView view, int errorCode, String description, final String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);

        view.loadData("<html><body><h1>Error </h1><p>Loading " + failingUrl + " failed...</body></html>", "text/html", "UTF-8");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    // nothing
                }
                view.loadUrl(failingUrl);
            }
        }).start();
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
}
