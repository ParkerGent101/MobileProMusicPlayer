package com.example.musicplayer;

import android.media.MediaPlayer;

public class MyMusicPlayer {
    static MediaPlayer instance;

    public static MediaPlayer getInstace(){
        if(instance == null){
            instance = new MediaPlayer();
        }
        return instance;
    }

    public static int currentIndex = -1;
}
