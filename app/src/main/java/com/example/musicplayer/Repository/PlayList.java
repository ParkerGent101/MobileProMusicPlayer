package com.example.musicplayer.Repository;

import android.util.Log;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "playlists")
public class PlayList {
    @PrimaryKey(autoGenerate = true)
    public int playlistId;

    public String name;

    public PlayList(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public int getId() { return playlistId; }

}
