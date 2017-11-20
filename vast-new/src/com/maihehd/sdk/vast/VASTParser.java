package com.maihehd.sdk.vast;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;
import com.maihehd.sdk.vast.util.AsyncRequest;
import com.maihehd.sdk.vast.util.AsyncRequestListener;
import com.maihehd.sdk.vast.util.SaxHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

/**
 * Created by roger on 6/29/15.
 */
public class VASTParser implements AsyncRequestListener {

    private final static String TAG = "VASTParser";

    private VASTParserListener listener;

    public VASTParser(VASTParserListener listener){
        this.listener = listener;
    }

    public void parse(String url, Context context){
        AsyncRequest request = new AsyncRequest(this, AsyncRequest.INPUT_STREAM);
        Object[] params = {url, context};
        request.execute(params);
    }

    @Override
    public void onError(){
        listener.onError();
    }

    @Override
    public void onCancelled() {
        listener.onCancelled();
    }

    @Override
    public void onPostExecute(Object data) {
        try {
            InputStream inputStream = (InputStream) data;

            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }

            //实例化一个SAXParserFactory对象
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser;
            //实例化SAXParser对象，创建XMLReader对象，解析器
            parser = factory.newSAXParser();
            XMLReader xmlReader = parser.getXMLReader();
            //实例化handler，事件处理器
            SaxHandler saxHandler = new SaxHandler();
            //解析器注册事件
            xmlReader.setContentHandler(saxHandler);
            //读取文件流
            InputSource is = new InputSource(inputStream);
            //解析文件
            xmlReader.parse(is);

            listener.onComplete(saxHandler.getVASTModel());

            return;
        }
        catch (MalformedURLException e){
            Log.e(TAG, "malformed url");
        }
        catch (IOException e){
            Log.e(TAG, "IOError");
        }
        catch (SAXException e){
            Log.e(TAG, "SAX exception " + e.toString());
        }
        catch (ParserConfigurationException e){
            Log.e(TAG, "parser configuration exception " + e.toString());
        }

        listener.onError();

    }

    @Override
    public void onPreExecute() {
        //
    }

    @Override
    public void onProgressUpdate(Integer percent) {
        //
    }
}
