package com.moor.im.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.dao.MessageDao;
import com.moor.im.db.dao.NewMessageDao;
import com.moor.im.model.entity.FromToMessage;
import com.moor.im.ui.adapter.SystemListAdapter;
import com.moor.im.ui.view.ChatListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by long on 2015/7/22.
 */
public class SystemActivity extends Activity implements ChatListView.OnRefreshListener {

    private ChatListView mChatList;

    private SystemListAdapter adapter;

    List<FromToMessage> fromToMessage;

    private List<FromToMessage> descFromToMessage = new ArrayList<FromToMessage>();

    ArrayList<FromToMessage> list = new ArrayList<FromToMessage>();

    private int i = 2;

    private Boolean JZflag = true;
    private View header;// 加载更多头
    private int height;

    ImageView title_back;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 2) {
                // 加载更多的时候
                JZMoreMessage();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_system);
        mChatList = (ChatListView) findViewById(R.id.system_list);
        header = View.inflate(this, R.layout.chatlist_header, null);
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        header.measure(w, h);
        height = header.getMeasuredHeight();
        fromToMessage = MessageDao.getInstance().getOneMessage("System", 1);
        descFromToMessage.clear();
        for (int i = fromToMessage.size() - 1; i >= 0; i--) {
            descFromToMessage.add(fromToMessage.get(i));
        }
        if (MessageDao.getInstance().isReachEndMessage(
                descFromToMessage.size(), "System")) {
            mChatList.dismiss();
        }
        list.addAll(descFromToMessage);
        adapter = new SystemListAdapter(SystemActivity.this, list);

        mChatList.setAdapter(adapter);



        NewMessageDao.getInstance().updateUnReadCount("System");
        Message msg = new Message();
        msg.obj = "newMsg";
        MobileApplication.getHandler().sendMessage(msg);


        title_back = (ImageView) findViewById(R.id.title_back);
        title_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // 分页加载更多
    public void JZMoreMessage() {
        fromToMessage = MessageDao.getInstance().getOneMessage("System", i);
        descFromToMessage.clear();
        for (int i = fromToMessage.size() - 1; i >= 0; i--) {
            descFromToMessage.add(fromToMessage.get(i));
        }

        list = new ArrayList<FromToMessage>();
        list.clear();
        list.addAll(descFromToMessage);
        mChatList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if (mChatList.getHeaderViewsCount() > 0) {
            mChatList.removeHeaderView(header);
        }

        // 是否有数据
        if (MessageDao.getInstance().isReachEndMessage(
                descFromToMessage.size(), "System")) {
            mChatList.setSelectionFromTop(fromToMessage.size() - (i - 1) * 15,
                    height);
            mChatList.dismiss();
        } else {
            mChatList.setSelectionFromTop(fromToMessage.size() - (i - 1) * 15
                    + 1, height);
        }

        mChatList.onRefreshFinished();
        JZflag = true;
        i++;

    }

    @Override
    public void toRefresh() {
        // TODO Auto-generated method stub
        if (JZflag == true) {
            JZflag = false;
            new Thread() {
                public void run() {
                    try {
                        sleep(800);
                        Message msg = new Message();
                        msg.what = 2;
                        handler.sendMessage(msg);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                };
            }.start();
        }
    }
}
