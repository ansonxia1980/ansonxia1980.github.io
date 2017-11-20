package com.maihehd.sdk.vast.util;

/**
 * Created by roger on 7/6/15.
 */
public interface AsyncRequestListener {

    public void onError();
    public void onCancelled();
    public void onPostExecute(Object data);
    public void onPreExecute();
    public void onProgressUpdate(Integer percent);

}
