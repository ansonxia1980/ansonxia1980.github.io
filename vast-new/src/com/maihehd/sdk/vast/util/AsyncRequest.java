package com.maihehd.sdk.vast.util;

import android.content.Context;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.webkit.CookieSyncManager;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Created by roger on 7/6/15.
 */
public class AsyncRequest extends AsyncTask {

    private static final String TAG = "AsyncRequest";

    public static final String INPUT_STREAM = "input_stream";
    public static final String STRING = "string";

    private AsyncRequestListener listener;
    private String dataType = AsyncRequest.STRING;
    private Integer timeout = 10 * 1000;

    public AsyncRequest(AsyncRequestListener listener){
        this.listener = listener;
    }

    public AsyncRequest(AsyncRequestListener listener, String dataType){
        this(listener);
        this.dataType = dataType;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        try {
            //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.2.231", 8888));
            URI requestURI = new URI(params[0].toString());
            URLConnection connection = new URL(params[0].toString()).openConnection();

            CookieManager cookieManager = new CookieManager(new PersistentCookieStore((Context)params[1]), CookiePolicy.ACCEPT_ALL);
            //CookieManager cookieManager = new CookieManager();
            //cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(cookieManager);

            String cookieURL = requestURI.getScheme() + "://" + requestURI.getHost() + "/";
            //List<HttpCookie> sendCookies = cookieManager.getCookieStore().get(new URI(cookieURL));

            List<HttpCookie> validCookies = new ArrayList<HttpCookie>();
//            List<String> addedKeys = new ArrayList<String>();
            List<HttpCookie> savedCookies = cookieManager.getCookieStore().getCookies();
            String requestDomain = requestURI.getHost().toLowerCase();
            for (HttpCookie cookie : savedCookies) {
                if (requestDomain.contains(cookie.getDomain().toLowerCase())){
//                    if (addedKeys.contains(cookie.getName())){
//                        for (HttpCookie ck : validCookies){
//                            if (ck.getName().equalsIgnoreCase(cookie.getName())){
//                                if(ck.getValue().compareTo(cookie.getValue()) < 0){
//                                    validCookies.remove(ck);
//                                    validCookies.add(cookie);
//                                }
//                                break;
//                            }
//                        }
//                    }
//                    else {
                        validCookies.add(cookie);
//                        addedKeys.add(cookie.getName());
                        //LogUtil.d(TAG, "add cookie named " + cookie.getName());
//                    }
                }
            }

            //connection.setRequestProperty("Cookie", TextUtils.join(";",  cookieManager.getCookieStore().getCookies()));
            //LogUtil.d(TAG, "sent cookie " + TextUtils.join(";",  cookieManager.getCookieStore().getCookies()));
            connection.setRequestProperty("Cookie", TextUtils.join(";",  validCookies));
            //connection.setRequestProperty("Cookie", TextUtils.join(";",  sendCookies));
//            LogUtil.d(TAG, "sent cookie " + TextUtils.join(";",  sendCookies));

//            String headerName;
//            String cookie;
//            for (int i=1; (headerName = connection.getHeaderFieldKey(i))!=null; i++) {
//                if (headerName.equalsIgnoreCase("set-cookie")){
//                    cookie = connection.getHeaderField(i);
//                    LogUtil.d(TAG, "response cookie: " + headerName + "=" + cookie);
//                    //cookieStore.add(new URI(params[0].toString()), new HttpCookie());
//                }
//            }

            if (connection.getHeaderFields() != null) {
                //cookieManager.put(new URI(params[0].toString()), connection.getHeaderFields());
                //URI sourceURI = new URI(params[0].toString());
                //String cookieURL = sourceURI.getScheme() + "://" + sourceURI.getHost() + "/";
                cookieManager.put(new URI(cookieURL), connection.getHeaderFields());
            }

            //CookieStore cookieStore = cookieManager.getCookieStore();
            //List<HttpCookie> cookieList = cookieStore.getCookies();
            // iterate HttpCookie object
            // String uuid = "";
            // for (HttpCookie ck : cookieList) {
                // LogUtil.d(TAG, "response cookie: " + ck.getName() + "=" + ck.getValue() + "; domain=" + ck.getDomain() + "; path=" + ck.getPath());
                //if (ck.getName().equalsIgnoreCase("mh_ur")){
                    //LogUtil.d(TAG, "received mh_ur => " + StringUtil.cookieDecrypt(ck.getValue(), uuid));
                //}
            // }

            InputStream ins = null;
            connection.setConnectTimeout(this.timeout);
            connection.setReadTimeout(this.timeout);
            if (connection.getHeaderField("Content-Encoding") != null &&
                    connection.getHeaderField("Content-Encoding").equals("gzip")){
                ins = new GZIPInputStream(connection.getInputStream());
            } else {
                ins = connection.getInputStream();
            }

            if(this.dataType == AsyncRequest.INPUT_STREAM){
                return ins;
            }

            StringBuilder content = new StringBuilder();
            BufferedReader inr = new BufferedReader(new InputStreamReader(ins));
            String inputLine;
            while ((inputLine = inr.readLine()) != null){
                content.append(inputLine + "\n");
            }
            inr.close();

            return content.toString();
        }
        catch (IOException ex) {
            ex.printStackTrace();

            listener.onError();
        }
        catch (URISyntaxException ex){
            ex.printStackTrace();

            listener.onError();
        }

        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        listener.onCancelled();
    }

    @Override
    protected void onPostExecute(Object object) {
        listener.onPostExecute(object);
    }

    @Override
    protected void onPreExecute() {
        listener.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Object[] objects) {
        listener.onProgressUpdate(Integer.getInteger(objects[0].toString()));
    }
}
