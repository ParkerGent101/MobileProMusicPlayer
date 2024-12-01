package com.example.musicplayer;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.musicplayer.Repository.Song;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Activity for the play screen
public class MusicPlayerActivity extends AppCompatActivity {
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private TextView titleTV, artistTv, currentTimeTV, totalTimeTv;
    private SeekBar seekBar;
    private ImageView pausePlay, nextBtn, previousBtn, musicIcon, backBtn, addToPlaylistBtn;
    private ImageView shuffleBtn, loopBtn;
    private MusicPlayerViewModel musicPlayerViewModel;
    private SongViewModel songViewModel;
    ArrayList<Song> songsList;

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
        shuffleBtn = findViewById(R.id.shuffle);
        loopBtn = findViewById(R.id.loop);

        // Set title marquee effect
        titleTV.setSelected(true);

        // Initialize ViewModel
        musicPlayerViewModel = MusicPlayerViewModel.getInstance(getApplication());
        songViewModel = new ViewModelProvider(this).get(SongViewModel.class);

        // Load in songs from database
        executorService.execute(this::loadSongs);

        // If songList is not null, set the song list in view model
        if(songsList != null)
            musicPlayerViewModel.setSongsList(songsList);

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
                    drawableId = R.drawable.song_loop;
                    break;
                default: // No loop
                    drawableId = R.drawable.no_loop_24;
            }
            loopBtn.setImageResource(drawableId);
        });

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
        shuffleBtn.setOnClickListener(v -> musicPlayerViewModel.toggleShuffle());
        shuffleBtn.setOnLongClickListener(v -> showPopUpMenu());
        loopBtn.setOnClickListener(v -> musicPlayerViewModel.toggleLoop());
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

    public void loadSongs() {
        ArrayList<Integer> songIdList = (ArrayList<Integer>) getIntent().getSerializableExtra("LIST", ArrayList.class);
        if(songIdList != null) {
            songsList = new ArrayList<Song>();
            for (Integer songId : songIdList) {
                songsList.add(songViewModel.getSong(songId));
            }

            if(songsList != null)
                runOnUiThread(() -> musicPlayerViewModel.setSongsList(songsList));
        }
    }

    private boolean showPopUpMenu(){
        PopupMenu popupMenu = new PopupMenu(MusicPlayerActivity.this, shuffleBtn);
        popupMenu.inflate(R.menu.overflow_menu_shuffle);
        Menu menu = popupMenu.getMenu();

        musicPlayerViewModel.getShuffleMode().observe(this, shuffleMode -> {
            Log.d("Shuffle Type", Integer.toString(shuffleMode));
            menu.getItem(shuffleMode).setChecked(true);
        });

        popupMenu.setOnMenuItemClickListener(item -> {
            if(item.getItemId() == R.id.random_shuffle) {
                for (int i = 0; i < menu.size(); i++) {
                    menu.getItem(i).setChecked(false);
                }
                item.setChecked(true);
                musicPlayerViewModel.setShuffleMode(0);
                return true;
            } else if (item.getItemId() == R.id.artist_shuffle) {
                for (int i = 0; i < menu.size(); i++) {
                    menu.getItem(i).setChecked(false);
                }
                item.setChecked(true);
                musicPlayerViewModel.setShuffleMode(1);
                return true;
            }
            return false;
        });
        popupMenu.show();
        return true;
    }
}