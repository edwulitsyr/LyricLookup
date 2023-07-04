package com.example.project2;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Class used to represent a song found in the Genius API
 */
@SuppressWarnings("serial")
public class GeniusAPISong implements Serializable {

    // member variables
    private String songName = "";
    private String artistName = "";
    private String albumImageUrl = "";
    private String albumName = "";
    private String releaseDate = "";
    private String lyrics = "";

    /**
     * Constructor
     * @param rawData raw data string to extract info from
     */
    public GeniusAPISong(String rawData) {
        try {
            JSONObject jsonObj  = new JSONObject(rawData);
            JSONObject response = jsonObj.getJSONObject("response");
            JSONObject songInfo = response.getJSONObject("song");
            songName = songInfo.getString("title");
            artistName = songInfo.getString("artist_names");
            JSONObject albumInfo = songInfo.getJSONObject("album");
            albumImageUrl = albumInfo.getString("cover_art_url");
            albumName = albumInfo.getString("name");
            releaseDate = songInfo.getString("release_date_for_display");
            lyrics = jsonObj.getString("lyrics");
        } catch (JSONException e) {
            Log.e("apiSongConstructor", "Failed to create JSON Object");
        }
    }

    @Override
    public String toString() {
        return "GeniusAPISong{" +
                "songName='" + songName + '\'' +
                ", artistName='" + artistName + '\'' +
                ", albumImageUrl='" + albumImageUrl + '\'' +
                ", albumName='" + albumName + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", lyrics='" + lyrics + '\'' +
                '}';
    }

    // getters and setters

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbumImageUrl() {
        return albumImageUrl;
    }

    public void getAlbumImageUrl(String albumImageUrl) {
        this.albumImageUrl = albumImageUrl;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }
}
