package com.example.musicplayer;

import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MusicPlayerActivity extends AppCompatActivity {

    TextView titleTV, artistTv, currentTimeTV,totalTimeTv;
    SeekBar seekBar;
    ImageView backButton, pausePlay,nextBtn,previousBtn,musicIcon;
    ArrayList<AudioModel> songsList;
    AudioModel currentSong;
    MediaPlayer mediaPlayer = MyMusicPlayer.getInstace();

    @Override
    protected  void onCreate(Bundle saveInstaceState) {
        super.onCreate(saveInstaceState);
        setContentView(R.layout.activity_music_player);

        titleTV = findViewById(R.id.song_title);
        titleTV.setSelected(true);
        artistTv = findViewById(R.id.song_artist);

        currentTimeTV = findViewById(R.id.current_time);
        totalTimeTv = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seek_bar);
        pausePlay = findViewById(R.id.pause_play);
        nextBtn = findViewById(R.id.next);
        previousBtn = findViewById(R.id.previous);
        musicIcon = findViewById(R.id.music_icon_big);
        backButton = findViewById(R.id.go_back);


        songsList = (ArrayList<AudioModel>) getIntent().getSerializableExtra("LIST", ArrayList.class);

        setResourcesWithMusic();

        MusicPlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer!=null){
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    currentTimeTV.setText(convertToMMSS(mediaPlayer.getCurrentPosition()+""));

                    if(mediaPlayer.isPlaying()){
                        pausePlay.setImageResource(R.drawable.pause_48);
                    }else{
                        pausePlay.setImageResource(R.drawable.play_48);
                    }
                }
                new Handler(Looper.getMainLooper()).postDelayed(this, 100);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer!=null && fromUser){
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    void setResourcesWithMusic(){
        currentSong = songsList.get(MyMusicPlayer.currentIndex);

        titleTV.setText(currentSong.getTitle());
        artistTv.setText(currentSong.getArtist());

        totalTimeTv.setText(convertToMMSS(currentSong.getDuration()));

        if (currentSong.getSongCover() != null)
            musicIcon.setImageBitmap(BitmapFactory.decodeByteArray(currentSong.getSongCover(), 0, currentSong.getSongCover().length));
        else
            musicIcon.setImageResource(R.drawable.cover_placeholder);

        pausePlay.setOnClickListener(v-> pausePlay());
        nextBtn.setOnClickListener(v-> playNextSong());
        previousBtn.setOnClickListener(v-> playPreviousSong());
        backButton.setOnClickListener(v-> goBack());

        playMusic();
    }

    private void playMusic(){

        mediaPlayer.reset();
        try{
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar.setProgress(0);
            seekBar.setMax(mediaPlayer.getDuration());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void playNextSong(){
        if(MyMusicPlayer.currentIndex == songsList.size() - 1)
            return;
        MyMusicPlayer.currentIndex += 1;
        mediaPlayer.reset();
        setResourcesWithMusic();
    }

    private void playPreviousSong(){
        if(MyMusicPlayer.currentIndex == 0)
            return;
        MyMusicPlayer.currentIndex -= 1;
        mediaPlayer.reset();
        setResourcesWithMusic();
    }

    private void pausePlay(){
        if(mediaPlayer.isPlaying())
            mediaPlayer.pause();
        else{
            mediaPlayer.start();
        }
    }

    private void goBack(){
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.reset();
        }
        finish();
    }

    public static String convertToMMSS(String duration){
        Long millis = Long.parseLong(duration);
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

}
