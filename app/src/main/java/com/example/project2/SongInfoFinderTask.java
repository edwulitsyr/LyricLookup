package com.example.project2;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Async Task used to find information about a song using the Genius API
 */
public class SongInfoFinderTask extends AsyncTask<String, Integer, String> {

    // Delegate to indicate that song info was found
    public AsyncResponseIF responseDelegate = null;

    // urls of api endpoints being used to get song info
    private static final String SEARCH_API_URL = "https://genius.p.rapidapi.com/search?";
    private static final String SONG_API_URL = "https://genius.p.rapidapi.com/songs/";

    // class used by the Genius website to wrap raw lyrics in its links
    private static final String GENIUS_LYIRCS_DATA_TAG = "data-lyrics-container";

    @Override
    protected String doInBackground(String... strings) {
        String findSongResults = "";
        String artist = strings[0];
        String songName = strings[1];
        try {
            // search by the artist and find their song
            String findArtistSongsResult = findArtistSongs(artist);
            String songId = getSongId(songName, findArtistSongsResult);
            if (songId.isEmpty()) {
                // artist not found
                Log.i("findSong", "Song not found");
                return null;
            }

            // find the song information
            findSongResults = findSong(songId);
            if (findSongResults.isEmpty()) {
                Log.i("findSongInfo", "Song information not found");
                return null;
            }

            // find the lyrics
            String lyrics = getLyrics(findSongResults);
            findSongResults = findSongResults.substring(0, findSongResults.length() - 1); // remove closing brace in order to add lyrics
            findSongResults += ", \"lyrics\": \"" + lyrics + "\"}";

        } catch (Exception e) {
            Log.e("SongInfoFinderTask", e.getStackTrace().toString());
        }

        //Log.d("finderTask", foundLyrics);
        return findSongResults;
    }

    /**
     * Find an artist in the API
     * @param artist artist to find
     * @return JSON response to find request as String
     * @throws Exception
     */
    private String findArtistSongs(String artist) throws Exception {
        // search for the artist to get artist id
        String searchUrl = SEARCH_API_URL + "q=" + artist.replaceAll(" ", "%20"); // replace spaces
        URL url = new URL(searchUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("X-RapidAPI-Key", "<insert API Key Here>"); //TODO: Add the API Key
        conn.setRequestProperty("X-RapidAPI-Host", "genius.p.rapidapi.com");
        conn.setDoInput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.connect();
        int resCode = conn.getResponseCode();
        Log.i("findArtist", "response code=>" + conn.getResponseCode());

        // proceed if successful api call
        String findResults = "";
        if (resCode == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line  = br.readLine()) != null) {
                findResults += line.trim();
            }
        } else {
            Log.e("findArtist", "Error finding artist");
            return "";
        }
        return findResults;
    }

    /**
     * Get the artist Id from a json result from findArtist API Endpoint
     * @param findResults Full JSON results from find endpoint as a String
     * @return found artist id
     */
    private String getSongId(String songName, String findResults) {
        String songId = "";
        try {
            JSONObject json = new JSONObject(findResults);
            JSONObject respnseField = json.getJSONObject("response");
            JSONArray hits = respnseField.getJSONArray("hits");
            for (int i = 0; i < hits.length(); i++) {
                JSONObject hit = hits.getJSONObject(i);
                JSONObject resultObj = hit.getJSONObject("result");
                String songTitle = resultObj.getString("title");
                if (songTitle.equalsIgnoreCase(songName)) {
                    songId = resultObj.getString("id");
                    return songId;
                }
            }

        } catch (Exception e) {
            Log.e("findArtistId", e.getStackTrace().toString());
        }
        return songId;
    }

    /**
     * Find the song in Genius data
     * @param songId name of song
     * @return String represenation of response JSON from API call
     */
    private String findSong(String songId) {
        try {
            // search for songs by artist
            String searchUrl = SONG_API_URL + songId;
            URL url = new URL(searchUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-RapidAPI-Key", "");
            conn.setRequestProperty("X-RapidAPI-Host", "genius.p.rapidapi.com");
            conn.setDoInput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.connect();
            int resCode = conn.getResponseCode();
            Log.i("getSongs", "response code=>" + conn.getResponseCode());

            // proceed if successful api call
            String songInfo = "";
            if (resCode == 200) {
                // get the song info from the response data
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line  = br.readLine()) != null) {
                    songInfo += line.trim();
                }
            } else {
                Log.e("findSong", "Error finding song");
                return "";
            }

            return songInfo;
        } catch (Exception e) {
            Log.e("findSong", "Error finding song");
        }
        return "";
    }

    /**
     * Get the lyrics from the Genius url
     * @param findSongResults result of find song API call
     * @return String of lyrics
     */
    public String getLyrics(String findSongResults) {
        String lyrics = "";
        String lyricsData = "";
        try {
            // get the url from the raw json string
            JSONObject json = new JSONObject(findSongResults);
            JSONObject response = json.getJSONObject("response");
            JSONObject song = response.getJSONObject("song");
            String songUrl = song.getString("url");

            // extract the lyrics from the song url webpage contents
            URL url = new URL(songUrl);
            URLConnection conn = url.openConnection();
            conn.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.contains(GENIUS_LYIRCS_DATA_TAG)) {
                    lyricsData = line;
                    break;
                }
            }

            // extract the lyrics from Genius's html template
            lyrics = lyricsData.replaceAll("<br/>", "\n");
            lyrics = lyrics.substring(lyrics.indexOf("["));
            lyrics = lyrics.substring(0, lyrics.indexOf("<div class=\"Lyrics__Footer"));
            lyrics = lyrics.replaceAll("<[^>]+>", "");
            // replace common characters
            lyrics = lyrics.replaceAll("&amp;", "&");
            lyrics = lyrics.replaceAll("&#x27;", "\'");
        } catch (Exception e) {
            Log.e("lyricFinder", "Exception finding lyrics. Exception: " + e.getMessage());
        }
        return lyrics;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
    }

    @Override
    protected void onPostExecute(String result) {
        responseDelegate.processFinish(result);
    }
}