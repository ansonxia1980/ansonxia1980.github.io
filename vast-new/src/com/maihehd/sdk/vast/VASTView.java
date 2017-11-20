package com.maihehd.sdk.vast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import com.maihehd.sdk.vast.model.AdModel;
import com.maihehd.sdk.vast.model.CreativeModel;
import com.maihehd.sdk.vast.model.MediaFileModel;
import com.maihehd.sdk.vast.model.VASTModel;
import com.maihehd.sdk.vast.player.VASTImagePlayer;
import com.maihehd.sdk.vast.player.VASTPlayer;
import com.maihehd.sdk.vast.player.VASTPlayerListener;
import com.maihehd.sdk.vast.player.VASTVideoPlayer;
import com.maihehd.sdk.vast.util.LogUtil;
import com.maihehd.sdk.vast.util.ResourceUtil;
import com.maihehd.sdk.vast.util.StringUtil;
import org.OpenUDID.OpenUDID_manager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by roger on 6/28/15.
 */
public class VASTView extends RelativeLayout implements IVPAID, VASTPlayerListener, VASTParserListener {

    private final static String SDK_VERSION = "1.0.2.1603111052";
    private final static String VPAID_VERSION = "2.0.0";
    private final static String TAG = "VASTView";
    private final static String VAST_SERVER = "http://delivery.maihehd.com/d/vast/3.0?";

    private final int CLOSE_BTN_ID = 1;
    private final int SKIP_BTN_ID = 2;
    private final int MUTE_BTN_ID = 3;

    private final int TOOLBAR_BG_COLOR = 0x88000000;
    private final int TOOLBAR_FT_COLOR = 0xFFFFFFFF;

    private Context context;
    private VASTViewListener listener;
    private VASTPlayer player;
    private VASTModel vastData = null;
    private Map<String, String> envMap;

    private Map<String, String> mediaTypes;

    // 显示比例
    private float displayScale = 1;
    // 工具栏高度
    private int toolbarHeight = 24;
    // 字体大小
    private int fontSize = 12;
    // 边距
    private int padding = 4;
    // 按钮x坐标
    private int lastControllerId = 0;
    // 关闭按钮
    private ImageView closeButtonImageView;
    // 跳过按钮
    private TextView skipButtonTextView;
    // 静音按钮
    private ImageView muteButtonImageView;
    // 倒计时
    private TextView countDownTextView;
    // 详情按钮
    private TextView buttonMoreTextView;
    // 提示框
    //private TextView loadingTextView;
    private ProgressBar loadingIndicator;

    private Statistics statistics;

    private int initWidth;
    private int initHeight;

    // 已播放的广告
    private int playedTime = 0;

    public VASTView(Context context, VASTViewListener listener){
        super(context);

        this.displayScale = context.getResources().getDisplayMetrics().density;
        //this.fontSize = (int)(displayScale * fontSize);
        this.padding = (int)(displayScale * padding);
        this.toolbarHeight = (int)(displayScale * toolbarHeight);

        initMediaTypes();

        this.context = context;
        this.listener = listener;

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        this.setLayoutParams(layoutParams);

        this.setPadding(0, 0, 0, 0);
        //this.setBackgroundColor(0x00000000);

        // init OpenUDID
        if(!OpenUDID_manager.isInitialized())
        {
            OpenUDID_manager.sync(context);
        }

        // 初始化统计对象
        this.statistics = new Statistics(context);
    }

    /**
     * player events
     * @param player
     */
    @Override
    public void PlayerReady(VASTPlayer player){
        //
    }

