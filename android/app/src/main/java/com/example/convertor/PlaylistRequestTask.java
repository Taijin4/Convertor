package com.example.convertor;

import static com.example.convertor.MainActivity.spotifyApi;

import android.os.AsyncTask;

import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Image;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;

public class PlaylistRequestTask extends AsyncTask<Void, Void, List<PlaylistSimplified>> {

    public static String spotifyPlaylists[][] = new String[0][];

    @Override
    protected List<PlaylistSimplified> doInBackground(Void... params) {
        GetListOfCurrentUsersPlaylistsRequest playlistsRequest = spotifyApi.getListOfCurrentUsersPlaylists().build();

        try {
            Paging<PlaylistSimplified> playlistSimplifiedPaging = playlistsRequest.execute();
            List<PlaylistSimplified> playlists = Arrays.asList(playlistSimplifiedPaging.getItems());
            return playlists;
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }



    @Override
    protected void onPostExecute(List<PlaylistSimplified> playlistsList) {
        spotifyPlaylists = getPlaylist(playlistsList);
    }

    public static String[][] getPlaylist(List<PlaylistSimplified> playlistsList)
    {
        String playlists[][] = new String[0][];

        if (playlistsList != null) {
            playlists = new String[playlistsList.size()][4];
            int i = 0;
            for (PlaylistSimplified playlist : playlistsList) {
                String playlistId = playlist.getId();
                String playlistName = playlist.getName();
                String playlistNb = playlist.getTracks().getTotal().toString();
                Image[] playlistImages = playlist.getImages();
                String playlistImageUrl = null;// Définissez une valeur par défaut
                if (playlistImages.length > 0) {
                    Image playlistImage = playlistImages[0];
                    playlistImageUrl = playlistImage.getUrl();
                }

                playlists[i][0] = playlistName;
                playlists[i][1] = playlistId;
                playlists[i][2] = playlistNb;
                playlists[i][3] = playlistImageUrl;

                // Faites ce que vous voulez avec les playlists, par exemple, les afficher à l'utilisateur, les enregistrer dans une liste, etc.
                i++;
            }
        } else {
            // Gérer le cas d'erreur ici
        }
        return playlists;
    }

    public static String[][] getMusics(String playlistId) throws IOException, ParseException, SpotifyWebApiException {
        String musics[][] = new String[0][];
        GetPlaylistsItemsRequest playlistsItemsRequest = spotifyApi.getPlaylistsItems(playlistId)
                .build();

        Paging<PlaylistTrack> playlistTracks = playlistsItemsRequest.execute();
        musics = new String[playlistTracks.getTotal()][2];
        int nbMusiques = 0;
        for (PlaylistTrack playlistTrack : playlistTracks.getItems()) {
            Track track = (Track) playlistTrack.getTrack();
            String trackName = track.getName();
            String artistName = track.getArtists()[0].getName();
            musics[nbMusiques][0] = trackName;
            musics[nbMusiques][1] = artistName;
            nbMusiques++;
        }

        return musics;
    }


}
