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

public class MusicPlayerActivity extends AppCompatActivity {
    private TextView titleTV, artistTv, currentTimeTV, totalTimeTv;
    private SeekBar seekBar;
    private ImageView pausePlay, nextBtn, previousBtn, musicIcon, backBtn, addToPlaylistBtn;
    private ImageView shuffleBtn, loopBtn;
    private MusicPlayerViewModel musicPlayerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        // Initialize UI components
        initUIComponents();

        // Initialize ViewModel
        musicPlayerViewModel = new ViewModelProvider(this).get(MusicPlayerViewModel.class);
        ArrayList<Song> songsList = (ArrayList<Song>) getIntent().getSerializableExtra("LIST");
        musicPlayerViewModel.setSongsList(songsList);

        // Set up observers
        setupObservers();

        // Set up click listeners
        setupClickListeners();

        // Set up seek bar listener
        setupSeekBar();
    }

    private void initUIComponents() {
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
        shuffleBtn = findViewById(R.id.shuffle);
        loopBtn = findViewById(R.id.loop);

        // Enable marquee effect for title
        titleTV.setSelected(true);
    }

    private void setupObservers() {
        // Shuffle state observer
        musicPlayerViewModel.isShuffleEnabled().observe(this, isShuffleEnabled ->
                shuffleBtn.setImageResource(isShuffleEnabled ? R.drawable.shuffle_24: R.drawable.no_shuffle_24)
        );

        // Loop mode observer
        musicPlayerViewModel.getLoopMode().observe(this, loopMode -> {
            int drawableId;
            switch (loopMode) {
                case 1: // Loop all
                    drawableId = R.drawable.loop_24;
                    break;
                case 2: // Loop one, need to update drawables just made AI generated ones to differentiate which is running at a given time
                    drawableId = R.drawable.loop_24_song;
                    break;
                default: // No loop
                    drawableId = R.drawable.loop_24_pl;
            }
            loopBtn.setImageResource(drawableId);
        });

        // Current song observer
        musicPlayerViewModel.getCurrentSong().observe(this, song -> {
            if (song != null) {
                updateSongDetails(song);
            }
        });

        // Play/pause state observer
        musicPlayerViewModel.isPlaying().observe(this, isPlaying ->
                pausePlay.setImageResource(isPlaying ? R.drawable.pause_48 : R.drawable.play_48)
        );

        // Current time observer
        musicPlayerViewModel.getCurrentTime().observe(this, time -> {
            if (time != null) {
                seekBar.setProgress(time);
                currentTimeTV.setText(MusicPlayerViewModel.convertToMMSS(String.valueOf(time)));
            }
        });
    }

    private void setupClickListeners() {
        pausePlay.setOnClickListener(v -> musicPlayerViewModel.togglePlayPause());
        nextBtn.setOnClickListener(v -> musicPlayerViewModel.playNextSong());
        previousBtn.setOnClickListener(v -> musicPlayerViewModel.playPreviousSong());
        shuffleBtn.setOnClickListener(v -> musicPlayerViewModel.toggleShuffle());
        loopBtn.setOnClickListener(v -> musicPlayerViewModel.toggleLoop());
        backBtn.setOnClickListener(v -> finish());
        addToPlaylistBtn.setOnClickListener(v -> {
            Song currentSong = musicPlayerViewModel.getCurrentSong().getValue();
            if (currentSong != null) {
                Intent intent = new Intent(MusicPlayerActivity.this, AddSongToPlayListActivity.class);
                intent.putExtra("song", currentSong);
                startActivity(intent);
                musicPlayerViewModel.stopPlaying();
                finish();
            }
        });
    }

    private void setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    musicPlayerViewModel.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateSongDetails(Song song) {
        seekBar.setProgress(0);
        seekBar.setMax(musicPlayerViewModel.getDuration());
        titleTV.setText(song.getTitle());
        artistTv.setText(song.getArtist());
        totalTimeTv.setText(MusicPlayerViewModel.convertToMMSS(song.getDuration()));
        // Set song cover image
        if (song.getSongCover() != null) {
            musicIcon.setImageBitmap(BitmapFactory.decodeByteArray(song.getSongCover(), 0, song.getSongCover().length));
        } else {
            musicIcon.setImageResource(R.drawable.cover_placeholder);
        }
    }
}