    @Override
    public void PlayerStarted(VASTPlayer player){
        // impressions
        AdModel ad = vastData.getPlayingAd();
        if (ad == null){
            return;
        }

        List<String> impressions = ad.impressions;
        statistics.sendRequests(impressions);

        // start events
        CreativeModel creative = vastData.getPlayingCreative();
        if (creative == null){
            return;
        }

        List<String> starts = creative.trackingEvents.get("start");
        statistics.sendRequests(starts);

        // hide loading
//        if (loadingTextView != null && loadingTextView.getParent() != null){
//            this.removeView(loadingTextView);
//            LogUtil.d(TAG, "loading tips removed");
//        }
        if (loadingIndicator != null && loadingIndicator.getParent() != null){
            this.removeView(loadingIndicator);
            LogUtil.d(TAG, "loading tips removed");
        }

        this.relayoutToolbar(this.initWidth, this.initHeight);
    }

    @Override
    public void PlayerProgressChanged(VASTPlayer player, int position){
        if(vastData.duration <= 0){
            return;
        }

        int left = Math.round((vastData.duration - position - this.playedTime) / 1000);
        if(left < 0){
            left = 0;
        }

        int leftLength = String.valueOf(left).length();
        if(countDownTextView != null){
            Spannable ss = new SpannableString("广告剩余 " + left + " 秒");
            ss.setSpan(new ForegroundColorSpan(0xffffff00), 5, 5 + leftLength, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            //ss.setSpan(new AbsoluteSizeSpan(fontSize), 5, 5 + leftLength, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            countDownTextView.setText(ss);
        }
    }

    @Override
    public void PlayerCompleted(VASTPlayer player){
        LogUtil.d(TAG, "player completed");
        // complete events
        CreativeModel creative = vastData.getPlayingCreative();
        if(creative != null){
            if(creative.trackingEvents.size() > 0) {
                List<String> completes = creative.trackingEvents.get("complete");
                statistics.sendRequests(completes);
            }

            // 已播放的广告时长
            this.playedTime += creative.duration;
        }

        // 重置背景色
        this.setBackgroundColor(0x00000000);

        // then play next
        this.playAd();
    }

    @Override
    public void PlayerPaused(VASTPlayer player){
        //
    }

    @Override
    public void PlayerError(VASTPlayer player){
        destroyPlayer();
        listener.AdError(this);
    }

    @Override
    public void PlayerClicked(VASTPlayer player){
        adClicked();
    }

    /**
     * parser events
     */
    @Override
    public void onComplete(VASTModel vastData) {

        if(vastData == null){
            Log.e(TAG, "parse vast error or get vast failed");
            listener.AdError(this);
            return;
        }

        this.vastData = vastData;

        listener.AdLoaded(this);

    }

    @Override
    public void onError() {
        listener.AdError(this);
    }

    @Override
    public void onCancelled(){
        listener.AdError(this);
    }

    /**
     * log helper
     */
    public void setLogEnabled(Boolean enabled){
        LogUtil.logEnabled = enabled;
    }

    public void setLogTag(String logTag){
        LogUtil.logTag = logTag;
    }

    /****************************************/
    /**  VPAID Interfaces                  **/
    /****************************************/

    public String sdkVersion(){
        return SDK_VERSION;
    }

    /**
     * 版本号交换
     * @param version
     * @return
     */
    public String handshakeVersion(String version){
        return VPAID_VERSION;
    }


    public void initAd(int width, int height, String viewMode, int desiredBitrate){
        initAd(width, height, viewMode, desiredBitrate, "", "");
    }

    public void initAd(int width, int height, String viewMode, int desiredBitrate, String creativeData){
        initAd(width, height, viewMode, desiredBitrate, creativeData, "");
    }

    /**
     * 初始化广告，获取广告数据
     *
     * @param width
     * @param height
     * @param viewMode
     * @param desiredBitrate
     * @param creativeData
     * @param environmentVars
     */
    public void initAd(int width, int height, String viewMode, int desiredBitrate, String creativeData, String environmentVars){
        stopPlayer();

        this.playedTime = 0;

        this.initWidth = width;
        this.initHeight = height;

        String vastUrl = VAST_SERVER;
        if (creativeData.startsWith("http://") || creativeData.startsWith("https://")) {
            vastUrl = creativeData;
            // 替换
            vastUrl = StringUtil.http2https(vastUrl);
        }
        else {
            vastUrl = vastUrl + creativeData;
        }
        LogUtil.d(TAG, "vast url = " + vastUrl);

        String separator = "?";
        if (vastUrl.contains("?")){
            separator = "&";
        }
        vastUrl = vastUrl + separator + "uuid=" + statistics.getUID();
        VASTParser parser = new VASTParser(this);
        parser.parse(vastUrl, context);

        envMap = StringUtil.getQueryMap(environmentVars);
    }

    public void resizeAd(int width, int height, String viewMode){
        resizePlayer(width, height);
    }

    public void startAd(){
        listener.AdStarted(this);
        this.playAd();
    }

    public void stopAd(){
        LogUtil.d(TAG, "stopAd called");
        stopPlayer();
        listener.AdStopped(this);
    }

    public void pauseAd(){
        pausePlayer();
    }

    public void resumeAd(){
        resumePlayer();
    }

    public void expandAd(){
        return;
    }

    public void collapseAd(){
        return;
    }

    public void skipAd(){
        destroyPlayer();
        listener.AdSkipped(this);
    }

    /****************************************/
    /**  Private methods                   **/
    /****************************************/


    /**
     * 按顺序播放广告
     *
     */
    private void playAd(){
        if (vastData == null){
            LogUtil.d(TAG, "vast data is null");
            destroyPlayer();
            listener.AdStopped(this);
            return;
        }

        CreativeModel creative = vastData.findNextCreative();
        if(creative == null){
            LogUtil.d(TAG, "no creative to play " + vastData.playingAdIndex);
            destroyPlayer();
            listener.AdStopped(this);
            return;
        }

        if(creative.mediaFiles.size() == 0 || creative.mediaFiles.get(0).uri == null) {
            this.playAd();
            return;
        }

        // 显示提示
//        if (loadingTextView == null){
//            loadingTextView = new TextView(context);
//            loadingTextView.setBackgroundColor(0x88000000);
//            loadingTextView.setTextColor(0xFFFFFFFF);
//            loadingTextView.setText("正在加载……");
//            loadingTextView.setGravity(Gravity.CENTER);
//        }
//
//        if (loadingTextView.getParent() == null){
//            this.addView(loadingTextView);
//            LogUtil.d(TAG, "loading tips created");
//        }

        if (loadingIndicator == null){
           loadingIndicator = new ProgressBar(context);
            loadingIndicator.setIndeterminate(true);
        }

        if (loadingIndicator.getParent() == null){
            this.addView(loadingIndicator);
            LogUtil.d(TAG, "loading tips created");
        }

        MediaFileModel mediaFile = creative.mediaFiles.get(0);
        LogUtil.d(TAG, mediaFile.type + " => " + mediaFile.uri);

        if(player != null && player.isCompatibleWith(mediaFile.type)){// && player.isCompatibleWith("video/mp4") == false) {
            // 视频会destory surfaceView，需要重新创建
            LogUtil.d(TAG, "resuse of player ");
            player.play(mediaFile);
        }
        else {
            LogUtil.d(TAG, "create new player");
            destroyPlayer();
            //this.removeAllViews();
            createPlayer(mediaFile, creative.duration, initWidth, initHeight);
        }

        //RelativeLayout.LayoutParams loadingLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        int loadingSize = (int) (32 * this.displayScale);
        RelativeLayout.LayoutParams loadingLayoutParams = new RelativeLayout.LayoutParams(loadingSize, loadingSize);
        loadingLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        //loadingTextView.setLayoutParams(loadingLayoutParams);
        loadingIndicator.setLayoutParams(loadingLayoutParams);
    }

    /**
     * 创建播放器
     *
     * @param mediaFile
     * @param duration
     * @param width
     * @param height
     */
    private void createPlayer(MediaFileModel mediaFile, int duration, int width, int height){

        String type = mediaTypes.get(mediaFile.type);
        if(type.equalsIgnoreCase("video")){
            LogUtil.d(TAG, "create video player");
            //createVideoPlayer(mediaUrl, width, height);
            player = new VASTVideoPlayer(context);
        }
        else if(type.equalsIgnoreCase("image")){
            LogUtil.d(TAG, "create image player");
            //createImagePlayer(mediaUrl, width, height);
            player = new VASTImagePlayer(context);
        }

        if (player == null){
            return;
        }

        if (mediaFile.scalable) {
            player.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    adClicked();
                }
            });
        }

        player.init(this, this, mediaFile, duration, width, height);
        this.addView(player);

        if (envMap.containsKey("scaletype")){
            player.setScaleType(envMap.get("scaletype"));
        }

        // 创建小控件
        createControllers();
    }

    /**
     * 创建小控件
     */
    private void createControllers(){
        this.lastControllerId = 0;

        // 布局顺序：左->倒计时->静音->跳过->关闭->右
        if(envMap.containsKey("showclose") && envMap.get("showclose").equalsIgnoreCase("1")) {
            createCloseButton();
        }

        if(envMap.containsKey("showskip") && envMap.get("showskip").equalsIgnoreCase("1")) {
            createSkipButton();
        }

        if(envMap.containsKey("showmute") && envMap.get("showmute").equalsIgnoreCase("1")) {
            createMuteButton();
        }

        if(envMap.containsKey("showcountdown") && envMap.get("showcountdown").equalsIgnoreCase("1")) {
            createCountdown();
        }

        CreativeModel creative = this.vastData.getPlayingCreative();
        if (creative.videoClicks.get("ClickThrough").size() > 0) {
            String clickUrl = creative.videoClicks.get("ClickThrough").get(0);
            if (clickUrl != null && clickUrl.length() > 0){
                if(envMap.containsKey("showlink") && envMap.get("showlink").equalsIgnoreCase("1")) {
                    createLinkButton();
                }
            }
        }
    }

    /**
     * 创建关闭按钮
     *
     */
    private void createCloseButton(){
        LogUtil.d(TAG, "create close button");

        if(closeButtonImageView == null){
            closeButtonImageView = new ImageView(context);
            closeButtonImageView.setBackgroundColor(TOOLBAR_BG_COLOR);
            Activity activity = (Activity)context;
            //LogUtil.d(TAG, "resource id = " + ResourceUtil.getIdByName(activity.getApplication(), "drawable-hdpi", "icon_close_white"));
            //closeButtonImageView.setImageDrawable(getResources().getDrawable(ResourceUtil.getIdByName(activity.getApplication(), "drawable-hdpi", "icon_close_white")));
            //closeButtonImageView.setImageDrawable(getResources().getDrawable(R.drawable.icon_close_white));

            //Drawable drawable = Assets.getDrawableFromBase64(getResources(), Assets.CLOSE_PNG);
            try {
                ResourceUtil res = new ResourceUtil();
                Drawable drawable = res.getDrawable(getResources(), "icon_close_white");
                closeButtonImageView.setImageDrawable(drawable);
            }
            catch (IOException e){
                LogUtil.d(TAG, e.getMessage());
            }
            closeButtonImageView.setId(CLOSE_BTN_ID);
            final VASTView self = this;
            closeButtonImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.AdUserClose(self);
                }
            });
        }

        if(closeButtonImageView.getParent() == null){
            this.addView(closeButtonImageView);
        }

        // 设置布局参数
        RelativeLayout.LayoutParams closeLayoutParams = new RelativeLayout.LayoutParams(toolbarHeight, toolbarHeight);
        closeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        closeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        closeLayoutParams.setMargins(0, 0, 0, 0);
        closeButtonImageView.setLayoutParams(closeLayoutParams);
        closeButtonImageView.setPadding(padding, padding, padding, padding);

        lastControllerId = CLOSE_BTN_ID;
    }

    /**
     *  创建跳过按钮
     *
     */
    private void createSkipButton(){
        LogUtil.d(TAG, "create skip button");
        if(skipButtonTextView == null){
            skipButtonTextView = new TextView(context);
            skipButtonTextView.setTextColor(TOOLBAR_FT_COLOR);
            skipButtonTextView.setBackgroundColor(TOOLBAR_BG_COLOR);
            skipButtonTextView.setPadding(padding, padding, padding, padding);
            skipButtonTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            skipButtonTextView.setId(SKIP_BTN_ID);
            //绑定事件
            final VASTView self = this;
            skipButtonTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.AdUserSkip(self);
                }
            });
        }

        skipButtonTextView.setText("跳过");
        if(skipButtonTextView.getParent() == null) {
            this.addView(skipButtonTextView);
        }

        // 设置布局参数
        RelativeLayout.LayoutParams skipLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, toolbarHeight);
        skipLayoutParams.setMargins(0, 0, 0, 0);
        this.updateLayoutRule(skipLayoutParams, lastControllerId);
        skipButtonTextView.setPadding(padding, padding / 2, padding, 0);
        skipButtonTextView.setLayoutParams(skipLayoutParams);

        lastControllerId = SKIP_BTN_ID;
    }

    /**
     * 创建静音按钮
     *
     */
    private void createMuteButton(){
        LogUtil.d(TAG, "create mute button");
        if(muteButtonImageView == null){
            muteButtonImageView = new ImageView(context);
            muteButtonImageView.setBackgroundColor(TOOLBAR_BG_COLOR);
            //Drawable drawable = Assets.getDrawableFromBase64(getResources(), Assets.SPEAKER_PNG);
            //muteButtonImageView.setImageDrawable(getResources().getDrawable(R.drawable.icon_speaker_white));
            try {
                ResourceUtil res = new ResourceUtil();
                Drawable drawable = res.getDrawable(getResources(), "icon_speaker_white");
                muteButtonImageView.setImageDrawable(drawable);
            }
            catch (IOException e){
                LogUtil.d(TAG, e.getMessage());
            }
            muteButtonImageView.setId(MUTE_BTN_ID);
            //绑定事件
            muteButtonImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleMute();
                }
            });
        }

        if(muteButtonImageView.getParent() == null){
            this.addView(muteButtonImageView);
        }

        // 设置布局参数
        RelativeLayout.LayoutParams muteLayoutParams = new RelativeLayout.LayoutParams(toolbarHeight, toolbarHeight);
        muteLayoutParams.setMargins(0, 0, 0, 0);
        this.updateLayoutRule(muteLayoutParams, lastControllerId);
        muteButtonImageView.setPadding(padding, padding, padding, padding);
        muteButtonImageView.setLayoutParams(muteLayoutParams);

        lastControllerId = MUTE_BTN_ID;
    }

    /**
     * 创建倒计时控件
     *
     */
    private void createCountdown(){
        LogUtil.d(TAG, "create count down");
        if(countDownTextView == null){
            countDownTextView = new TextView(context);
            countDownTextView.setTextColor(TOOLBAR_FT_COLOR);
            countDownTextView.setBackgroundColor(TOOLBAR_BG_COLOR);
            countDownTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        }

        countDownTextView.setText("");
        if(countDownTextView.getParent() == null) {
            this.addView(countDownTextView);
        }

        RelativeLayout.LayoutParams countDownLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, toolbarHeight);
        countDownLayoutParams.setMargins(0, 0, 0, 0);
        this.updateLayoutRule(countDownLayoutParams, lastControllerId);
        countDownTextView.setPadding(2 * padding, padding / 2, padding, 0);
        countDownTextView.setLayoutParams(countDownLayoutParams);
    }

    /**
     * 创建链接按钮
     *
     */
    private void createLinkButton(){
        LogUtil.d(TAG, "create link button");
        if(buttonMoreTextView == null){
            buttonMoreTextView = new TextView(context);
            buttonMoreTextView.setTextColor(0xffffff00);
            buttonMoreTextView.setBackgroundColor(0x88000000);
            buttonMoreTextView.setText("了解更多详情＞");
            buttonMoreTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            this.addView(buttonMoreTextView);

            buttonMoreTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    adClicked();
                }
            });
        }

        if(buttonMoreTextView.getParent() == null) {
            this.addView(buttonMoreTextView);
        }

        RelativeLayout.LayoutParams buttonMoreLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        buttonMoreLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        buttonMoreLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        buttonMoreLayoutParams.setMargins(0, 0, 0, 0);
        buttonMoreTextView.setLayoutParams(buttonMoreLayoutParams);
        buttonMoreTextView.setPadding(2 * padding, padding, padding, padding);
    }

    /**
     * 根据相邻按钮判断布局参数
     *
     * @param layoutParams  需要更改的布局参数
     * @param lastControllerId  上一个按钮ID
     */
    private void updateLayoutRule(RelativeLayout.LayoutParams layoutParams, int lastControllerId){
        if(lastControllerId != 0) {
            layoutParams.addRule(RelativeLayout.LEFT_OF, lastControllerId);
        }
        else {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        }
    }

    /**
     * 停止播放
     */
    private void stopPlayer(){
        if(player != null) {
            player.stop();
        }
    }

    /**
     * 销毁播放器
     */
    private void destroyPlayer(){
        if(player != null) {
            player.destroy();
            this.removeView(player);
        }

        if(closeButtonImageView != null && closeButtonImageView.getParent() != null) {
            this.removeView(closeButtonImageView);
        }

        if(skipButtonTextView != null && skipButtonTextView.getParent() != null) {
            this.removeView(skipButtonTextView);
        }

        if(muteButtonImageView != null && muteButtonImageView.getParent() != null) {
            this.removeView(muteButtonImageView);
        }

        if(countDownTextView != null && countDownTextView.getParent() != null) {
            this.removeView(countDownTextView);
        }

        if(buttonMoreTextView != null && buttonMoreTextView.getParent() != null) {
            this.removeView(buttonMoreTextView);
        }
    }

    /**
     * 暂停播放器
     */
    private void pausePlayer(){
        if (player != null) {
            player.pause();
        }
    }

    /**
     * 恢复播放
     */
    private void resumePlayer() {
        if (player != null) {
            player.resume();
        }
    }

    /**
     * 静音
     */
    private void mutePlayer() {
        if (player != null) {
            player.mute();
        }
    }

    /**
     * 取消静音
     */
    private void unmutePlayer() {
        if (player != null) {
            player.unmute();
        }
    }

    /**
     * 调整播放器大小
     */
    private void resizePlayer(int width, int height) {
        if (player != null) {
            player.resize(width, height);
        }

        this.relayoutToolbar(width, height);
    }

    private void initMediaTypes(){
        mediaTypes = new HashMap<String, String>(){{
            put("application/octet-stream", "video");
            put("video/mp4", "video");
            put("image/png", "image");
            put("image/jpeg", "image");
        }};
    }

    private void relayoutToolbar(int width, int height) {
        if (this.vastData == null){
            return;
        }

        MediaFileModel mediaFile = this.vastData.getPlayingMediaFile();
        if (mediaFile == null) {
            LogUtil.d(TAG, "playing media file is null");
            return;
        }

        if (!envMap.containsKey("scaletype") || !envMap.get("scaletype").equalsIgnoreCase("fill")) {
            if (!mediaFile.scalable) {
                // 禁止缩放而且不是fill模式
                int playerWidth = mediaFile.width;
                int playerHeight = mediaFile.height;
                if (width == 0){
                    width = this.player.getWidth();
                }
                if (height == 0){
                    height = this.player.getHeight();
                }

                int containerWidth = width;
                int containerHeight = height;
                int marginRight = (containerWidth - playerWidth) / 2;
                int marginTop = (containerHeight - playerHeight) / 2;

                if (playerWidth == 0){
                    marginRight = 0;
                }

                if (playerHeight == 0){
                    marginTop = 0;
                }

                LogUtil.d(TAG, " media-width = " + playerWidth + "; media-height = " + playerHeight
                        + " container-width = " + containerHeight + "container-height = " + containerHeight
                        + " margin-right = " + marginRight + "; margin-top = " + marginTop);

                Boolean rightFilled = false;

                if(closeButtonImageView != null && closeButtonImageView.getParent() != null) {
                    rightFilled = this.resetMargin(marginTop, marginRight, closeButtonImageView, rightFilled);
                }

                if(skipButtonTextView != null && skipButtonTextView.getParent() != null) {
                    rightFilled = this.resetMargin(marginTop, marginRight, skipButtonTextView, rightFilled);
                }

                if(muteButtonImageView != null && muteButtonImageView.getParent() != null) {
                    rightFilled = this.resetMargin(marginTop, marginRight, muteButtonImageView, rightFilled);
                }

                if(countDownTextView != null && countDownTextView.getParent() != null) {
                    rightFilled = this.resetMargin(marginTop, marginRight, countDownTextView, rightFilled);
                }

                if(buttonMoreTextView != null && buttonMoreTextView.getParent() != null) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)buttonMoreTextView.getLayoutParams();
                    params.setMargins(0, 0, marginRight, marginTop);
                }

                return;
            }
        }

        // setbackground to black
        this.setBackgroundColor(0xff000000);
    }

    private Boolean resetMargin(int marginTop, int marginRight, View view, Boolean rightFilled){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)view.getLayoutParams();
        if (rightFilled){
            params.setMargins(0, marginTop, 0, 0);
        }
        else {
            params.setMargins(0, marginTop, marginRight, 0);
        }
        return true;
    }

    // 静音按钮操作
    private void toggleMute(){
        if (player == null) {
            return;
        }
        Boolean muted = player.getMuted();
        // String image = muted ? Assets.SPEAKER_PNG : Assets.MUTE_PNG;
        // Drawable drawable = Assets.getDrawableFromBase64(getResources(), image);
        // muteButtonImageView.setImageDrawable(drawable);
        try {
            String image = muted ? "icon_speaker_white" : "icon_mute_white";
            ResourceUtil res = new ResourceUtil();
            Drawable drawable = res.getDrawable(getResources(), image);
            muteButtonImageView.setImageDrawable(drawable);
        }
        catch (IOException e){
            LogUtil.d(TAG, e.getMessage());
        }
        if (muted){
            player.unmute();
        }
        else {
            player.mute();
        }
    }

    // 广告点击操作
    private void adClicked(){
        if (vastData == null){
            return;
        }

        //if (loadingTextView != null && loadingTextView.getParent() != null){
        if (loadingIndicator != null && loadingIndicator.getParent() != null){
            // 尚未加载完成,忽略点击
            return;
        }

        CreativeModel creative = vastData.getPlayingCreative();

        String uid = statistics.getUID();
        Boolean playerHandles = false;
        if (envMap.containsKey("playerhandles") && envMap.get("playerhandles").equalsIgnoreCase("1")){
            playerHandles = true;
        }

        if (creative == null || creative.videoClicks == null || creative.videoClicks.size() <= 0) {
            listener.AdClickThru(this, "", uid, playerHandles);
            return;
        }

        // 发送统计
        List<String> clickTrackings = creative.videoClicks.get("ClickTracking");
        statistics.sendRequests(clickTrackings);
        String clickUrl = null;

        if (creative.videoClicks.get("ClickThrough").size() > 0) {
            clickUrl = creative.videoClicks.get("ClickThrough").get(0);
        }

        if(clickUrl == null || clickUrl.length() == 0){
            LogUtil.d(TAG, "clicked url is empty");
            return;
        }

        LogUtil.d(TAG, "clicked => " + clickUrl);

        player.pause();

        if (playerHandles) {
            listener.AdClickThru(this, clickUrl, uid, true);
            return;
        }

        listener.AdClickThru(this, clickUrl, uid, false);

        Intent intent = new Intent(this.context, VASTWebView.class);
        intent.putExtra("url", clickUrl);
        if (null != intent.resolveActivity(context.getPackageManager())) {
            context.startActivity(intent);
        }
    }
}
