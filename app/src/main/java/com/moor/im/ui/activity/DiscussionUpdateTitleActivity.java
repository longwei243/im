package com.moor.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.http.HttpManager;
import com.moor.im.model.entity.Discussion;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.utils.DiscussionActivityUtil;
import com.moor.im.utils.GroupActivityUtil;

import org.apache.http.Header;

/**
 * Created by long on 2015/7/22.
 */
public class DiscussionUpdateTitleActivity extends Activity {

    String sessionId;
    Button group_update_btn_save;
    private EditText group_update_et_name;
    ProgressBar pb;

    TextView title_tv_name;

    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_group_update_title);
        sp = getSharedPreferences("SP", 4);
        sessionId = getIntent().getStringExtra("sessionId");
        if(sessionId == null) {
            sessionId = "";
        }

        title_tv_name = (TextView) findViewById(R.id.title_tv_name);
        title_tv_name.setText("修改讨论组名称");

        group_update_et_name = (EditText) findViewById(R.id.group_update_et_name);

        group_update_btn_save = (Button) findViewById(R.id.group_update_btn_save);

        pb = (ProgressBar) findViewById(R.id.group_update_progress);

        group_update_btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = group_update_et_name.getText().toString().trim();
                if(!"".equals(title)) {
                    HttpManager.updateDiscussionTitle(sp.getString("connecTionId", ""), sessionId, title, new UpdateDiscussionTitleResponseHandler());
                    group_update_btn_save.setVisibility(View.GONE);
                    pb.setVisibility(View.VISIBLE);
                }
            }
        });

        findViewById(R.id.chat_btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    class UpdateDiscussionTitleResponseHandler extends TextHttpResponseHandler {
        @Override
        public void onFailure(int statusCode, Header[] headers,
                              String responseString, Throwable throwable) {
            group_update_btn_save.setVisibility(View.VISIBLE);
            pb.setVisibility(View.GONE);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers,
                              String responseString) {
            String succeed = HttpParser.getSucceed(responseString);
            String message = HttpParser.getMessage(responseString);
            group_update_btn_save.setVisibility(View.VISIBLE);
            pb.setVisibility(View.GONE);
            System.out.println("修改群名称返回结果是:"+responseString);
            if ("true".equals(succeed)) {
                DiscussionActivityUtil.getInstance().exit();
                Intent intent = new Intent(DiscussionUpdateTitleActivity.this, DiscussionActivity.class);
                startActivity(intent);
                finish();


            } else {

            }
        }
    }

}
