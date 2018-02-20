package com.example.tomiz.guessinggame_shiyu;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;


        import android.content.SharedPreferences;
        import android.content.res.AssetFileDescriptor;
        import android.content.res.AssetManager;
        import android.media.AudioManager;
        import android.media.SoundPool;
        import android.support.v7.widget.Toolbar;
        import android.view.View;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.TextView;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.widget.Toast;
        import org.w3c.dom.Text;

        import java.io.IOException;
        import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button play;

    static SoundPool soundPool;
    static int sound1 = -1;
    static int sound2 = -1;
    static int sound3 = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        play = (Button) findViewById(R.id.buttonPlay);
        play.setOnClickListener(this);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try {
            AssetManager assetManager = getAssets();
            AssetFileDescriptor descriptor;
            descriptor = assetManager.openFd("coins_drop.mp3");
            sound1 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("rgong.wav");
            sound2 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sound3.mp3");
            sound3 = soundPool.load(descriptor, 0);

        } catch (IOException e) {
        }

    }

    @Override
    public void onClick(View v) {

        soundPool.play(sound2, 1, 1, 0, 0, (float)2);
        startActivity(new Intent(this, GamingActivity.class));

    }
}