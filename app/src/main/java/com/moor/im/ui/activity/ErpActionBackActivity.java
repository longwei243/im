package com.moor.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.db.dao.UserDao;
import com.moor.im.event.ErpExcuteSuccess;
import com.moor.im.http.MobileHttpManager;
import com.moor.im.model.entity.MAErpDetail;
import com.moor.im.model.entity.User;
import com.moor.im.model.parser.HttpParser;

import org.apache.http.Header;

import de.greenrobot.event.EventBus;

/**
 * Created by longwei on 2016/3/29.
 */
public class ErpActionBackActivity extends Activity{

    private EditText erp_action_back_et;
    private Button erp_action_back_btn;

    User user = UserDao.getInstance().getUser();
    private MAErpDetail business;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_erp_action_back);

        Intent intent = getIntent();
        business = (MAErpDetail) intent.getSerializableExtra("business");

        erp_action_back_et = (EditText) findViewById(R.id.erp_action_back_et);
        erp_action_back_btn = (Button) findViewById(R.id.erp_action_back_btn);
        erp_action_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = erp_action_back_et.getText().toString().trim();
                if(!"".equals(content)) {
                    MobileHttpManager.excuteBusinessBackAction(user._id, business._id, content, new ExcuteBusBackHandler());
                }else {
                    Toast.makeText(ErpActionBackActivity.this, "请填写原因", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    class ExcuteBusBackHandler extends TextHttpResponseHandler{

        @Override
        public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
            Toast.makeText(ErpActionBackActivity.this, "退回失败，请稍后重试", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess(int i, Header[] headers, String s) {
            String succeed = HttpParser.getSucceed(s);

            if ("true".equals(succeed)) {
                Toast.makeText(ErpActionBackActivity.this, "退回成功", Toast.LENGTH_SHORT).show();
                EventBus.getDefault().post(new ErpExcuteSuccess());
                finish();
            }else if("403".equals(HttpParser.getErrorCode(s))) {
                Toast.makeText(ErpActionBackActivity.this, "该工单步骤已被执行或您已无权限执行", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(ErpActionBackActivity.this, "退回失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
