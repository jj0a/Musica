package com.example.musica;

import static com.example.musica.ApplicationClass.ACTION_NEXT;
import static com.example.musica.ApplicationClass.ACTION_PLAY;
import static com.example.musica.ApplicationClass.ACTION_PREVIOUS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String actionName = intent.getAction();
        Log.e("NotificationReceiver", "Action received: " + actionName); // Log the received action
        Intent serviceIntent = new Intent(context, MusicService.class);

        if (actionName != null) {
            switch (actionName) {
                case ACTION_PLAY:
                    Log.e("NotificationReceiver", "Play action triggered");
                    serviceIntent.putExtra("ActionName", "playPause");
                    context.startService(serviceIntent);
                    break;
                case ACTION_NEXT:
                    Log.e("NotificationReceiver", "Next action triggered");
                    serviceIntent.putExtra("ActionName", "next");
                    context.startService(serviceIntent);
                    break;
                case ACTION_PREVIOUS:
                    Log.e("NotificationReceiver", "Previous action triggered");
                    serviceIntent.putExtra("ActionName", "previous");
                    context.startService(serviceIntent);
                    break;
                default:
                    Log.e("NotificationReceiver", "Unknown action: " + actionName);
            }
        } else {
            Log.e("NotificationReceiver", "Received null action name in onReceive");
        }
    }

}
