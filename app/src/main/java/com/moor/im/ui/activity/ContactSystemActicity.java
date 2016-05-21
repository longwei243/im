package com.moor.im.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.csipsimple.api.ISipService;
import com.csipsimple.api.SipProfile;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.dao.UserDao;
import com.moor.im.model.entity.ContactBean;
import com.moor.im.model.entity.Contacts;
import com.moor.im.model.entity.User;
import com.moor.im.ui.adapter.SystemContactAdapter;
import com.moor.im.ui.dialog.LoadingFragmentDialog;
import com.moor.im.ui.view.SideBar;
import com.moor.im.utils.PingYinUtil;
import com.moor.im.utils.SCPinyinComparator;
import com.moor.im.utils.Utils;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by long on 2015/8/4.
 */
public class ContactSystemActicity extends Activity{

    private ListView systemcontact_listview;
    private SideBar systemcontact_sidebar;
    private TextView dialog;
    private ImageView title_btn_back;

    private Map<Integer, ContactBean> contactIdMap = null;
    private List<ContactBean> list;

    private String contentContact = "";
    private AsyncQueryHandler asyncQueryHandler;

    private SCPinyinComparator pinyinComparator = new SCPinyinComparator();
    private SystemContactAdapter adapter;
    private int firstItem = 0;

    private LoadingFragmentDialog loadingFragmentDialog;

    User user = UserDao.getInstance().getUser();
    private EditText editText;
    private SharedPreferences sp;
    private ISipService service;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = ISipService.Stub.asInterface(arg1);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            service = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        MobileApplication.getInstance().add(this);
        setContentView(R.layout.activity_systemcontact);
        sp = getSharedPreferences("SP", 4);
        bindService(new Intent().setComponent(new ComponentName("com.moor.im", "com.csipsimple.service.SipService"))
                , connection,
                Context.BIND_AUTO_CREATE);
        loadingFragmentDialog = new LoadingFragmentDialog();
        loadingFragmentDialog.show(getFragmentManager(), "");

        systemcontact_listview = (ListView) findViewById(R.id.systemcontact_listview);
        systemcontact_sidebar = (SideBar) findViewById(R.id.systemcontact_sidebar);
        dialog = (TextView) findViewById(R.id.systemcontact_textview_dialog);
        systemcontact_sidebar.setTextView(dialog);

        asyncQueryHandler = new MyAsyncQueryHandler(getContentResolver());

