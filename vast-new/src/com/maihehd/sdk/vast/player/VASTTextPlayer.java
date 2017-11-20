package com.maihehd.sdk.vast.player;

import android.content.Context;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

import java.util.Arrays;
import java.util.List;

/**
 * Created by roger on 7/4/15.
 */
public class VASTTextPlayer extends VASTPlayer {

    protected final List<String> MEDIA_TYPES = Arrays.asList("text/plain");

    public VASTTextPlayer(Context context){
        super(context);
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

    public void play(String url){
        //
    }

    public void pause(){
        //
    }

    public void resume(){
        //
    }

    public void stop(){
        //
    }

    public void destroy(){
        //
    }

    public void resize(int width, int height){
        //
    }
}
