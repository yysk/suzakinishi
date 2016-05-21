package works.langley.suzakinishi.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.trello.rxlifecycle.components.support.RxFragment;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import icepick.State;
import io.codetail.animation.SupportAnimator;
import rx.Observable;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import works.langley.suzakinishi.R;
import works.langley.suzakinishi.event.BusProvider;
import works.langley.suzakinishi.event.ClickPauseEvent;
import works.langley.suzakinishi.event.StateChangeEvent;
import works.langley.suzakinishi.event.VolumeChangeEvent;
import works.langley.suzakinishi.model.Info;
import works.langley.suzakinishi.service.MusicPlayerService;
import works.langley.suzakinishi.service.PlayerState;
import works.langley.suzakinishi.ui.view.PlayerView;
import works.langley.suzakinishi.util.InfoUtil;
import works.langley.suzakinishi.util.Observables;
import works.langley.suzakinishi.util.ToastUtil;

public class MainFragment extends RxFragment {

    @Bind(R.id.text_title)
    TextView mTextTitle;
    @Bind(R.id.text_author)
    TextView mTextAuthor;
    @Bind(R.id.button_play)
    FloatingActionButton mButtonPlay;
    @Bind(R.id.loading)
    FrameLayout mLoading;
    @Bind(R.id.container_title)
    LinearLayout mContainerTitle;
    @Bind(R.id.view_player)
    PlayerView mPlayerView;

    @State
    PlayerState mPlayerState = PlayerState.Preparing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        startMusicPlayerService(MusicPlayerService.ACTION_REQUEST_STATE, null);
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @OnClick(R.id.button_play)
    void onClickPlay() {
        if (mPlayerState == PlayerState.Playing) {
            return;
        }
        startMusicPlayerService(MusicPlayerService.ACTION_PLAY, null);
    }

    private void startMusicPlayerService(String action, Info info) {
        Intent intent = new Intent(getContext(), MusicPlayerService.class)
                .setAction(action);
        if (info!= null) {
            intent.putExtra("info", info);
        }
        getContext().startService(intent);
    }

    private void hidePlayButton() {
        if (mButtonPlay.getVisibility() == View.GONE) {
            return;
        }
        Timber.d("hidePlayButton");
        mButtonPlay.animate().cancel();
        mButtonPlay.animate()
                .scaleX(0)
                .scaleY(0)
                .setDuration(120)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mPlayerView.setVisibility(View.VISIBLE);
                        mPlayerView.revealShowButtonPause((mButtonPlay.getLeft() + mButtonPlay.getRight()) / 2, 0);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mButtonPlay.setVisibility(View.GONE);
                    }
                });

        mContainerTitle.animate().cancel();
        mContainerTitle.animate()
                .translationY(mContainerTitle.getHeight() / 2)
                .alpha(0)
                .setDuration(200)
                .setListener(null);
    }

    private void pausePlayer() {
        if (mPlayerView.getVisibility() == View.INVISIBLE) {
            return;
        }
        Timber.d("pausePlayer");

        int cx = (mButtonPlay.getLeft() + mButtonPlay.getRight()) / 2;
        int radius = Math.max(mPlayerView.getWidth(), mPlayerView.getHeight());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            hidePlayerViewForLollipop(cx, 0, radius);
        } else {
            hidePlayerView(cx, 0, radius);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void hidePlayerViewForLollipop(int cx, int cy, int radius) {
        Animator animator = ViewAnimationUtils.createCircularReveal(
                mPlayerView, cx, cy, radius, 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(200);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                showPlayButton();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mPlayerView.setVisibility(View.INVISIBLE);
            }
        });
        animator.start();
    }

    private void hidePlayerView(int cx, int cy, int radius) {
        SupportAnimator animator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(
                mPlayerView, cx, cy, radius, 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(200);
        animator.addListener(new SupportAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart() {
                showPlayButton();
            }

            @Override
            public void onAnimationEnd() {
                mPlayerView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel() {
            }

            @Override
            public void onAnimationRepeat() {
            }
        });
        animator.start();
    }

    private void showPlayButton() {
        mButtonPlay.animate().cancel();
        mButtonPlay.animate()
                .translationY(0)
                .scaleX(1)
                .scaleY(1)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mButtonPlay.setVisibility(View.VISIBLE);
                    }
                });

        mContainerTitle.animate().cancel();
        mContainerTitle.animate()
                .translationY(0)
                .alpha(1)
                .setDuration(200)
                .setListener(null);
    }

    private void getRadioContent() {
        Observables.usingProgressDialog(getContext())
                .flatMap(new Func1<Void, Observable<Info>>() {
                    @Override
                    public Observable<Info> call(Void aVoid) {
                        return InfoUtil.getInfo()
                                .subscribeOn(Schedulers.io());
                    }
                })
                .compose(this.<Info>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .toSingle()
                .subscribe(new SingleSubscriber<Info>() {
                    @Override
                    public void onSuccess(Info value) {
                        startMusicPlayerService(MusicPlayerService.ACTION_REQUEST_STATE, value);
                        Timber.d(value.getUrl());
                    }

                    @Override
                    public void onError(Throwable error) {
                        showErrorToast(error);
                    }
                });
    }

    private void showErrorToast(Throwable error) {
        if (error instanceof IOException) {
            // ネットワークエラー
            ToastUtil.showNetworkError(getContext());
        } else {
            ToastUtil.showOtherError(getContext());
        }
        Timber.e(error, error.getMessage());
    }

    @Subscribe
    public void onClickPause(ClickPauseEvent event) {
        if (mPlayerState != PlayerState.Paused) {
            startMusicPlayerService(MusicPlayerService.ACTION_PAUSE, null);
        }
    }

    @Subscribe
    public void onStateChanged(StateChangeEvent event) {
        Intent intent = event.getIntent();
        Info info = intent.getParcelableExtra("info");
        if (info != null) {
            String title = info.getTitle();
            if (!TextUtils.isEmpty(title)) {
                mTextTitle.setText(title);
            }
            String author = info.getAuthor();
            if (!TextUtils.isEmpty(author)) {
                mTextAuthor.setText(author);
            }
        }

        mPlayerView.updatePlayer(intent);

        mPlayerState = (PlayerState) intent.getSerializableExtra("state");
        switch (mPlayerState) {
            case Playing:
                hidePlayButton();
                mPlayerView.start();
                mLoading.setVisibility(View.GONE);
                break;
            case Paused:
                pausePlayer();
                mPlayerView.pause();
                mLoading.setVisibility(View.GONE);
                break;
            case Stopped:
                pausePlayer();
                mPlayerView.stop();
                break;
            case Seek:
                mPlayerView.seek();
                mLoading.setVisibility(View.VISIBLE);
                break;
            case SeekStart:
                mPlayerView.pause();
                mLoading.setVisibility(View.VISIBLE);
                break;
            default:
                getRadioContent();
                break;
        }
    }

    @Subscribe
    public void onVolumeChanged(VolumeChangeEvent event) {
        mPlayerView.updateVolumeBar();
    }
}