package com.maihehd.sdk.vast.player;

/**
 * Created by roger on 7/7/15.
 */
public interface AsyncImageListener {

    public void onImageStart();
    public void onImageComplete();
    public void onImageFailed();
    public void onImageCancelled();

}
