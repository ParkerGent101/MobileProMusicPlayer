package com.example.musicplayer.Repository;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface PlayListDao {
    @Insert
    void insert(PlayList playlist);

    @Query("SELECT * FROM playlists")
    LiveData<List<PlayList>> getAllPlaylists();

    @Delete
    void delete(PlayList playlist);

    @Query("DELETE FROM playlists")
    void deleteAll();
}