package com.example.musicplayer;

import java.io.Serializable;

public class AudioModel implements Serializable {
    String path;
    String title;
    String artist;
    String duration;
    byte[] songCover;

    public AudioModel(String path, String title, String artist, String duration, byte[] songCover) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.songCover = songCover;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() { return artist; }

    public void setArtist(String artist) { this.artist = artist; }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public byte[] getSongCover() { return songCover; }

    public void setSongCover(byte[] songCover) { this.songCover = songCover; }
}
