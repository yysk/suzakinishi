package works.langley.suzakinishi.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.IOException;

import io.vov.vitamio.MediaPlayer;
import timber.log.Timber;
import works.langley.suzakinishi.R;
import works.langley.suzakinishi.event.BusProvider;
import works.langley.suzakinishi.event.ClickPauseEvent;
import works.langley.suzakinishi.model.Info;
import works.langley.suzakinishi.receiver.MusicPlayerReceiver;
import works.langley.suzakinishi.ui.activity.MainActivity;

public class MusicPlayerService extends Service implements MediaPlayer.OnInfoListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MusicFocusable {
    final static String TAG = MusicPlayerService.class.getSimpleName();

    public static final String ACTION_STATE_CHANGED = "works.langley.suzakinishi.service.ACTION_STATE_CHANGED";
    public static final String ACTION_PLAY = "works.langley.suzakinishi.service.ACTION_PLAY";
    public static final String ACTION_PAUSE = "works.langley.suzakinishi.service.ACTION_PAUSE";
    public static final String ACTION_STOP = "works.langley.suzakinishi.service.ACTION_STOP";
    public static final String ACTION_REQUEST_STATE = "works.langley.suzakinishi.service.ACTION_REQUEST_STATE";
    public static final String ACTION_SEEK = "works.langley.suzakinishi.service.ACTION_SEEK";
    public static final String ACTION_SEEK_START = "works.langley.suzakinishi.service.ACTION_SEEK_START";

    private static final float DUCK_VOLUME = 0.1f;

    private MediaPlayer mMediaPlayer = null;
    private AudioFocusHelper mAudioFocusHelper = null;

    private WifiManager.WifiLock mWifiLock;

    private PlayerState mPlayerState = PlayerState.Retrieving;

    // if in Retrieving mode, this flag indicates whether we should start
    // playing immediately when we are ready or not.
    boolean mStartPlayingAfterRetrieve = false;

    private AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;

    private Info mInfo;

    final int NOTIFICATION_ID = 1;

    private AudioManager mAudioManager;
    private NotificationManager mNotificationManager;

    private long mRelaxTime = System.currentTimeMillis();

    private Thread mSelfStopThread = new Thread() {
        public void run() {
            while (true) {
                // 停止後5分再生がなかったらサービスを止める
                boolean needSleep = false;
                if (mPlayerState == PlayerState.Preparing || mPlayerState == PlayerState.Playing || mPlayerState == PlayerState.Paused) {
                    needSleep = true;
                } else if (mRelaxTime + 5 * 1000 * 60 > System.currentTimeMillis()) {
                    needSleep = true;
                }
                if (!needSleep) {
                    break;
                }
                try {
                    Thread.sleep(1000 * 60); // 停止中でない、または 10 分経過してない場合は 1 分休む
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            MusicPlayerService.this.stopSelf();
        }
    };

    /**
     * Makes sure the media player exists and has been reset. This will create
     * the media player if needed, or reset the existing media player if one
     * already exists.
     */
    private void createMediaPlayerIfNeeded() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer(this);
            mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnInfoListener(this);
        } else {
            mMediaPlayer.reset();
        }
    }

    @Override
    public void onCreate() {
        awakeWifi();

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);

        mSelfStopThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getExtras() != null && intent.getExtras().containsKey("info")) {
            mInfo = intent.getParcelableExtra("info");
            mPlayerState = PlayerState.Stopped;
            sendPlayerState();
        }
        String action = intent.getAction();

        switch (action) {
            case ACTION_PLAY:
                processPlayRequest();
                break;
            case ACTION_PAUSE:
                BusProvider.getInstance().post(new ClickPauseEvent());
                processPauseRequest();
                mNotificationManager.cancel(NOTIFICATION_ID);
                break;
            case ACTION_STOP:
                processStopRequest();
                mNotificationManager.cancel(NOTIFICATION_ID);
                break;
            case ACTION_REQUEST_STATE:
                sendPlayerState();
                break;
            case ACTION_SEEK:
                if (intent.getExtras() != null && intent.getExtras().containsKey("seek")) {
                    processSeekRequest(intent.getIntExtra("seek", 0));
                }
                break;
            case ACTION_SEEK_START:
                processSeekStartRequest();
                break;
        }

        return START_NOT_STICKY;
    }

    /**
     * WiFiがスリープしないように
     */
    private void awakeWifi() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, TAG);
        mWifiLock.acquire();
    }

    /**
     * WiFiの設定を解除
     */
    private void resetWifi() {
        mWifiLock.release();
    }

    private void processPlayRequest() {
        tryToGetAudioFocus();

        if (mPlayerState == PlayerState.Stopped) {
            playAudio();
        } else if (mPlayerState == PlayerState.Paused) {
            mPlayerState = PlayerState.Playing;
            startMediaPlayer();
        }
    }

    private void processSeekRequest(int seek) {
        if (mPlayerState == PlayerState.Retrieving) {
            mStartPlayingAfterRetrieve = true;
            return;
        }

        tryToGetAudioFocus();

        if (mPlayerState == PlayerState.SeekStart) {
            mPlayerState = PlayerState.Seek;
            seekMediaPlayer(seek);
        }
    }

    private void processSeekStartRequest() {
        if (mPlayerState == PlayerState.Retrieving) {
            mStartPlayingAfterRetrieve = false;
            return;
        }

        if (mPlayerState == PlayerState.Playing) {
            // Pause media player and cancel the 'foreground service' state.
            mPlayerState = PlayerState.SeekStart;
            mMediaPlayer.pause();
            relaxResources(false); // while paused, we always retain the MediaPlayer do not give up audio focus
            sendPlayerState();
        }
    }

    private void processPauseRequest() {
        if (mPlayerState == PlayerState.Retrieving) {
            mStartPlayingAfterRetrieve = false;
            return;
        }

        if (mPlayerState == PlayerState.Playing) {
            // Pause media player and cancel the 'foreground service' state.
            mPlayerState = PlayerState.Paused;
            mMediaPlayer.pause();
            relaxResources(false); // while paused, we always retain the MediaPlayer do not give up audio focus
            sendPlayerState();
        }
    }

    private void processStopRequest() {
        if (mPlayerState == PlayerState.Playing || mPlayerState == PlayerState.Paused) {
            mPlayerState = PlayerState.Stopped;

            // let go of all resources...
            relaxResources(true);
            clearAudioFocus();

            sendPlayerState();
        }
    }

    /**
     * Releases resources used by the service for playback. This includes the
     * "foreground service" status and notification, the wake locks and possibly
     * the MediaPlayer.
     *
     * @param releaseMediaPlayer Indicates whether the Media Player should also be released or not
     */
    private void relaxResources(boolean releaseMediaPlayer) {
        // stop being a foreground service
        stopForeground(true);

        // stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mRelaxTime = System.currentTimeMillis();
    }

    private void clearAudioFocus() {
        if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null && mAudioFocusHelper.abandonFocus()) {
            mAudioFocus = AudioFocus.NoFocusNoDuck;
        }
    }

    private boolean configMediaPlayer() {
        if (mAudioFocus == AudioFocus.NoFocusNoDuck) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mPlayerState = PlayerState.Paused;
                sendPlayerState();
            }
            return false;
        } else if (mAudioFocus == AudioFocus.NoFocusCanDuck) {
            mMediaPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME); // we'll be relatively quiet
        } else {
            mMediaPlayer.setVolume(1.0f, 1.0f); // we can be loud
        }
        return true;
    }

    private void startMediaPlayer() {
        if (configMediaPlayer() && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            createNotification();
            sendPlayerState();
        }
    }

    private void seekMediaPlayer(int seek) {
        if (configMediaPlayer() && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(seek);
            sendPlayerState();
        }
    }

    private void tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null && mAudioFocusHelper.requestFocus()) {
            mAudioFocus = AudioFocus.Focused;
        }
    }

    private void playAudio() {
        mPlayerState = PlayerState.Stopped;
        relaxResources(false); // release everything except MediaPlayer

        try {
            createMediaPlayerIfNeeded();
            mMediaPlayer.setDataSource(mInfo.getUrl());

            mPlayerState = PlayerState.Preparing;

            // Use the media button APIs (if available) to register ourselves
            // for media button events
            mAudioManager.registerMediaButtonEventReceiver(new ComponentName(this, MusicPlayerReceiver.class));
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END && mPlayerState != PlayerState.Playing) {
            mPlayerState = PlayerState.Playing;
            startMediaPlayer();
        }
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        mPlayerState = PlayerState.Playing;
        // optional need Vitamio 4.0
        player.setPlaybackSpeed(1.0f);
        sendPlayerState();
        startMediaPlayer();
    }

    @Override
    public void onCompletion(MediaPlayer player) {
        processStopRequest();
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(getApplicationContext(), "Media player error! Resetting.", Toast.LENGTH_SHORT).show();
        Timber.e("Error : what=%d, extra=%d", what, extra);

        mPlayerState = PlayerState.Stopped;
        relaxResources(true);
        clearAudioFocus();
        return true;
    }

    @Override
    public void onDestroy() {
        mPlayerState = PlayerState.Stopped;
        relaxResources(true);
        clearAudioFocus();
        resetWifi();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pauseIntent = PendingIntent.getService(this, 0, new Intent(ACTION_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_stat)
                .addAction(R.drawable.ic_action_av_pause, "Pause", pauseIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentTitle(mInfo.getTitle())
                .setContentText(mInfo.getAuthor())
                .setWhen(0);
        mNotificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    public void onGainedAudioFocus() {
        mAudioFocus = AudioFocus.Focused;

        // restart media player with new focus settings
        if (mPlayerState == PlayerState.Playing) {
            startMediaPlayer();
        }
    }

    public void onLostAudioFocus(boolean canDuck) {
        mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck : AudioFocus.NoFocusNoDuck;

        // start/restart/pause media player with new focus settings
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            startMediaPlayer();
        }
    }

    private void sendPlayerState() {
        Intent intent = new Intent(ACTION_STATE_CHANGED);
        intent.putExtra("info", mInfo);
        intent.putExtra("state", mPlayerState);
        if (mMediaPlayer != null) {
            intent.putExtra("duration", mMediaPlayer.getDuration());
            intent.putExtra("currentPosition", mMediaPlayer.getCurrentPosition());
        }
        sendBroadcast(intent);
    }
}
