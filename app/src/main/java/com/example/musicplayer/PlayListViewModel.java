package com.example.musicplayer;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.musicplayer.Repository.PlayList;
import com.example.musicplayer.Repository.PlayListRepository;
import java.util.List;

public class PlayListViewModel extends AndroidViewModel {
    private PlayListRepository playListRepository;

    private LiveData<List<PlayList>> mAllPlayLists;

    public PlayListViewModel (Application application) {
        super(application);
        playListRepository = new PlayListRepository(application);
        mAllPlayLists = playListRepository.getAllPlayLists();
    }

    public LiveData<List<PlayList>> getAllPlayLists() { return mAllPlayLists; }

    public void insert(PlayList playList) { playListRepository.insert(playList); }

    public void deleteAll() { playListRepository.deleteAll(); }

    public void delete(PlayList playlist) { playListRepository.delete(playlist); }

}
