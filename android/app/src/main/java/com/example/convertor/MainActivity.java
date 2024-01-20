package com.example.convertor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONException;
import org.json.JSONObject;

import api.deezer.DeezerApi;
import api.deezer.exceptions.DeezerException;
import api.deezer.objects.DeezerAccessToken;
import api.deezer.objects.Permission;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;

import okhttp3.Request;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "conversion";
    private SecretKeys secretKeys = new SecretKeys();
    private final String CLIENT_ID = secretKeys.getClientId();
    private final String CLIENT_SECRET = secretKeys.getClientSecret();
    private static final String REDIRECT_URI = "com.example.convertor://callback/?source=spotify";
    private static User spotifyUser = null;
    public static SpotifyApi spotifyApi;
    public String spotifyPlaylists[][] = new String[0][];
    public String spotifyMusics[][] = new String[0][];

    private CountDownLatch latchDeezer;
    private String authorizationCodeDeezer;
    private DeezerApi deezerApi;

    private static final int REQUEST_LOGIN = 1;



    private static final String REDIRECT_URI_DEEZER = "http://37.65.52.171:22";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .setRedirectUri(URI.create(REDIRECT_URI))
                .build();

        deezerApi = new DeezerApi();
    }

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine);

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler((call, result) -> {
                    if (call.method.equals("spotifyPlaylist")) {
                        authenticationSpotify();
                        spotifyPlaylists = PlaylistRequestTask.spotifyPlaylists;
                        List<List<String>> convertedPlaylists = convertirTableauEnListe(spotifyPlaylists);
                        result.success(convertedPlaylists);
                    }
                    if (call.method.equals("spotifyMusics")) {
                        String playlistId = call.argument("playlistId");
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    spotifyMusics = PlaylistRequestTask.getMusics(playlistId);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                } catch (SpotifyWebApiException e) {
                                    throw new RuntimeException(e);
                                }
                                List<List<String>> convertedMusiques = convertirTableauEnListe(spotifyMusics);
                                result.success(convertedMusiques);
                            }
                        });
                        thread.start();
                    }
                    if (call.method.equals("deezerPlaylist")) {
                        try {
                            authentificationDeezer();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                });
    }

    private void test() {
        System.out.println("test");
    }

    private void authenticationSpotify() {
        String authorizationUrl = "https://accounts.spotify.com/authorize" +
                "?response_type=code" +
                "&client_id=" + CLIENT_ID +
                "&redirect_uri=" + REDIRECT_URI +
                "&scope=user-read-private%20playlist-modify-public";

        // Afficher l'URL d'autorisation (pour le débogage)
        System.out.println("Authorization URL: " + authorizationUrl);

        Uri uri = Uri.parse(authorizationUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void authentificationDeezer() throws DeezerException {
        // Step 1. Create login URL.
        String loginUrl = deezerApi.auth().getLoginUrl(616404, REDIRECT_URI_DEEZER, Permission.MANAGE_LIBRARY);

        // Step 2. Open the login URL in the default browser
        Uri uri = Uri.parse(loginUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }



    @Override
    protected void onNewIntent(Intent intent) {
        System.out.println("test");
        super.onNewIntent(intent);
        System.out.println("test");

        Uri callbackUri = Uri.parse(REDIRECT_URI);
        System.out.println("test");
        Uri receivedUri = intent.getData();

        System.out.println("URL recu : " + receivedUri);

        if (receivedUri != null && receivedUri.toString().startsWith(callbackUri.toString())) {
            String authorizationCode = receivedUri.getQueryParameter("code");
            if (authorizationCode != null) {
                String source = receivedUri.getQueryParameter("source");
                if (source.equals("spotify")) {
                    handleSpotifyAuthorizationCode(authorizationCode);
                } else if (source.equals("deezer")) {
                    try {
                        handleDeezerAuthorizationCode(authorizationCode);
                    } catch (DeezerException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (receivedUri.getQueryParameter("error") != null) {
                // Une erreur s'est produite pendant l'authentification
                String errorMessage = receivedUri.getQueryParameter("error_description");
            }
        }
    }



    private void handleSpotifyAuthorizationCode(String authorizationCode) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(authorizationCode)
                        .build();

                try {
                    AuthorizationCodeCredentials credentials = authorizationCodeRequest.execute();
                    String accessToken = credentials.getAccessToken();

                    if (accessToken != null) {
                        runOnUiThread(() -> {
                            spotifyApi.setAccessToken(accessToken);
                            new PlaylistRequestTask().execute();  // Execute the ProfileRequestTask
                        });
                    }
                } catch (IOException | SpotifyWebApiException | ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void handleDeezerAuthorizationCode(String authorizationCode) throws DeezerException {

        authorizationCodeDeezer = authorizationCode;

        DeezerAccessToken accessToken = deezerApi.auth().getAccessToken(616404, "b1dc56aad0e4f1dae765b73ac7a1bdd1", authorizationCodeDeezer).execute();
        deezerApi.setAccessToken(accessToken);
    }




    public static void handleUser(User user) {
        if (user != null) {
            spotifyUser = user;
            // Utilisez les informations de l'utilisateur selon vos besoins
            String userId = user.getId();
            String displayName = user.getDisplayName();

            System.out.println("------------------------------------------");
            System.out.println("Userid : " + userId + ", Username : " + displayName);
            System.out.println("------------------------------------------");
        }
    }

    public static List<List<String>> convertirTableauEnListe(String[][] tableau) {
        List<List<String>> liste = new ArrayList<>();

        for (String[] ligne : tableau) {
            List<String> ligneListe = new ArrayList<>();
            for (String element : ligne) {
                ligneListe.add(element);
            }
            liste.add(ligneListe);
        }

        return liste;
    }



}



