package com.moor.im.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.dao.ContactsDao;
import com.moor.im.db.dao.MessageDao;
import com.moor.im.db.dao.NewMessageDao;
import com.moor.im.http.HttpManager;
import com.moor.im.model.entity.Discussion;
import com.moor.im.model.entity.FromToMessage;
import com.moor.im.model.entity.NewMessage;
import com.moor.im.model.parser.DepartmentParser;
import com.moor.im.model.parser.DiscussionParser;
import com.moor.im.model.parser.GroupParser;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.tcpservice.manager.SocketManager;
import com.moor.im.ui.activity.ChatActivity;
import com.moor.im.ui.activity.DepartmentAddActivity;
import com.moor.im.ui.activity.DepartmentUpdateActivity;
import com.moor.im.ui.activity.LoginActivity;
import com.moor.im.ui.activity.MAActivity;
import com.moor.im.ui.activity.SystemActivity;
import com.moor.im.ui.activity.WebActivity;
import com.moor.im.ui.adapter.MessageAdapter;
import com.moor.im.ui.dialog.KickedActicity;
import com.moor.im.utils.LogUtil;
import com.moor.im.utils.VibratorUtil;

/**
 * 消息页
 * 
 * @author Mr.li
 * 
 */
public class MessageFragment extends Fragment implements OnItemClickListener {

	private View mView;
	private ListView mFragmentMessageList;
	private SharedPreferences sp;
	private List<FromToMessage> fromToMessage = new ArrayList<FromToMessage>();
	public static Handler chatHandler = new Handler();
	private List<NewMessage> newMsgs;
	private MessageAdapter messageAdapter;
	private TextView mAllUnreadcount;

	private LinearLayout fragment_message_gongdan;
	
	private String largeMsgId = "";

	SharedPreferences myPreferences;

	// 接收消息
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			System.out.println("=====收到msg 1");
			if ("msg".equals(msg.obj)) {
				//tcp发送了有新消息来了
				updateMessage();
			} else if ("newMsg".equals(msg.obj)) {
				//发送了消息之后
				updateMessage();
			}else if ("backMsg".equals(msg.obj)) {
				//发送了消息之后
				updateMessage();
			}
			if(msg.what == 0x111){
				System.out.println("========打开被踢框1");
				Intent intent = new Intent(MobileApplication.getInstance(), KickedActicity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				MobileApplication.getInstance().startActivity(intent);
			}
			if(msg.what == 0x99) {
				//工单助手打开
				LogUtil.d("MessageFragment", "收到了发送过来的0x99");
				fragment_message_gongdan.setVisibility(View.VISIBLE);
			}
			if(msg.what == 0x97) {
				//工单助手关闭
				LogUtil.d("MessageFragment", "收到了发送过来的0x97");
				fragment_message_gongdan.setVisibility(View.GONE);
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_message, null);
		sp = getActivity().getSharedPreferences("SP", 4);
		MobileApplication.setHandler(handler);

		myPreferences = getActivity().getSharedPreferences(MobileApplication.getInstance()
						.getResources().getString(R.string.spname),
				Activity.MODE_PRIVATE);
		init();
		registerListener();

		updateMessage();

		return mView;
	}

