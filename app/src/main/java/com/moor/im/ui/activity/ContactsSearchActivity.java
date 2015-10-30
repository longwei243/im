package com.moor.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.moor.im.R;
import com.moor.im.db.dao.ContactsDao;
import com.moor.im.db.dao.UserDao;
import com.moor.im.model.entity.Contacts;
import com.moor.im.model.entity.User;
import com.moor.im.ui.adapter.ContactListViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 联系人搜索界面
 * Created by longwei on 2015/10/30.
 */
public class ContactsSearchActivity extends Activity{

    private ListView listView;
    private ContactListViewAdapter adapter;
    private EditText contact_et_search;

    private ImageView title_btn_back;

    private User user = UserDao.getInstance().getUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_contact_serach);

        title_btn_back = (ImageView) findViewById(R.id.title_btn_back);
        title_btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        listView = (ListView) findViewById(R.id.contact_search_listview);

        contact_et_search = (EditText) findViewById(R.id.contact_et_search);

        contact_et_search.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
                String key = contact_et_search.getText().toString().trim();
                if (user != null && user.exten.equals(key)) {
                    return;
                } else if (!"".equals(key)) {
                    SearchContactsTask searchContactsTask = new SearchContactsTask();
                    searchContactsTask.execute(key);
                } else {
                    List<Contacts> list = getContactsFromDB();
                    if (list != null && list.size() != 0) {
                        initDatas(list);
                    }
                }

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {

            }
        });
    }

    private List<Contacts> getContactsFromDB() {
        return ContactsDao.getInstance().getContacts();
    }

    private void initDatas(final List<Contacts> contacts) {

        if(contacts != null && contacts.size() > 0 && user != null) {
            // 将自己在联系人中移除
            for (int i = 0; i < contacts.size(); i++) {
                if (contacts.get(i)._id.equals(user._id)) {
                    contacts.remove(i);
                }
            }

            adapter = new ContactListViewAdapter(ContactsSearchActivity.this, contacts);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Contacts messageWall = (Contacts) parent.getAdapter().getItem(position);
                    Intent intent = new Intent(ContactsSearchActivity.this,
                            ContactDetailActivity.class);
                    intent.putExtra("_id", messageWall._id + "");
                    intent.putExtra("otherName", messageWall.displayName + "");
                    intent.putExtra("contact", messageWall);
                    startActivity(intent);
                }
            });
        }

    }



    class SearchContactsTask extends AsyncTask<String, Void, List<Contacts>> {

        @Override
        protected List<Contacts> doInBackground(String[] params) {
            String key = params[0];
            List<Contacts> contactsList = ContactsDao.getInstance().getContactsByMoHu(key);

            List<Contacts> searchContacts = new ArrayList<Contacts>();

            if(contactsList != null && contactsList.size() != 0) {
                for (int i=0; i<contactsList.size(); i++) {
                    Contacts contacts = contactsList.get(i);
                    searchContacts.add(contacts);

                }
            }
            return searchContacts;
        }

        @Override
        protected void onPostExecute(List<Contacts> contactses) {
            super.onPostExecute(contactses);

            initDatas(contactses);
        }
    }
}
