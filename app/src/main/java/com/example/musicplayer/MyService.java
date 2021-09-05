package com.example.musicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MyService extends Service {

    private PlayerActivity mPlayerActivity;
    private static final String TAG = "MyTag";
    private static final String CHANNEL_ID = "musicplayer_channelid";
    private Binder myBinder = new MyServiceBinder();
    public MediaPlayer mediaPlayer;
    private Uri uri;
    //private static String title;
    public static final int notificationId = 10;
    private int actionPauseImg = R.drawable.ic_pause;


    // inner class which extends Binder, binder implements iBinder so this class automatically implements iBinder
    public class MyServiceBinder extends Binder {

        // returns the reference of MY service class
        public MyService getMyService() {
            return MyService.this;
        }

    }

    @Override
    public void onCreate() {
        Log.d(TAG,"onCreate MyService");
        super.onCreate();
        createChannelNotification();

//        mediaPlayer = MediaPlayer.create(getApplicationContext(),);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");
        if (uri != null) {
            switch (intent.getAction()){
                case Constants.PLAY_MUSIC:
                    if (mediaPlayer == null) {
                        Log.d(TAG,"media player is null");
                        musicInitializer(uri);
                        startMusic();
                        String title = mPlayerActivity.getSongName();
                        showNotification(notificationId,title);
                    }else {
                        Log.d(TAG,"media player is not null");
                        stopMusic();
                        releasMediaPlayer();
                        mediaPlayer = null;
                        Log.d(TAG,"media player object = "+mediaPlayer);
                        musicInitializer(uri);
                        startMusic();
                    }
                    break;

                case Constants.ACTION_PAUSE:
                    Log.d(TAG,"Action Pause");
                    Intent musicPause = new Intent(Constants.INTENT_FILETER_PAUSED);
                    if (isSongPlaying()) {
                        pauseMusic();
                        actionPauseImg = R.drawable.ic_play;
                        String title = mPlayerActivity.getSongName();
                        showNotification(notificationId,title);
                        musicPause.putExtra(Constants.MUSIC_PAUSE_KEY,true);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(musicPause);
                    }else {
                        startMusic();
                        actionPauseImg = R.drawable.ic_pause;
                        String title = mPlayerActivity.getSongName();
                        showNotification(notificationId,title);
                        musicPause.putExtra(Constants.MUSIC_PAUSE_KEY,false);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(musicPause);
                    }
                    break;

                case Constants.ACTION_NEXT:
                    Log.d(TAG,"Action Next");
                    mPlayerActivity.nextSong();
                    String title = mPlayerActivity.getSongName();
                    showNotification(notificationId,title);
                    break;

                case Constants.ACTION_PREVIOUS:
                    Log.d(TAG,"Action Previous");
                    mPlayerActivity.previousSong();
                    String song = mPlayerActivity.getSongName();
                    showNotification(notificationId,song);
                    break;
            }


        }
        return START_NOT_STICKY;
    }

    private void createChannelNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,"MusicPlayer_Channel", NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setDescription("This is a music player description");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public void setPauseImg(int img) {
        actionPauseImg = img;
        String title = mPlayerActivity.getSongName();
        showNotification(notificationId,title);
    }

    public void showNotification(int id,String title) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID);

        // action play
        Intent pauseIntent = new Intent(this,MyService.class);
        pauseIntent.setAction(Constants.ACTION_PAUSE);
        PendingIntent actionPause = PendingIntent.getService(this,101,pauseIntent,0);


        // action next
        Intent  nextIntent = new Intent(this,MyService.class);
        nextIntent.setAction(Constants.ACTION_NEXT);
        PendingIntent actionNext = PendingIntent.getService(this,101,nextIntent,0);


        // action previous
        Intent previousIntent = new Intent(this,MyService.class);
        previousIntent.setAction(Constants.ACTION_PREVIOUS);
        PendingIntent actionPrevious = PendingIntent.getService(this,101,previousIntent,0);

        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText("unknown")
                .setAutoCancel(true)
                .addAction(R.drawable.ic_previous,"Previous",actionPrevious)
                .addAction(actionPauseImg,"Pause",actionPause)
                .addAction(R.drawable.ic_next,"Next",actionNext)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0,1,2));

        Notification notification = builder.build();
        startForeground(id,notification);
    }


    public void test() {
        int a = R.drawable.ic_backward;
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"onBind");
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG,"onUnbind");
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(TAG,"onRebind");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");
        super.onDestroy();
 //       mediaPlayer.stop();
//        mediaPlayer.release();
    }

    //play music for first time
    public void musicInitializer(Uri uri) {
        mediaPlayer = MediaPlayer.create(this,uri);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d(TAG,"music completed");
                mediaPlayer.stop();
                mediaPlayer.release();
                stopSelf();
                stopForeground(true);
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d(TAG,"error called");
                return true;
            }
        });
    }

    // checks if the media is playing or not and returns accordingly
    public boolean isSongPlaying() {
        return mediaPlayer.isPlaying();
    }

    // start the music
    public void startMusic() {
        mediaPlayer.start();
    }

    // pause the music
    public void pauseMusic() {
        mediaPlayer.pause();
    }

    // stops the music
    public void stopMusic() {
        mediaPlayer.stop();
    }

    // release media player
    public void releasMediaPlayer() {
        mediaPlayer.release();
    }

    // reset the music
    public void resetMusic() {
        mediaPlayer.reset();
    }

    // song path
    public void setUri(Uri uri) {
        this.uri  = uri;
    }

    public void setPlayerActivity(PlayerActivity mPlayerActivity) {
        this.mPlayerActivity = mPlayerActivity;
    }

}