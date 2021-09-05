package com.example.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {


    private static final String TAG = "MyTag";
    private final String[] songs;
    private Context context;
    private final String[] songsAbsolutePaths;

    public Adapter(Context context, String[] songs,String[] songsAbsolutePaths) {
        this.context = context;
        this.songs = songs;
        this.songsAbsolutePaths = songsAbsolutePaths;
    }

    @NonNull
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull Adapter.MyViewHolder holder, int position) {
        holder.songTxtView.setText(songs[position]);

    }

    @Override
    public int getItemCount() {
        return songs.length;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView songTxtView;

        public MyViewHolder(@NonNull @org.jetbrains.annotations.NotNull View itemView) {
            super(itemView);

            songTxtView = itemView.findViewById(R.id.txtSong);
            songTxtView.setSelected(true); // to scroll the song name or text view horizontally

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startPlayerActivity();
                }
            });
        }

        private void startPlayerActivity() {
            int position = getAdapterPosition();
//            String songName = songTxtView.getText().toString();


            Intent intent = new Intent(context,PlayerActivity.class);
            intent.putExtra(Constants.SONG_ABSOLUTE_PATH,songsAbsolutePaths);
            intent.putExtra(Constants.SONG_NAME_ARRAY,songs);
            intent.putExtra(Constants.SONG_POSITION_KEY,position);
            context.startActivity(intent);


        }
    }
}
