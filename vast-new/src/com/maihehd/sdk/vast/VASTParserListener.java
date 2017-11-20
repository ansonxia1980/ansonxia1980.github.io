package com.maihehd.sdk.vast;

import com.maihehd.sdk.vast.model.VASTModel;

/**
 * Created by roger on 7/7/15.
 */
public interface VASTParserListener {

    public void onCancelled();
    public void onComplete(VASTModel vastData);
    public void onError();

}
