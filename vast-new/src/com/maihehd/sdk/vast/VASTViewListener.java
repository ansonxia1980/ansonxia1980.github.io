package com.maihehd.sdk.vast;

/**
 * Created by roger on 6/28/15.
 */
public interface VASTViewListener {
    public void AdLoaded(VASTView vastView);
    public void AdStarted(VASTView vastView);
    public void AdStopped(VASTView vastView);
    public void AdUserSkip(VASTView vastView);
    public void AdSkipped(VASTView vastView);
    public void AdError(VASTView vastView);
    public void AdClickThru(VASTView vastView, String url, String id, Boolean playerHandles);
    public void AdUserClose(VASTView vastView);
}
