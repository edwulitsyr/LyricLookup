package com.example.project2;

/**
 * Class used to represent a song found in external storage
 */
public class UserStorageSong {
    // song artist
    private String artist;

    // name of song
    private String songName;

    /**
     * Constructor
     * @param songName
     * @param artist
     */
    public UserStorageSong(String songName, String artist) {
        this.songName = songName;
        this.artist = artist;
    }

    /**
     * Get song formatted as artist-songName
     * @return display string representing a song
     */
    public String getDisplayName() {
        return artist + " - " + songName;
    }

    /**
     * Get artist
     * @return name of artist
     */
    public String getArtist() {
        return artist;
    }

    /**
     * Set artist
     * @param artist artist name
     */
    public void setArtist(String artist) {
        this.artist = artist;
    }

    /**
     * Get song name
     * @return name of song
     */
    public String getSongName() {
        return songName;
    }

    /**
     * Set song name
     * @param songName name of song
     */
    public void setSongName(String songName) {
        this.songName = songName;
    }
}
