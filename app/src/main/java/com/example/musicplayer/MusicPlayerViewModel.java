package com.example.musicplayer;

import android.app.Application;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.musicplayer.Repository.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MusicPlayerViewModel extends AndroidViewModel {

    private final MutableLiveData<Song> currentSong = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentTime = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isShuffleEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> loopMode = new MutableLiveData<>(0); // 0: No loop, 1: Loop all, 2: Loop one

    private final ArrayList<Song> songsList = new ArrayList<>();
    private ArrayList<Song> originalList = new ArrayList<>();
    private final MediaPlayer mediaPlayer = MyMusicPlayer.getInstance();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateTask;
    private final Random random = new Random();

    public MusicPlayerViewModel(Application application) {
        super(application);
        initializeUpdateTask();
    }

    public void setSongsList(ArrayList<Song> songsList) {
        if (songsList == null || songsList.isEmpty()) return;

        this.originalList = new ArrayList<>(songsList);
        this.songsList.clear();
        this.songsList.addAll(songsList);

        // Ensure the current index is valid
        MyMusicPlayer.currentIndex = Math.min(MyMusicPlayer.currentIndex, this.songsList.size() - 1);
        setCurrentSong(this.songsList.get(MyMusicPlayer.currentIndex));
    }

    public LiveData<Boolean> isShuffleEnabled() {
        return isShuffleEnabled;
    }

    public LiveData<Integer> getLoopMode() {
        return loopMode;
    }

    public void toggleShuffle() {
        boolean newShuffleState = !Boolean.TRUE.equals(isShuffleEnabled.getValue());
        isShuffleEnabled.setValue(newShuffleState);

        Song current = songsList.get(MyMusicPlayer.currentIndex);
        if (newShuffleState) {
            Collections.shuffle(songsList, random);
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
        if (song == null) return;

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

            mediaPlayer.setOnCompletionListener(mp -> {
                int mode = loopMode.getValue() != null ? loopMode.getValue() : 0;
                switch (mode) {
                    case 0: // No loop
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
            Log.e("MusicPlayerViewModel", "Error playing music", e);
        }
    }

    public void playNextSong() {
        int mode = loopMode.getValue() != null ? loopMode.getValue() : 0;
        if (Boolean.TRUE.equals(isShuffleEnabled.getValue()) && mode != 2) {
            MyMusicPlayer.currentIndex = random.nextInt(songsList.size());
        } else if (MyMusicPlayer.currentIndex < songsList.size() - 1) {
            MyMusicPlayer.currentIndex++;
        } else if (mode == 1) { // Loop all
            MyMusicPlayer.currentIndex = 0;
        } else {
            return; // End of playlist
        }
        setCurrentSong(songsList.get(MyMusicPlayer.currentIndex));
    }

    public void playPreviousSong() {
        int mode = loopMode.getValue() != null ? loopMode.getValue() : 0;
        if (MyMusicPlayer.currentIndex > 0) {
            MyMusicPlayer.currentIndex--;
        } else if (mode == 1) { // Loop all
            MyMusicPlayer.currentIndex = songsList.size() - 1;
        }
        setCurrentSong(songsList.get(MyMusicPlayer.currentIndex));
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

    public static String convertToMMSS(String duration) {
        long millis = Long.parseLong(duration);
        return String.format(Locale.getDefault(), "%02d:%02d",
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

    private void startUpdatingTime() {
        handler.post(updateTask);
    }

    private void stopUpdatingTime() {
        handler.removeCallbacks(updateTask);
    }

    public void stopPlaying() {
        mediaPlayer.pause();
        isPlaying.setValue(false);
    }
}
