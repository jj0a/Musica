package com.example.musica;

import static com.example.musica.MainActivity.musicFiles;

import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AlbumDetails extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView albumPhoto;
    String albumName;
    ArrayList<MusicFiles> albumSongs = new ArrayList<>();
    AlbumDetailsAdapter albumDetailsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_album_details);
        recyclerView = findViewById(R.id.recyclerView);
        albumPhoto = findViewById(R.id.albumPhoto);
        albumName = getIntent().getStringExtra("albumName");
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        int j = 0;
        for(int i = 0; i<musicFiles.size(); i++){
            if(albumName.equals(musicFiles.get(i).getAlbum()))
            {
                albumSongs.add(j, musicFiles.get(i));
                j++;
            }
        }
        byte[] image = getAlbumArt(albumSongs.get(0).getPath());
        if(image != null){
            Glide.with(this).load(image).into(albumPhoto);
        }else{
            Glide.with(this).load(R.drawable.artic).into(albumPhoto);

        }

    }

    protected void onResume(){
        super.onResume();
        if(!(albumSongs.size() < 1))
        {
              albumDetailsAdapter = new AlbumDetailsAdapter(this, albumSongs);
              recyclerView.setAdapter(albumDetailsAdapter);
              recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

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
}