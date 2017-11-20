package com.maihehd.sdk.vast;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.maihehd.sdk.vast.util.*;
import org.OpenUDID.OpenUDID_manager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by roger on 7/4/15.
 */
public class Statistics implements AsyncRequestListener {

    private final static String TAG = "Statistics";

//    private String IESID = "";
//    private String OS = "0";    // 0=>android, 1=>iOS, 2=>WP, 3=>Others
//    private String IMEI = "";
//    private String IMSI = "";
//    private String MAC = "";
//    private String MAC1 = "";
//    private String IDFA = "";
//    private String AAID = "";
//    private String OPENUDID = "";
//    private String ANDROIDID = "";
//    private String ANDROIDID1 = "";
//    private String UDID = "";
//    private String IP = "";
//    private String UA = "";
//    private String DRA = "";
//    private String TIME = "";
//    private String APP = "";

    /**
     * 域名区分
     * 秒针：g.dtv.cn.miaozhen.com
     * admaster：v.admaster.com.cn
     * 尼尔森：ott.nielsenccdata.tv
     */
    private Map<String, String> mmaCommonParams;
    private Map<String, String> miaozhenParams;
    private Map<String, String> admasterParams;
    private Map<String, String> nielsenParams;

    private Context context = null;
    //private static Boolean inited = false;

    //private static Statistics instance = null;

    @Override
    public void onError() {
        return;
    }

    @Override
    public void onCancelled() {
        return;
    }

    @Override
    public void onPostExecute(Object data) {
        if(data != null) {
            LogUtil.d(TAG, data.toString());
        }
    }

    @Override
    public void onPreExecute() {
        return;
    }

    @Override
    public void onProgressUpdate(Integer percent) {
        return;
    }

//    private static class StatisticsHodler{
//        private static final Statistics INSTANCE = new Statistics();
//    }

    public Statistics(Context context){
        this.context = context;
        initCommonParams();
    }

