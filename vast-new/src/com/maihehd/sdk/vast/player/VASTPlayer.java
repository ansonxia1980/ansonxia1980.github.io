package com.maihehd.sdk.vast.player;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import com.maihehd.sdk.vast.model.MediaFileModel;
import com.maihehd.sdk.vast.util.LogUtil;

/**
 * Created by roger on 7/4/15.
 */
public class VASTPlayer extends View {

    private final String TAG = "VASTPlayer";

    protected RelativeLayout rootView;
    protected VASTPlayerListener playerListener;
    protected MediaFileModel mediaFile;
    protected int mediaDuration = 0;
    protected int containerWidth = 0;
    protected int containerHeight = 0;

    protected Handler mediaTimerHandler;
    protected Runnable mediaTimerRunnable;
    protected Boolean mediaTimerNeedStop;
    protected Boolean muted = false;
    protected String scaleType = "fit";

    public VASTPlayer(Context context){
        super(context);
    }

    public VASTPlayer(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public VASTPlayer(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
    }

    public void init(RelativeLayout rootView, VASTPlayerListener playerListener, MediaFileModel mediaFile){
        this.rootView = rootView;
        this.playerListener = playerListener;
        this.mediaFile = mediaFile;
    }

    public void init(RelativeLayout rootView, VASTPlayerListener playerListener, MediaFileModel mediaFile, int duration, int width, int height) {
        this.init(rootView, playerListener, mediaFile);

        this.mediaDuration = duration;
        this.containerWidth = width;
        this.containerHeight = height;
    }

    // abstract
    public boolean isCompatibleWith(String type){
        return false;
    }

    public void setScaleType(String type) {
        this.scaleType = type;
    }

    public void play(MediaFileModel mediaFile){
        return;
    }

    public void pause(){
        return;
    }

    public void resume(){
        return;
    }

    public void stop(){
        return;
    }

    public void destroy(){
        return;
    }

    public void resize(int width, int height){
        return;
    }

    public Boolean getMuted(){
        return muted;
    }

    public void mute() {
        return;
    }

    public void unmute() {
        return;
    }

    protected void stopTimer(){
        mediaTimerNeedStop = true;
        LogUtil.d(TAG, "timer is going to stop");
    }

    protected void startTimer(){
        LogUtil.d(TAG, "start timer");
        // 如果总时长小于等于,取消倒计时
        if (mediaDuration <= 0){
            return;
        }

        mediaTimerNeedStop = false;
        if(mediaTimerHandler == null){
            mediaTimerHandler = new Handler();
        }

        if(mediaTimerRunnable == null){
            mediaTimerRunnable = new Runnable() {
                @Override
                public void run() {
                    onTimerTick();
                }
            };
        }
        mediaTimerHandler.postDelayed(mediaTimerRunnable, 300);
    }

    protected void onTimerTick(){
        if(mediaTimerNeedStop) {
            return;
        }
        mediaTimerHandler.postDelayed(mediaTimerRunnable, 300);
    }
}
