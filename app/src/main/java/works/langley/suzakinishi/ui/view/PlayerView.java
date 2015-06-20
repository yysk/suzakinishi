package works.langley.suzakinishi.ui.view;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import io.codetail.animation.SupportAnimator;
import works.langley.suzakinishi.R;
import works.langley.suzakinishi.event.BusProvider;
import works.langley.suzakinishi.event.ClickPauseEvent;
import works.langley.suzakinishi.model.Info;
import works.langley.suzakinishi.service.MusicPlayerService;

public class PlayerView extends RelativeLayout {

    private SeekBar mTrackBar;
    private ImageView mButtonPause;
    private SeekBar mVolumeBar;
    private AppCompatTextView mTextPlaying;
    private Chronometer mChronoCurrent;
    private Chronometer mChronoDuration;

    private AudioManager mAudioManager;
    private long mCurrentPosition;

    public PlayerView(Context context) {
        super(context);
        initialize(context);
    }

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public PlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        inflate(context, R.layout.view_player, this);
        mTextPlaying = (AppCompatTextView) findViewById(R.id.text_playing);

        setupButtonPause();
        setupVolumeBar();
        setupChronometer();
        setupTrackBar();
    }

    private void setupButtonPause() {
        mButtonPause = (ImageView) findViewById(R.id.button_pause);
        mButtonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BusProvider.getInstance().post(new ClickPauseEvent());
            }
        });
    }

    private void setupVolumeBar() {
        mVolumeBar = (SeekBar) findViewById(R.id.volume_bar);
        mVolumeBar.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        mVolumeBar.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        mVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setupChronometer() {
        mChronoCurrent = (Chronometer) findViewById(R.id.chrono_current);
        mChronoDuration = (Chronometer) findViewById(R.id.chrono_duration);
        mChronoCurrent.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                mTrackBar.setProgress((int) (SystemClock.elapsedRealtime() - chronometer.getBase()));
            }
        });
    }

    private void setupTrackBar() {
        mTrackBar = (SeekBar) findViewById(R.id.track_bar);
        mTrackBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mChronoCurrent.setBase(SystemClock.elapsedRealtime() - progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Intent intent = new Intent(getContext(), MusicPlayerService.class);
                intent.setAction(MusicPlayerService.ACTION_SEEK_START);
                getContext().startService(intent);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Intent intent = new Intent(getContext(), MusicPlayerService.class);
                intent.setAction(MusicPlayerService.ACTION_SEEK);
                intent.putExtra("seek", seekBar.getProgress());
                getContext().startService(intent);
            }
        });
    }

    public void revealShowButtonPause(int cx, int cy) {
        int radius = Math.max(getWidth(), getHeight());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Animator animator = ViewAnimationUtils.createCircularReveal(
                    this, cx, cy, 0, radius);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(200);
            animator.start();
        } else {
            SupportAnimator animator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(
                    this, cx, cy, 0, radius);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(200);
            animator.start();
        }
    }

    public void updatePlayer(Intent intent) {
        Info info = intent.getParcelableExtra("info");
        if (info != null) {
            if (!TextUtils.isEmpty(info.title)) {
                mTextPlaying.setText(info.title);
            }
        }
        int duration = (int) intent.getLongExtra("duration", 0);
        mTrackBar.setMax(duration);
        mChronoDuration.setBase(SystemClock.elapsedRealtime() - duration);

        mCurrentPosition = (int) intent.getLongExtra("currentPosition", 0);
        mChronoCurrent.setBase(SystemClock.elapsedRealtime() - mCurrentPosition);
    }

    public void updateVolumeBar() {
        if (mVolumeBar == null) {
            return;
        }
        mVolumeBar.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }

    public void start() {
        mChronoCurrent.setBase(SystemClock.elapsedRealtime() - mCurrentPosition);
        mChronoCurrent.start();
        mTrackBar.setEnabled(true);
    }

    public void pause() {
        mCurrentPosition = SystemClock.elapsedRealtime() - mChronoCurrent.getBase();
        mChronoCurrent.stop();
    }

    public void seek() {
        mTrackBar.setEnabled(false);
        mChronoCurrent.stop();
    }

    public void stop() {
        mChronoCurrent.stop();
    }
}