package com.moor.im.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.dao.ContactsDao;
import com.moor.im.db.dao.NewMessageDao;
import com.moor.im.http.HttpManager;
import com.moor.im.model.entity.Contacts;
import com.moor.im.model.entity.Discussion;
import com.moor.im.model.entity.Group;
import com.moor.im.model.entity.GroupAdminAndMembers;
import com.moor.im.model.parser.DiscussionParser;
import com.moor.im.model.parser.GroupParser;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.ui.adapter.ContactListViewAdapter;
import com.moor.im.ui.adapter.GroupAdminAndMemberAdapter;
import com.moor.im.ui.dialog.ConfirmDialog;
import com.moor.im.utils.DiscussionActivityUtil;
import com.moor.im.utils.GroupActivityUtil;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by long on 2015/7/22.
 */
public class DiscussionSettingActivity extends Activity implements View.OnClickListener{

    ImageView chat_btn_back, chat_btn_setting;

    String sessionId;
    private SharedPreferences sp;

    private ListView group_setting_list;

    TextView title_tv;

    ConfirmDialog confirmDialog;

    List<Contacts> members = new ArrayList<Contacts>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        DiscussionActivityUtil.getInstance().add(this);
        setContentView(R.layout.activity_group_setting);
        sp = getSharedPreferences("SP", 4);
        sessionId = getIntent().getStringExtra("sessionId");
        members.clear();

        title_tv = (TextView) findViewById(R.id.title_tv);
        title_tv.setText("讨论组信息");

        chat_btn_back = (ImageView) findViewById(R.id.chat_btn_back);
        chat_btn_setting = (ImageView) findViewById(R.id.chat_btn_setting);
        chat_btn_setting.setVisibility(View.VISIBLE);

        chat_btn_back.setOnClickListener(this);
        chat_btn_setting.setOnClickListener(this);

        group_setting_list = (ListView) findViewById(R.id.group_setting_list);

        Discussion discussion = DiscussionParser.getInstance().getDiscussionById(sessionId);




        List<String> memberIdList = discussion.member;

        for (int i=0; i<memberIdList.size(); i++) {
            Contacts contact = ContactsDao.getInstance().getContactById(memberIdList.get(i));
            members.add(contact);
        }

        ContactListViewAdapter adapter = new ContactListViewAdapter(DiscussionSettingActivity.this, members);
        group_setting_list.setAdapter(adapter);


        group_setting_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(DiscussionSettingActivity.this, DiscussionMemberDetailActivity.class);
                String _id = members.get(position)._id;
                String name = members.get(position).displayName;
                intent.putExtra("_id", _id);
                intent.putExtra("name", name);
                intent.putExtra("sessionId", sessionId);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chat_btn_back:
                finish();
                break;
            case R.id.chat_btn_setting:
                //弹出对话框
                settingDialog();
                break;
        }
    }

    public void settingDialog() {
        LayoutInflater myInflater = LayoutInflater.from(DiscussionSettingActivity.this);
        final View myDialogView = myInflater.inflate(R.layout.discussionsettingdialog,
                null);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(DiscussionSettingActivity.this)
                .setView(myDialogView);
        final AlertDialog alert = dialog.show();
        alert.setCanceledOnTouchOutside(true);// 设置点击Dialog外部任意区域关闭Dialog
        alert.getWindow().setGravity(Gravity.BOTTOM);

        // 修改名称
        LinearLayout mDirectSeeding = (LinearLayout) myDialogView
                .findViewById(R.id.direct_seeding_linear);
        mDirectSeeding.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DiscussionSettingActivity.this, DiscussionUpdateTitleActivity.class);
                intent.putExtra("sessionId", sessionId);
                startActivity(intent);
                alert.dismiss();


            }
        });

        // 添加成员
        LinearLayout mOrdinaryCall = (LinearLayout) myDialogView
                .findViewById(R.id.ordinary_call_linear);
        mOrdinaryCall.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                alert.dismiss();
                Intent intent = new Intent(DiscussionSettingActivity.this, DiscussionAddMemberActivity.class);
                intent.putExtra("sessionId", sessionId);
                startActivity(intent);
            }
        });
        // 删除该组
        LinearLayout mCancelLinear = (LinearLayout) myDialogView
                .findViewById(R.id.cancel_linear);
        mCancelLinear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                confirmDialog = new ConfirmDialog(DiscussionSettingActivity.this, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmDialog.dismiss();
                        HttpManager.deleteDiscussion(sp.getString("connecTionId", ""), sessionId, new DeleteDiscussionResponseHandler());
                    }
                });
                confirmDialog.show();
                alert.dismiss();

            }
        });
    }

    class DeleteDiscussionResponseHandler extends TextHttpResponseHandler {
        @Override
        public void onFailure(int statusCode, Header[] headers,
                              String responseString, Throwable throwable) {

        }

        @Override
        public void onSuccess(int statusCode, Header[] headers,
                              String responseString) {
            String succeed = HttpParser.getSucceed(responseString);
            String message = HttpParser.getMessage(responseString);

            if ("true".equals(succeed)) {

                NewMessageDao.getInstance().deleteMsgById(sessionId);
                Message msg = new Message();
                msg.obj = "msg";
                MobileApplication.getHandler().sendMessage(msg);

                Toast.makeText(DiscussionSettingActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                DiscussionActivityUtil.getInstance().exit();
                Intent intent = new Intent(DiscussionSettingActivity.this, DiscussionActivity.class);
                startActivity(intent);
                finish();


            } else {

            }
        }
    }

}
