package act.angelman.presentation.custom;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewConfiguration;

import java.io.IOException;

import act.angelman.presentation.manager.ApplicationManager;

public class VideoCardTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    // Log tag
    private static final String TAG = VideoCardTextureView.class.getName();

    private MediaPlayer mMediaPlayer;

    private float mVideoHeight;
    private float mVideoWidth;

    private boolean mIsDataSourceSet;
    private boolean mIsViewAvailable;
    private boolean mIsVideoPrepared;
    private boolean mIsPlayCalled;
    private Context context;

    private ScaleType mScaleType;
    private State mState;
    public int pivotPointX;
    public int pivotPointY;

    public enum ScaleType {
        CENTER_CROP, TOP, BOTTOM
    }

    public enum State {
        UNINITIALIZED, PLAY, STOP, PAUSE, END
    }

    public VideoCardTextureView(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public VideoCardTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    public VideoCardTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        initView();
    }

    private void initView() {
        initPlayer();
        setScaleType(ScaleType.CENTER_CROP);
        setSurfaceTextureListener(this);
    }

    public void setScaleType(ScaleType scaleType) {
        mScaleType = scaleType;
        updateTextureViewSize();
    }

    private void updateTextureViewSize() {
        float viewWidth = getWidth();
        float viewHeight = getHeight();

        if(viewWidth == 0 || viewHeight == 0) {
            return;
        }

        float scaleX = getRootView().getWidth() / viewWidth;
        float scaleY = (getRootView().getHeight() * 3 / 4) / viewHeight;

        int navigationBarHeight = 0;
        if(hasNavigationBar()) {
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                navigationBarHeight = resources.getDimensionPixelSize(resourceId);
            }
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            double dmRatio = (double)dm.heightPixels/dm.widthPixels;
            if(dmRatio > 1.72f) {
                navigationBarHeight *= -1;
            }
        }

        switch (mScaleType) {
            case TOP:
                pivotPointX = 0;
                pivotPointY = 0;
                break;
            case BOTTOM:
                pivotPointX = (int) (viewWidth);
                pivotPointY = (int) (viewHeight);
                break;
            case CENTER_CROP:
                pivotPointX = (int) (viewWidth / 2);
                pivotPointY = (int) ((viewHeight - navigationBarHeight) * 0.66f);
                break;
            default:
                pivotPointX = (int) (viewWidth / 2);
                pivotPointY = (int) (viewHeight / 2);
                break;
        }

        if (ApplicationManager.getDeviceName().contains("SM-G850")) {
            scaleY *= 1.66f;
            pivotPointY /= 2.4f;
        }

        Matrix matrix = new Matrix();
        matrix.setScale(scaleX, scaleY, pivotPointX, pivotPointY);

        setTransform(matrix);
    }

    private boolean hasNavigationBar() {
        boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        return !hasMenuKey && !hasBackKey;
    }

    private void initPlayer() {
        if (mMediaPlayer == null ) {
            mMediaPlayer = new MediaPlayer();
        } else {
            mMediaPlayer.reset();
        }
        mIsVideoPrepared = false;
        mIsPlayCalled = false;
        mState = State.UNINITIALIZED;
        mMediaPlayer.setVolume(0.0f, 0.0f);
    }

    /**
     * @see android.media.MediaPlayer#setDataSource(String)
     */
    public void setDataSource(String path) {
        initPlayer();

        try {
            mMediaPlayer.setDataSource(path);
            mIsDataSourceSet = true;
            prepare();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void prepare() {
        try {
            mMediaPlayer.setOnVideoSizeChangedListener(
                    new MediaPlayer.OnVideoSizeChangedListener() {
                        @Override
                        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                            mVideoWidth = width;
                            mVideoHeight = height;
                            updateTextureViewSize();
                        }
                    }
            );
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mState = State.END;
                    Log.d(TAG,"Video has ended.");

                    if (mListener != null) {
                        mListener.onVideoEnd();
                    }
                }
            });

            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mIsVideoPrepared = true;
                    if (mIsPlayCalled && mIsViewAvailable) {
                        Log.d(TAG,"Player is prepared and play() was called.");
                        play(null);
                    }

                    if (mListener != null) {
                        mListener.onVideoPrepared();
                    }
                }
            });

        } catch (IllegalArgumentException e) {
            Log.d(TAG, e.getMessage());
        } catch (IllegalStateException e) {
            Log.d(TAG, e.toString());
        }
    }

    /**
     * Play or resume video. Video will be played as soon as view is available and media player is
     * prepared.
     *
     * If video is stopped or ended and play() method was called, video will start over.
     */
    public void play(MediaPlayer.OnCompletionListener completionListener) {
        if (mMediaPlayer == null ) {
            initPlayer();
        }
        if (!mIsDataSourceSet) {
            Log.d(TAG,"play() was called but data source was not set.");
            return;
        }

        mIsPlayCalled = true;

        if (!mIsVideoPrepared) {
            Log.d(TAG,"play() was called but video is not prepared yet, waiting.");
            return;
        }

        if (!mIsViewAvailable) {
            Log.d(TAG,"play() was called but view is not available yet, waiting.");
            return;
        }

        if (mState == State.PLAY) {
            Log.d(TAG,"play() was called but video is already playing.");
            return;
        }

        if (mState == State.PAUSE) {
            Log.d(TAG,"play() was called but video is paused, resuming.");
            mState = State.PLAY;
            mMediaPlayer.start();
            return;
        }

        if (mState == State.END || mState == State.STOP) {
            Log.d(TAG,"play() was called but video already ended, starting over.");
            mState = State.PLAY;
            mMediaPlayer.seekTo(0);
            mMediaPlayer.start();
            return;
        }

        mState = State.PLAY;
        if(completionListener != null){
            mMediaPlayer.setOnCompletionListener(completionListener);
        }
        mMediaPlayer.start();
    }

    /**
     * Pause video. If video is already paused, stopped or ended nothing will happen.
     */
    public void pause() {
        if (mState == State.PAUSE) {
            Log.d(TAG,"pause() was called but video already paused.");
            return;
        }

        if (mState == State.STOP) {
            Log.d(TAG,"pause() was called but video already stopped.");
            return;
        }

        if (mState == State.END) {
            Log.d(TAG,"pause() was called but video already ended.");
            return;
        }

        mState = State.PAUSE;
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    /**
     * Stop video (pause and seek to beginning). If video is already stopped or ended nothing will
     * happen.
     */
    public void stop() {
        if (mState == State.STOP) {
            Log.d(TAG,"stop() was called but video already stopped.");
            return;
        }

        if (mState == State.END) {
            Log.d(TAG,"stop() was called but video already ended.");
            return;
        }

        mState = State.STOP;
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mMediaPlayer.seekTo(0);
        }
    }

    /**
     * @see android.media.MediaPlayer#setLooping(boolean)
     */
    public void setLooping(boolean looping) {
        mMediaPlayer.setLooping(looping);
    }

    /**
     * @see android.media.MediaPlayer#seekTo(int)
     */
    public void seekTo(int milliseconds) {
        mMediaPlayer.seekTo(milliseconds);
    }

    /**
     * @see android.media.MediaPlayer#getDuration()
     */
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    private MediaPlayerListener mListener;

    /**
     * Listener trigger 'onVideoPrepared' and `onVideoEnd` events
     */
    public void setListener(MediaPlayerListener listener) {
        mListener = listener;
    }

    public interface MediaPlayerListener {

        public void onVideoPrepared();

        public void onVideoEnd();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        Surface surface = new Surface(surfaceTexture);
        if(mMediaPlayer != null) {
            mMediaPlayer.setSurface(surface);
            mIsViewAvailable = true;
        }

        if (mIsDataSourceSet && mIsPlayCalled && mIsVideoPrepared) {
            Log.d(TAG, "View is available and play() was called.");
            play(null);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public void resetPlayer(){
        mMediaPlayer.seekTo(0);
        mState = State.STOP;
    }

    public boolean isDataSourceSetup(){
        return mIsDataSourceSet;
    }


}
