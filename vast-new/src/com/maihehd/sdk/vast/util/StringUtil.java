package com.maihehd.sdk.vast.util;

import android.util.Base64;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by roger on 7/4/15.
 */
public class StringUtil {

    public final static String TAG = "StringUtil";

    public static Map<String, String> getQueryMap(String query){
        Map<String, String> map = new HashMap<String, String>();

        String[] params = query.split("&");
        String[] pair;
        for (String param : params)
        {
            pair = param.split("=");
            String name = pair[0];
            String value = pair.length > 1 ? pair[1] : "";
            LogUtil.d(TAG, name + " => " + value);
            map.put(name, value);
        }
        return map;
    }

    public static String int2ip(long ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    public static Boolean string2Bool(String str){
        if (str == null){
            return false;
        }

        return str.equalsIgnoreCase("true") || str.equals("1");
    }

    public static String cookieEncrypt(String value, String uuid) {
        byte[] vb = value.getBytes();
        int vl = vb.length;
        byte[] ub = uuid.getBytes();
        int ul = ub.length;
        for (int i = 0; i < vb.length; i++) {
            vb[i] = (byte) (vb[i] ^ ub[vl % ul]);
        }
        return Base64.encodeToString(vb, Base64.DEFAULT);
        //return new BASE64Encoder().encode(vb);
    }

    public static String cookieDecrypt(String value, String uuid) {
        byte[] vb;
        //try {
            vb = Base64.decode(value, Base64.DEFAULT);
            //vb = new BASE64Decoder().decodeBuffer(value);
        //} catch (IOException e) {
//            return "";
//        }
        int vl = vb.length;
        byte[] ub = uuid.getBytes();
        int ul = ub.length;
        for (int i = 0; i < vb.length; i++) {
            vb[i] = (byte) (vb[i] ^ ub[vl % ul]);
        }
        return new String(vb, 0, vb.length);
    }

    public static String getRootDomain(String url) {
        if (url == null) {
            return null;
        }
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            String[] hostStr = host.split("\\.");
            int length = hostStr.length;
            if (hostStr.length >= 2) {
                return hostStr[length - 2] + "." + hostStr[length - 1];
            } else {
                return host;
            }
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static  String http2https(String url) {
        String regex = "^http://";
        return url.replaceFirst(regex, "https://");
    }
}
