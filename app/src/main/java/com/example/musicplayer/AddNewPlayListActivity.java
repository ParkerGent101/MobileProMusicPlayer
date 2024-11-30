package com.example.musicplayer;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.musicplayer.Repository.PlayList;

//Activity for creating a new playlist
public class AddNewPlayListActivity extends AppCompatActivity {

    // Initialize variables
    private PlayListViewModel playListViewModel;
    EditText playListNameEditText;
    TextView cancelBtn, createBtn;

    @Override
    protected  void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_playlist);

        playListNameEditText = findViewById(R.id.playlist_title);
        cancelBtn = findViewById(R.id.cancel_btn);
        createBtn = findViewById(R.id.create_btn);

        playListViewModel = new ViewModelProvider(this).get(PlayListViewModel.class);

        cancelBtn.setOnClickListener( v-> {
            finish();
        });

        createBtn.setOnClickListener( v-> {
            playListViewModel.insert(new PlayList(playListNameEditText.getText().toString()));
            finish();
        });
    }
}
