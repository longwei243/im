package com.moor.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.dao.UserDao;
import com.moor.im.http.HttpManager;
import com.moor.im.model.entity.Contacts;
import com.moor.im.model.entity.User;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.ui.adapter.GVContactAdapter;
import com.moor.im.ui.view.GridViewInScrollView;
import com.moor.im.utils.LogUtil;

import org.apache.http.Header;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by long on 2015/7/22.
 */
public class AddDiscussionActivity extends Activity implements View.OnClickListener{

    private List<Contacts> membersTempList = new ArrayList<Contacts>();

    private SharedPreferences sp;
    private List<Contacts> adminContacts = new ArrayList<Contacts>();
    private List<Contacts> memberContacts = new ArrayList<Contacts>();

    private GridViewInScrollView group_add_gv_members;
    Button group_add_btn_save, group_add_btn_select_members;
    private EditText group_add_et_name;
    ProgressBar pb;
    GVContactAdapter memberAdapter;

    ImageView title_btn_back;
    TextView title_name;

    User user = UserDao.getInstance().getUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        MobileApplication.getInstance().add(this);
        setContentView(R.layout.activity_add_gourp);

        adminContacts.clear();
        memberContacts.clear();
        sp = getSharedPreferences("SP", 4);

        title_name = (TextView) findViewById(R.id.title_name);
        title_name.setText("创建讨论组");

        group_add_btn_select_members = (Button) findViewById(R.id.group_add_btn_select_members);
        group_add_btn_select_members.setOnClickListener(this);

        group_add_gv_members = (GridViewInScrollView) findViewById(R.id.group_add_gv_members);

        group_add_et_name = (EditText) findViewById(R.id.group_add_et_name);
        group_add_btn_save = (Button) findViewById(R.id.group_add_btn_save);
        group_add_btn_save.setOnClickListener(this);

        pb = (ProgressBar) findViewById(R.id.group_add_progress);

        title_btn_back = (ImageView) findViewById(R.id.title_btn_back);
        title_btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        MobileApplication.getInstance().remove(this);
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
//            case R.id.group_add_btn_select_admins:
//                //添加管理员
//                Intent adminSelectIntent = new Intent(AddGourpActivity.this, MembersSelectActivity.class);
//                adminSelectIntent.putExtra("membersIdList", (Serializable)adminContacts);
//                adminSelectIntent.putExtra("selectType","groupAdmin");
//                startActivityForResult(adminSelectIntent, 0x221);
//                break;
            case R.id.group_add_btn_select_members:
                //添加成员
                Intent memberSelectIntent = new Intent(AddDiscussionActivity.this, MembersSelectActivity.class);
                memberSelectIntent.putExtra("membersIdList", (Serializable)memberContacts);
                memberSelectIntent.putExtra("selectType", "Discussion");
                startActivityForResult(memberSelectIntent, 0x222);
                break;
            case R.id.group_add_btn_save:
                //添加部门，请求网络
                String name = group_add_et_name.getText().toString();

                ArrayList admins = new ArrayList();
                admins.add(user._id);
                ArrayList members = new ArrayList();
                for (int i = 0; i < membersTempList.size(); i++) {
                    members.add(membersTempList.get(i)._id);
                }

                members.add(admins.get(0));

                if(!"".equals(name)) {
                    HttpManager.createDiscussion(sp.getString("connecTionId", ""), members, name, new CreateDiscussionResponseHandler());
                    group_add_btn_save.setVisibility(View.GONE);
                    pb.setVisibility(View.VISIBLE);
                }else {
                    Toast.makeText(AddDiscussionActivity.this, "输入内容不能为空", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    class CreateDiscussionResponseHandler extends TextHttpResponseHandler {
        @Override
        public void onFailure(int statusCode, Header[] headers,
                              String responseString, Throwable throwable) {
            Toast.makeText(AddDiscussionActivity.this, "请检查您的网络问题！！！", Toast.LENGTH_SHORT).show();
            group_add_btn_save.setVisibility(View.VISIBLE);
            pb.setVisibility(View.GONE);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers,
                              String responseString) {
            String succeed = HttpParser.getSucceed(responseString);
            group_add_btn_save.setVisibility(View.VISIBLE);
            pb.setVisibility(View.GONE);
            if ("true".equals(succeed)) {
                //若添加讨论组成功
                Intent intent = new Intent(AddDiscussionActivity.this, DiscussionActivity.class);
                startActivity(intent);
                finish();

            } else {
//                Toast.makeText(AddGourpActivity.this, message, Toast.LENGTH_SHORT)
//                        .show();

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == 0x222 && resultCode == 0x222){
            memberContacts.clear();
            membersTempList = (List<Contacts>) data.getSerializableExtra("memberslist");
            memberContacts = membersTempList;
            memberAdapter = new GVContactAdapter(AddDiscussionActivity.this, membersTempList);
            group_add_gv_members.setAdapter(memberAdapter);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
