package com.example.musica;

import static com.example.musica.MainActivity.musicFiles;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import static com.example.musica.MainActivity.musicFiles;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class SongsFragment extends Fragment {

    RecyclerView recyclerView;
    static MusicAdapter musicAdapter;
    public SongsFragment(){

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_songs, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        // Check if musicFiles is not null and not empty
        if (musicFiles != null && musicFiles.size() > 0) {
            musicAdapter = new MusicAdapter(getContext(), musicFiles);
            recyclerView.setAdapter(musicAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        }
        return view;
    }

}