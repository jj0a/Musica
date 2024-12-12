package com.example.musica;

import android.app.Activity;
import android.app.RecoverableSecurityException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import com.example.musica.R;  // Correct


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

    private Context mContext;
    static ArrayList<MusicFiles> mFiles;

    MusicAdapter(Context mContext, ArrayList<MusicFiles> mFiles){
        this.mFiles = mFiles;
        this.mContext = mContext;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
         holder.file_name.setText(mFiles.get(position).getTitle());
         byte[] image = getAlbumArt(mFiles.get(position).getPath());
         if(image != null){
             Glide.with(mContext).asBitmap().load(image).into(holder.album_art);
         }else{
             Glide.with(mContext).load(R.drawable.musica).into(holder.album_art);
         }


         holder.itemView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Log.d("MusicAdapter", "Item clicked at position: " + position);
                 Intent intent = new Intent(mContext, PlayerActivity.class);
                 intent.putExtra("position", position);
                 mContext.startActivity(intent);
             }
         });

        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(mContext, v);
                popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener((item) -> {
                    if (item.getItemId() == R.id.delete) {
                        Toast.makeText(mContext, "Delete Clicked!", Toast.LENGTH_SHORT).show();
                        deleteFile(position, v);
                    }
                    return true;
                });
            }
        });
    }

    public void deleteFile(int position, View v) {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(mFiles.get(position).getId())); // content://

        try {
            // Attempt to delete the file
            int rowsDeleted = mContext.getContentResolver().delete(contentUri, null, null);
            Log.d("FileDeletion", "Rows Deleted via ContentResolver: " + rowsDeleted);

            if (rowsDeleted > 0) {
                mFiles.remove(position);
                notifyItemRemoved(position);
                notifyItemChanged(position, mFiles.size());
                Snackbar.make(v, "File Deleted", Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(v, "File can't be deleted", Snackbar.LENGTH_LONG).show();
            }

        } catch (RecoverableSecurityException e) {
            // Handle RecoverableSecurityException (API 29+)
            Log.d("FileDeletion", "RecoverableSecurityException encountered.");

            // Retrieve the IntentSender to request permission from the user
            IntentSender intentSender = e.getUserAction().getActionIntent().getIntentSender();

            try {
                // Request permission from the user to delete the file
                ((Activity) mContext).startIntentSenderForResult(intentSender, 1001, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException ex) {
                ex.printStackTrace();
            }
        }
    }





    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView file_name;
        ImageView album_art, menuMore;

        public MyViewHolder(@NonNull View itemView){
            super(itemView);
            file_name = itemView.findViewById(R.id.music_file_name);
            album_art = itemView.findViewById(R.id.music_img);
            menuMore = itemView.findViewById(R.id.menuMore);
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

    void updateList(ArrayList<MusicFiles> musicFilesArrayList){
        mFiles = new ArrayList<>();
        mFiles.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }



}
