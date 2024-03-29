package com.example.caroproject;

import static androidx.core.content.ContentProviderCompat.requireContext;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;

import com.example.caroproject.Adapter.FirebaseHelper;
import com.example.caroproject.Data.AppData;
import com.example.caroproject.Data.Background;
import com.example.caroproject.Data.Music;
import com.example.caroproject.Data.SoundMaking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public final static String PREF_FILE = "CARO";
    public final static String LOGGED_IN_ACCOUNT = "username";
    public final static String BACKGROUND = "background";
    public final static String MUSIC = "music";
    public final static String MUSIC_VOLUME = "musicVolume";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startSetUpAppSetting();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            FirebaseFirestore.getInstance()
                    .collection("UserInfo").document(user.getUid())
                    .update("online", true);
        }
    }


    private void startSetUpAppSetting() {
        SharedPreferences pref = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        int backgroundPosition;
        int musicPosition;
        int musicVolume;
//        pref.edit().clear().apply();

        if(pref != null) {
            backgroundPosition = pref.getInt(BACKGROUND, 0);
            musicPosition = pref.getInt(MUSIC, 0);
            musicVolume = pref.getInt(MUSIC_VOLUME, 50);

        } else {
            backgroundPosition = 0;
            musicPosition = 0;
            musicVolume = 50;
        }


        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, musicVolume, 0);

        AppData appData = AppData.getInstance();
        getWindow().setBackgroundDrawableResource(((Background)appData.getBackgroundList().get(backgroundPosition).getItem()).getLayoutBackground());
        SoundMaking.getInstance().createMusic(this,((Music) appData.getMusicList().get(musicPosition).getItem()).getSourceId());
        SoundMaking.getInstance().playMusic();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SoundMaking.getInstance().pauseMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SoundMaking.getInstance().playMusic();
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            FirebaseFirestore.getInstance()
                    .collection("UserInfo").document(user.getUid())
                    .update("online", false);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundMaking.getInstance().releaseMusic();
    }

}