package com.maihehd.sdk.vast.util;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by roger on 7/4/15.
 */
public class MD5 {

    private final static String TAG = "MD5";

    public static String hash(String text){
        if(text == null){
            return "";
        }

        String str = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(text.getBytes());
            str = byteToHexString(bytes);
        }
        catch (NoSuchAlgorithmException e){
            Log.e(TAG, e.getMessage());
        }

        return str;
    }

    public static String hash16(String text){
        String str = hash(text);
        if(str.length() < 24){
            return str;
        }

        return str.substring(8, 24);
    }

    /**
     * 将指定byte数组转换成16进制字符串
     * @param b
     * @return
     */
    private static String byteToHexString(byte[] b) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            hexString.append(hex.toUpperCase());
        }
        return hexString.toString();
    }
}
