package com.example.musica;

import static com.example.musica.ApplicationClass.ACTION_NEXT;
import static com.example.musica.ApplicationClass.ACTION_PLAY;
import static com.example.musica.ApplicationClass.ACTION_PREVIOUS;
import static com.example.musica.ApplicationClass.CHANNEL_ID_2;
import static com.example.musica.PlayerActivity.listSongs;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    IBinder mBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    Uri uri;
    int position = -1;
    ActionPlaying actionPlaying;
    MediaSessionCompat mediaSessionCompat;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";


    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My Audio");

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Bind", "Method");
        return mBinder;
    }



    public  class MyBinder extends Binder{
        MusicService getService(){
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition", -1);
        String actionName = intent.getStringExtra("ActionName");
        if(myPosition != -1){
            playMedia(myPosition);
        }
        if(actionName != null){
            switch (actionName){
                case "playPause":
                    Toast.makeText(this, "PlayPause", Toast.LENGTH_SHORT).show();
                    //playPauseBtnClicked();
                    if(actionPlaying != null){
                        actionPlaying.playPauseBtnClicked();
                    }
                    break;
                case "next":
                    Toast.makeText(this, "Next", Toast.LENGTH_SHORT).show();
                   // nextBtnClicked();
                    if(actionPlaying != null){
                        actionPlaying.nextBtnClicked();
                    }
                    break;
                case "previous":
                    Toast.makeText(this, "Previous", Toast.LENGTH_SHORT).show();
                   // prevBtnClicked();
                    if(actionPlaying != null){
                        actionPlaying.prevBtnClicked();
                    }
                    break;
            }
        }


        return START_STICKY;
    }


     void playMedia(int StartPosition) {
        musicFiles = listSongs;
        position = StartPosition;
        if(mediaPlayer != null)
        {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
        else{
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    void start(){
        mediaPlayer.start();
    }

    boolean isPlaying(){
       return mediaPlayer.isPlaying();
    }

    void stop(){
        mediaPlayer.stop();
    }

    void release(){
        mediaPlayer.release();
    }

    int getDuration(){
        return mediaPlayer.getDuration();
    }

    void seekTo(int position){
        mediaPlayer.seekTo(position);
    }

    int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }

    void createMediaPlayer(int positionInner){
        position= positionInner;
        uri = Uri.parse(musicFiles.get(position).getPath());
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit();
        editor.putString(MUSIC_FILE, uri.toString());
        editor.putString(ARTIST_NAME, musicFiles.get(position).getArtist());
        editor.putString(SONG_NAME, musicFiles.get(position).getTitle());
        editor.apply();
        mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
    }

    void pause(){
        mediaPlayer.pause();
    }

    void OnCompleted(){
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(actionPlaying != null) {
            actionPlaying.nextBtnClicked();
            if(mediaPlayer != null){
                createMediaPlayer(position);
                mediaPlayer.start();
                OnCompleted();
            }
        }
    }

    void setCallBack(ActionPlaying actionPlaying){
        this.actionPlaying = actionPlaying;
    }

    void showNotification(int playPauseBtn) {
        Log.e("showNotification", "Showing notification with playPauseBtn: " + playPauseBtn);

        // Create the content intent to launch the PlayerActivity
        Intent intent = new Intent(this, PlayerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Retrieve the album art as a bitmap
        byte[] picture = getAlbumArt(musicFiles.get(position).getPath());
        Bitmap thumb = picture != null
                ? BitmapFactory.decodeByteArray(picture, 0, picture.length)
                : BitmapFactory.decodeResource(getResources(), R.drawable.musica);

        // Create the notification
        Notification notification = new NotificationCompat.Builder(this, ApplicationClass.CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken())) // Ensure MediaSession is active
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setContentIntent(contentIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        // Show the notification
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);

        Log.e("showNotification", "Notification displayed successfully");

        // Ensure that the MediaSession state is updated
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f); // Set the state to playing
        mediaSessionCompat.setPlaybackState(stateBuilder.build());
        mediaSessionCompat.setActive(true); // Ensure the session is active
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



    void prevBtnClicked(){
        if(actionPlaying != null){
            actionPlaying.prevBtnClicked();
        }
    }

    void playPauseBtnClicked() {
        if(actionPlaying != null){
            actionPlaying.playPauseBtnClicked();
        }
    }
    void nextBtnClicked(){
        if(actionPlaying != null){
            actionPlaying.nextBtnClicked();
        }
    }


}
