package com.moor.im.http;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.moor.im.app.MobileApplication;
import com.moor.im.app.RequestUrl;
import com.moor.im.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by longwei on 2016/2/17.
 */
public class MobileHttpManager {

    /**
     * 获取通话记录
     */
    public static void queryCdr(String sessionId, HashMap<String, String> datas,
                              ResponseHandlerInterface responseHandler) {

        AsyncHttpClient httpclient = MobileApplication.httpclient;
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.queryCdr");
            for(String key : datas.keySet()) {
                json.put(key, datas.get(key));
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RequestParams params = new RequestParams();
        params.add("data", json + "");
        httpclient.post(RequestUrl.baseHttpMobile, params, responseHandler);
    }

    /**
     * 获取坐席缓存
     */
    public static void getAgentCache(String sessionId,
                              ResponseHandlerInterface responseHandler) {

        AsyncHttpClient httpclient = MobileApplication.httpclient;
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "common.getDicCache");
            json.put("type", "agents");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RequestParams params = new RequestParams();
        params.add("data", json + "");
        httpclient.post(RequestUrl.baseHttpMobile, params, responseHandler);
    }
    /**
     * 获取技能组缓存
     */
    public static void getQueueCache(String sessionId,
                                     ResponseHandlerInterface responseHandler) {

        AsyncHttpClient httpclient = MobileApplication.httpclient;
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "common.getDicCache");
            json.put("type", "queues");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RequestParams params = new RequestParams();
        params.add("data", json + "");
        httpclient.post(RequestUrl.baseHttpMobile, params, responseHandler);
    }

    /**
     * 获取缓存
     */
    public static void getOptionCache(String sessionId,
                                     ResponseHandlerInterface responseHandler) {

        AsyncHttpClient httpclient = MobileApplication.httpclient;
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "common.getDicCache");
            json.put("type", "options");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RequestParams params = new RequestParams();
        params.add("data", json + "");
        httpclient.post(RequestUrl.baseHttpMobile, params, responseHandler);
    }
}
