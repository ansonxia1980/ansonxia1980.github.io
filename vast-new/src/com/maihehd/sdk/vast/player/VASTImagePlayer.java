package com.maihehd.sdk.vast.player;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.maihehd.sdk.vast.model.MediaFileModel;
import com.maihehd.sdk.vast.util.LogUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by roger on 7/4/15.
 */
public class VASTImagePlayer extends VASTPlayer implements AsyncImageListener {

    private final static String TAG = "VASTImagePlayer";

    protected final List<String> MEDIA_TYPES = Arrays.asList("image/png", "image/jpeg", "image/png", "image/gif");

    private ImageView imageView;

    private int imageWidth = 0;
    private int imageHeight = 0;

    private long lastTime = 0;
    private int playerPosition = 0;

    public VASTImagePlayer(Context context){
        super(context);
    }

    public void init(RelativeLayout rootView, VASTPlayerListener playerListener, MediaFileModel mediaFile){
        super.init(rootView, playerListener, mediaFile);

        this.imageView = new ImageView(this.getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        this.imageView.setLayoutParams(layoutParams);
        //this.imageView.setMaxWidth(containerWidth);
        //this.imageView.setMaxHeight(containerHeight);
        this.imageView.setBackgroundColor(0x00000000);

        play(mediaFile);
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

    public void setScaleType(String type) {
        this.scaleType = type;

        if (this.imageView == null) {
            return;
        }

        // 默认缩放,保持宽高比
        ImageView.ScaleType scale = ImageView.ScaleType.FIT_CENTER;
        if (mediaFile.scalable) {
            // 允许缩放
            if (mediaFile.maintainAspectRatio == false) {
                // 不保持宽高比
                scale = ImageView.ScaleType.FIT_XY;
            } else {
                if (this.scaleType.equalsIgnoreCase("fill")) {
                    // 填满情况下必须缩放
                    scale = ImageView.ScaleType.CENTER_CROP;
                }
            }
        } else {
            // 不允许缩放
            scale = ImageView.ScaleType.CENTER;
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.imageView.getLayoutParams();
            params.width = RelativeLayout.LayoutParams.WRAP_CONTENT;// mediaFile.width;
            params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;// mediaFile.height;
            this.imageView.setLayoutParams(params);
            //this.imageView.setMaxWidth(mediaFile.width);
            //this.imageView.setMaxHeight(mediaFile.height);
            this.imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    playerClicked();
                }
            });
        }

        LogUtil.d(TAG, "scale mode = " + scale.toString() + "container width = " + this.imageView.getWidth());

        this.imageView.setScaleType(scale);
    }

    public void play(MediaFileModel mediaFile){
        LogUtil.d(TAG, "show image " + mediaFile.uri);

        this.playerPosition = 0;

        if(imageView.getParent() == null) {
            this.rootView.addView(this.imageView);
        }

        AsyncImage asyncImage = new AsyncImage(imageView, this);
        Object[] ps = {mediaFile.uri};
        asyncImage.execute(ps);
    }

    public void pause(){
        stopTimer();
    }

    public void resume(){
        lastTime = System.currentTimeMillis();
        startTimer();
    }

    public void stop(){
        stopTimer();
        lastTime = 0;
        playerPosition = 0;
    }

    public void destroy(){
        stop();

        if(imageView != null && imageView.getParent() != null){
            rootView.removeView(imageView);
        }
    }

    public void resize(int width, int height){
        LogUtil.d(TAG, "entered resize width/height " + width + "/" + height);

        if ( imageWidth == 0 || imageHeight == 0 ) {
            Log.w(TAG, "media width or mediaHeight is 0, skipping calculateAspectRatio");
            return;
        }

        if(height > 0) {
            //this.imageView.setMaxHeight(height);
        }

        if(width > 0){
            //this.imageView.setMaxWidth(width);
        }

    }

    public void playerClicked(){
        playerListener.PlayerClicked(this);
    }

    @Override
    public void onImageStart() {
        LogUtil.d(TAG, "start loading image");
    }

    @Override
    public void onImageComplete() {
        LogUtil.d(TAG, "image loaded");
        this.imageView.setBackgroundColor(0x00000000);
        this.setScaleType(this.scaleType);
        playerListener.PlayerStarted(this);
        lastTime = System.currentTimeMillis();
        if (mediaDuration > 0) {
            startTimer();
        }

        if (this.imageView.getScaleType() == ImageView.ScaleType.CENTER) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            this.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void onImageFailed() {
        LogUtil.d(TAG, "image load failed");
        playerListener.PlayerError(this);
    }

    @Override
    public void onImageCancelled() {
        LogUtil.d(TAG, "image load cancelled");
    }


    @Override
    protected void onTimerTick(){
        if(mediaTimerNeedStop) {
            LogUtil.d(TAG, "player timer stopped");
            return;
        }
        long currentTime = System.currentTimeMillis();
        playerPosition = playerPosition + (int)(currentTime - lastTime);
        lastTime = currentTime;
        playerListener.PlayerProgressChanged(this, playerPosition);
        if (playerPosition >= this.mediaDuration) {
            playerListener.PlayerCompleted(this);
            return;
        }

        mediaTimerHandler.postDelayed(mediaTimerRunnable, 300);
    }
}
