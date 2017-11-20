package com.maihehd.sdk.vast.model;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

/**
 * Created by roger on 6/29/15.
 */
public class CreativeModel {
    // <Linear>, <NonLinear> or <CompanionAds>
    public String type;

    // for companionAds, save the XML
    // for VPAID, default return empty string
    public String companionAds = "";

    // companionAds should be displayed
    // it could be all, any, none
    public String companionAdsRequired = "none";

    // an ad server-defined identifier for the creative
    public String id;

    // the numerical order in which each sequenced creative should display
    // (not to be confused with the <Ad> sequence attribute used to define
    // Ad Pods)
    public Number sequence;

    // identifies the ad with which the creative is served
    public String adId;

    // the technology used for any included API
    public String apiFramework;

    // linear with skipoffset attribute
    // format could be HH:MM:SS.mmm, HH:MM:SS or pecentage
    public int skipOffset = -1;

    // required elements: <Duration>, <MediaFiles>
    // duration format: HH:MM:SS.mmm or HH:MM:SS
    public int duration;

    // a container for one or more <MediaFile> elements
    public List<MediaFileModel> mediaFiles;

    // optional elements: <VideoClicks>, <AdParameters>, <TrackingEvents>,
    // <Icons>

    // contains a single <ClickThrough> element, and optionally contain one
    // or more child <ClickTracking> and <CustomClick> elements
    public Map<String, List<String>> videoClicks;

    public List<String> adParameters;
    public Map<String, List<String>> trackingEvents;
    public List<String> adIcons;

    // CreativeExtensions
    public List<String> creativeExtensions;

    public CreativeModel()
    {
    }
}
