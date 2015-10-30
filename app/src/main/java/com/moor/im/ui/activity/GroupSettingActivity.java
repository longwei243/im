package com.moor.im.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.dao.ContactsDao;
import com.moor.im.db.dao.NewMessageDao;
import com.moor.im.db.dao.UserDao;
import com.moor.im.http.HttpManager;
import com.moor.im.model.entity.Group;
import com.moor.im.model.entity.GroupAdminAndMembers;
import com.moor.im.model.entity.User;
import com.moor.im.model.parser.GroupParser;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.ui.adapter.GroupAdminAndMemberAdapter;
import com.moor.im.ui.dialog.ConfirmDialog;
import com.moor.im.utils.GroupActivityUtil;
import com.moor.im.utils.Utils;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by long on 2015/7/20.
 */
public class GroupSettingActivity extends Activity implements View.OnClickListener {

    ImageView chat_btn_back, chat_btn_setting;

    String sessionId;
    private SharedPreferences sp;
    ConfirmDialog confirmDialog;

    private ListView group_setting_list;

    User user = UserDao.getInstance().getUser();

    List<GroupAdminAndMembers> adminAndMemberses = new ArrayList<GroupAdminAndMembers>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        GroupActivityUtil.getInstance().add(this);
        setContentView(R.layout.activity_group_setting);
        sp = getSharedPreferences("SP", 4);
        sessionId = getIntent().getStringExtra("sessionId");
        adminAndMemberses.clear();

        if(sessionId == null) {
            sessionId = "";
        }
        chat_btn_back = (ImageView) findViewById(R.id.chat_btn_back);
        chat_btn_setting = (ImageView) findViewById(R.id.chat_btn_setting);

        chat_btn_back.setOnClickListener(this);
        chat_btn_setting.setOnClickListener(this);

        group_setting_list = (ListView) findViewById(R.id.group_setting_list);

        Group group = GroupParser.getInstance().getGroupById(sessionId);
        if(group == null) {
            return;
        }

        List<String> admins = group.admin;

        for (int i=0; i<admins.size(); i++) {
            if(user._id.equals(admins.get(i))) {
                chat_btn_setting.setVisibility(View.VISIBLE);
            }
        }

        for (int i=0; i<admins.size(); i++) {
            GroupAdminAndMembers gaam = new GroupAdminAndMembers();
            gaam.set_id(admins.get(i));
            String name = ContactsDao.getInstance().getContactsName(admins.get(i));
            String icicon = ContactsDao.getInstance().getContactsIcon(admins.get(i));
            gaam.setName(name);
            gaam.setType("Admin");
            gaam.setImicon(icicon);
            adminAndMemberses.add(gaam);
        }


        List<String> members = group.member;
        List<String> tempMembers = members;
        for (int i=0; i<admins.size(); i++) {
            for (int j=tempMembers.size()-1; j>=0; j--) {
                if(admins.get(i).equals(tempMembers.get(j))) {
                    tempMembers.remove(j);
                }
            }
        }
        for (int i=0; i<tempMembers.size(); i++) {
            GroupAdminAndMembers gaam = new GroupAdminAndMembers();
            gaam.set_id(tempMembers.get(i));
            String name = ContactsDao.getInstance().getContactsName(tempMembers.get(i));
            String icicon = ContactsDao.getInstance().getContactsIcon(tempMembers.get(i));
            gaam.setName(name);
            gaam.setType("Member");
            gaam.setImicon(icicon);
            adminAndMemberses.add(gaam);
        }

        GroupAdminAndMemberAdapter adapter = new GroupAdminAndMemberAdapter(GroupSettingActivity.this, adminAndMemberses);
        group_setting_list.setAdapter(adapter);


//        group_setting_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(GroupSettingActivity.this, GroupMemberDetailActivity.class);
//                String _id = adminAndMemberses.get(position).get_id();
//                String name = adminAndMemberses.get(position).getName();
//                String type = adminAndMemberses.get(position).getType();
//                intent.putExtra("_id", _id);
//                intent.putExtra("name", name);
//                intent.putExtra("type", type);
//                intent.putExtra("sessionId", sessionId);
//                System.out.println("type是："+type);
//                startActivity(intent);
//            }
//        });
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
        LayoutInflater myInflater = LayoutInflater.from(GroupSettingActivity.this);
        final View myDialogView = myInflater.inflate(R.layout.groupsettingdialog,
                null);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(GroupSettingActivity.this)
                .setView(myDialogView);
        final AlertDialog alert = dialog.show();
        alert.setCanceledOnTouchOutside(true);// 设置点击Dialog外部任意区域关闭Dialog
        alert.getWindow().setGravity(Gravity.BOTTOM);

        // 修改群名称
        LinearLayout mDirectSeeding = (LinearLayout) myDialogView
                .findViewById(R.id.direct_seeding_linear);
        mDirectSeeding.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupSettingActivity.this, GroupUpdateTitleActivity.class);
                intent.putExtra("sessionId", sessionId);
                startActivity(intent);
                alert.dismiss();


            }
        });
        // 添加管理员
        LinearLayout mCallReturn = (LinearLayout) myDialogView
                .findViewById(R.id.call_return_linear);

        mCallReturn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                alert.dismiss();
                Intent intent = new Intent(GroupSettingActivity.this, GroupAddAdminActivity.class);
                intent.putExtra("sessionId", sessionId);
                startActivity(intent);

            }
        });

        // 添加群成员
        LinearLayout mOrdinaryCall = (LinearLayout) myDialogView
                .findViewById(R.id.ordinary_call_linear);
        mOrdinaryCall.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                alert.dismiss();
                Intent intent = new Intent(GroupSettingActivity.this, GroupAddMemberActivity.class);
                intent.putExtra("sessionId", sessionId);
                startActivity(intent);
            }
        });
        // 删除该群组
        LinearLayout mCancelLinear = (LinearLayout) myDialogView
                .findViewById(R.id.cancel_linear);
        mCancelLinear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                confirmDialog = new ConfirmDialog(GroupSettingActivity.this, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmDialog.dismiss();
                        HttpManager.deleteGroup(sp.getString("connecTionId", ""), sessionId, new DeleteGroupResponseHandler());

                    }
                });
                confirmDialog.show();
                alert.dismiss();

            }
        });
    }

    class DeleteGroupResponseHandler extends TextHttpResponseHandler {
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

                Toast.makeText(GroupSettingActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                GroupActivityUtil.getInstance().exit();
                Intent intent = new Intent(GroupSettingActivity.this, GroupActivity.class);
                startActivity(intent);
                finish();


            } else {

            }
        }
    }

}