    public String getUID(){
        String openUDID = mmaCommonParams.get("OpenUDID");
        if (openUDID == null || openUDID.isEmpty()){
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        return openUDID;
    }

//    public static final Statistics getInstance(Context context){
//        Statistics.context = context;
//        if(instance == null){
//            instance = new Statistics();
//        }
//        return instance;
//        //return StatisticsHodler.INSTANCE;
//    }

//    public void sendRequests(String[] urls){
//        for(int i = 0; i < urls.length; i++){
//            this.sendRequest(urls[i]);
//        }
//    }

    /**
     * 批量发送统计请求
     *
     * @param urls url列表
     *
     */
    public void sendRequests(List<String> urls){
        if(urls == null){
            return;
        }

        Iterator iterator = urls.iterator();
        while(iterator.hasNext()) {
            this.sendRequest(iterator.next().toString());
        }
    }

    /**
     * 发送请求
     *
     * @param url 请求地址
     *
     */
    public void sendRequest(String url){
        /**
         * 域名区分
         * 秒针：g.dtv.cn.miaozhen.com
         * admaster：v.admaster.com.cn
         * 尼尔森：ott.nielsenccdata.tv
         */
        if (url.contains(".miaozhen.com")){
            if (miaozhenParams == null){
                this.initMiaozhenParams();
            }

            // 更新时间戳
            miaozhenParams.put("TIME", String.valueOf(System.currentTimeMillis()));
            // replace miaozhen params
            url = replaceMMAParams(url, miaozhenParams);
        }
        else if (url.contains(".admaster.com.cn")){
            if (admasterParams == null){
                this.initAdmasterParams();
            }

            // 更新时间戳
            admasterParams.put("TS", String.valueOf(System.currentTimeMillis()));
            // replace admaster params
            url = replaceMMAParams(url, admasterParams);
        }
        else if (url.contains(".nielsenccdata.tv")){
            if (nielsenParams == null){
                this.initNielsenParams();
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            nielsenParams.put("TIME", dateFormat.format(new Date()));  // required
            url = replaceMMAParams(url, nielsenParams);
        }

        LogUtil.d(TAG, "sending statistics => " + url);
        Object[] ps = {url, context};
        AsyncRequest request = new AsyncRequest(this);
        request.execute(ps);
    }

    /**
     * 替换MMA参数
     *
     * @param url 统计URL
     * @param params mma参数
     *
     * @return
     */
    private String replaceMMAParams(String url, Map<String, String> params){
        Iterator iterator = params.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if(entry.getKey() == null || entry.getValue() == null){
                continue;
            }

            String key = entry.getKey().toString();
            String val = "";
            try {
                val = URLEncoder.encode(entry.getValue().toString(), "UTF-8");
            }
            catch (UnsupportedEncodingException ex){
                LogUtil.d(TAG, "encode message " + ex.toString());
            }
            url = url.replaceAll("__" + key + "__", val);
        }
        return url;
    }

    /**
     * 发送尼尔森统计
     *
     * @param params 参数
     *
     */
    private void sendToNielsen(Map<String, String> params){
        StringBuilder sb = new StringBuilder();
        for(HashMap.Entry<String, String> e : params.entrySet()){
            if(sb.length() > 0){
                sb.append('&');
            }
            String val = "";
            try{
                val = URLEncoder.encode(e.getValue(), "UTF-8");
            }
            catch (UnsupportedEncodingException ex){
                LogUtil.d(TAG, "encode message " + ex.toString());
            }
            sb.append(e.getKey()).append('=').append(val);
        }

        String url = "http://ott.nielsenccdata.tv:50001/collection?" + sb.toString();

        Object[] ps = {url, context};
        AsyncRequest request = new AsyncRequest(this);
        request.execute(ps);
    }

    private void initCommonParams(){
        mmaCommonParams = new HashMap<String, String>();
        mmaCommonParams.put("OPENUDID", this.getOpenUDID()); // required
        mmaCommonParams.put("IESID", "_IESID_");
        mmaCommonParams.put("OS", "0"); // required
    }

    /**
     * 初始化秒针MMA相关参数
     *
     */
    private void initMiaozhenParams(){
        miaozhenParams = new HashMap<String, String>();
        String[] imes = getIMES(context);
        miaozhenParams.put("IMEI", MD5.hash16(imes[0]));
        miaozhenParams.put("IMSI", imes[1]);
        String[] addresses = getNetworkAddress(context);
        String mac = "";
        if (addresses.length > 0 && addresses[0] != null) {
            mac = addresses[0].toUpperCase();
        }
        miaozhenParams.put("MAC", MD5.hash16(mac.replaceAll(":", "").toUpperCase()));
        miaozhenParams.put("MAC1", MD5.hash16(mac.toLowerCase()));
        String ip = "";
        if (addresses.length > 1 && addresses[1] != null){
            ip = addresses[1];
        }
        miaozhenParams.put("IP", ip); // required
        miaozhenParams.put("AAID", mmaCommonParams.get("OPENUDID"));
        String androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        miaozhenParams.put("ANDROIDID", MD5.hash16(androidID));
        miaozhenParams.put("ANDROIDID1", androidID);
        miaozhenParams.put("ODIN", "__ODIN__");
        miaozhenParams.put("UA", "__UA__");
        miaozhenParams.put("DRA", "__DRA__");
        miaozhenParams.put("TIME", String.valueOf(System.currentTimeMillis()));
        miaozhenParams.put("APP", getApplicationName(context)); // required
    }

    /**
     * 初始化AdMaster MMA相关参数
     *
     */
    private void initAdmasterParams() {
        admasterParams = new HashMap<String, String>();
        admasterParams.put("TS", String.valueOf(System.currentTimeMillis())); // required
        String[] addresses = getNetworkAddress(context);
        String mac = "";
        if (addresses.length > 0 && addresses[0] != null) {
            mac = addresses[0].toUpperCase();
        }
        admasterParams.put("MAC", MD5.hash(mac.replaceAll(":", "").toUpperCase()).toLowerCase()); // required
        admasterParams.put("MAC1", mac.toLowerCase()); // required
        String ip = "";
        if (addresses.length > 1 && addresses[1] != null){
            ip = addresses[1];
        }
        admasterParams.put("IP", ip); // required
        admasterParams.put("CNAME", "__CNAME__");
        admasterParams.put("PNAME", "__PNAME__");
        admasterParams.put("STBID", "__STBID__");
        String androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        admasterParams.put("ANDROIDID", MD5.hash(androidID).toLowerCase());
        admasterParams.put("ANDROIDID1", androidID);
        String[] imes = getIMES(context);
        admasterParams.put("IMEI", MD5.hash(imes[0]).toLowerCase());
        admasterParams.put("IMEI1", imes[0]);
        admasterParams.put("PROVINCE", "__PROVINCE__");
        admasterParams.put("CITY", "__CITY__");
        // tv related
        admasterParams.put("TVSIZE", "__TVSIZE__");
        admasterParams.put("TVTYPE", "__TVTYPE__");
        admasterParams.put("TVL", "__TVL__");
        admasterParams.put("OZ", "0zapi"); // required
    }

    /**
     * 初始化尼尔森相关参数
     *
     */
    private void initNielsenParams() {
        // http://ott.nielsenccdata.tv:50001/collection?DataMark=1&CustomerID=C2015112301&DeviceID=__UDID__&Mac=__MAC__&IP=__IP__&Time=__TIME__&AppName=__APP__&AppPackageName=__PACKAGE__&ADEventType=1&ADSID=__ADSID__&ADMaterialID=__MATERIALID__&ADSpace=__ADSPACE__&ProgramName=__PNAME__&PagePath=__PPATH__&MovieName=__CNAME__
        nielsenParams = new HashMap<String, String>();
        //nielsenParams.put("DataMark", "1");
        //nielsenParams.put("CustomerID", "C2015112301");
        //nielsenParams.put("DeviceID", this.getOpenUDID());  // required, TVID
        nielsenParams.put("UDID", this.getOpenUDID());  // required, TVID
        String[] addresses = getNetworkAddress(context);
        String mac = "";
        if (addresses.length > 0 && addresses[0] != null) {
            mac = addresses[0].toUpperCase();
        }
        //nielsenParams.put("Mac", mac.replaceAll(":", "")); // required
        nielsenParams.put("MAC", mac.replaceAll(":", "")); // required
        String ip = "";
        if (addresses.length > 1 && addresses[1] != null){
            ip = addresses[1];
        }
        nielsenParams.put("IP", ip); // required
        //nielsenParams.put("Time", String.valueOf(new Date()));  // required
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        nielsenParams.put("TIME", dateFormat.format(new Date()));  // required
        //nielsenParams.put("AppName", this.getApplicationName(context) + "_" + this.getAppVersionName(context)[0]); // required
        nielsenParams.put("APP", this.getApplicationName(context) + "_" + this.getAppVersionName(context)[0]); // required
        //nielsenParams.put("AppPackageName", this.getPackageName(context)); // required
        nielsenParams.put("PACKAGE", this.getPackageName(context));
        //nielsenParams.put("ADEventType", ""); // required, implemented in sendRequests
        nielsenParams.put("ADSID", ""); // required, 广告的系统ID
        //nielsenParams.put("ADMaterialID", "");  // required, 投放ID
        nielsenParams.put("MATERIALID", "");
        //nielsenParams.put("ADSpace", "");   // optional, 广告位置
        nielsenParams.put("ADSPACE", "");
        //nielsenParams.put("ProgramName", "");
        nielsenParams.put("PNAME", "");
        //nielsenParams.put("PagePath", "");
        nielsenParams.put("PPATH", "");
        //nielsenParams.put("MovieName", "");
        nielsenParams.put("CNAME", "");
    }

    /**
     * 获取OpenUDID
     *
     * @return
     */
    public String getOpenUDID(){
        if(OpenUDID_manager.isInitialized())
        {
            return OpenUDID_manager.getOpenUDID();
        }

        return "";
    }

    /**
     * 获取网络地址
     *
     * @param context
     * @return
     */
    public String[] getNetworkAddress(Context context){
        String[] addresses = {"", ""};
        WifiManager wifiMgr = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());

        if (!wifiMgr.isWifiEnabled())
        {
            //必须先打开，才能获取到MAC地址
            wifiMgr.setWifiEnabled(true);
            wifiMgr.setWifiEnabled(false);
        }

        if (null != info) {
            addresses[0] = info.getMacAddress();
            addresses[1] = StringUtil.int2ip(info.getIpAddress());
        }
        return addresses;
    }

