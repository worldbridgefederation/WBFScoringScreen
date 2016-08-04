package net.strocamp.hugo.wbfscoringscreen;

import android.webkit.*;

public class MonitoringWebViewClient extends WebViewClient {
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
    }
}
