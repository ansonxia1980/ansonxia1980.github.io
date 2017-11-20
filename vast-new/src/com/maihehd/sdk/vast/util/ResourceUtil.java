package com.maihehd.sdk.vast.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Roger on 16/1/7.
 */
public class ResourceUtil {
    private final String TAG = "Resources";

    public Drawable getDrawable(Resources resources, String name) throws IOException {
        InputStream is = this.getClass().getResourceAsStream("/drawable-hdpi/" + name + ".png");
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        return new BitmapDrawable(resources, bitmap);
    }
}
