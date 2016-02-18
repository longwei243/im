package com.moor.im.ui.js;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.JavascriptInterface;

/**
 * Created by longwei on 2015/12/3.
 */
public class JSObject {
    private Context context;
    private SharedPreferences sp;

    public JSObject(Context context){
        this.context = context;
        sp = context.getSharedPreferences("SP", 0);
    }

    /*
     * JS调用android的方法
     * @JavascriptInterface仍然必不可少
     *
     * */
    @JavascriptInterface
    public String  JsCallAndroid(){
        String connectionId = sp.getString("connecTionId", "");
        return connectionId;
    }
}
