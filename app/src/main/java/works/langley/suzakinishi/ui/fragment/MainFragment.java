package works.langley.suzakinishi.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
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

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.codetail.animation.SupportAnimator;
import timber.log.Timber;
import works.langley.suzakinishi.R;
import works.langley.suzakinishi.event.BusProvider;
import works.langley.suzakinishi.event.ClickPauseEvent;
import works.langley.suzakinishi.event.StateChangeEvent;
import works.langley.suzakinishi.event.VolumeChangeEvent;
import works.langley.suzakinishi.model.Info;
import works.langley.suzakinishi.service.MusicPlayerService;
import works.langley.suzakinishi.ui.view.PlayerView;
import works.langley.suzakinishi.util.InfoUtil;

public class MainFragment extends Fragment {

    @InjectView(R.id.text_title)
    TextView mTextTitle;
    @InjectView(R.id.text_author)
    TextView mTextAuthor;
    @InjectView(R.id.button_play)
    FloatingActionButton mButtonPlay;
    @InjectView(R.id.loading)
    FrameLayout mLoading;
    @InjectView(R.id.container_title)
    LinearLayout mContainerTitle;
    @InjectView(R.id.view_player)
    PlayerView mPlayerView;

    private MusicPlayerService.State mState = MusicPlayerService.State.Preparing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupPlayButton();
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(getActivity(), MusicPlayerService.class);
        intent.setAction(MusicPlayerService.ACTION_REQUEST_STATE);
        getActivity().startService(intent);

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
        ButterKnife.reset(this);
    }

    private void setupPlayButton() {
        mButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mState != MusicPlayerService.State.Playing) {
                    Intent intent = new Intent(getActivity(), MusicPlayerService.class);
                    intent.setAction(MusicPlayerService.ACTION_PLAY);
                    getActivity().startService(intent);
                }
            }
        });
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

    private class GetContentTask extends AsyncTask<Void, Void, Info> {
        @Override
        protected void onPreExecute() {
            mLoading.setVisibility(View.VISIBLE);
        }

        @Override
        protected Info doInBackground(Void... params) {
            return InfoUtil.getInfo();
        }

        @Override
        protected void onPostExecute(Info info) {
            mLoading.setVisibility(View.INVISIBLE);
            if (info == null) {
                return;
            }
            Timber.d(info.url);
            Intent intent = new Intent(getActivity(), MusicPlayerService.class)
                    .setAction(MusicPlayerService.ACTION_REQUEST_STATE)
                    .putExtra("info", info);
            getActivity().startService(intent);
        }
    }

    @Subscribe
    public void onClickPause(ClickPauseEvent event) {
        if (mState != MusicPlayerService.State.Paused) {
            Intent intent = new Intent(getActivity(), MusicPlayerService.class);
            intent.setAction(MusicPlayerService.ACTION_PAUSE);
            getActivity().startService(intent);
        }
    }

    @Subscribe
    public void onStateChanged(StateChangeEvent event) {
        Intent intent = event.getIntent();
        Info info = intent.getParcelableExtra("info");
        if (info != null) {
            if (!TextUtils.isEmpty(info.title)) {
                mTextTitle.setText(info.title);
            }
            if (!TextUtils.isEmpty(info.author)) {
                mTextAuthor.setText(info.author);
            }
        }

        mPlayerView.updatePlayer(intent);

        mState = (MusicPlayerService.State) intent.getSerializableExtra("state");
        switch (mState) {
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
                new GetContentTask().execute();
                break;
        }
    }

    @Subscribe
    public void onVolumeChanged(VolumeChangeEvent event) {
        mPlayerView.updateVolumeBar();
    }
}