package com.maihehd.sdk.vast.model;

/**
 * Created by roger on 6/29/15.
 */
public class MediaFileModel {

    public String uri;
    ////////////////////////////////////////
    // Required Attributes                //
    ////////////////////////////////////////

    // either “progressive” for progressive download protocols (such as
    // HTTP) or “streaming” for streaming protocols.
    public String delivery;

    // MIME type for the file container. Popular MIME types include, but
    // are not limited to “video/x- flv” for Flash Video and “video/mp4”
    // for MP4
    public String type;

    // the native width of the video file, in pixels
    public int width;

    // the native height of the video file, in pixels
    public int height;


    ////////////////////////////////////////
    // Optional Attributes                //
    ////////////////////////////////////////

    // the codec used to encode the file which can take values as specified
    // by RFC 4281: http://tools.ietf.org/html/rfc4281
    public String codec;

    // an identifier for the media file
    public String id;

    // or minBitrate and maxBitrate: for progressive load video, the bitrate
    // value specifies the average bitrate for the media file; otherwise the
    // minBitrate and maxBitrate can be used together to specify the minimum
    // and maximum bitrates for streaming videos
    public int bitrate;

    // identifies whether the media file is meant to scale to larger
    // dimensions
    public Boolean scalable = true;

    // a Boolean value that indicates whether aspect ratio for media file
    // dimensions should be maintained when scaled to new dimensions
    public Boolean maintainAspectRatio = true;

    // identifies the API needed to execute an interactive media file
    public String apiFramework;

    public MediaFileModel()
    {
    }
}