	// 查询数据库数据 适配
	public void updateMessage() {

		newMsgs = NewMessageDao.getInstance().queryMessage();

		for (int i=0; i<newMsgs.size(); i++) {
			if("User".equals(newMsgs.get(i).type)) {
				if("".equals(newMsgs.get(i).fromName)) {
					String name = ContactsDao.getInstance().getContactsName(
							newMsgs.get(i).from);
					newMsgs.get(i).fromName = name;
					NewMessageDao.getInstance().updateMsg(newMsgs.get(i));
				}
			}else if("Group".equals(newMsgs.get(i).type)) {
				if("".equals(newMsgs.get(i).fromName)) {
					if(!"".equals(GroupParser.getInstance().getNameById(
							newMsgs.get(i).sessionId))) {
						String name = GroupParser.getInstance().getNameById(
								newMsgs.get(i).sessionId);
						newMsgs.get(i).fromName = name;
						NewMessageDao.getInstance().updateMsg(newMsgs.get(i));
					}

				}
			}else if("Discussion".equals(newMsgs.get(i).type)) {
				if("".equals(newMsgs.get(i).fromName)) {
					if(!"".equals(DiscussionParser.getInstance().getNameById(
							newMsgs.get(i).sessionId))) {
						String name = DiscussionParser.getInstance().getNameById(
								newMsgs.get(i).sessionId);
						newMsgs.get(i).fromName = name;
						NewMessageDao.getInstance().updateMsg(newMsgs.get(i));
					}

				}
			}

		}

		newMsgs.clear();
		newMsgs = NewMessageDao.getInstance().queryMessage();
		messageAdapter = new MessageAdapter(getActivity());
		List list = messageAdapter.getAdapterData();
		list.clear();
		list.addAll(newMsgs);
		mFragmentMessageList.setAdapter(messageAdapter);
		messageAdapter.notifyDataSetChanged();

		// 所有的未读消息
		if (NewMessageDao.getInstance().getAllUnReadCount() > 0) {
			mAllUnreadcount.setVisibility(View.VISIBLE);
			mAllUnreadcount.setText(NewMessageDao.getInstance()
					.getAllUnReadCount() + "");
		} else if (NewMessageDao.getInstance().getAllUnReadCount() >= 99) {
			mAllUnreadcount.setVisibility(View.VISIBLE);
			mAllUnreadcount.setText(99 + "");
		} else {
			mAllUnreadcount.setVisibility(View.GONE);
		}

	}


	// 初始化方法
	public void init() {
		mFragmentMessageList = (ListView) mView
				.findViewById(R.id.fragment_message_list);
		mAllUnreadcount = (TextView) getActivity().findViewById(
				R.id.all_unreadcount);

		fragment_message_gongdan = (LinearLayout) mView.findViewById(R.id.fragment_message_gongdan);
		boolean ischeck = myPreferences.getBoolean("gongdan", true);
		if(ischeck) {
			fragment_message_gongdan.setVisibility(View.VISIBLE);
		}else {
			fragment_message_gongdan.setVisibility(View.GONE);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	// 注册监听方法
	public void registerListener() {
		mFragmentMessageList.setOnItemClickListener(this);
		mFragmentMessageList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
				menu.add(0, 0, 0, "删除该聊天");
			}
		});

		fragment_message_gongdan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent maIntent = new Intent(getActivity(), MAActivity.class);
				startActivity(maIntent);
			}
		});

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

		switch(item.getItemId()) {
			case 0:
				NewMessageDao.getInstance().deleteMsgById(newMsgs.get(info.position).sessionId);
				updateMessage();
				break;
		}

		return super.onContextItemSelected(item);
	}


	// ListView 点击事件监听
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		NewMessage newMessage = (NewMessage) newMsgs.get(position);

		// 跳转到聊天页面
		Intent chatActivity = new Intent(this.getActivity(), ChatActivity.class);
		if("User".equals(newMessage.type)) {
			chatActivity.putExtra("_id", newMessage.from);
			chatActivity.putExtra("otherName", newMessage.fromName);
			chatActivity.putExtra("type", newMessage.type);
			this.getActivity().startActivity(chatActivity);
		}else if("Group".equals(newMessage.type)) {
			chatActivity.putExtra("_id", newMessage.sessionId);
			chatActivity.putExtra("otherName", newMessage.fromName);
			chatActivity.putExtra("type", newMessage.type);

			this.getActivity().startActivity(chatActivity);
		}else if("Discussion".equals(newMessage.type)) {
			chatActivity.putExtra("_id", newMessage.sessionId);
			chatActivity.putExtra("otherName", newMessage.fromName);
			chatActivity.putExtra("type", newMessage.type);
			this.getActivity().startActivity(chatActivity);
		}else if("System".equals(newMessage.type)) {
			Intent systemActivity = new Intent(getActivity(), SystemActivity.class);
			startActivity(systemActivity);
		}
	}


}
