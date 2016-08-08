package org.worldbridge.development.wbfscoringscreen;

import android.webkit.WebView;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MonitoringWebViewClientTest {

    @Test
    public void retryTest() {
        MonitoringWebViewClient client = new MonitoringWebViewClient();
        WebView view = mock(WebView.class);
        client.handleError(view, "http://www.example.com");

    }
}
