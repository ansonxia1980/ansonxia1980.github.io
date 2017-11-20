package com.maihehd.sdk.vast.model;

import android.util.Log;
import com.maihehd.sdk.vast.util.LogUtil;

import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;

/**
 * Created by roger on 6/29/15.
 */
public class VASTModel {

    private final static String TAG = "VASTModel";

    // VAST版本
    public String version = "3.0";

    // 是否是轮播广告
    public Boolean isSlider = false;

    // 广告列表
    public List<AdModel> adList;

    // 广告时长
    public int duration = 0;

    // 当前播放的序号
    public int playingAdIndex = 0;

    // wrapper地址对应的广告
    // {
    //   url: vastUrl
    //   ad:  adModel
    // }
    private Dictionary<String, String> _wrapperAds;

    public VASTModel()
    {
        //
    }

    /**
     *
     * 计算整体广告时长
     *
     */
    public void calculateDuration(){
        duration = 0;
        Iterator ads = this.adList.iterator();
        Iterator creatives;
        AdModel ad;
        CreativeModel creative;
        while(ads.hasNext()){
            ad = (AdModel) ads.next();
            creatives = ad.creatives.iterator();
            while(creatives.hasNext()){
                creative = (CreativeModel) creatives.next();
                duration += creative.duration;
                LogUtil.d(TAG, "===" + duration);
            }
        }
        LogUtil.d(TAG, "total duration: " + this.duration);
    }

    /**
     * 获取下一个可以播放的广告创意
     *
     * @return
     *
     */
    public CreativeModel findNextCreative(){
        if (this.adList.size() <= 0){
            return null;
        }

        AdModel ad = this.adList.get(this.playingAdIndex);
        // 轮播广告处理
        if (this.isSlider){
            while (!ad.isSlider){
                this.playingAdIndex++;
                if (this.adList.size() <= this.playingAdIndex){
                    this.playingAdIndex = 0;
                }
                ad = this.adList.get(this.playingAdIndex);
                ad.playingCreativeIndex = -1;
            }
        }

        ad.playingCreativeIndex++;
        if(ad.creatives.size() <= ad.playingCreativeIndex) {
            // then next ad
            LogUtil.d(TAG, "creative size/index " + String.valueOf(ad.creatives.size()) + "/" + ad.playingCreativeIndex);
            ad.playingCreativeIndex = -1;
            this.playingAdIndex++;
            if (this.adList.size() <= this.playingAdIndex) {
                if (this.isSlider){
                    // 如果是轮播则从头开始寻找
                    this.playingAdIndex = 0;
                }
                else {
                    // 否则结束
                    LogUtil.d(TAG, "ad all played, ad size/index " + String.valueOf(this.adList.size()) + "/" + this.playingAdIndex);
                    return null;
                }
            }

            return this.findNextCreative();
        }

        LogUtil.d(TAG, "creative found ad/creative " + String.valueOf(this.playingAdIndex) + "/" + ad.playingCreativeIndex);

        return ad.creatives.get(ad.playingCreativeIndex);
    }

    public MediaFileModel getPlayingMediaFile(){
        CreativeModel creative = this.getPlayingCreative();
        LogUtil.d(TAG, "media files in creative " + creative.mediaFiles.size());
        if (creative != null && creative.mediaFiles.size() > 0) {
            return creative.mediaFiles.get(0);
        }

        return null;
    }

    /**
     * 获取当前正在播放的广告创意
     *
     * @return
     */
    public CreativeModel getPlayingCreative(){
        if(this.adList.size() <= this.playingAdIndex){
            return null;
        }

        AdModel ad = this.adList.get(this.playingAdIndex);
        if(ad.creatives.size() <= ad.playingCreativeIndex){
            return  null;
        }

        return ad.creatives.get(ad.playingCreativeIndex);
    }

    /**
     * 获取当前正在播放的广告单元
     *
     * @return
     */
    public AdModel getPlayingAd(){
        if(this.adList.size() <= this.playingAdIndex){
            return null;
        }

        return this.adList.get(this.playingAdIndex);
    }
}
