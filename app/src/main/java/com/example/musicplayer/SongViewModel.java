package com.example.musicplayer;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.musicplayer.Repository.Song;
import com.example.musicplayer.Repository.SongRepository;

import java.util.List;

public class SongViewModel extends AndroidViewModel {
    private SongRepository songRepository;

    private LiveData<List<Song>> mAllSongs;

    public SongViewModel (Application application) {
        super(application);
        songRepository = new SongRepository(application);
        mAllSongs = songRepository.getAllSongs();
    }

    public LiveData<List<Song>> getAllSongs() { return mAllSongs; }

    public void insert(Song song) { songRepository.insert(song); }

    public void clearPlayList(int playListId) { songRepository.clearPlayList(playListId); }

    public LiveData<List<Song>> getSongsForPlayList(int playListId) { return songRepository.getSongsForPlayList(playListId);}

    public void deleteSong(Song song) { songRepository.deleteSong(song);}

    public boolean songExists(String uri, int playListId) { return songRepository.songExists(uri, playListId); }

    public List<Song> getCurrentSongsForPlayList(int playListId) { return songRepository.getCurrentSongsForPlayList(playListId); }

    public Song getSong(int songId) { return songRepository.getSong(songId); }

}
