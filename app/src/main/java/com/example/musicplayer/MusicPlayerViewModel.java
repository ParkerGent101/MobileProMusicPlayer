package com.example.musicplayer;

import android.app.Application;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import com.example.musicplayer.Repository.Song;

public class MusicPlayerViewModel extends AndroidViewModel {
    private final MutableLiveData<Song> currentSong = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentTime = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>();
    private ArrayList<Song> songsList;
    private MediaPlayer mediaPlayer = MyMusicPlayer.getInstance();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateTask;

    public MusicPlayerViewModel(Application application) {
        super(application);
        isPlaying.setValue(false);
        initializeUpdateTask();
    }

    public void setSongsList(ArrayList<Song> songsList) {
        this.songsList = songsList;
        setCurrentSong(songsList.get(MyMusicPlayer.currentIndex));
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
            mediaPlayer.setOnCompletionListener(mp -> playNextSong());
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
        if (MyMusicPlayer.currentIndex < songsList.size() - 1) {
            MyMusicPlayer.currentIndex++;
            setCurrentSong(songsList.get(MyMusicPlayer.currentIndex));
        }
    }

    public void playPreviousSong() {
        if (MyMusicPlayer.currentIndex > 0) {
            MyMusicPlayer.currentIndex--;
            setCurrentSong(songsList.get(MyMusicPlayer.currentIndex));
        }
    }

    public static String convertToMMSS(String duration) {
        Long millis = Long.parseLong(duration);
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    private void initializeUpdateTask() {
        updateTask = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    currentTime.postValue(mediaPlayer.getCurrentPosition());
                    handler.postDelayed(this, 100);
                }
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
        mediaPlayer.pause();
    }
}
