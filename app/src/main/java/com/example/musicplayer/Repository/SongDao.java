package com.example.musicplayer.Repository;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface SongDao {
    @Insert
    void insert(Song song);

    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId")
    LiveData<List<Song>> getSongsForPlayList(int playlistId);

    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId")
    List<Song> getCurrentSongsForPlayList(int playlistId);

    @Query("SELECT * FROM playlist_songs")
    LiveData<List<Song>> getAllSongs();

    @Delete
    void deleteSong(Song song);

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    void clearPlayList(int playlistId);

    @Query("DELETE FROM playlist_songs")
    void deleteAll();

    @Query("SELECT EXISTS (SELECT 1 FROM playlist_songs WHERE uri = :uri AND playListId = :playListId)")
    boolean songExists(String uri, int playListId);
}