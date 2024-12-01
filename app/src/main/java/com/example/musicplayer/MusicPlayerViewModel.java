package com.example.musicplayer;

import android.app.Application;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import com.example.musicplayer.Repository.Song;

public class MusicPlayerViewModel extends AndroidViewModel {
    public final MutableLiveData<Song> currentSong = new MutableLiveData<>();
    public final MutableLiveData<Integer> currentTime = new MutableLiveData<>(0);
    public final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isShuffleEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> loopMode = new MutableLiveData<>(0); // 0: No loop, 1: Loop all, 2: Loop one
    private final MutableLiveData<Integer> shuffleMode = new MutableLiveData<>(0); // 0: Random Shuffle, 1: Artist Shuffle

    public final MutableLiveData<ArrayList<Song>> currentSongsList = new MutableLiveData<>();
    private ArrayList<Song> songsList;
    private ArrayList<Song> originalList = new ArrayList<>();
    private MediaPlayer mediaPlayer = MyMusicPlayer.getInstance();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateTask;
    private final Random random = new Random();

    private static MusicPlayerViewModel instance;

    public static synchronized MusicPlayerViewModel getInstance(Application application) {
        if (instance == null) {
            instance = new ViewModelProvider.AndroidViewModelFactory(application)
                    .create(MusicPlayerViewModel.class);
        }
        return instance;
    }

    public MusicPlayerViewModel(Application application) {
        super(application);
        isPlaying.setValue(false);
        initializeUpdateTask();
    }

    public void setSongsList(ArrayList<Song> songsList) {
        if (songsList == null || songsList.isEmpty()) return;

        this.originalList = new ArrayList<>(songsList);
        this.songsList = songsList;
        this.currentSongsList.setValue(songsList);

        MyMusicPlayer.currentIndex = Math.min(MyMusicPlayer.currentIndex, this.songsList.size() - 1);
        setCurrentSong(songsList.get(MyMusicPlayer.currentIndex));
    }

    public LiveData<Boolean> isShuffleEnabled() {
        return isShuffleEnabled;
    }

    public LiveData<Integer> getLoopMode() {
        return loopMode;
    }

    public void setShuffleMode(int shuffleMode) {
        this.shuffleMode.setValue(shuffleMode);
    }

    public LiveData<Integer> getShuffleMode() { return shuffleMode; }

    public void randomShuffle(Song current) {
        ArrayList<Song> tempSongsList = songsList;
        tempSongsList.remove(current);

        Collections.shuffle(tempSongsList, random);
        songsList = tempSongsList;
        songsList.add(0, current);
    }

    public void artistShuffle(Song current) {
        ArrayList<Song> tempSongsList = songsList;
        tempSongsList.remove(current);

        Map<String, ArrayList<Song>> artistTypeMap = new HashMap<>();
        for (Song song : tempSongsList) {
            artistTypeMap.computeIfAbsent(song.getArtist(), k -> new ArrayList<>()).add(song);
        }

        ArrayList<String> artistTypes = new ArrayList<>(artistTypeMap.keySet());
        Collections.shuffle(artistTypes);

        ArrayList<Song> shuffledSongs = new ArrayList<>();
        for (String artistType : artistTypes) {
            shuffledSongs.addAll(artistTypeMap.get(artistType));
        }

        songsList = shuffledSongs;
        songsList.add(0, current);
    }

    public void toggleShuffle() {
        boolean newShuffleState = !Boolean.TRUE.equals(isShuffleEnabled.getValue());
        isShuffleEnabled.setValue(newShuffleState);

        Song current = songsList.get(MyMusicPlayer.currentIndex);
        if (newShuffleState) {
            // Random Shuffle
            if(shuffleMode.getValue() == 0)
                randomShuffle(current);
            // Artist Shuffle
            else if(shuffleMode.getValue() == 1)
                artistShuffle(current);

        } else {
            songsList.clear();
            songsList.addAll(originalList);
        }
        MyMusicPlayer.currentIndex = songsList.indexOf(current);
    }

    public void toggleLoop() {
        int currentMode = loopMode.getValue() != null ? loopMode.getValue() : 0;
        loopMode.setValue((currentMode + 1) % 3); // Cycle through 0 -> 1 -> 2
    }


    public LiveData<ArrayList<Song>> getCurrentSongList() { return currentSongsList; }

    public LiveData<Song> getCurrentSong() {
        return currentSong;
    }

    public LiveData<Integer> getCurrentTime() {
        return currentTime;
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public LiveData<Boolean> isPlaying() {
        return isPlaying;
    }

    public void setCurrentSong(Song song) {
        playMusic(song);
        currentSong.setValue(song);
        startUpdatingTime();
    }

    private void playMusic(Song song) {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(song.getUri());
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying.setValue(true);

            // Set a listener to update the current time
            mediaPlayer.setOnCompletionListener(mp -> {
                int mode = loopMode.getValue() != null ? loopMode.getValue() : 0;
                switch (mode) {
                    case 0: // No loop
                        stopUpdatingTime();
                        stopPlaying();
                    case 1: // Loop all
                        playNextSong();
                        break;
                    case 2: // Loop one
                        mp.seekTo(0);
                        mp.start();
                        break;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void togglePlayPause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying.setValue(false);
            stopUpdatingTime();
        } else {
            mediaPlayer.start();
            isPlaying.setValue(true);
            startUpdatingTime();
        }
    }

    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
        currentTime.postValue(mediaPlayer.getCurrentPosition());
    }

    public void playNextSong() {
        int mode = loopMode.getValue() != null ? loopMode.getValue() : 0;
        if(mode == 0 && MyMusicPlayer.currentIndex < songsList.size() - 1) {
            MyMusicPlayer.currentIndex++;
        }
        else if(mode == 0) {
            return;
        }
        else if (mode != 0 && MyMusicPlayer.currentIndex < songsList.size() - 1) {
            MyMusicPlayer.currentIndex++;
        } else if(mode != 0) {
            MyMusicPlayer.currentIndex = 0;
        }
        setCurrentSong(songsList.get(MyMusicPlayer.currentIndex));
    }

    public void playPreviousSong() {
        int mode = loopMode.getValue() != null ? loopMode.getValue() : 0;

        if(mode == 0 && MyMusicPlayer.currentIndex > 0) {
            MyMusicPlayer.currentIndex--;
        }
        else if(mode == 0) {
            return;
        }
        else if (mode != 0 && MyMusicPlayer.currentIndex > 0) {
            MyMusicPlayer.currentIndex--;
        } else if (mode != 0) { // Loop all
            MyMusicPlayer.currentIndex = songsList.size() - 1;
        }
        setCurrentSong(songsList.get(MyMusicPlayer.currentIndex));
    }

    public static String convertToMMSS(String duration) {
        Long millis = Long.parseLong(duration);
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    private void initializeUpdateTask() {
        updateTask = () -> {
            if (mediaPlayer.isPlaying()) {
                currentTime.postValue(mediaPlayer.getCurrentPosition());
                handler.postDelayed(updateTask, 100);
            }
        };
    }

    public void startUpdatingTime() {
        if (mediaPlayer != null) {
            handler.post(updateTask);
            isPlaying.postValue(mediaPlayer.isPlaying());
        }
    }

    public void stopUpdatingTime() {
        handler.removeCallbacks(updateTask);
        isPlaying.postValue(false);
    }

    public void stopPlaying() {
        isPlaying.setValue(false);
        mediaPlayer.pause();
    }
}
