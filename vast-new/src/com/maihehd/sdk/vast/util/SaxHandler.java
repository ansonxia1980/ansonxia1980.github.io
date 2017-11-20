package com.maihehd.sdk.vast.util;

/**
 * Created by roger on 6/29/15.
 */
import android.util.Log;
import com.maihehd.sdk.vast.model.AdModel;
import com.maihehd.sdk.vast.model.CreativeModel;
import com.maihehd.sdk.vast.model.MediaFileModel;
import com.maihehd.sdk.vast.model.VASTModel;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

public class SaxHandler extends DefaultHandler {

    private final static String TAG = "SaxHandler";
    private final static List<String> MEDIA_TYPE = Arrays.asList("application/octet-stream", "video/mp4", "image/png", "image/jpeg");

    private VASTModel vastModel = null;
    private AdModel adModel = null;
    private CreativeModel creativeModel = null;
    private MediaFileModel mediaFileModel = null;
    private StringBuilder content = new StringBuilder();
    private Map<String, List<String>> trackingEvents = null;
    private String eventName;
    private Map<String, List<String>> videoClicks = null;

    private AdModel preferedAdModel = null;
    private Boolean isBackup = false;

    public VASTModel getVASTModel(){
        return vastModel;
    }

    /* 此方法有三个参数
       arg0是传回来的字符数组，其包含元素内容
       arg1和arg2分别是数组的开始位置和结束位置 */
    @Override
    public void characters(char[] chars, int start, int length) throws SAXException {
        content.append(new String(chars, start, length));
        super.characters(chars, start, length);
    }

    @Override
    public void endDocument() throws SAXException {
        LogUtil.d(TAG, "end document");
        super.endDocument();
    }

    @Override
    public void startDocument() throws SAXException {
        vastModel = new VASTModel();
        vastModel.adList = new ArrayList<AdModel>();
        super.startDocument();
    }

    /* arg0是名称空间
       arg1是包含名称空间的标签，如果没有名称空间，则为空
       arg2是不包含名称空间的标签 */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        //LogUtil.d(TAG, "end element " + qName);
        super.endElement(uri, localName, qName);

        if(qName.equalsIgnoreCase("VAST")){
            vastModel.calculateDuration();
        }
        else if(qName.equalsIgnoreCase("Ad")){
            if(this.isBackup){
                LogUtil.d(TAG, "is backup ended " + qName + " " + this.isBackup);
                preferedAdModel.backups.add(adModel);
            }
            else {
                vastModel.adList.add(adModel);
            }
        }
        else if(qName.equalsIgnoreCase("AdSystem")){
            adModel.adSystem = content.toString();
        }
        else if(qName.equalsIgnoreCase("Creative")){
            adModel.creatives.add(creativeModel);
        }
        else if(qName.equalsIgnoreCase("Duration")){
            creativeModel.duration = TimeUtil.HHMMSSToSeconds(content.toString());
        }
        else if(qName.equalsIgnoreCase("TrackingEvents")){
            creativeModel.trackingEvents = trackingEvents;
        }
        else if(qName.equalsIgnoreCase("Tracking")){
            if(trackingEvents.containsKey(eventName) == false){
                trackingEvents.put(eventName, new ArrayList<String>());
            }
            String trackingUrl = content.toString();
            trackingUrl = StringUtil.http2https(trackingUrl);
            LogUtil.d(TAG, "Tracking url: " + trackingUrl);
            trackingEvents.get(eventName).add(trackingUrl.toString());
        }
        else if(qName.equalsIgnoreCase("VideoClicks")){
            creativeModel.videoClicks = videoClicks;
        }
        else if(qName.equalsIgnoreCase("ClickThrough") || qName.equalsIgnoreCase("ClickTracking")){
            if(videoClicks.containsKey(eventName) == false){
                videoClicks.put(eventName, new ArrayList<String>());
            }

            String clickUrl = content.toString();
            videoClicks.get(eventName).add(StringUtil.http2https(clickUrl));
        }
        else if(qName.equalsIgnoreCase("MediaFile")){
            if(mediaFileModel != null){
                mediaFileModel.uri = StringUtil.http2https(content.toString());
                creativeModel.mediaFiles.add(mediaFileModel);
            }
            LogUtil.d(TAG, StringUtil.http2https(content.toString()));
        }
        else if(qName.equalsIgnoreCase("BackupAdList")){
            this.isBackup = false;
        }
        // in extension
        else if(qName.equalsIgnoreCase("Slider")){
            if (content.toString().equalsIgnoreCase("true")) {
                vastModel.isSlider = true;
                adModel.isSlider = true;
            }
        }
    }

    /*arg0是名称空间
      arg1是包含名称空间的标签，如果没有名称空间，则为空
      arg2是不包含名称空间的标签
      arg3是属性的集合 */
    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attrs) throws SAXException {
        //LogUtil.d(TAG, "start element " + qName);
        if(qName.equalsIgnoreCase("Ad")){
            adModel = new AdModel();
            adModel.id = attrs.getValue("id");
        }
        else if(qName.equalsIgnoreCase("Inline")){
            adModel.type = "Inline";
        }
        else if(qName.equalsIgnoreCase("Creatives")){
            adModel.creatives = new ArrayList<CreativeModel>();
        }
        else if(qName.equalsIgnoreCase("Creative")){
            creativeModel = new CreativeModel();
        }
        else if(qName.equalsIgnoreCase("Linear")){
            creativeModel.type = "Linear";
        }
        else if(qName.equalsIgnoreCase("TrackingEvents")){
            trackingEvents = new HashMap<String, List<String>>();
        }
        else if(qName.equalsIgnoreCase("Tracking")){
            eventName = attrs.getValue("event");
        }
        else if(qName.equalsIgnoreCase("VideoClicks")){
            videoClicks = new HashMap<String, List<String>>();
        }
        else if(qName.equalsIgnoreCase("ClickThrough") || qName.equalsIgnoreCase("ClickTracking")){
            eventName = qName;
        }
        else if(qName.equalsIgnoreCase("MediaFiles")){
            creativeModel.mediaFiles = new ArrayList<MediaFileModel>();
        }
        else if(qName.equalsIgnoreCase("MediaFile")){
            mediaFileModel = null;
            String mediaType = attrs.getValue("type").toLowerCase();
            if( MEDIA_TYPE.contains(mediaType)){
                mediaFileModel = new MediaFileModel();
                mediaFileModel.type = mediaType;
                mediaFileModel.width = Integer.parseInt(attrs.getValue("width"));
                mediaFileModel.height = Integer.parseInt(attrs.getValue("height"));
                mediaFileModel.scalable = attrs.getValue("scalable") == null || StringUtil.string2Bool(attrs.getValue("scalable"));
                mediaFileModel.maintainAspectRatio = StringUtil.string2Bool(attrs.getValue("maintainAspectRatio"));
            }
        }
        else if(qName.equalsIgnoreCase("BackupAdList")){
            LogUtil.d(TAG, "is backup start" + qName);
            this.isBackup = true;
            preferedAdModel = adModel;
            preferedAdModel.backups = new ArrayList<AdModel>();
        }

        content.delete(0, content.length());

        super.startElement(uri, localName, qName, attrs);
    }
}
