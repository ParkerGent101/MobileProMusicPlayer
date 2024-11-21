package com.example.musicplayer;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
    private SongViewModel songViewModel;
    private PlayListViewModel playListViewModel;
    private int playListId;
    private String playListName = "Playlist";
    RecyclerView recyclerView;
    TextView noMusicTextView, playListNameTextView;
    ImageView backBtn;

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
    }

    public void deleteSong(Song song) {
        songViewModel.deleteSong(song);
    }
}
