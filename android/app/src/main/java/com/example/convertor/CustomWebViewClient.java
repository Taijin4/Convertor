package com.example.convertor;

import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CustomWebViewClient extends WebViewClient {

    private static final String REDIRECT_URI_DEEZER = "http://localhost:8080/callback/?source=deezer";
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        // Vérifier si l'URL de redirection a été atteinte
        String redirectUrl = request.getUrl().toString();
        if (redirectUrl.startsWith(REDIRECT_URI_DEEZER)) {
            // Extraire le code d'authentification de l'URL de redirection
            Uri uri = Uri.parse(redirectUrl);
            String authorizationCode = uri.getQueryParameter("code");
            if (authorizationCode != null) {
                System.out.println("Code d'authentification Deezer : " + authorizationCode);
                // Faites ici ce que vous souhaitez réaliser avec le code d'authentification
            } else if (uri.getQueryParameter("error") != null) {
                // Une erreur s'est produite pendant l'authentification
                String errorMessage = uri.getQueryParameter("error_description");
            }
        }

        return super.shouldOverrideUrlLoading(view, request);
    }
}
