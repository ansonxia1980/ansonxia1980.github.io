package com.maihehd.sdk.vast.player;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by roger on 7/7/15.
 */
public class AsyncImage extends AsyncTask {

    private AsyncImageListener listener;
    private ImageView imageView;
    private Integer timeout = 10 * 1000;

    public AsyncImage(ImageView image, AsyncImageListener listener){
        this.imageView = image;
        this.listener = listener;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            URL url = new URL(objects[0].toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(this.timeout);
            conn.setReadTimeout(this.timeout);
            if (conn.getResponseCode() == 200) {
                InputStream input = conn.getInputStream();
                Bitmap map = BitmapFactory.decodeStream(input);
                return map;
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onImageFailed();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        super.onPostExecute(result);

        if (imageView != null && result != null) {
            imageView.setImageBitmap((Bitmap)result);
            listener.onImageComplete();
            return;
        }

        listener.onImageFailed();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.onImageStart();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        listener.onImageCancelled();
    }

}
