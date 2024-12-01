package com.example.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicplayer.Repository.Song;
import java.util.ArrayList;

//Activity for playlist view
public class PlayListActivity extends AppCompatActivity{

    //Intitialize variables
    private MusicPlayerViewModel musicPlayerViewModel;
    private SongViewModel songViewModel;
    private PlayListViewModel playListViewModel;
    private int playListId;
    private String playListName = "Playlist";
    RecyclerView recyclerView;
    TextView noMusicTextView, playListNameTextView;
    ImageView backBtn;

    LinearLayout musicControlPanel;
    TextView songTitle;
    ImageView playPause, previous, next;
    ArrayList<Song> currentSongList;


    @Override
    protected  void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        playListId = getIntent().getIntExtra("playListId", 0);
        playListName = getIntent().getStringExtra("playListName");

        recyclerView = findViewById(R.id.recycler_view);
        noMusicTextView = findViewById(R.id.no_songs_text);
        playListNameTextView = findViewById(R.id.playlist_name);
        backBtn = findViewById(R.id.go_back);
        songViewModel = new ViewModelProvider(this).get(SongViewModel.class);
        playListViewModel = new ViewModelProvider(this).get(PlayListViewModel.class);

        playListNameTextView.setText(playListName);

        songViewModel.getSongsForPlayList(playListId).observe(this, songs -> {
            // Update the cached copy of the words in the adapter.
            ArrayList<Song> songsList = new ArrayList<>(songs);
            if (songsList.isEmpty()) {
                noMusicTextView.setVisibility(View.VISIBLE);
            } else {
                noMusicTextView.setVisibility(View.INVISIBLE);
            }
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
            recyclerView.setLayoutManager(gridLayoutManager);
            final SongListAdapter adapter = new SongListAdapter(songsList, this);
            recyclerView.setAdapter(adapter);
        });

        backBtn.setOnClickListener( v-> {
                finish();
        });


        musicPlayerViewModel = MusicPlayerViewModel.getInstance(getApplication());
        // Music Controller
        songTitle = findViewById(R.id.songTitle);
        playPause = findViewById(R.id.pause_play);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        musicControlPanel = findViewById(R.id.control_panel);

        songTitle.setSelected(true);

        playPause.setOnClickListener(v -> {
            musicPlayerViewModel.togglePlayPause();
        });

        previous.setOnClickListener(v -> {
            musicPlayerViewModel.playPreviousSong();
        });

        next.setOnClickListener(v -> {
            musicPlayerViewModel.playNextSong();
        });

        musicPlayerViewModel.getCurrentSongList().observe(this, songList -> {
            currentSongList = songList;
        });

        musicPlayerViewModel.getCurrentSong().observe(this, song -> {
            songTitle.setText(song.getTitle());
        });

        musicPlayerViewModel.isPlaying().observe(this, isPlaying -> {
            if (isPlaying) {
                playPause.setImageResource(R.drawable.pause_48);
            } else {
                playPause.setImageResource(R.drawable.play_48);
            }
        });

        musicControlPanel.setOnClickListener(v -> {
            if(currentSongList != null) {
                Intent intent = new Intent(this, MusicPlayerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        songTitle.setSelected(true);

        playPause.setOnClickListener(v -> {
            musicPlayerViewModel.togglePlayPause();
        });

        previous.setOnClickListener(v -> {
            musicPlayerViewModel.playPreviousSong();
        });

        next.setOnClickListener(v -> {
            musicPlayerViewModel.playNextSong();
        });

        musicPlayerViewModel.getCurrentSongList().observe(this, songList -> {
            currentSongList = songList;
        });

        musicPlayerViewModel.getCurrentSong().observe(this, song -> {
            songTitle.setText(song.getTitle());
        });

        musicPlayerViewModel.isPlaying().observe(this, isPlaying -> {
            if (isPlaying) {
                playPause.setImageResource(R.drawable.pause_48);
            } else {
                playPause.setImageResource(R.drawable.play_48);
            }
        });

        musicControlPanel.setOnClickListener(v -> {
            if(currentSongList != null) {
                Intent intent = new Intent(this, MusicPlayerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

    }

    public void deleteSong(Song song) {
        songViewModel.deleteSong(song);
    }
}
