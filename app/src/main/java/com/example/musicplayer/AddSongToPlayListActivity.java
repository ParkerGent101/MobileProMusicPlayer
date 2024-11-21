package com.example.musicplayer;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicplayer.Repository.PlayList;
import com.example.musicplayer.Repository.Song;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Activity for adding a song to a playlist
public class AddSongToPlayListActivity extends AppCompatActivity {

    private PlayListViewModel playListViewModel;
    private SongViewModel songViewModel;
    private Song song;
    RecyclerView recyclerView;
    TextView cancelBtn;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    int playListId;

    @Override
    protected  void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song_to_playlist);

        song = getIntent().getSerializableExtra("song", Song.class);

        cancelBtn = findViewById(R.id.cancel_btn);
        recyclerView = findViewById(R.id.recycler_view);
        playListViewModel = new ViewModelProvider(this).get(PlayListViewModel.class);
        songViewModel = new ViewModelProvider(this).get(SongViewModel.class);

        cancelBtn.setOnClickListener( v-> {
            finish();
        });

        //List all of the playlists
        playListViewModel.getAllPlayLists().observe(this, playLists -> {
            ArrayList<PlayList> playListsList = new ArrayList<>(playLists);
            playListsList.remove(0);

            songViewModel.getAllSongs().observe(this, songs -> {
                ArrayList<Song> songsList = new ArrayList<>(songs);
                GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
                recyclerView.setLayoutManager(gridLayoutManager);
                final PlayListAdapter adapter = new PlayListAdapter(playListsList, songsList, this);
                recyclerView.setAdapter(adapter);
            });
        });

    }

    public void onPlaylistClicked(int itemId) {
        playListId = itemId;
        executorService.execute(this::addSongToPlaylist);
    }

    public void addSongToPlaylist() {
        if(!(songViewModel.songExists(song.getUri(), playListId))) {
            songViewModel.insert(new Song(song.getUri(), song.getTitle(), song.getArtist(), song.getDuration(), song.getSongCover(), playListId));
        }
        finish();
    }
}