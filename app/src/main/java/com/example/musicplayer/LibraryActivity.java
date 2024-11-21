package com.example.musicplayer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.PopupMenu;
import com.example.musicplayer.Repository.PlayList;
import com.example.musicplayer.Repository.Song;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Activity for the library screen
public class LibraryActivity extends AppCompatActivity{

    // Initialize variables
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private SongViewModel songViewModel;
    private PlayListViewModel playListViewModel;
    RecyclerView recyclerView;
    TextView noMusicTextView, songsBtnText, playListsBtnText;
    ImageView songsBar, playListBar, menuBtn;

    @Override
    protected  void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create references
        recyclerView = findViewById(R.id.recycler_view);
        noMusicTextView = findViewById(R.id.no_songs_text);
        songViewModel = new ViewModelProvider(this).get(SongViewModel.class);
        playListViewModel = new ViewModelProvider(this).get(PlayListViewModel.class);
        songsBtnText = findViewById(R.id.songs_button);
        playListsBtnText = findViewById(R.id.playlists_button);
        songsBar = findViewById(R.id.songs_black_bar);
        playListBar = findViewById(R.id.playlists_black_bar);
        menuBtn = findViewById(R.id.menu);

        // Make the black bar below the playlist button invisible
        playListBar.setVisibility(View.INVISIBLE);

        //Make sure user has correct permissions
        if(!checkPermission()){
            requestPermission();
        } else {
            // These two calls are placeholders until I make it so the user manually adds songs from their files
            // Runs loadSongs function on a new thread
            executorService.execute(this::loadSongs);
            // Clean out any songs that no longer have viable paths
            cleanDatabase();
        }

        //Display all the songs when activity starts
        displaySongs();

        // Set on click listeners for buttons
        songsBtnText.setOnClickListener(v -> displaySongs());
        playListsBtnText.setOnClickListener(v -> displayPlaylists());
        menuBtn.setOnClickListener(v -> showPopUpMenu());

    }

    // Display all songs
    private void displaySongs() {
        playListBar.setVisibility(View.INVISIBLE);
        songsBar.setVisibility(View.VISIBLE);
        songViewModel.getSongsForPlayList(1).observe(this, songs -> {
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
    }

    // Display playlists
    private void displayPlaylists() {
        songsBar.setVisibility(View.INVISIBLE);
        playListBar.setVisibility(View.VISIBLE);

        playListViewModel.getAllPlayLists().observe(this, playLists -> {
            ArrayList<PlayList> playListsList = new ArrayList<>(playLists);
            playListsList.remove(0);
            noMusicTextView.setVisibility(View.INVISIBLE);

            songViewModel.getAllSongs().observe(this, songs -> {
                ArrayList<Song> songsList = new ArrayList<>(songs);
                final PlayListAdapter adapter = new com.example.musicplayer.PlayListAdapter(playListsList, songsList, this);
                recyclerView.setAdapter(adapter);
            });
        });
    }

    //Load in songs from device storage
    //This function is a placeholder until I get manual song adding set up
    private void loadSongs() {
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Cursor cursor = getContentResolver().query(Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                byte[] songCover = getSongCover(cursor.getString(1));
                Song songData = new Song(cursor.getString(1), cursor.getString(0), cursor.getString(3), cursor.getString(2), songCover, 1);
                if (new File(songData.getUri()).exists()) {
                    if(!songViewModel.songExists(songData.uri, 1))
                        songViewModel.insert(songData);
                }
            }
            cursor.close();
        }
    }

    //Display popup menu when three dots clicked
    private void showPopUpMenu() {
        PopupMenu popupMenu = new PopupMenu(LibraryActivity.this, menuBtn);
        popupMenu.getMenuInflater().inflate(R.menu.overflow_menu_library, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.add_playlist) {
                    Intent intent = new Intent(LibraryActivity.this, AddNewPlayListActivity.class);
                    startActivity(intent);
                }
                return true;
            }
        });
        popupMenu.show();
    }

    // Deletes any songs from database that no longer exist in files
    private void cleanDatabase() {
        LiveData<List<Song>> allSongsInDB = songViewModel.getAllSongs();
        allSongsInDB.observe(this, songs -> {
            for(int i = 0; i < songs.size(); i++) {
                if(!(new File(songs.get(i).getUri()).exists())) {
                    songViewModel.deleteSong(songs.get(i));
                }
            }
        });
    }

    //Extract image from song metadata
    public byte[] getSongCover(String path) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        return mmr.getEmbeddedPicture();
    }

    //Calls the view model to delete a song
    public void deleteSong(Song song) {
        songViewModel.deleteSong(song);
    }

    //Calls the view model to delete a playlist and all its corresponding songs
    public void deletePlaylist(PlayList playlist) {
        songViewModel.clearPlayList(playlist.getId());
        playListViewModel.delete(playlist);
    }



    // ALL OF THIS BELOW IS PERMISSION STUFF \\

    //PermissionCheck
    boolean checkPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    //PermissionRequest
    void requestPermission(){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(LibraryActivity.this,Manifest.permission.READ_MEDIA_AUDIO)){
                Toast.makeText(LibraryActivity.this, "READ PERMISSION IS REQUIRED. PLEASE ALLOW FROM SETTINGS",Toast.LENGTH_SHORT);
            }

            ActivityCompat.requestPermissions(LibraryActivity.this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 123);
        } else {
            if(ActivityCompat.shouldShowRequestPermissionRationale(LibraryActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Toast.makeText(LibraryActivity.this, "READ PERMISSION IS REQUIRED. PLEASE ALLOW FROM SETTINGS",Toast.LENGTH_SHORT);
            }
            ActivityCompat.requestPermissions(LibraryActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, reload the songs list
                executorService.execute(this::loadSongs);
                cleanDatabase();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
