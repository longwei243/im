package com.moor.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.dao.UserDao;
import com.moor.im.event.UserIconUpdate;
import com.moor.im.http.HttpManager;
import com.moor.im.model.entity.User;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.ui.dialog.LoadingFragmentDialog;
import com.moor.im.utils.LogUtil;
import com.moor.im.utils.NullUtil;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.greenrobot.event.EventBus;

/**
 * Created by longwei on 2015/8/27.
 */
public class EditActivity extends Activity{

    ImageView title_btn_back, title_btn_ok;
    EditText edit_userinfo;

    TextView title_name;

    String type = "";
    User user = UserDao.getInstance().getUser();

    LoadingFragmentDialog loadingFragmentDialog;
    private SharedPreferences sp;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        MobileApplication.getInstance().add(this);
        setContentView(R.layout.activity_edituserinfo);
        sp = getSharedPreferences("SP", 4);
        Intent intent = getIntent();
        type = intent.getStringExtra("edittype");
        if(type == null) {
            type = "";
        }
        loadingFragmentDialog = new LoadingFragmentDialog();
        title_btn_back = (ImageView) findViewById(R.id.title_btn_back);
        title_btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        title_name = (TextView) findViewById(R.id.title_name);

        edit_userinfo = (EditText) findViewById(R.id.edit_userinfo);

        title_btn_ok = (ImageView) findViewById(R.id.title_btn_ok);

        if(type.equals("name")) {
            edit_userinfo.setText(NullUtil.checkNull(user.displayName));
            title_name.setText("修改名字");
            title_btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String name = edit_userinfo.getText().toString().trim();
                    LogUtil.d("Editacitivity", "name是:"+name);
                    if(!"".equals(name)) {
                        //修改姓名
                        loadingFragmentDialog.show(getFragmentManager(), "");
                        HttpManager.editUserInfo(sp.getString("connecTionId", ""),
                                user._id, name, user.email, user.mobile,
                                user.product, new EditUserResponseHandler());
                    }else {
                        Toast.makeText(EditActivity.this, "请输入内容", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else if(type.equals("phone")) {
            title_name.setText("修改手机");
            edit_userinfo.setText(NullUtil.checkNull(user.mobile));
            edit_userinfo.setInputType(EditorInfo.TYPE_CLASS_PHONE);
            title_btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String phone = edit_userinfo.getText().toString().trim();
                    LogUtil.d("Editacitivity", "phone是:"+phone);
                    if(!"".equals(phone)) {
                        //修改电话
                        loadingFragmentDialog.show(getFragmentManager(), "");
                        HttpManager.editUserInfo(sp.getString("connecTionId", ""),
                                user._id, user.displayName, user.email, phone,
                                user.product, new EditUserResponseHandler());
                    }else {
                        Toast.makeText(EditActivity.this, "请输入内容", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else if(type.equals("email")) {
            title_name.setText("修改邮箱");
            edit_userinfo.setText(NullUtil.checkNull(user.email));
            edit_userinfo.setInputType(EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            title_btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String email = edit_userinfo.getText().toString().trim();
                    LogUtil.d("Editacitivity", "email是:"+email);
                    if(!"".equals(email)) {
                        //修改邮箱
                        loadingFragmentDialog.show(getFragmentManager(), "");
                        HttpManager.editUserInfo(sp.getString("connecTionId", ""),
                                user._id, user.displayName, email, user.mobile,
                                user.product, new EditUserResponseHandler());
                    }else {
                        Toast.makeText(EditActivity.this, "请输入内容", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    class EditUserResponseHandler extends TextHttpResponseHandler {
        @Override
        public void onFailure(int statusCode, Header[] headers,
                              String responseString, Throwable throwable) {
            Toast.makeText(EditActivity.this, "请检查您的网络问题！！！", Toast.LENGTH_SHORT).show();
            loadingFragmentDialog.dismiss();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers,
                              String responseString) {
            String succeed = HttpParser.getSucceed(responseString);
            String message = HttpParser.getMessage(responseString);
            if ("true".equals(succeed)) {
                EventBus.getDefault().post(new UserIconUpdate());
                loadingFragmentDialog.dismiss();
                Toast.makeText(EditActivity.this, "信息修改成功", Toast.LENGTH_SHORT)
                        .show();
                finish();
            } else {
                if ("408".equals(message)) {
                    JSONObject o;
                    try {
                        o = new JSONObject(responseString);
                        JSONArray ja = o.getJSONArray("RepeatList");
                        StringBuffer sb = new StringBuffer();
                        for (int i = 0; i < ja.length(); i++) {
                            if("email".equals(ja.get(i))) {
                                sb.append("您的邮箱与别人重复");
                            }
                            if("mobile".equals(ja.get(i))) {
                                sb.append("您的电话与别人重复");
                            }
                            if("name".equals(ja.get(i))) {
                                sb.append("您的姓名与别人重复");
                            }
                        }
                        loadingFragmentDialog.dismiss();
                        Toast.makeText(EditActivity.this, sb.toString(), Toast.LENGTH_SHORT)
                                .show();
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }else {
                    loadingFragmentDialog.dismiss();
                    Toast.makeText(EditActivity.this, "网络不稳定，请稍后重试", Toast.LENGTH_SHORT)
                            .show();
                }

            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        MobileApplication.getInstance().remove(this);
    }
}
