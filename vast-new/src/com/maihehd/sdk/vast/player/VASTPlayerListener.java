package com.maihehd.sdk.vast.player;

/**
 * Created by roger on 6/28/15.
 */
public interface VASTPlayerListener {

    public void PlayerReady(VASTPlayer player);
    public void PlayerStarted(VASTPlayer player);
    public void PlayerProgressChanged(VASTPlayer player, int position);
    public void PlayerPaused(VASTPlayer player);
    public void PlayerCompleted(VASTPlayer player);
    public void PlayerError(VASTPlayer player);
    public void PlayerClicked(VASTPlayer player);
}
