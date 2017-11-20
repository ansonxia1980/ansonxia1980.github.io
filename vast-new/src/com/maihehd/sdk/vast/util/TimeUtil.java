package com.maihehd.sdk.vast.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by roger on 6/29/15.
 */
public class TimeUtil {


    /**
     * HH:MM:SS.mmm or HH:MM:SS to seconds
     *
     * */
    public static int HHMMSSToSeconds(String time)
    {
        if(time == null)
        {
            return 0;
        }


        Pattern pattern = Pattern.compile("(\\d{1,2})\\:(\\d{1,2})\\:(\\d{1,2})(?:\\.(\\d{1,3}))?");
        Matcher matcher = pattern.matcher(time);
        if(matcher.find() == false){
            return 0;
        }

        int hours = Integer.parseInt(matcher.group(1));
        int minutes = Integer.parseInt(matcher.group(2));
        int seconds = Integer.parseInt(matcher.group(3));
        int milseconds = 0;
        if(matcher.groupCount() > 4) {
            milseconds = Integer.parseInt(matcher.group(4));
        }

        return (hours * 3600 + minutes * 60 + seconds) * 1000 + milseconds;
    }
}