    /**
     * 获取手机IMEI信息
     *
     * @param context
     * @return
     */
    public String[] getIMES(Context context){
        String[] imes = {"", ""};
        TelephonyManager tm = ((TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE));
        if(tm != null){
            imes[0] = tm.getDeviceId();
            imes[1] = tm.getSubscriberId();
        }
        return imes;
    }

    public String getPackageName(Context context){
        PackageManager pm = context.getApplicationContext().getPackageManager();
        String packageName = context.getPackageName();
        return packageName;
    }

    /**
     * 获取应用程序名称
     *
     * @param context
     * @return
     */
    public String getApplicationName(Context context){
        PackageManager pm = null;
        ApplicationInfo ai = null;
        try{
            pm = context.getApplicationContext().getPackageManager();
            ai = pm.getApplicationInfo(context.getPackageName(), 0);
        }
        catch (PackageManager.NameNotFoundException e){
            ai = null;
        }
        String name = (String) pm.getApplicationLabel(ai);
        return name;
    }

    /**
     * 获取当前应用程序版本号
     *
     * @param context
     * @return
     */
    public String[] getAppVersionName(Context context) {
        String[] versions = {"", ""};
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versions[0] = pi.versionName;
            versions[1] = String.valueOf(pi.versionCode);
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versions;
    }

    /**
     * 获取系统构建版本
     * @return
     */
    public String getAndroidVersion(){
        return Build.VERSION.RELEASE;
    }

}
