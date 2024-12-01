package com.example.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicplayer.Repository.Song;
import java.util.ArrayList;

//Adapter for displaying songs
public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder>{

    ArrayList<Song> songsList;
    Context context;

    public SongListAdapter(ArrayList<Song> songsList, Context context) {
        this.songsList = songsList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.songs_recycler_item,parent,false);
        return new SongListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SongListAdapter.ViewHolder holder, int position) {
        // Get current song object
        Song songData = songsList.get(position);
        // Update title and artist views with song data
        holder.titleTextView.setText(songData.getTitle());
        holder.artistTextView.setText(songData.getArtist());
        // Update the song cover if it has one
        if (songData.getSongCover() != null)
            holder.iconImageView.setImageBitmap(BitmapFactory.decodeByteArray(songData.getSongCover(), 0, songData.getSongCover().length));

        //If a song is clicked on it will start the music player activity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Navigate to another activity
                int currentPosition = holder.getAdapterPosition(); // Always get the current position
                if (currentPosition != RecyclerView.NO_POSITION) {
                    MyMusicPlayer.currentIndex = currentPosition;

                    Intent intent = new Intent(context, MusicPlayerActivity.class);

                    ArrayList<Integer> songIdList = new ArrayList<Integer>();
                    for (Song song : songsList) {
                        songIdList.add(song.getSongId());
                    }

                    intent.putExtra("LIST", songIdList);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        });

        //If a song is clicked and held, it will bring up a popup menu
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int currentPosition = holder.getAdapterPosition(); // Always get the current position
                if (currentPosition != RecyclerView.NO_POSITION) {
                    PopupMenu popupMenu = new PopupMenu(context, view);
                    popupMenu.getMenuInflater().inflate(R.menu.overflow_menu_item, popupMenu.getMenu());

                    // Set menu item click listener
                    popupMenu.setOnMenuItemClickListener(menuItem -> {
                        if (menuItem.getItemId() == R.id.delete_item) {
                            if (context instanceof LibraryActivity) {
                                ((LibraryActivity) context).deleteSong(songData);
                            } else if (context instanceof PlayListActivity) {
                                ((PlayListActivity) context).deleteSong(songData);
                            }
                        }
                        return true;
                    });
                    popupMenu.show();
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView titleTextView;
        ImageView iconImageView;
        TextView artistTextView;

        public ViewHolder(View itemView){
            super(itemView);
            titleTextView = itemView.findViewById(R.id.music_title_text);
            iconImageView = itemView.findViewById(R.id.icon_view);
            artistTextView = itemView.findViewById(R.id.music_artist_text);
        }

    }
}
