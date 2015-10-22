package com.moor.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.moor.im.R;
import com.moor.im.db.dao.ContactsDao;
import com.moor.im.model.entity.Contacts;
import com.moor.im.model.entity.Group;
import com.moor.im.model.parser.GroupParser;
import com.moor.im.ui.adapter.MembersSelectAdapter;
import com.moor.im.utils.LogUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by long on 2015/7/21.
 */
public class GroupAdminSelectActivity extends Activity{

    private ListView mListView;

    private ImageView title_btn_back, title_btn_ok;

    private MembersSelectAdapter adapter;

    private List<Contacts> members = new ArrayList<Contacts>();

    private List<Contacts> tempList = new ArrayList<Contacts>();
    private List<Contacts> intentContacts = new ArrayList<Contacts>();


    private String sessionId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_subdepartment_select);
        mListView = (ListView) findViewById(R.id.department_select_list);
        tempList.clear();
        members.clear();

        Intent intent = getIntent();
        if(intent.getSerializableExtra("membersIdList") != null) {
            intentContacts = (List<Contacts>) intent.getSerializableExtra("membersIdList");
        }

        if(intent.getStringExtra("sessionId") != null) {
            sessionId = intent.getStringExtra("sessionId");
            LogUtil.d("GroupAdminSelectActivity", "传过来的sessionId是:"+sessionId);
        }

        Group group = GroupParser.getInstance().getGroupById(sessionId);
        List<String> memberIdList = group.member;

        for (int i=0; i<memberIdList.size(); i++) {
            Contacts contacts = ContactsDao.getInstance().getContactById(memberIdList.get(i));
            members.add(contacts);
        }

        adapter = new MembersSelectAdapter(GroupAdminSelectActivity.this, members);

        for (int i = 0; i < intentContacts.size(); i++) {
            for (int j = 0; j < members.size(); j++) {
                if(intentContacts.get(i)._id.equals(members.get(j)._id)){
                    MembersSelectAdapter.getIsSelected().put(j, true);
                }
            }
        }


        mListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                MembersSelectAdapter.ViewHolder holder = (MembersSelectAdapter.ViewHolder) view.getTag();
                holder.select.toggle();
                if (holder.select.isChecked()) {
                    MembersSelectAdapter.getIsSelected().put(position, true);
                } else {
                    MembersSelectAdapter.getIsSelected().put(position, false);
                }
            }
        });

        title_btn_back = (ImageView) findViewById(R.id.title_btn_back);
        title_btn_ok = (ImageView) findViewById(R.id.title_btn_ok);

        title_btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        title_btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                HashMap<Integer,Boolean> isSelected = MembersSelectAdapter.getIsSelected();
                LogUtil.d("GroupAdminSelectActivity", "选择的数量为:"+isSelected.size());
                tempList.clear();
                for (int i = 0; i < isSelected.size(); i++) {
                    if(isSelected.get(i)) {
                        Contacts cb = members.get(i);
                        tempList.add(cb);
                    }
                }
                data.putExtra("memberslist", (Serializable) tempList);

                setResult(0x222, data);


                finish();
            }
        });

    }
}
