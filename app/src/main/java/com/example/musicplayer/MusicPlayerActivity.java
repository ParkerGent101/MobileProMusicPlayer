package com.example.musicplayer;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.musicplayer.Repository.Song;
import java.util.ArrayList;

// Activity for the play screen
public class MusicPlayerActivity extends AppCompatActivity {
    private TextView titleTV, artistTv, currentTimeTV, totalTimeTv;
    private SeekBar seekBar;
    private ImageView pausePlay, nextBtn, previousBtn, musicIcon, backBtn, addToPlaylistBtn;
    private MusicPlayerViewModel musicPlayerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        // Initialize UI components
        titleTV = findViewById(R.id.song_title);
        artistTv = findViewById(R.id.song_artist);
        currentTimeTV = findViewById(R.id.current_time);
        totalTimeTv = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seek_bar);
        pausePlay = findViewById(R.id.pause_play);
        nextBtn = findViewById(R.id.next);
        previousBtn = findViewById(R.id.previous);
        musicIcon = findViewById(R.id.music_icon_big);
        backBtn = findViewById(R.id.go_back);
        addToPlaylistBtn = findViewById(R.id.add_to_playlist);

        // Set title marquee effect
        titleTV.setSelected(true);

        // Initialize ViewModel
        musicPlayerViewModel = new ViewModelProvider(this).get(MusicPlayerViewModel.class);
        ArrayList<Song> songsList = (ArrayList<Song>) getIntent().getSerializableExtra("LIST", ArrayList.class);
        musicPlayerViewModel.setSongsList(songsList);

        // Observe ViewModel
        musicPlayerViewModel.getCurrentSong().observe(this, song -> {
            if (song != null) {
                seekBar.setProgress(0);
                seekBar.setMax(musicPlayerViewModel.getDuration());
                titleTV.setText(song.getTitle());
                artistTv.setText(song.getArtist());
                totalTimeTv.setText(MusicPlayerViewModel.convertToMMSS(song.getDuration()));
                // Set cover image
                if (song.getSongCover() != null) {
                    musicIcon.setImageBitmap(BitmapFactory.decodeByteArray(song.getSongCover(), 0, song.getSongCover().length));
                } else {
                    musicIcon.setImageResource(R.drawable.cover_placeholder);
                }
            }
        });

        musicPlayerViewModel.isPlaying().observe(this, isPlaying -> {
            if (isPlaying) {
                pausePlay.setImageResource(R.drawable.pause_48);
            } else {
                pausePlay.setImageResource(R.drawable.play_48);
            }
        });

        // UI interactions
        pausePlay.setOnClickListener(v -> musicPlayerViewModel.togglePlayPause());
        nextBtn.setOnClickListener(v -> musicPlayerViewModel.playNextSong());
        previousBtn.setOnClickListener(v -> musicPlayerViewModel.playPreviousSong());
        backBtn.setOnClickListener(v -> finish());
        addToPlaylistBtn.setOnClickListener(v -> {
            musicPlayerViewModel.getCurrentSong().observe(this, song -> {
                if (song != null) {
                    Intent intent = new Intent(MusicPlayerActivity.this, AddSongToPlayListActivity.class);
                    intent.putExtra("song", song);
                    startActivity(intent);
                    musicPlayerViewModel.stopPlaying();
                    finish();
                }
            });
        });

        // Seek bar update loop
        musicPlayerViewModel.getCurrentTime().observe(this, time -> {
            if (time != null) {
                seekBar.setProgress(time);
                currentTimeTV.setText(MusicPlayerViewModel.convertToMMSS(time + ""));
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    musicPlayerViewModel.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }
}