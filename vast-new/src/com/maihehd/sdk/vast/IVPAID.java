package com.maihehd.sdk.vast;

/**
 * Created by roger on 7/2/15.
 */
public interface IVPAID {

    public String handshakeVersion(String version);


    public void initAd(int width, int height, String viewMode, int desiredBitrate);
    // creativeData: he ad unit should pass the value for either the Linear or Nonlinear
    // AdParameter element specified in the VAST document.
    public void initAd(int width, int height, String viewMode, int desiredBitrate, String creativeData);
    public void initAd(int width, int height, String viewMode, int desiredBitrate,
                       String creativeData, String environmentVars);
    public void resizeAd(int width, int height, String viewMode);
    public void startAd();
    public void stopAd();
    public void pauseAd();
    public void resumeAd();
    public void expandAd();
    public void collapseAd();
    public void skipAd();
}
