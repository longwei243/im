package com.moor.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.db.dao.ContactsDao;
import com.moor.im.http.HttpManager;
import com.moor.im.model.entity.Contacts;
import com.moor.im.model.entity.Group;
import com.moor.im.model.parser.GroupParser;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.ui.adapter.GVContactAdapter;
import com.moor.im.ui.view.GridViewInScrollView;
import com.moor.im.utils.GroupActivityUtil;
import com.moor.im.utils.LogUtil;

import org.apache.http.Header;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by long on 2015/7/20.
 */
public class GroupAddMemberActivity extends Activity implements View.OnClickListener {
    Button group_add_member_btn_select_members, group_add_member_btn_save;
    ProgressBar pb;
    private List<Contacts> membersTempList = new ArrayList<Contacts>();

    private GridViewInScrollView group_add_member_gv;

    private SharedPreferences sp;

    private String sessionId;

    private ImageView title_btn_back;
    private GVContactAdapter adapter;
    private List<Contacts> contacts = new ArrayList<Contacts>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_group_add_member);
        sp = getSharedPreferences("SP", 4);
        sessionId = getIntent().getStringExtra("sessionId");

        group_add_member_btn_select_members = (Button) findViewById(R.id.group_add_member_btn_select_members);
        group_add_member_btn_select_members.setOnClickListener(this);

        group_add_member_btn_save = (Button) findViewById(R.id.group_add_member_btn_save);
        group_add_member_btn_save.setOnClickListener(this);

        group_add_member_gv = (GridViewInScrollView) findViewById(R.id.group_add_member_gv);
        pb = (ProgressBar) findViewById(R.id.group_add_member_progress);
        //先把该群组已有的管理员显示出来
        Group group  = GroupParser.getInstance().getGroupById(sessionId);
        List<String> adminId = group.member;
        if(adminId.size() != 0) {
            for (int i=0; i<adminId.size(); i++) {
                Contacts contact = ContactsDao.getInstance().getContactById(adminId.get(i));
                contacts.add(contact);
            }
        }

        adapter = new GVContactAdapter(GroupAddMemberActivity.this, contacts);
        group_add_member_gv.setAdapter(adapter);


        title_btn_back = (ImageView) findViewById(R.id.title_btn_back);
        title_btn_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.group_add_member_btn_select_members:
                //选择成员
                Intent memberSelectIntent = new Intent(GroupAddMemberActivity.this, MembersSelectActivity.class);
                memberSelectIntent.putExtra("membersIdList", (Serializable)contacts);
                memberSelectIntent.putExtra("selectType", "Discussion");
                startActivityForResult(memberSelectIntent, 0x222);
                break;
            case R.id.group_add_member_btn_save:
                //保存
                ArrayList members = new ArrayList();
                if(membersTempList.size() != 0) {
                    for (int i = 0; i < membersTempList.size(); i++) {
                        members.add(membersTempList.get(i)._id);
                    }
                }else {
                    for (int i = 0; i < contacts.size(); i++) {
                        members.add(contacts.get(i)._id);
                    }
                }

                HttpManager.addGroupMember(sp.getString("connecTionId", ""), sessionId, members, new AddGroupMemberResponseHandler());
                group_add_member_btn_save.setVisibility(View.GONE);
                pb.setVisibility(View.VISIBLE);
                break;
            case R.id.title_btn_back:
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == 0x222 && resultCode == 0x222) {
            contacts.clear();
//			System.out.println("选择成员返回结果");
            membersTempList = (List<Contacts>) data.getSerializableExtra("memberslist");
            contacts = membersTempList;
            System.out.println("size:"+membersTempList.size());
            adapter = new GVContactAdapter(GroupAddMemberActivity.this, membersTempList);
            group_add_member_gv.setAdapter(adapter);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    class AddGroupMemberResponseHandler extends TextHttpResponseHandler {
        @Override
        public void onFailure(int statusCode, Header[] headers,
                              String responseString, Throwable throwable) {
            Toast.makeText(GroupAddMemberActivity.this, "请检查您的网络问题！！！", Toast.LENGTH_SHORT).show();
            group_add_member_btn_save.setVisibility(View.VISIBLE);
            pb.setVisibility(View.GONE);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers,
                              String responseString) {
            String succeed = HttpParser.getSucceed(responseString);
            String message = HttpParser.getMessage(responseString);
            group_add_member_btn_save.setVisibility(View.VISIBLE);
            pb.setVisibility(View.GONE);
            LogUtil.d("GroupAddAdminActivity", "添加成员返回数据:"+responseString);
            if ("true".equals(succeed)) {

                //添加管理员成功了

                GroupActivityUtil.getInstance().exit();
                Intent intent = new Intent(GroupAddMemberActivity.this, GroupActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}
