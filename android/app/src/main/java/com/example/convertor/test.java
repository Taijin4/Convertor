package com.example.convertor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import org.apache.hc.core5.http.ParseException;

import api.deezer.DeezerApi;
import api.deezer.exceptions.DeezerException;
import api.deezer.objects.Permission;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class test extends FlutterActivity {
    private static final String CHANNEL = "conversion";
    private static final String CLIENT_ID = "1ab88cd8e4fb415d890e6634fa7fa97f";
    private static final String CLIENT_SECRET = "c420592a44ba473e9961fb31cd17079b";
    private static final String REDIRECT_URI = "com.example.convertor://callback";
    private static User spotifyUser = null;
    public static SpotifyApi spotifyApi;
    public String spotifyPlaylists[][] = new String[0][];
    public String spotifyMusics[][] = new String[0][];

    private CountDownLatch latchDeezer;
    private String authorizationCodeDeezer;
    private DeezerApi deezerApi;


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
                        authentificationSpotify();
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
                        } catch (DeezerException e) {
                            throw new RuntimeException(e);
                        }
                    }

                });
    }

    private void test() {
        System.out.println("test");
    }

    private void authentificationSpotify() {
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
        String loginUrl = deezerApi.auth().getLoginUrl(616404, REDIRECT_URI, Permission.MANAGE_LIBRARY);

        // Step 2. Open the login URL in the default browser
        Uri uri = Uri.parse(loginUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri callbackUri = Uri.parse(REDIRECT_URI);
        Uri receivedUri = intent.getData();

        if (receivedUri != null && receivedUri.toString().startsWith(callbackUri.toString())) {
            // L'utilisateur est revenu à l'application avec l'URL de redirection

            String authorizationCode = receivedUri.getQueryParameter("code");

            if (authorizationCode != null) {
                // Exemple de traitement du code d'autorisation
                if (authorizationCode.startsWith("spotify")) {
                    handleSpotifyAuthorizationCode(authorizationCode);
                } else if (authorizationCode.startsWith("deezer")) {
                    handleDeezerAuthorizationCode(authorizationCode);
                }
            } else if (receivedUri.getQueryParameter("error") != null) {
                // Une erreur s'est produite pendant l'authentification
                String errorMessage = receivedUri.getQueryParameter("error_description");
                // Gérez l'erreur selon vos besoins
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

    private void handleDeezerAuthorizationCode(String authorizationCode) {
        // Step 3. Exchange the authorization code for access token.
        authorizationCodeDeezer = authorizationCode;
        latchDeezer.countDown();
    }

    private void handleAuthorizationCode(String authorizationCode) {
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
