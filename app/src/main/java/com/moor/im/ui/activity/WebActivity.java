package com.moor.im.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.app.RequestUrl;
import com.moor.im.ui.js.JSObject;

/**
 * Created by longwei on 2015/11/9.
 */
@SuppressLint("SetJavaScriptEnabled")
public class WebActivity extends Activity{

    private WebView mWebView;
    static ProgressDialog m_Dialog;

    private SharedPreferences sp;

    String action = "webGetCalllog";
    String connectionId = "";

    String Now_Url = RequestUrl.baseHttp1+"/?Action="+action+"&ConnectionId="+connectionId;
    String Now_Url_def;

    JSObject jsobject;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_web);
        sp = this.getSharedPreferences("SP", 0);
//        connectionId = sp.getString("connecTionId", "");
        mWebView = (WebView) findViewById(R.id.webView1);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        jsobject = new JSObject(WebActivity.this);
        mWebView.addJavascriptInterface(jsobject, "JsGetConnectionId");

//        Intent intent = getIntent();
//        Now_Url = intent.getStringExtra("OpenUrl");


        Now_Url = RequestUrl.baseHttp1+"/?Action="+action+"&ConnectionId="+sp.getString("connecTionId", "");
        System.out.println("网页请求的地址是："+Now_Url);
        Now_Url_def = Now_Url;

        TextView topbar_title =(TextView) findViewById(R.id.topbar_title);
        topbar_title.setText("");

        mWebView.setWebChromeClient(new chromeClient() );
        mWebView.setWebViewClient(new WebViewClient() {
            Dialog progressDialog = ProgressDialog.show(WebActivity.this, null, "正在加载，请稍后...");

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Now_Url = url;
                view.loadUrl(url);

                return true;
            }

            public void onPageStarted(WebView view, String url,
                                      Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                progressDialog.show();
                progressDialog.setCancelable(true);

            }

            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressDialog.cancel();

            }
        });
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.loadUrl(Now_Url);

    }


    public void exit_zhuxiao(View v) {
        finish();
    }

    class chromeClient extends WebChromeClient {

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Toast.makeText(WebActivity.this, message, Toast.LENGTH_SHORT).show();
            return true;
        }
        @Override

        public void onProgressChanged(WebView view, int newProgress) {

            super.onProgressChanged(view, newProgress);

        }


        public void onReceivedTitle(WebView view, String title) {


            TextView topbar_title =(TextView) findViewById(R.id.topbar_title);
            topbar_title.setText(title);
            super.onReceivedTitle(view, title);

        }

    }

    public void Show_Toast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
    public void Show_Dialog(String tit, String txt) {Dialog dialog = new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher).setTitle(tit).setMessage(txt).setPositiveButton("确定", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int which) {}}).create();dialog.show();}
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }
}