        title_btn_back = (ImageView) findViewById(R.id.title_btn_back);
        title_btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        init();
    }

    private class MyAsyncQueryHandler extends AsyncQueryHandler {

        public MyAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {

            if (cursor != null && cursor.getCount() > 0) {

                contactIdMap = new HashMap<Integer, ContactBean>();

                list = new ArrayList<ContactBean>();

                Pattern p = Pattern.compile("[a-zA-Z]");
                Matcher m = p.matcher(contentContact);
                if (m.find()) {

                    cursor.moveToFirst();
                    for (int i = 0; i < cursor.getCount(); i++) {

                        cursor.moveToPosition(i);
                        String name = cursor.getString(1);
                        String number = cursor.getString(2);
                        String sortKey = cursor.getString(3);
                        int contactId = cursor.getInt(4);
                        Long photoId = cursor.getLong(5);
                        String lookUpKey = cursor.getString(6);

                        if (PingYinUtil.getFirstSpell(name).contains(contentContact.toLowerCase())) {
                            ContactBean cb = new ContactBean();

                            cb.setDesplayName(name);
                            if (number.contains(" ")) {
                                number = number.replace(" ", "");
                            }

                            if (number.startsWith("+86")) {// 去除多余的中国地区号码标志，对这个程序没有影响。
                                cb.setPhoneNum(number.substring(3));
                            } else {
                                cb.setPhoneNum(number);
                            }
                            cb.setSortKey(sortKey);
                            cb.setContactId(contactId);
                            cb.setPhotoId(photoId);
                            cb.setLookUpKey(lookUpKey);
                            cb.setPinyin(PingYinUtil.getPingYin(name));
                            list.add(cb);

                            contactIdMap.put(contactId, cb);

                        }
                    }
                } else {
                    cursor.moveToFirst();
                    for (int i = 0; i < cursor.getCount(); i++) {

                        cursor.moveToPosition(i);
                        String name = cursor.getString(1);
                        String number = cursor.getString(2);
                        String sortKey = cursor.getString(3);
                        int contactId = cursor.getInt(4);
                        Long photoId = cursor.getLong(5);
                        String lookUpKey = cursor.getString(6);

                        ContactBean cb = new ContactBean();
                        cb.setDesplayName(name);
                        if (number.contains(" ")) {
                            number = number.replace(" ", "");
                        }
                        if (number.startsWith("+86")) {// 去除多余的中国地区号码标志，对这个程序没有影响。
                            cb.setPhoneNum(number.substring(3));
                        } else {
                            cb.setPhoneNum(number);
                        }
                        cb.setSortKey(sortKey);
                        cb.setContactId(contactId);
                        cb.setPhotoId(photoId);
                        cb.setLookUpKey(lookUpKey);
                        cb.setPinyin(PingYinUtil.getPingYin(name));
                        list.add(cb);

                        contactIdMap.put(contactId, cb);
                    }
                }
                if (list.size() > 0) {
//                    initDatas(list);

                    SetLetterTask slt = new SetLetterTask();
                    slt.execute(list);
                }



            }
            cursor.close();
        }

    }

    class SetLetterTask extends AsyncTask<List<ContactBean>, Void, List<ContactBean>> {

        @Override
        protected List<ContactBean> doInBackground(List<ContactBean>... params) {
            List<ContactBean> cbs = params[0];
            setLetter(cbs);
            Collections.sort(cbs, pinyinComparator);
            return cbs;
        }

        @Override
        protected void onPostExecute(List<ContactBean> contacts) {

            initDatas(contacts);

            super.onPostExecute(contacts);
        }
    }

    /**
     * 加载数据
     */
    private void initDatas(final List<ContactBean> contacts) {

//        setLetter(contacts);
//        Collections.sort(contacts, pinyinComparator);

        loadingFragmentDialog.dismiss();

        adapter = new SystemContactAdapter(ContactSystemActicity.this, contacts);
        systemcontact_listview.setAdapter(adapter);

        systemcontact_sidebar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {

                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    systemcontact_listview.setSelection(position);
                }

            }
        });

        systemcontact_listview.setOnScrollListener(new AbsListView.OnScrollListener() {//

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {

                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                firstItem = firstVisibleItem;
                String letter = ((ContactBean) adapter.getItem(firstItem)).getPinyin().toUpperCase()
                        .substring(0, 1);
                systemcontact_sidebar.setBar(letter);

            }
        });
        systemcontact_listview.getParent().requestDisallowInterceptTouchEvent(true);

        systemcontact_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ContactBean cb = (ContactBean) parent.getAdapter().getItem(position);
                String num = cb.getPhoneNum();
                callingDialog(num);
            }
        });
    }

    private void setLetter(List<ContactBean> contacts) {
        for (int i=0; i<contacts.size(); i++) {
            if(!contacts.get(i).getPinyin().toUpperCase().substring(0, 1).matches("[A-Z]")) {
                contacts.get(i).setPinyin("#");
            }
        }
    }

    private void init() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        String[] projection = { ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.DATA1, "sort_key",
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY };
        asyncQueryHandler.startQuery(0, null, uri, projection, null, null,
                "sort_key COLLATE LOCALIZED asc");
        editText = (EditText) findViewById(R.id.editTextId_ContactList);


        contentContact = editText.getText().toString();
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
                contentContact = editText.getText().toString();
                asyncQueryHandler = new MyAsyncQueryHandler(getContentResolver());
                init2(contentContact);

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

    private void init2(String inptxt) {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
        String[] projection = { ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.DATA1, "sort_key",
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.PHOTO_ID, ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY }; // 查询的列
        Pattern p = Pattern.compile("[a-zA-Z]");
        Matcher m = p.matcher(inptxt);
        if (m.find()) {
            // System.out.println("进入了字母查询");
            asyncQueryHandler.startQuery(0, null, uri, projection, null, null, "sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询
        } else {
            // System.out.println("没有进入字母查询");
            asyncQueryHandler.startQuery(0, null, uri, projection, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + inptxt + "%' or " + ContactsContract.CommonDataKinds.Phone.NUMBER
                    + " like '%" + inptxt + "%'", null, "sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询

        }
    }


    public void callingDialog(final String number) {
        LayoutInflater myInflater = LayoutInflater.from(ContactSystemActicity.this);
        final View myDialogView = myInflater.inflate(R.layout.calling_dialog,
                null);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(ContactSystemActicity.this)
                .setView(myDialogView);
        final AlertDialog alert = dialog.show();
        alert.setCanceledOnTouchOutside(true);// 设置点击Dialog外部任意区域关闭Dialog
        alert.getWindow().setGravity(Gravity.BOTTOM);

        // 直播
        LinearLayout mDirectSeeding = (LinearLayout) myDialogView
                .findViewById(R.id.direct_seeding_linear);
        mDirectSeeding.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                try {
                    if (Utils.isNetWorkConnected(ContactSystemActicity.this)) {
                        makeCall(number);
                    } else {
                        Toast.makeText(ContactSystemActicity.this, "网络错误，请重试！",
                                Toast.LENGTH_LONG).show();
                    }
                    alert.dismiss();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
        // 回拨
        LinearLayout mCallReturn = (LinearLayout) myDialogView
                .findViewById(R.id.call_return_linear);
        if("zj".equals(user.product)) {
            mCallReturn.setVisibility(View.GONE);
        }
        mCallReturn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                // TODO Auto-generated method stub
                if (Utils.isNetWorkConnected(ContactSystemActicity.this)) {
                    // 跳转到正在通话页面
                    Intent calling = new Intent(ContactSystemActicity.this,
                            CallingActivity.class);
                    calling.putExtra("phone_number", number);
                    startActivity(calling);
                } else {
                    Toast.makeText(ContactSystemActicity.this, "网络错误，请重试！",
                            Toast.LENGTH_LONG).show();
                }
                alert.dismiss();
            }


        });

        // 普通电话
        LinearLayout mOrdinaryCall = (LinearLayout) myDialogView
                .findViewById(R.id.ordinary_call_linear);
        mOrdinaryCall.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(Intent.ACTION_CALL, Uri
                        .parse("tel://"
                                + number));
                startActivity(intent);
                alert.dismiss();
            }
        });
        // 取消
        LinearLayout mCancelLinear = (LinearLayout) myDialogView
                .findViewById(R.id.cancel_linear);
        mCancelLinear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                alert.dismiss();
            }
        });
    }

    /**
     * 拨打直拨电话
     * @param callee
     */
    public void makeCall(String callee) {
        //TODO 获取id
        Long id = -1L;
        Cursor c = getContentResolver().query(SipProfile.ACCOUNT_URI, null, null, null, null);
        if(c != null) {
            while(c.moveToNext()) {
                id = c.getLong(c.getColumnIndex("id"));
            }
        }
//		System.out.println("sip账户ID是："+id);
        try {
            service.makeCall(callee, id.intValue());
        } catch (RemoteException e) {
            Toast.makeText(ContactSystemActicity.this, "拨打电话失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unbindService(connection);

        MobileApplication.getInstance().remove(this);

    }
}
