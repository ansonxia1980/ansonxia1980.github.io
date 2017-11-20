package com.maihehd.sdk.vast.model;

import java.lang.reflect.Array;
import java.util.List;

/**
 * Created by roger on 6/29/15.
 */
public class AdModel {
    // an ad server-defined identifier string for the ad
    public String id;

    // a number greater than zero (0) that identifies the sequence in which
    // an ad should play
    public int sequence;

    // inline or wrapper
    public String type;

    // the name of the ad server that returned the ad
    public String adSystem;

    // the common name of the ad
    public String adTitle;

    // a URI that directs the video player to a tracking resource file that
    // the video player should request when the first frame of the ad is
    // displayed
    public List<String> impressions;

    // The redirecting URI to the next VAST response
    public String vastAdTagUri;

    // creatives
    public List<CreativeModel> creatives;

    // current playing creative
    public int playingCreativeIndex = -1;


    //////////////   BEGIN  ////////////////
    // Optional InLine Elements           //
    ////////////////////////////////////////

    // a string value that provides a longer description of the ad
    public String description;

    // the name of the advertiser as defined by the ad serving party
    public String advertiser;

    // a URI to a survey vendor that could be the survey, a tracking pixel,
    // or anything to do with the survey. Multiple survey elements can be
    // provided. A type attribute is available to specify the MIME type
    // being served
    public Array surveys;

    // a URI representing an error-tracking pixel; this element can occur
    // multiple times
    public Array errors;

    // provides a value that represents a price that can be used by
    // real-time bidding (RTB) systems
    // If used, the following two attributes must be identified:
    //   model: identifies the pricing model as one of “CPM”, “CPC”, “CPE”,
    //     or“CPV”.
    //   currency: the 3-letter ISO-4217 currency symbol that identifies
    //     the currency of the value provided (i.e. USD, GBP, etc....)
    public Array pricing;

    // XML node for custom extensions, as defined by the ad server
    public Array extensions;

    ////////////////////////////////////////
    // Optional InLine Elements           //
    ////////////////   END  ////////////////

    // extensions
    public List<AdModel> backups;

    public Boolean isSlider = false;


    public AdModel()
    {
    }
}
