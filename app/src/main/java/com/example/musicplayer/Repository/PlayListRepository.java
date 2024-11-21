package com.example.musicplayer.Repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;

public class PlayListRepository {
    private PlayListDao playListDao;
    private LiveData<List<PlayList>> mAllPlayLists;

    public PlayListRepository(Application application) {
        MusicDatabase database = MusicDatabase.getDatabase(application);
        playListDao = database.playListDao();
        mAllPlayLists = playListDao.getAllPlaylists();
    }

    public void insert(PlayList playlist) {
        MusicDatabase.databaseWriteExecutor.execute(() -> {
            playListDao.insert(playlist);
        });
    }

    public LiveData<List<PlayList>> getAllPlayLists() {
        return mAllPlayLists;
    }

    public void deleteAll() {
        MusicDatabase.databaseWriteExecutor.execute(() -> {
            playListDao.deleteAll();
        });
    }

    public void delete(PlayList playlist) {
        MusicDatabase.databaseWriteExecutor.execute(() -> {
            playListDao.delete(playlist);
        });
    }

}
