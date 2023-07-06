package com.example.convertor;

import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CustomWebViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest url) {
        // Step 4. Print message when the URL changes
        System.out.println("URL changed: " + url.getUrl());
        return super.shouldOverrideUrlLoading(view, url);
    }
}
