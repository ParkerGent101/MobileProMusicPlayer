package com.example.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicplayer.Repository.PlayList;
import com.example.musicplayer.Repository.Song;
import java.util.ArrayList;

//Adapter for displaying playlists
public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.ViewHolder>{

    ArrayList<PlayList> playListsList;
    ArrayList<Song> songsList;
    Context context;

    public PlayListAdapter(ArrayList<PlayList> playListsList, ArrayList<Song> songsList, Context context) {
        this.playListsList = playListsList;
        this.songsList = songsList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.playlists_recycler_item,parent,false);
        return new PlayListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder( PlayListAdapter.ViewHolder holder, int position) {
        // Get current playlist object
        PlayList playListData = playListsList.get(position);
        // Set the title text to be the playlist name
        holder.titleTextView.setText(playListData.getName());
        // Iterate through the song list until the first song with a cover is found, this will be used for the playlist's cover
        for(int i = 0; i < songsList.size(); i++) {
            if (songsList.get(i).getPlaylistId() == playListData.getId() && songsList.get(i).getSongCover() != null) {
                holder.iconImageView.setImageBitmap(BitmapFactory.decodeByteArray(songsList.get(i).getSongCover(), 0, songsList.get(i).getSongCover().length));
                break;
            }
        }

        //If a playlist is clicked, it will open the playlist activity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Navigate to another activity
                if(context instanceof AddSongToPlayListActivity) {
                    ((AddSongToPlayListActivity) context).onPlaylistClicked(playListData.getId());
                }
                Intent intent = new Intent(context, PlayListActivity.class);
                intent.putExtra("playListId",playListData.getId());
                intent.putExtra("playListName", playListData.getName());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        //If a playlist is clicked and held, it will bring up a popup menu
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, view);
                popupMenu.getMenuInflater().inflate(R.menu.overflow_menu_item, popupMenu.getMenu());

                // Set menu item click listener
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    if(menuItem.getItemId() == R.id.delete_item) {
                        if (context instanceof LibraryActivity) {
                            ((LibraryActivity) context).deletePlaylist(playListData); // Call delete through the activity
                        }
                    }
                    return false;
                });
                popupMenu.show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return playListsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView titleTextView;
        ImageView iconImageView;

        public ViewHolder(View itemView){
            super(itemView);
            titleTextView = itemView.findViewById(R.id.playlist_title_text);
            iconImageView = itemView.findViewById(R.id.icon_view);
        }

    }
}