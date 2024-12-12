package com.example.musica;

import static android.content.Context.MODE_PRIVATE;
import static com.example.musica.MainActivity.ARTIST_TO_FRAG;
import static com.example.musica.MainActivity.PATH_TO_FRAG;
import static com.example.musica.MainActivity.SHOW_MINI_PLAYER;
import static com.example.musica.MainActivity.SONG_NAME_TO_FRAG;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class NowPlayingFragmentBottom extends Fragment implements ServiceConnection {

    ImageView nextBtn, albumArt;
    TextView artist, songName;
    FloatingActionButton playPauseBtn;
    View view;
    MusicService musicService;

    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";
   // private FrameLayout miniPlayerLayout;

    public NowPlayingFragmentBottom(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_now_playing_bottom, container, false);
        artist = view.findViewById(R.id.song_artist_miniPlayer);
        songName = view.findViewById(R.id.song_name_miniPlayer);
        albumArt = view.findViewById(R.id.bottom_album_art);
        playPauseBtn = view.findViewById(R.id.play_pause_miniPlayer);


        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "PlayPause", Toast.LENGTH_SHORT).show();
                musicService.playPauseBtnClicked();
                if(musicService.isPlaying()){
                    playPauseBtn.setImageResource(R.drawable.ic_pause);
                }else{
                    playPauseBtn.setImageResource(R.drawable.ic_play);
                }
            }
        });
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        if(SHOW_MINI_PLAYER){
            if(PATH_TO_FRAG != null) {
                byte[] art = getAlbumArt(PATH_TO_FRAG);
                if(art != null){
                    Glide.with(getContext()).load(art).into(albumArt);
                }else{
                    Glide.with(getContext()).load(R.drawable.musica).into(albumArt);
                }

                songName.setText(SONG_NAME_TO_FRAG);
                artist.setText(ARTIST_TO_FRAG);
                Intent intent = new Intent(getContext(), MusicService.class);
                if(getContext() != null){
                    getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(getContext() != null && musicService != null){
            getContext().unbindService(this);
        }
    }


    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(uri); // May throw IllegalArgumentException or RuntimeException
            return retriever.getEmbeddedPicture(); // May return null if no embedded picture is available
        } catch (IllegalArgumentException | SecurityException e) {
            e.printStackTrace(); // Handle invalid URI or permission issues
            return null; // Return null when album art cannot be retrieved
        } finally {
            try {
                // retriever.release(); // Ensure the retriever is released to free resources
            } catch (RuntimeException e) {
                e.printStackTrace(); // Log unexpected exceptions on release
            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder binder =(MusicService.MyBinder) service;
        musicService = binder.getService();

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;

    }
}