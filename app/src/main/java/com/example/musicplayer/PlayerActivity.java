package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = "MyTag";
    private Button playBtn,nextBtn,previousBtn,fastForwardBtn,fastBackwardBtn;
    private TextView songNameTxtView, songStartedTxtView, songEndedTxtView;
    private ImageView imgView;
    private SeekBar seekBar;
    //private BlastVisualizer barVisualizer;
    private MediaPlayer mediaPlayer;
    private String[] songPath;
    private int songPosition;
    private String[] songsName;
    private MyService myService;
    private boolean isConnected = false; // checks if the connection is established or not between component and service
    private Uri songUri;

    // service connection for the service
    private ServiceConnection myServiceConn = new ServiceConnection() {
        // this method will be called if the service is successfully established between calling component and the service
        @Override
        public void onServiceConnected(ComponentName name, IBinder myBinder) {
            Log.d(TAG,"onServiceConnected");
            isConnected = true;
            MyService.MyServiceBinder myServiceBinder = (MyService.MyServiceBinder) myBinder;
            myService = myServiceBinder.getMyService();
            myService.setPlayerActivity(PlayerActivity.this);
            myService.setUri(songUri);

            Intent startedIntent = new Intent(PlayerActivity.this,MyService.class);
            startedIntent.setAction(Constants.PLAY_MUSIC);
            startService(startedIntent);


        }

        // this will be called if connection is interrypted between service and component
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isConnected = false;
            Log.d(TAG,"onServiceDisConnected");
        }
    };

    // if the action button on notification is clicked then update the play, pause state of player activity
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"broadcast receive");
            boolean isPaused = intent.getBooleanExtra(Constants.MUSIC_PAUSE_KEY,false);
            if (isPaused) {
                Log.d(TAG,"Music is paused");
                playBtn.setBackgroundResource(R.drawable.ic_play);
            }else {
                Log.d(TAG,"music is playing");
                playBtn.setBackgroundResource(R.drawable.ic_pause);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Log.d(TAG,"onCreate Player Activity");
        initViews();
        receiveIntent();
    }

    @Override
    protected void onStart() {
        Log.d(TAG,"onStart");
        super.onStart();
        Intent intent = new Intent(this,MyService.class);
        bindService(intent,myServiceConn,BIND_AUTO_CREATE);

        // registering broad cast receiver
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiver, new IntentFilter(Constants.INTENT_FILETER_PAUSED));
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
        unbindService(myServiceConn);

        // unregistering broad cast receiver
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiver);
    }



    // initializing views
    private void initViews() {
        Log.d(TAG,"initializing views");
        // txtView reference
        songNameTxtView = findViewById(R.id.txtSongName);
        songStartedTxtView = findViewById(R.id.txtSongStart);
        songEndedTxtView = findViewById(R.id.txtSongStop);

        // imageview reference
        imgView = findViewById(R.id.imageView);

        // seekbar reference
        seekBar = findViewById(R.id.seekbar);


        // button reference
        playBtn = findViewById(R.id.btnPlay);
        nextBtn = findViewById(R.id.btnNext);
        previousBtn = findViewById(R.id.btnPrevious);
        fastForwardBtn = findViewById(R.id.btnFastForward);
        fastBackwardBtn = findViewById(R.id.btnFastBackward);

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextSong();
            }
        });

        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousSong();
            }
        });
    }

    // getting intent
    private void receiveIntent() {
        Log.d(TAG,"receiving intent");
        Intent intent = getIntent();
        if (intent.hasExtra(Constants.SONG_ABSOLUTE_PATH)) {
            songPath = intent.getStringArrayExtra(Constants.SONG_ABSOLUTE_PATH);
            songPosition = intent.getIntExtra(Constants.SONG_POSITION_KEY,-1);
            songsName = intent.getStringArrayExtra(Constants.SONG_NAME_ARRAY);
            // horizontally scrolls the txt view
            songNameTxtView.setSelected(true);
            songNameTxtView.setText(songsName[songPosition]);

            songUri = Uri.parse(songPath[songPosition]);
        }else {
            Log.d(TAG,"intent is null");
        }
    }

    // plays  next song
    public void nextSong() {
        // song position can not be greater than total songs or array out of bounds exception will be occured
        if (songPosition < songPath.length-1) {
            myService.stopMusic();
            myService.releasMediaPlayer();
            myService.mediaPlayer = null;
            songPosition += 1;
            songUri = Uri.parse(songPath[songPosition]);
            String newSongName = songsName[songPosition];
            songNameTxtView.setText(newSongName);
            myService.showNotification(MyService.notificationId,newSongName);
            myService.musicInitializer(songUri);
            myService.startMusic();

            // if the song is already playing then the background should be in pause state
            if (myService.isSongPlaying()) {
                playBtn.setBackgroundResource(R.drawable.ic_pause);
            }
            // showing animation if the next song img is clicked
            animationDesign(imgView,360f);
        }

    }

    // plays previous song
    public void previousSong() {
        // song position cannot be less than 0
        if (songPosition > 0) {
            myService.stopMusic();
            myService.releasMediaPlayer();
            myService.mediaPlayer = null;
            songPosition -=1;
            songUri = Uri.parse(songPath[songPosition]);
            String newSongName = songsName[songPosition];
            songNameTxtView.setText(newSongName);
            myService.showNotification(MyService.notificationId,newSongName);
            myService.musicInitializer(songUri);
            myService.startMusic();

            if (myService.isSongPlaying()) {
                playBtn.setBackgroundResource(R.drawable.ic_pause);
            }
            animationDesign(imgView,-360f);
        }
    }

    // starts song if it not started pause other wise
    private void playMusic() {
        if (myService.isSongPlaying()) {
            playBtn.setBackgroundResource(R.drawable.ic_play);
            myService.setPauseImg(R.drawable.ic_play);
            myService.pauseMusic();
        }else {
            playBtn.setBackgroundResource(R.drawable.ic_pause);
            myService.startMusic();
            myService.setPauseImg(R.drawable.ic_pause);


            TranslateAnimation moveAnim = new TranslateAnimation(-25,25,-25,25);
            moveAnim.setInterpolator(new AccelerateInterpolator());
            moveAnim.setDuration(600);
            moveAnim.setFillEnabled(true);
            moveAnim.setFillAfter(true);
            moveAnim.setRepeatMode(Animation.REVERSE);
            moveAnim.setRepeatCount(1);
            imgView.startAnimation(moveAnim);
        }
    }

    // animation design
    private void animationDesign(View view, Float degree) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imgView,"rotation",0f,degree);
        objectAnimator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator);
        animatorSet.start();
    }

    public String getSongName() {
        return songsName[songPosition];
    }

}