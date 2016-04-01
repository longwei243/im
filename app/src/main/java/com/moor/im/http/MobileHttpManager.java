package com.moor.im.http;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.moor.im.app.MobileApplication;
import com.moor.im.app.RequestUrl;
import com.moor.im.ui.activity.ErpActionProcessActivity;
import com.moor.im.utils.Utils;

import org.json.JSONArray;
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
     * 获取option缓存
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

    /**
     * 获取工单流程缓存
     */
    public static void getBusinessFlow(String sessionId,
                                      ResponseHandlerInterface responseHandler) {

        AsyncHttpClient httpclient = MobileApplication.httpclient;
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "common.getDicCache");
            json.put("type", "businessFlow");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RequestParams params = new RequestParams();
        params.add("data", json + "");
        httpclient.post(RequestUrl.baseHttpMobile, params, responseHandler);
    }

    /**
     * 获取工单步骤缓存
     */
    public static void getBusinessStep(String sessionId,
                                       ResponseHandlerInterface responseHandler) {

        AsyncHttpClient httpclient = MobileApplication.httpclient;
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "common.getDicCache");
            json.put("type", "businessFlowStep");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RequestParams params = new RequestParams();
        params.add("data", json + "");
        httpclient.post(RequestUrl.baseHttpMobile, params, responseHandler);
    }

    /**
     * 获取工单字段缓存
     */
    public static void getBusinessField(String sessionId,
                                       ResponseHandlerInterface responseHandler) {

        AsyncHttpClient httpclient = MobileApplication.httpclient;
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "common.getDicCache");
            json.put("type", "businessFlowField");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RequestParams params = new RequestParams();
        params.add("data", json + "");
        httpclient.post(RequestUrl.baseHttpMobile, params, responseHandler);
    }

    /**
     * 获取待领取工单
     */
    public static void queryRoleUnDealOrder(String sessionId, HashMap<String, String> datas,
                                      ResponseHandlerInterface responseHandler) {

        AsyncHttpClient httpclient = MobileApplication.httpclient;
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "getRoleUnDealBusiness");
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
     * 获取待处理工单
     */
    public static void queryUserUnDealOrder(String sessionId, HashMap<String, String> datas,
                                            ResponseHandlerInterface responseHandler) {

        AsyncHttpClient httpclient = MobileApplication.httpclient;
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "getUnDealBusiness");
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
     * 获取参与的工单
     */
    public static void queryFollowedOrder(String sessionId, HashMap<String, String> datas,
                                            ResponseHandlerInterface responseHandler) {

        AsyncHttpClient httpclient = MobileApplication.httpclient;
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "getFollowedBusiness");
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
     * 获取创建工单
     */
    public static void queryAssignedOrder(String sessionId, HashMap<String, String> datas,
                                          ResponseHandlerInterface responseHandler) {

        AsyncHttpClient httpclient = MobileApplication.httpclient;
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "getAssignedBusiness");
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
     * 领取工单
     */
    public static void haveThisOrder(String sessionId, HashMap<String, String> datas,
                                            ResponseHandlerInterface responseHandler) {

        AsyncHttpClient httpclient = MobileApplication.httpclient;
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "setTaskToMe");
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
     * 获取工单详情
     */
    public static void getBusinessDetailById(String sessionId, String busId,
                                     ResponseHandlerInterface responseHandler) {

        AsyncHttpClient httpclient = MobileApplication.httpclient;
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "getBusinessDetailById");
            json.put("_id", busId);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RequestParams params = new RequestParams();
        params.add("data", json + "");
        httpclient.post(RequestUrl.baseHttpMobile, params, responseHandler);
    }

    /**
     * 执行动作操作
     */
    public static void excuteBusinessStepAction(String sessionId, HashMap<String, String> datas,HashMap<String, JSONArray> jadata,
                                     ResponseHandlerInterface responseHandler) {

        AsyncHttpClient httpclient = MobileApplication.httpclient;
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "excuteBusinessStepAction");
            for(String key : datas.keySet()) {
                json.put(key, datas.get(key));
            }
            for (String key : jadata.keySet()) {
                json.put(key, jadata.get(key));
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RequestParams params = new RequestParams();
        params.add("data", json + "");
        httpclient.post(RequestUrl.baseHttpMobile, params, responseHandler);
    }

    public static void getQiNiuToken(ResponseHandlerInterface responseHandler) {
        AsyncHttpClient httpclient = MobileApplication.httpclient;
        JSONObject json = new JSONObject();
        try {
            json.put("Action", "app.weixin.getUptoken");

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RequestParams params = new RequestParams();
        params.add("data", json + "");
        httpclient.post(RequestUrl.baseHttpMobileQiNiu, params, responseHandler);
    }

    /**
     * 保存备注
     */
    public static void saveBusinessBackInfo(String sessionId, String busId, String backInfo,
                                             ResponseHandlerInterface responseHandler) {

        AsyncHttpClient httpclient = MobileApplication.httpclient;
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "addBusinessBackInfo");
            json.put("_id", busId);
            json.put("backInfo", backInfo);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RequestParams params = new RequestParams();
        params.add("data", json + "");
        httpclient.post(RequestUrl.baseHttpMobile, params, responseHandler);
    }

    /**
     * 退回工单
     */
    public static void excuteBusinessBackAction(String sessionId, String busId, String backInfo,
                                            ResponseHandlerInterface responseHandler) {

        AsyncHttpClient httpclient = MobileApplication.httpclient;
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "excuteBusinessBackAction");
            json.put("_id", busId);
            json.put("backInfo", backInfo);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RequestParams params = new RequestParams();
        params.add("data", json + "");
        httpclient.post(RequestUrl.baseHttpMobile, params, responseHandler);
    }

    /**
     * 重新提交工单
     */
    public static void reSaveBusiness(String sessionId, HashMap<String, String> datas,HashMap<String, JSONArray> jadata,
                                                ResponseHandlerInterface responseHandler) {

        AsyncHttpClient httpclient = MobileApplication.httpclient;
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "addBusinessTask");
            for(String key : datas.keySet()) {
                json.put(key, datas.get(key));
            }
            for (String key : jadata.keySet()) {
                json.put(key, jadata.get(key));
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
     * 客户详情
     */
    public static void getCustomerDetails(String sessionId, String customerId,
                                                ResponseHandlerInterface responseHandler) {

        AsyncHttpClient httpclient = MobileApplication.httpclient;
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "queryCustInfo");
            json.put("_id", customerId);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RequestParams params = new RequestParams();
        params.add("data", json + "");
        httpclient.post(RequestUrl.baseHttpMobile, params, responseHandler);
    }

    /**
     * 获取客户缓存
     */
    public static void getCustCache(String sessionId,
                                      ResponseHandlerInterface responseHandler) {

        AsyncHttpClient httpclient = MobileApplication.httpclient;
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "common.getDicCache");
            json.put("type", "custTmpls");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RequestParams params = new RequestParams();
        params.add("data", json + "");
        httpclient.post(RequestUrl.baseHttpMobile, params, responseHandler);
    }
}
