package com.example.musicplayer.Repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;

public class SongRepository {
    private SongDao songDao;
    private LiveData<List<Song>> mAllSongs;

    public SongRepository(Application application) {
        MusicDatabase database = MusicDatabase.getDatabase(application);
        songDao = database.songDao();
        mAllSongs = songDao.getAllSongs();
    }

    public void insert(Song song) {
        MusicDatabase.databaseWriteExecutor.execute(() -> {
            songDao.insert(song);
        });
    }

    public LiveData<List<Song>> getAllSongs() {
        return mAllSongs;
    }

    public void deleteAll() {
        MusicDatabase.databaseWriteExecutor.execute(() -> {
            songDao.deleteAll();
        });
    }

    public void clearPlayList(int playListId) {
        MusicDatabase.databaseWriteExecutor.execute(() -> {
            songDao.clearPlayList(playListId);
        });
    }

    public LiveData<List<Song>> getSongsForPlayList(int playListId) {
        return songDao.getSongsForPlayList(playListId);
    }

    public List<Song> getCurrentSongsForPlayList(int playListId) {
        return songDao.getCurrentSongsForPlayList(playListId);
    }

    public void deleteSong(Song song) {
        MusicDatabase.databaseWriteExecutor.execute(() -> {
            songDao.deleteSong(song);
        });
    }

    public boolean songExists(String uri, int playListId) {
        return songDao.songExists(uri, playListId);
    }

}