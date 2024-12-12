package com.example.musica;

import android.content.Context;
import android.media.AudioManager;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;


public class DeafModeFragment extends Fragment {

    private AudioManager audioManager;
    private BassBoost bassBoost;
    private Equalizer equalizer;
    private Vibrator vibrator;
    private int audioSessionId = 0;

    // Variables to save previous volume levels
    private int previousMediaVolume;
    private int previousSystemVolume;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deaf_mode, container, false);

        // Initialize AudioManager
        audioManager = (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);

        // Initialize Vibrator
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);

        // Find Switch and set Listener
        Switch deafModeSwitch = view.findViewById(R.id.red_icon);


        // Restore the switch state
        boolean isDeafModeOn = requireContext()
                .getSharedPreferences("DeafModePrefs", Context.MODE_PRIVATE)
                .getBoolean("isDeafModeOn", false);

        deafModeSwitch.setChecked(isDeafModeOn);

        if (isDeafModeOn) {
            enableDeafMode(); // Enable deaf mode if the saved state is ON
        }

        deafModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                enableDeafMode();
            } else {
                disableDeafMode();
            }

            // Save the state of the switch
            requireContext()
                    .getSharedPreferences("DeafModePrefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("isDeafModeOn", isChecked)
                    .apply();
        });

        return view;
    }

    private void enableDeafMode() {
        if (audioManager == null) return;

        // Save current volume levels
        previousMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        previousSystemVolume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);

        // Maximize both media and system volume
        int maxMediaVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxMediaVolume, AudioManager.FLAG_SHOW_UI);

        // Initialize and enable Bass Boost
        if (bassBoost == null) {
            bassBoost = new BassBoost(0, audioSessionId);
        }
        bassBoost.setEnabled(true);

        // Initialize and enable Equalizer
        if (equalizer == null) {
            equalizer = new Equalizer(0, audioSessionId);
        }
        equalizer.setEnabled(true);

        // Boost all bands to max
        short bands = equalizer.getNumberOfBands();
        for (short i = 0; i < bands; i++) {
            equalizer.setBandLevel(i, equalizer.getBandLevelRange()[1]); // Max gain
        }

        // Trigger vibration
        if (vibrator != null && vibrator.hasVibrator()) {
            VibrationEffect effect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE); // Vibrate for 500ms
            vibrator.vibrate(effect);
        }
    }

    private void disableDeafMode() {
        if (audioManager == null) return;

        // Restore previous volume levels
        double lowerVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 1.5;  // Set to 1/3 of the max volume
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) lowerVolume, AudioManager.FLAG_SHOW_UI);

        // Disable Bass Boost
        if (bassBoost != null) {
            bassBoost.setEnabled(false);
        }

        // Disable Equalizer
        if (equalizer != null) {
            equalizer.setEnabled(false);
        }

        // Cancel vibration
        if (vibrator != null) {
            vibrator.cancel();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        // If Deaf Mode is enabled, restore the media volume
        if (audioManager != null && isDeafModeOn()) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, previousMediaVolume, AudioManager.FLAG_SHOW_UI);
        }
    }

    // Helper method to check if Deaf Mode was enabled previously
    private boolean isDeafModeOn() {
        // Check the saved state of Deaf Mode from SharedPreferences
        return requireContext()
                .getSharedPreferences("DeafModePrefs", Context.MODE_PRIVATE)
                .getBoolean("isDeafModeOn", false);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bassBoost != null) {
            bassBoost.release();
        }
        if (equalizer != null) {
            equalizer.release();
        }
    }
}