package com.example.musicplayer.Repository;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;

import java.io.Serializable;

@Entity(tableName = "playlist_songs")
        //,foreignKeys = @ForeignKey(entity = PlayList.class, parentColumns = "playlistId", childColumns = "playlistId", onDelete = ForeignKey.CASCADE))
public class Song implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int songId;

    public String uri;
    public String title; // Optional
    public String artist;
    public String duration;
    public byte[] songCover;
    public int playlistId; // Foreign key to link to a specific playlist

    public Song(String uri, String title, String artist, String duration, byte[] songCover, int playlistId) {
        this.uri = uri;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.songCover = songCover;
        this.playlistId = playlistId;
    }

    public String getUri() { return uri; }

    public void setUri(String uri) { this.uri = uri; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }

    public void setArtist(String artist) { this.artist = artist; }

    public String getDuration() { return duration; }

    public void setDuration(String duration) { this.duration = duration; }

    public byte[] getSongCover() { return songCover; }

    public void setSongCover(byte[] songCover) { this.songCover = songCover; }

    public int getPlaylistId() { return playlistId; }
}
