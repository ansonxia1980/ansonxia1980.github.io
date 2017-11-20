package com.maihehd.sdk.vast.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.maihehd.sdk.vast.model.MediaFileModel;
import com.maihehd.sdk.vast.util.LogUtil;
import com.maihehd.sdk.vast.util.MySSLSocketFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;


/**
 * Created by roger on 6/29/15.
 */
public class VASTVideoPlayer extends VASTPlayer implements OnBufferingUpdateListener, OnPreparedListener,
        OnCompletionListener, OnErrorListener, OnVideoSizeChangedListener, OnInfoListener, OnSeekCompleteListener, SurfaceHolder.Callback {

    private final static String TAG = "VASTVideoPlayer";

    protected final List<String> MEDIA_TYPES = Arrays.asList("application/octet-stream", "video/mp4");

    private int videoWidth = 0;
    private int videoHeight = 0;

    private MediaPlayer mediaPlayer;
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;

//    private Handler mediaTimerHandler;
//    private Runnable mediaTimerRunnable;
//    private Boolean mediaTimerNeedStop;

    private int playerPosition = 0;


    public VASTVideoPlayer(Context context) {
        super(context);
    }

    public void init(RelativeLayout rootView, VASTPlayerListener playerListener, MediaFileModel mediaFile){
        super.init(rootView, playerListener, mediaFile);

        this.surfaceView = new SurfaceView(this.getContext());
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setKeepScreenOn(true);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(this);

        this.rootView.addView(this.surfaceView);
    }

    /**
     * 判断播放器是否支持当前媒体类型
     *
     * @param type
     * @return
     */
    public boolean isCompatibleWith(String type){
        return MEDIA_TYPES.contains(type);
    }

    public void setScaleType(String type){
        this.scaleType = type;
        this.resize(containerWidth, containerHeight);
    }

    /**
     * 播放视频
     */
    public void play(MediaFileModel mediaFile)
    {
        this.mediaFile = mediaFile;
        String videoUrl = this.mediaFile.uri;
        LogUtil.d(TAG, "start to play " + videoUrl);
        this.playerPosition = 0;
        if(this.mediaPlayer == null){
            createMediaPlayer();
//            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mediaPlayer.setDisplay(surfaceHolder);
//            mediaPlayer.prepareAsync();
            LogUtil.d(TAG, "media preparing");
        }
        else{
            mediaPlayer.setDisplay(null);
            mediaPlayer.reset();
            LogUtil.d(TAG, "media player is reset");
        }

        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            sf.fixHttpsURLConnection();
            HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mediaPlayer.setDataSource(videoUrl);
            mediaPlayer.prepareAsync();
            LogUtil.d(TAG, "media player preparing");
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 暂停播放
     */
    public void pause(){
        LogUtil.d(TAG, "media pause");
        stopTimer();
        if(mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    /**
     * 恢复播放
     */
    public void resume(){
        //if(mediaPlayer != null) {
            //startTimer();
            //mediaPlayer.start();
        //}
    }

    /**
     * 停止播放
     */
    public void stop(){
        LogUtil.d(TAG, "stop called");
        stopTimer();
        playerPosition = 0;
        if(mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    /**
     * 销毁播放器
     */
    public void destroy(){
        stop();
        if(surfaceView.getParent() != null) {
            ViewGroup rootView = (ViewGroup) this.surfaceView.getParent();
            rootView.removeView(surfaceView);
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * 静音
     */
    public void mute(){
        if (mediaPlayer != null){
            mediaPlayer.setVolume(0, 0);
            muted = true;
        }
    }

    /**
     * 取消静音
     */
    public void unmute(){
        if (mediaPlayer != null){
            mediaPlayer.setVolume(1, 1);
            muted = false;
        }
    }

    /**
     * 调整大小
     * @param width
     * @param height
     */
    public void resize(int width, int height){
        this.resizeSurfaceView(width, height);
    }

    //
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent)
            throws IllegalStateException {
        //
    }

    // 视频播放完成的回调方法
    @Override
    public void onCompletion(MediaPlayer mp) throws IllegalStateException {
        LogUtil.d(TAG, "media completed");

        stopTimer();
        playerListener.PlayerCompleted(this);
    }

    // 视频播放出错的回调方法
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        LogUtil.d(TAG, "media error");
        stopTimer();
        playerListener.PlayerError(this);
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                Log.v(TAG, "MEDIA_INFO_BAD_INTERLEAVING extra is :" + extra);
                break;
            case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                Log.v(TAG, "MEDIA_INFO_METADATA_UPDATE extra is :" + extra);
                break;
            case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                Log.v(TAG, "MEDIA_INFO_NOT_SEEKABLE extra is :" + extra);
                break;
            case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                Log.v(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING extra is :" + extra);
                break;
            case MediaPlayer.MEDIA_INFO_UNKNOWN:
                Log.v(TAG, "MEDIA_INFO_UNKNOWN extra is :" + extra);
                break;
        }

        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        LogUtil.d(TAG, "seek completed, position => " + mediaPlayer.getCurrentPosition());
    }

    @Override
    /**
     * 通过onPrepared播放
     */
    public void onPrepared(MediaPlayer mediaplayer) {
        LogUtil.d(TAG, "onPrepared");
        if(mediaPlayer == null){
            return;
        }

        videoWidth = mediaPlayer.getVideoWidth();
        videoHeight = mediaPlayer.getVideoHeight();
        LogUtil.d(TAG, "media width/height/duration " + videoWidth + "/" + videoHeight + "/" + mediaPlayer.getDuration());
        this.resizeSurfaceView(this.containerWidth, this.containerHeight);
        mediaplayer.setDisplay(this.surfaceHolder);
        mediaPlayer.start();
        if(playerPosition != 0){
            mediaPlayer.seekTo(playerPosition);
        }
        else{
            LogUtil.d(TAG, "playing next");
            playerListener.PlayerStarted(this);
        }
        startTimer();
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height){
        LogUtil.d(TAG, "media size changed width/height" + width + "/" + height);
        videoWidth = width;
        videoHeight = height;
        this.resizeSurfaceView(this.containerWidth, this.containerHeight);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        LogUtil.d(TAG, "surface changed");
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        LogUtil.d(TAG, "surface created");
        if (mediaPlayer == null) {
            createMediaPlayer();
            this.play(mediaFile);
        }
        else{
            LogUtil.d(TAG, "player resume, position => " + playerPosition);
            mediaPlayer.setDisplay(surfaceHolder);
            mediaPlayer.start();
            /*
            if (playerPosition > 0){
                LogUtil.d(TAG, "seek to " + playerPosition);
                mediaPlayer.seekTo(playerPosition);
            }
            else {
                mediaPlayer.start();
            }
            //*/
            startTimer();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        Log.e("mediaPlayer", "surface destroyed");
        stopTimer();
    }

    private void createMediaPlayer() {
        if (surfaceView == null) {
            Log.e(TAG, "surfaceview is null");
            return;
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDisplay(surfaceHolder);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnSeekCompleteListener(this);
            LogUtil.d(TAG, "media player created");
        } catch (Exception e) {
            Log.e(TAG, "create media error", e);
        }
    }

    private void resizeSurfaceView(int containerWidth, int containerHeight){
        LogUtil.d(TAG, "entered resize");

        if ( videoWidth == 0 || videoHeight == 0 ) {
            Log.w(TAG, "media width or mediaHeight is 0, skipping calculateAspectRatio");
            return;
        }

        if(containerWidth == 0) {
            containerWidth = surfaceView.getWidth();
        }

        if(containerHeight == 0){
            containerHeight = surfaceView.getHeight();
        }

        LogUtil.d(TAG, "media size width/height " + videoWidth + "/" + videoHeight
                + " ; container width/height " + containerWidth + "/" + containerHeight);

        int surfaceWidth = containerWidth;
        int surfaceHeight = containerHeight;

        if (mediaFile.scalable){
            // 允许缩放
            if (mediaFile.maintainAspectRatio) {
                // 等比缩放
                double widthRatio = 1.0 * containerWidth / videoWidth;
                double heightRatio = 1.0 * containerHeight / videoHeight;
                double scale = Math.min(widthRatio, heightRatio);
                surfaceWidth = (int) (scale * videoWidth);
                surfaceHeight = (int) (scale * videoHeight);
            }
        }
        else {
            // 不能缩放,尺寸为设置大小
            surfaceWidth = mediaFile.width == 0 ? containerWidth : mediaFile.width;
            surfaceHeight = mediaFile.height ==  0 ? containerHeight : mediaFile.height;
        }

        LogUtil.d(TAG, "resize to width/height " + String.valueOf(surfaceWidth) + "/" + String.valueOf(surfaceHeight));

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                surfaceWidth, surfaceHeight);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        surfaceView.setLayoutParams(layoutParams);

        surfaceHolder.setFixedSize(surfaceWidth, surfaceHeight);
    }

    @Override
    protected void onTimerTick(){
        if(mediaTimerNeedStop) {
            LogUtil.d(TAG, "player timer stopped");
            return;
        }
        playerPosition = mediaPlayer.getCurrentPosition();
        //LogUtil.d(TAG, "player position " + playerPosition);
        playerListener.PlayerProgressChanged(this, playerPosition);
        if (playerPosition >= this.mediaDuration) {
            playerListener.PlayerCompleted(this);
            return;
        }

        mediaTimerHandler.postDelayed(mediaTimerRunnable, 300);
    }
}
