package com.moor.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.db.dao.ContactsDao;
import com.moor.im.db.dao.UserDao;
import com.moor.im.http.HttpManager;
import com.moor.im.model.entity.Contacts;
import com.moor.im.model.entity.User;
import com.moor.im.model.parser.DepartmentParser;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.ui.dialog.ConfirmDialog;
import com.moor.im.ui.view.RoundImageView;
import com.moor.im.utils.GroupActivityUtil;
import com.moor.im.utils.LogUtil;
import com.moor.im.utils.NullUtil;

import org.apache.http.Header;

import java.util.ArrayList;

/**
 * Created by long on 2015/7/21.
 */
public class GroupMemberDetailActivity extends Activity{

    String _id;
    String name;
    String type;
    String sessionId;

    Button delete_admin, delete_member;
    private SharedPreferences sp;

    private TextView contact_detail_tv_name,
            contact_detail_tv_num,
            contact_detail_tv_phone,
            contact_detail_tv_email,
            contact_detail_tv_product;

    RoundImageView contact_detail_image;

    private Contacts contact;
    ImageView title_btn_back;

    ConfirmDialog dialog;

    User user = UserDao.getInstance().getUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_group_memberdetail);
        sp = getSharedPreferences("SP", 4);
        Intent intent = getIntent();
        _id = intent.getStringExtra("_id");
        name = intent.getStringExtra("name");
        type = intent.getStringExtra("type");
        sessionId = intent.getStringExtra("sessionId");

        if(_id == null) {
            _id = "";
        }
        if(name == null) {
            name = "";
        }
        if(type == null) {
            type = "";
        }
        if(sessionId == null) {
            sessionId = "";
        }

        contact = ContactsDao.getInstance().getContactById(_id);
        if(contact == null) {
            return;
        }

        contact_detail_tv_name = (TextView) findViewById(R.id.contact_detail_tv_name);
        contact_detail_tv_num = (TextView) findViewById(R.id.contact_detail_tv_num);
        contact_detail_tv_phone = (TextView) findViewById(R.id.contact_detail_tv_phone);
        contact_detail_tv_email = (TextView) findViewById(R.id.contact_detail_tv_email);
        contact_detail_tv_product = (TextView) findViewById(R.id.contact_detail_tv_product);
        if("".equals(NullUtil.checkNull(contact.mobile))) {
            contact_detail_tv_phone.setText("未绑定");
        }else{
            contact_detail_tv_phone.setText(contact.mobile);
        }
        if("".equals(NullUtil.checkNull(contact.email))) {
            contact_detail_tv_email.setText("未绑定");
        }else{
            contact_detail_tv_email.setText(contact.email);
        }
        contact_detail_tv_name.setText(NullUtil.checkNull(contact.displayName));
        contact_detail_tv_num.setText(NullUtil.checkNull(contact.exten));
        if("zj".equals(NullUtil.checkNull(contact.product))) {
            contact_detail_tv_product.setText("企业总机");
        }else if("cc".equals(NullUtil.checkNull(contact.product))){
            contact_detail_tv_product.setText("联络中心");
        }

        contact_detail_image = (RoundImageView) findViewById(R.id.contact_detail_image);
        String im_icon = contact.im_icon;
        if(im_icon != null && !"".equals(im_icon)) {
            Glide.with(this).load(im_icon+"?imageView2/0/w/100/h/100").asBitmap().placeholder(R.drawable.head_default_local).into(contact_detail_image);
        }else {
            Glide.with(this).load(R.drawable.head_default_local).asBitmap().into(contact_detail_image);
        }

        title_btn_back = (ImageView) this.findViewById(R.id.title_btn_back);
        title_btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        final ArrayList member = new ArrayList();
        member.add(_id);
        delete_admin = (Button) findViewById(R.id.delete_admin);
        delete_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmDialog dialog = new ConfirmDialog(GroupMemberDetailActivity.this, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HttpManager.deleteGroupAdmin(sp.getString("connecTionId", ""), sessionId, member, new DeleteGroupAdminResponseHandler());
                    }
                });
                dialog.show();
            }
        });


        delete_member = (Button) findViewById(R.id.delete_member);
        delete_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new ConfirmDialog(GroupMemberDetailActivity.this, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        HttpManager.deleteGroupMember(sp.getString("connecTionId", ""), sessionId, member, new DeleteGroupAdminResponseHandler());
                    }
                });
                dialog.show();
            }
        });


        if(_id.equals(user._id)) {
            delete_admin.setVisibility(View.GONE);
            delete_member.setText("退出该群");
            delete_member.setVisibility(View.VISIBLE);
        }

    }

    class DeleteGroupAdminResponseHandler extends TextHttpResponseHandler {
        @Override
        public void onFailure(int statusCode, Header[] headers,
                              String responseString, Throwable throwable) {

        }

        @Override
        public void onSuccess(int statusCode, Header[] headers,
                              String responseString) {
            String succeed = HttpParser.getSucceed(responseString);
            String message = HttpParser.getMessage(responseString);
            LogUtil.d("GroupMemberDetailActivity", "删除返回数据:"+responseString);
            if ("true".equals(succeed)) {
                Toast.makeText(GroupMemberDetailActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                GroupActivityUtil.getInstance().exit();
                Intent intent = new Intent(GroupMemberDetailActivity.this, GroupActivity.class);
                startActivity(intent);
                finish();

            } else {

            }
        }
    }

}
