package com.moor.im.ui.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.http.Header;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.app.RequestUrl;
import com.moor.im.db.dao.ContactsDao;
import com.moor.im.db.dao.MessageDao;
import com.moor.im.db.dao.NewMessageDao;
import com.moor.im.db.dao.UserDao;
import com.moor.im.http.HttpManager;
import com.moor.im.model.entity.ChatEmoji;
import com.moor.im.model.entity.ChatMore;
import com.moor.im.model.entity.Contacts;
import com.moor.im.model.entity.Discussion;
import com.moor.im.model.entity.FromToMessage;
import com.moor.im.model.entity.NewMessage;
import com.moor.im.model.entity.User;
import com.moor.im.model.parser.DiscussionParser;
import com.moor.im.model.parser.GroupParser;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.ui.adapter.ChatAdapter;
import com.moor.im.ui.adapter.FaceAdapter;
import com.moor.im.ui.adapter.MoreAdapter;
import com.moor.im.ui.adapter.ViewPagerAdapter;
import com.moor.im.ui.base.MyBaseActivity;
import com.moor.im.ui.fragment.MessageFragment;
import com.moor.im.ui.view.ChatListView;
import com.moor.im.ui.view.ChatListView.OnRefreshListener;
import com.moor.im.ui.view.recordbutton.AudioRecorderButton;
import com.moor.im.ui.view.recordbutton.AudioRecorderButton.RecorderFinishListener;
import com.moor.im.ui.view.recordbutton.MediaManager;
import com.moor.im.utils.FaceConversionUtil;
import com.moor.im.utils.LogUtil;
import com.moor.im.utils.Utils;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;

/**
 * 聊天
 * @author LongWei
 */
public class ChatActivity extends MyBaseActivity implements OnClickListener,
		OnItemClickListener, OnRefreshListener, RecorderFinishListener {
	private ChatListView mChatList;
	private Button mChatSend, mChatMore, mChatSetModeVoice,
			mChatSetModeKeyboard;
	ImageView chat_btn_back, chat_btn_setting;
	private EditText mChatInput;
	private ChatAdapter chatAdapter;
	private List list;
	private RelativeLayout mChatEdittextLayout,
			mChatMoreContainer;
	private LinearLayout mMore;
	private AudioRecorderButton mRecorderButton;
	private RelativeLayout mChatFaceContainer;
	private ImageView mChatEmojiNormal, mChatEmojiChecked, mChatMicImage;
	private InputMethodManager manager;
	private TextView mOtherName;
	private OnCorpusSelectedListener mListener;
	private ViewPager mChatEmojiVPager, mChatMoreVPager;
	private ArrayList<View> facePageViews;
	private ArrayList<View> morePageViews;
	private LinearLayout mChatIvImageMore, mChatIvImageFace;
	private ArrayList<ImageView> pointViewsFace, pointViewsMore;
	private List<List<ChatEmoji>> emojis;
	private List<FaceAdapter> faceAdapters;
	private List<MoreAdapter> moreAdapters;
	private int current = 0;
	private ArrayList<ChatMore> moreList;
	// 表情分页的结果集合
	public List<List<ChatMore>> moreLists = new ArrayList<List<ChatMore>>();

	private SharedPreferences sp;
	private List<FromToMessage> fromToMessage;
	private String _id;
	private Boolean flag = false;
	private Boolean JZflag = true;
	private String otherName = "";
	private View header;// 加载更多头
	private int i = 2;
	private int height;
	private List<FromToMessage> descFromToMessage = new ArrayList<FromToMessage>();
	private String fromId = "";
	private Boolean readflag = true;// 是否在当前页面的标示
	private String img = "";

	private static final String tag = "ChatActivity";
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private static final int PICK_IMAGE_ACTIVITY_REQUEST_CODE = 200;
	private String picFileFullName;

	Contacts contact;

	/**
	 * 该聊天的会话ID
	 */
	private String sessionId;
	/**
	 * 该聊天的类型
	 */
	private String type;

	private String imicon;

	SharedPreferences myPreferences;
	SharedPreferences.Editor editor;
	private AudioManager audioManager;
	/**
	 * 当前模式是否为扬声器
	 */
	private boolean isSpeaker = true;

	private ImageView title_iv_voice_mode;

	private User user = UserDao.getInstance().getUser();

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				// 切换msg切换图片
			} else if (msg.what == 1) {
				fromId = msg.obj + "";
//
//				// 收到消息
				flag = true;
				fromToMessage = MessageDao.getInstance().getOneMessage(fromId, i);
				updateMessage();
			} else if (msg.what == 2) {
				// 加载更多的时候
				JZMoreMessage();
			}
			
			if(msg.what == 0x88) {
				updateMessage();
			}
			if ("拍照".equals(msg.obj)) {
				takePicture();
			} else if ("图库".equals(msg.obj)) {
				openAlbum();
			}

			if(msg.what == 0x777) {
				int mode = myPreferences.getInt("mode", AudioManager.MODE_NORMAL);
				if(AudioManager.MODE_NORMAL == mode) {
					isSpeaker = true;
					title_iv_voice_mode.setVisibility(View.GONE);

				}else if(AudioManager.MODE_IN_CALL == mode){
					isSpeaker = false;
					title_iv_voice_mode.setVisibility(View.VISIBLE);
				}
				updateMessage();
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobileApplication.getInstance().add(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_chat);
		myPreferences = getSharedPreferences(MobileApplication.getInstance()
						.getResources().getString(R.string.spname),
				Activity.MODE_PRIVATE);
		editor = myPreferences.edit();
		title_iv_voice_mode = (ImageView) findViewById(R.id.title_iv_voice_mode);

		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		Intent intent = getIntent();
		sp = this.getSharedPreferences("SP", 4);
		MessageFragment.chatHandler = handler;
		//对方的姓名
		otherName = intent.getStringExtra("otherName");
		if(otherName == null) {
			otherName = "";
		}

		_id = intent.getStringExtra("_id");
		if(_id == null) {
			_id = "";
		}
		//接收到聊天的类型
		type = intent.getStringExtra("type");
		if(type == null) {
			type = "";
		}
		if("User".equals(type)) {
			sessionId = createSessionId(user._id, _id);
			LogUtil.d("ChatActivity", "聊天的sessionId是:"+sessionId);
		}else if("Group".equals(type)) {
			sessionId = _id;
		}else if("Discussion".equals(type)) {
			sessionId = _id;
		}



		if("User".equals(type)) {
			contact = ContactsDao.getInstance().getContactById(_id);
			imicon = contact.im_icon;
		}
		chat_btn_setting = (ImageView) findViewById(R.id.chat_btn_setting);


		if("Group".equals(type)) {
			//群组，显示出群组的设置按键
			chat_btn_setting.setVisibility(View.VISIBLE);
			chat_btn_setting.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent settingIntent = new Intent(ChatActivity.this, GroupSettingActivity.class);
					settingIntent.putExtra("sessionId", sessionId);
					startActivity(settingIntent);
					finish();
				}
			});
		}else if("Discussion".equals(type)) {
			chat_btn_setting.setVisibility(View.VISIBLE);
			chat_btn_setting.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent settingIntent = new Intent(ChatActivity.this, DiscussionSettingActivity.class);
					settingIntent.putExtra("sessionId", sessionId);
					startActivity(settingIntent);
					finish();
				}
			});
		}

		init();
		registerListener();
		initEmojiViewPager();
		initEmojiPoint();
		initEmojiData();
		initMoreViewPager();
		initMorePoint();
		initMoreData();
	}

	/**
	 * 创建sessionID
	 * @param myId
	 * @param otherId
	 * @return
	 */
	private String createSessionId(String myId, String otherId) {
		String sid = "";
		String myExten = ContactsDao.getInstance().getContactById(myId).exten;
		String otherExten = ContactsDao.getInstance().getContactById(otherId).exten;
		if(Integer.parseInt(myExten) < Integer.parseInt(otherExten)) {
			sid = myId + otherId;
		}else {
			sid = otherId + myId;
		}
		return sid;
	}

	/**
	 * 查询数据库更新页面
	 */
	public void updateMessage() {
		fromToMessage = MessageDao.getInstance().getOneMessage(sessionId, 1);
		descFromToMessage.clear();
		for (int i = fromToMessage.size() - 1; i >= 0; i--) {
			descFromToMessage.add(fromToMessage.get(i));
		}
		// 是否有数据
		if (MessageDao.getInstance().isReachEndMessage(
				descFromToMessage.size(), sessionId)) {
			mChatList.dismiss();
		}
		if (flag == false) {
			//没有收到消息时
			chatAdapter = new ChatAdapter(ChatActivity.this, handler, imicon, isSpeaker);
			list = chatAdapter.getAdapterData();
			list.addAll(descFromToMessage);
			mChatList.setAdapter(chatAdapter);
			chatAdapter.notifyDataSetChanged();
			mChatList.setSelection(fromToMessage.size() + 1);
		} else if (flag == true) {
			//收到消息时
			list = chatAdapter.getAdapterData();
			// 判断是不是当前页面的新消息
			if (sessionId.equals(fromId)) {
				list.clear();
				list.addAll(descFromToMessage);
				mChatList.setAdapter(chatAdapter);
				chatAdapter.notifyDataSetChanged();
				mChatList.setSelection(fromToMessage.size() + 1);
			}
		}

		// 如果在当前页面就设置已读
		if (readflag == true) {
			// 设置消息为已读
			NewMessageDao.getInstance().updateUnReadCount(sessionId);
			Message msg = new Message();
			msg.obj = "newMsg";
			MobileApplication.getHandler().sendMessage(msg);
		}

	}


	// 分页加载更多
	public void JZMoreMessage() {
		fromToMessage = MessageDao.getInstance().getOneMessage(sessionId, i);
		descFromToMessage.clear();
		for (int i = fromToMessage.size() - 1; i >= 0; i--) {
			descFromToMessage.add(fromToMessage.get(i));
		}

		list = chatAdapter.getAdapterData();
		list.clear();
		list.addAll(descFromToMessage);
		System.out.println("加载更多消息后列表的长度："+list.size());
		mChatList.setAdapter(chatAdapter);
		chatAdapter.notifyDataSetChanged();

		if (mChatList.getHeaderViewsCount() > 0) {
			mChatList.removeHeaderView(header);
		}

		// 是否有数据
		if (MessageDao.getInstance().isReachEndMessage(
				descFromToMessage.size(), _id)) {
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

	// 初始化方法
	public void init() {
		// 设置进来时间软键盘不弹出的默认状态
		manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		mChatSend = (Button) this.findViewById(R.id.chat_send);
		chat_btn_back = (ImageView) this.findViewById(R.id.chat_btn_back);
		mRecorderButton = (AudioRecorderButton) findViewById(R.id.chat_press_to_speak);
		mRecorderButton.setRecordFinishListener(this);
		mChatInput = (EditText) this.findViewById(R.id.chat_input);
		mChatEdittextLayout = (RelativeLayout) this
				.findViewById(R.id.chat_edittext_layout);
		mMore = (LinearLayout) this.findViewById(R.id.more);
		mChatEmojiNormal = (ImageView) this
				.findViewById(R.id.chat_emoji_normal);
		mChatEmojiChecked = (ImageView) this
				.findViewById(R.id.chat_emoji_checked);
		mChatFaceContainer = (RelativeLayout) this
				.findViewById(R.id.chat_face_container);
		mChatMoreContainer = (RelativeLayout) this
				.findViewById(R.id.chat_more_container);
		mChatMore = (Button) this.findViewById(R.id.chat_more);
		mChatSetModeVoice = (Button) this
				.findViewById(R.id.chat_set_mode_voice);
		mChatSetModeKeyboard = (Button) this
				.findViewById(R.id.chat_set_mode_keyboard);

		mOtherName = (TextView) this.findViewById(R.id.other_name);
		mOtherName.setText(otherName + "");

		mChatInput.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mChatEdittextLayout
							.setBackgroundResource(R.drawable.input_bar_bg_active);
				} else {
					mChatEdittextLayout
							.setBackgroundResource(R.drawable.input_bar_bg_normal);
				}

			}
		});

		mChatInput.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mChatEdittextLayout
						.setBackgroundResource(R.drawable.input_bar_bg_active);
				mChatEmojiNormal.setVisibility(View.VISIBLE);
				mChatEmojiChecked.setVisibility(View.GONE);

				mMore.setVisibility(View.GONE);
				mChatFaceContainer.setVisibility(View.GONE);
				mChatMoreContainer.setVisibility(View.GONE);
			}
		});

		// 监听文字框
		mChatInput.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!TextUtils.isEmpty(s)) {
					mChatMore.setVisibility(View.GONE);
					mChatSend.setVisibility(View.VISIBLE);
				} else {
					mChatMore.setVisibility(View.VISIBLE);
					mChatSend.setVisibility(View.GONE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		mChatList = (ChatListView) this.findViewById(R.id.chat_list);
		header = View.inflate(this, R.layout.chatlist_header, null);
		int w = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		header.measure(w, h);
		height = header.getMeasuredHeight();

		mChatList.setOnTouchListener(new OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				hideKeyboard();
				mMore.setVisibility(View.GONE);
				mChatEmojiNormal.setVisibility(View.VISIBLE);
				mChatEmojiChecked.setVisibility(View.GONE);
				mChatFaceContainer.setVisibility(View.GONE);
				mChatMoreContainer.setVisibility(View.GONE);
				return false;
			}
		});

		emojis = FaceConversionUtil.getInstace().emojiLists;

		moreList = new ArrayList<ChatMore>();
		ChatMore chatMore1 = new ChatMore(1, R.drawable.icon_chat_vedio + "",
				"视频");
		ChatMore chatMore2 = new ChatMore(1, R.drawable.icon_chat_photo + "",
				"拍照");
		ChatMore chatMore3 = new ChatMore(1, R.drawable.icon_chat_pic + "",
				"图库");
		ChatMore chatMore4 = new ChatMore(1,
				R.drawable.icon_chat_location + "", "位置");
		moreList.add(chatMore2);
		moreList.add(chatMore3);

		int pageCount = (int) Math.ceil(moreList.size() / 8 + 0.1);
		for (int i = 0; i < pageCount; i++) {
			moreLists.add(getData(i));
		}

		mChatEmojiVPager = (ViewPager) findViewById(R.id.chat_emoji_vPager);
		mChatMoreVPager = (ViewPager) findViewById(R.id.chat_more_vPager);
		mChatInput = (EditText) findViewById(R.id.chat_input);
		mChatIvImageFace = (LinearLayout) findViewById(R.id.chat_iv_image_face);
		mChatIvImageMore = (LinearLayout) findViewById(R.id.chat_iv_image_more);

	}

	// 注册监听方法
	public void registerListener() {
		mChatSend.setOnClickListener(this);
		chat_btn_back.setOnClickListener(this);
		mChatSetModeVoice.setOnClickListener(this);
		mChatSetModeKeyboard.setOnClickListener(this);
		mChatEmojiNormal.setOnClickListener(this);
		mChatEmojiChecked.setOnClickListener(this);
		//将更多操作的监听器先注销了，发送图片时在放开
		mChatMore.setOnClickListener(this);
		mChatList.setOnRefreshListener(this);
	}

	// 获取分页数据
	private List<ChatMore> getData(int page) {
		int startIndex = page * 8;
		int endIndex = startIndex + 8;

		if (endIndex > moreList.size()) {
			endIndex = moreList.size();
		}
		// 不这么写，会在viewpager加载中报集合操作异常，我也不知道为什么
		List<ChatMore> list = new ArrayList<ChatMore>();
		list.addAll(moreList.subList(startIndex, endIndex));
		if (list.size() < 8) {
			for (int i = list.size(); i < 8; i++) {
				ChatMore object = new ChatMore();
				list.add(object);
			}
		}
		return list;
	}

	// 隐藏软键盘
	private void hideKeyboard() {
		if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getCurrentFocus() != null)
				manager.hideSoftInputFromWindow(getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	// 发消息
	class NewMessageResponseHandler extends TextHttpResponseHandler {
		FromToMessage fromToMessage;
		public NewMessageResponseHandler(FromToMessage fromToMessage) {
			this.fromToMessage = fromToMessage;
		}
		
		@Override
		public void onFailure(int statusCode, Header[] headers,
				String responseString, Throwable throwable) {
			MessageDao.getInstance().updateFailedMsgToDao(fromToMessage);
			sendMsgsToMessageFragment(fromToMessage);
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
				String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			String message = HttpParser.getMessage(responseString);
			
			if ("true".equals(succeed)) {
//				System.out.println("消息发送成功了");
				
				MessageDao.getInstance().updateSucceedMsgToDao(fromToMessage);
				sendMsgsToMessageFragment(fromToMessage);
			} else {
				MessageDao.getInstance().updateFailedMsgToDao(fromToMessage);
				sendMsgsToMessageFragment(fromToMessage);
				
				if("404".equals(message)) {
					//connection断了，启动重连
				}
			}
		}
	}

	/**
	 * 更新MessageFragment
	 * @param fromToMessage
	 */
	public void sendMsgsToMessageFragment(FromToMessage fromToMessage) {

		// 查询是否有某个人的消息如果没有则插入，如果有则先删除在插入
		List<NewMessage> newMsgs = NewMessageDao.getInstance().isQueryMessage(sessionId);

		String name = "";

		if("User".equals(type)) {
			// 取对方的姓名
			if (ContactsDao.getInstance().getContactsName(_id) == null) {
				name = "";
			} else {
				name = ContactsDao.getInstance().getContactsName(_id) + "";
			}
		}else if("Group".equals(type)) {
			name = GroupParser.getInstance().getNameById(_id);
		}else if("Discussion".equals(type)) {
			name = DiscussionParser.getInstance().getNameById(_id);
			LogUtil.d("ChatActivity", "讨论组的名称是:"+name);
		}


		if (newMsgs.size() == 0) {
			NewMessageDao.getInstance().insertNewMsgs(sessionId + "", fromToMessage.message, fromToMessage.msgType, name, fromToMessage.when, 0, fromToMessage.type, fromToMessage.from);
		} else {

			NewMessageDao.getInstance().deleteOneMessage(newMsgs);
			NewMessageDao.getInstance().insertNewMsgs(sessionId + "", fromToMessage.message, fromToMessage.msgType, name, fromToMessage.when, 0, fromToMessage.type, fromToMessage.from);
		}

		updateMessage();
		// 发送消息通知上个页面更新数据
		Message msg = new Message();
		msg.obj = "newMsg";
		MobileApplication.getHandler().sendMessage(msg);
	}

	/**
	 * 发送文本信息
	 * @param message
	 */
	private void sendMessage(FromToMessage message) {
		//先显示到界面上
		list.add(message);
		mChatList.setAdapter(chatAdapter);
		chatAdapter.notifyDataSetChanged();
		mChatList.setSelection(list.size());
		mChatInput.setText("");

		//再将消息存入数据库中，isSuccessed为false
		message.sendState = "sending";
		MessageDao.getInstance().insertSendMsgsToDao(message);
		//发送到网络中（成功就把isSuccessed设为true,进行一次数据的更新）
		HttpManager.newMsgToServer(sp.getString("connecTionId", ""),
				message, new NewMessageResponseHandler(message));

		
	}
	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.chat_btn_back:
			finish();
			break;
		case R.id.chat_send:
			
			FromToMessage fromToMessage = new FromToMessage("", mChatInput
					.getText().toString());
			fromToMessage.msgType = FromToMessage.MSG_TYPE_TEXT;
			fromToMessage.userType = "0";
			fromToMessage.when = System.currentTimeMillis();

			//判断是哪种类型，个人还是群组
			if("User".equals(type)) {
				LogUtil.d("ChatActivity", "个人聊天");
				fromToMessage.sessionId = sessionId;
				fromToMessage.tonotify  = _id;
				fromToMessage.type = "User";
				fromToMessage.from = _id;
			}else if("Group".equals(type)){
				LogUtil.d("ChatActivity", "群组聊天");
				fromToMessage.sessionId = sessionId;
				fromToMessage.type = "Group";
			}else if("Discussion".equals(type)) {
				LogUtil.d("ChatActivity", "讨论组聊天");
				fromToMessage.sessionId = sessionId;
				fromToMessage.type = "Discussion";
			}

			sendMessage(fromToMessage);
			break;

		case R.id.chat_set_mode_voice:
			hideKeyboard();
			mChatEdittextLayout.setVisibility(View.GONE);
			mMore.setVisibility(View.GONE);
			mChatSetModeVoice.setVisibility(View.GONE);
			mChatSetModeKeyboard.setVisibility(View.VISIBLE);
			mChatSend.setVisibility(View.GONE);
			mChatMore.setVisibility(View.VISIBLE);
			mRecorderButton.setVisibility(View.VISIBLE);
			mChatEmojiNormal.setVisibility(View.VISIBLE);
			mChatEmojiChecked.setVisibility(View.GONE);
			mChatMoreContainer.setVisibility(View.VISIBLE);
			mChatFaceContainer.setVisibility(View.GONE);

			break;

		case R.id.chat_set_mode_keyboard:
			mChatEdittextLayout.setVisibility(View.VISIBLE);
			mChatSetModeKeyboard.setVisibility(View.GONE);
			mChatSetModeVoice.setVisibility(View.VISIBLE);
			mChatInput.requestFocus();
			mRecorderButton.setVisibility(View.GONE);
			mChatFaceContainer.setVisibility(View.GONE);

			if (TextUtils.isEmpty(mChatInput.getText())) {
				mChatMore.setVisibility(View.VISIBLE);
				mChatSend.setVisibility(View.GONE);
			} else {
				mChatMore.setVisibility(View.GONE);
				mChatSend.setVisibility(View.VISIBLE);
			}

			break;
		case R.id.chat_emoji_normal:
			hideKeyboard();
			mMore.setVisibility(View.VISIBLE);
			mChatEmojiNormal.setVisibility(View.GONE);
			mChatEmojiChecked.setVisibility(View.VISIBLE);
			mChatMoreContainer.setVisibility(View.GONE);
			mChatFaceContainer.setVisibility(View.VISIBLE);
			mChatMoreVPager.setVisibility(View.GONE);
			mChatEmojiVPager.setVisibility(View.VISIBLE);
			break;
		case R.id.chat_emoji_checked:
			mChatEmojiNormal.setVisibility(View.VISIBLE);
			mChatEmojiChecked.setVisibility(View.GONE);
			mChatMoreContainer.setVisibility(View.GONE);
			mChatFaceContainer.setVisibility(View.GONE);
			mMore.setVisibility(View.GONE);
			break;

		case R.id.chat_more:
			if (mChatMoreVPager.getVisibility() == View.VISIBLE) {
				mChatMoreVPager.setVisibility(View.GONE);
				mMore.setVisibility(View.GONE);
			} else {
				mChatMoreVPager.setVisibility(View.VISIBLE);
				mMore.setVisibility(View.VISIBLE);
				mChatEmojiNormal.setVisibility(View.VISIBLE);
				mChatEmojiChecked.setVisibility(View.GONE);
				mChatFaceContainer.setVisibility(View.GONE);
				mChatMoreContainer.setVisibility(View.VISIBLE);
				mChatEmojiVPager.setVisibility(View.GONE);

				hideKeyboard();
			}

			break;
		default:
			break;
		}
	}

	public void setOnCorpusSelectedListener(OnCorpusSelectedListener listener) {
		mListener = listener;
	}

	// 表情选择监听器
	public interface OnCorpusSelectedListener {

		void onCorpusSelected(ChatEmoji emoji);

		void onCorpusDeleted();
	}

	// 初始化文件的viewpager
	private void initMoreViewPager() {

		morePageViews = new ArrayList<View>();
		// 左侧添加空页
		View nullView1 = new View(this);
		// 设置透明背景
		nullView1.setBackgroundColor(Color.TRANSPARENT);
		morePageViews.add(nullView1);

		// 中间添加表情页
		moreAdapters = new ArrayList<MoreAdapter>();
		for (int i = 0; i < moreLists.size(); i++) {
			GridView view = new GridView(this);
			MoreAdapter adapter = new MoreAdapter(this, moreLists.get(i),
					handler);
			view.setAdapter(adapter);
			moreAdapters.add(adapter);
			view.setOnItemClickListener(this);
			view.setNumColumns(4);
			view.setBackgroundColor(Color.TRANSPARENT);
			view.setHorizontalSpacing(1);
			view.setVerticalSpacing(1);
			view.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
			view.setCacheColorHint(0);
			view.setPadding(5, 0, 5, 0);
			view.setSelector(new ColorDrawable(Color.TRANSPARENT));
			view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT));
			view.setGravity(Gravity.CENTER);
			morePageViews.add(view);

		}

		// 右侧添加空页面
		View nullView2 = new View(this);
		// 设置透明背景
		nullView2.setBackgroundColor(Color.TRANSPARENT);
		morePageViews.add(nullView2);

	}

	// 初始化游标
	private void initMorePoint() {

		pointViewsMore = new ArrayList<ImageView>();
		ImageView imageView;

		for (int i = 0; i < morePageViews.size(); i++) {
			imageView = new ImageView(this);
			imageView.setBackgroundResource(R.drawable.d1);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT));
			layoutParams.leftMargin = 10;
			layoutParams.rightMargin = 10;
			layoutParams.width = 8;
			layoutParams.height = 8;
			mChatIvImageMore.addView(imageView, layoutParams);
			if (i == 0 || i == morePageViews.size() - 1) {
				imageView.setVisibility(View.GONE);
			}
			if (i == 1) {
				imageView.setBackgroundResource(R.drawable.d2);
			}
			pointViewsMore.add(imageView);

		}
	}

	// 填充数据
	private void initMoreData() {
		mChatMoreVPager.setAdapter(new ViewPagerAdapter(morePageViews));

		mChatMoreVPager.setCurrentItem(1);
		current = 0;
		mChatMoreVPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				current = arg0 - 1;
				// 描绘分页点
				drawMorePoint(arg0);
				// 如果是第一屏或者是最后一屏禁止滑动，其实这里实现的是如果滑动的是第一屏则跳转至第二屏，如果是最后一屏则跳转到倒数第二屏.
				if (arg0 == pointViewsMore.size() - 1 || arg0 == 0) {
					if (arg0 == 0) {
						mChatMoreVPager.setCurrentItem(arg0 + 1);// 第二屏
																	// 会再次实现该回调方法实现跳转.
						pointViewsMore.get(1).setBackgroundResource(
								R.drawable.d2);
					} else {
						mChatMoreVPager.setCurrentItem(arg0 - 1);// 倒数第二屏
						pointViewsMore.get(arg0 - 1).setBackgroundResource(
								R.drawable.d2);
					}
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

	}

	// 绘制游标背景
	public void drawMorePoint(int index) {
		for (int i = 1; i < pointViewsMore.size(); i++) {
			if (index == i) {
				pointViewsMore.get(i).setBackgroundResource(R.drawable.d2);
			} else {
				pointViewsMore.get(i).setBackgroundResource(R.drawable.d1);
			}
		}
	}

	// 初始化显示表情的viewpager
	private void initEmojiViewPager() {
		facePageViews = new ArrayList<View>();
		// 左侧添加空页
		View nullView1 = new View(this);
		// 设置透明背景
		nullView1.setBackgroundColor(Color.TRANSPARENT);
		facePageViews.add(nullView1);

		// 中间添加表情页

		faceAdapters = new ArrayList<FaceAdapter>();

		for (int i = 0; i < emojis.size(); i++) {
			GridView view = new GridView(this);
			FaceAdapter adapter = new FaceAdapter(this, emojis.get(i));
			view.setAdapter(adapter);
			faceAdapters.add(adapter);
			view.setOnItemClickListener(this);
			view.setNumColumns(7);
			view.setBackgroundColor(Color.TRANSPARENT);
			view.setHorizontalSpacing(1);
			view.setVerticalSpacing(1);
			view.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
			view.setCacheColorHint(0);
			view.setPadding(5, 0, 5, 0);
			view.setSelector(new ColorDrawable(Color.TRANSPARENT));
			view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT));
			view.setGravity(Gravity.CENTER);
			facePageViews.add(view);
		}

		// 右侧添加空页面
		View nullView2 = new View(this);
		// 设置透明背景
		nullView2.setBackgroundColor(Color.TRANSPARENT);
		facePageViews.add(nullView2);

	}

	// 初始化游标
	private void initEmojiPoint() {

		pointViewsFace = new ArrayList<ImageView>();
		ImageView imageView;
		for (int i = 0; i < facePageViews.size(); i++) {
			imageView = new ImageView(this);
			imageView.setBackgroundResource(R.drawable.d1);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT));
			layoutParams.leftMargin = 10;
			layoutParams.rightMargin = 10;
			layoutParams.width = 8;
			layoutParams.height = 8;
			mChatIvImageFace.addView(imageView, layoutParams);
			if (i == 0 || i == facePageViews.size() - 1) {
				imageView.setVisibility(View.GONE);
			}
			if (i == 1) {
				imageView.setBackgroundResource(R.drawable.d2);
			}
			pointViewsFace.add(imageView);

		}

	}

	// 填充数据
	private void initEmojiData() {
		mChatEmojiVPager.setAdapter(new ViewPagerAdapter(facePageViews));

		mChatEmojiVPager.setCurrentItem(1);
		current = 0;
		mChatEmojiVPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				current = arg0 - 1;
				// 描绘分页点
				drawFacePoint(arg0);
				// 如果是第一屏或者是最后一屏禁止滑动，其实这里实现的是如果滑动的是第一屏则跳转至第二屏，如果是最后一屏则跳转到倒数第二屏.
				if (arg0 == pointViewsFace.size() - 1 || arg0 == 0) {
					if (arg0 == 0) {
						mChatEmojiVPager.setCurrentItem(arg0 + 1);// 第二屏
																	// 会再次实现该回调方法实现跳转.
						pointViewsFace.get(1).setBackgroundResource(
								R.drawable.d2);
					} else {
						mChatEmojiVPager.setCurrentItem(arg0 - 1);// 倒数第二屏
						pointViewsFace.get(arg0 - 1).setBackgroundResource(
								R.drawable.d2);
					}
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

	}

	// 绘制游标背景
	public void drawFacePoint(int index) {
		for (int i = 1; i < pointViewsFace.size(); i++) {
			if (index == i) {
				pointViewsFace.get(i).setBackgroundResource(R.drawable.d2);
			} else {
				pointViewsFace.get(i).setBackgroundResource(R.drawable.d1);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (mChatFaceContainer.getVisibility() == View.VISIBLE
				&& mChatMoreContainer.getVisibility() == View.GONE) {

			ChatEmoji emoji = (ChatEmoji) faceAdapters.get(current).getItem(
					arg2);
			if (emoji.getId() == R.drawable.face_del_icon) {
				int selection = mChatInput.getSelectionStart();
				String text = mChatInput.getText().toString();
				if (selection > 0) {
					String text2 = text.substring(selection - 1);
					if ("]".equals(text2)) {
						int start = text.lastIndexOf("[");
						int end = selection;
						mChatInput.getText().delete(start, end);
						return;
					}
					mChatInput.getText().delete(selection - 1, selection);
				}
			}
			if (!TextUtils.isEmpty(emoji.getCharacter())) {
				if (mListener != null)
					mListener.onCorpusSelected(emoji);
				SpannableString spannableString = FaceConversionUtil
						.getInstace().addFace(this, emoji.getId(),
								emoji.getCharacter());
				mChatInput.append(spannableString);
			}
		} else if (mChatFaceContainer.getVisibility() == View.GONE
				&& mChatMoreContainer.getVisibility() == View.VISIBLE) {

			ChatMore chatMore = (ChatMore) moreAdapters.get(current).getItem(
					arg2);

			if ("视频".equals(chatMore.name)) {
//				Toast.makeText(this, "点击了视频", Toast.LENGTH_LONG).show();
			} else if ("拍照".equals(chatMore.name)) {
				takePicture();
			} else if ("图库".equals(chatMore.name)) {
				openAlbum();
			} else if ("位置".equals(chatMore.name)) {
//				Toast.makeText(this, "点击了位置", Toast.LENGTH_LONG).show();
			}

		}
	}

	// 拍照
	public void takePicture() {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			File outDir = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			if (!outDir.exists()) {
				outDir.mkdirs();
			}
			File outFile = new File(outDir, System.currentTimeMillis() + ".jpg");
			picFileFullName = outFile.getAbsolutePath();
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outFile));
			intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
			startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
		} else {
			Log.e(tag, "请确认已经插入SD卡");
		}
	}

	// 打开本地相册
	public void openAlbum() {

		Intent intent;
		if (Build.VERSION.SDK_INT < 19) {
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
		} else {
			intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		}
		startActivityForResult(intent, PICK_IMAGE_ACTIVITY_REQUEST_CODE);
	}

	// 拍照回调
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				LogUtil.d("拍照发送图片", "获取图片成功，本地路径是：" + picFileFullName);
				//发送图片
				ArrayList fromTomsgs = new ArrayList<FromToMessage>();
				//构建一条新消息
				FromToMessage fromToMessage = new FromToMessage();
				fromToMessage.message = "";
				fromToMessage.msgType = FromToMessage.MSG_TYPE_IMAGE;
				fromToMessage.userType = "0";
				fromToMessage.when = System.currentTimeMillis();
				fromToMessage.filePath = picFileFullName;

				if("User".equals(type)) {
					LogUtil.d("ChatActivity", "个人聊天");
					fromToMessage.sessionId = sessionId;
					fromToMessage.tonotify  = _id;
					fromToMessage.type = "User";
					fromToMessage.from = _id;
				}else if("Group".equals(type)){
					LogUtil.d("ChatActivity", "群组聊天");
					fromToMessage.sessionId = sessionId;
					fromToMessage.type = "Group";
				}else if("Discussion".equals(type)) {
					LogUtil.d("ChatActivity", "讨论组聊天");
					fromToMessage.sessionId = sessionId;
					fromToMessage.type = "Discussion";
				}
				fromTomsgs.add(fromToMessage);
				list.addAll(fromTomsgs);
				mChatList.setAdapter(chatAdapter);
				chatAdapter.notifyDataSetChanged();
				mChatList.setSelection(list.size());

				fromToMessage.sendState = "sending";
				MessageDao.getInstance().insertSendMsgsToDao(fromToMessage);

				//获取7牛token
//				System.out.println("从服务器获取七牛的token");
				HttpManager.getQiNiuToken(sp.getString("connecTionId", ""),
						fromToMessage.filePath, new UploadFileResponseHandler("img", fromToMessage));



			} else if (resultCode == RESULT_CANCELED) {
				// 用户取消了图像捕获
			} else {
				// 图像捕获失败，提示用户
				Log.e(tag, "拍照失败");
			}
		} else if (requestCode == PICK_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				if (uri != null) {
					String realPath = getRealPathFromURI(uri);
					picFileFullName = realPath;
					LogUtil.d("发送图片消息了", "图片的本地路径是："+picFileFullName);
					//准备发送图片消息
					ArrayList fromTomsgs = new ArrayList<FromToMessage>();
					//构建一条新消息
					FromToMessage fromToMessage = new FromToMessage();
					fromToMessage.message = "";
					fromToMessage.msgType = FromToMessage.MSG_TYPE_IMAGE;
					fromToMessage.userType = "0";
					fromToMessage.when = System.currentTimeMillis();
					fromToMessage.filePath = picFileFullName;

					if("User".equals(type)) {
						LogUtil.d("ChatActivity", "个人聊天");
						fromToMessage.sessionId = sessionId;
						fromToMessage.tonotify  = _id;
						fromToMessage.type = "User";
						fromToMessage.from = _id;
					}else if("Group".equals(type)){
						LogUtil.d("ChatActivity", "群组聊天");
						fromToMessage.sessionId = sessionId;
						fromToMessage.type = "Group";
					}else if("Discussion".equals(type)) {
						LogUtil.d("ChatActivity", "讨论组聊天");
						fromToMessage.sessionId = sessionId;
						fromToMessage.type = "Discussion";
					}
					fromTomsgs.add(fromToMessage);
					list.addAll(fromTomsgs);
					mChatList.setAdapter(chatAdapter);
					chatAdapter.notifyDataSetChanged();
					mChatList.setSelection(list.size());

					fromToMessage.sendState = "sending";
					MessageDao.getInstance().insertSendMsgsToDao(fromToMessage);

					//获取7牛token
//					System.out.println("从服务器获取七牛的token");
					HttpManager.getQiNiuToken(sp.getString("connecTionId", ""),
							fromToMessage.filePath, new UploadFileResponseHandler("img", fromToMessage));


				} else {
					Log.e(tag, "从相册获取图片失败");
				}
			}
		}

	}

	// 等比缩放
	public void scalePicture(String filename, int maxWidth, int maxHeight) {
		Bitmap bitmap = null;
		try {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			BitmapFactory.decodeFile(filename, opts);
			int srcWidth = opts.outWidth;
			int srcHeight = opts.outHeight;
			int desWidth = 0;
			int desHeight = 0;
			// 缩放比例
			double ratio = 0.0;
			if (srcWidth > srcHeight) {
				ratio = srcWidth / maxWidth;
				desWidth = maxWidth;
				desHeight = (int) (srcHeight / ratio);
			} else {
				ratio = srcHeight / maxHeight;
				desHeight = maxHeight;
				desWidth = (int) (srcWidth / ratio);
			}
			// 设置输出宽度、高度
			BitmapFactory.Options newOpts = new BitmapFactory.Options();
			newOpts.inSampleSize = (int) (ratio) + 1;
			newOpts.inJustDecodeBounds = false;
			newOpts.outWidth = desWidth;
			newOpts.outHeight = desHeight;
			bitmap = BitmapFactory.decodeFile(filename, newOpts);

			img = Utils.bitmaptoString(bitmap);
//			UserManager.getQiNiuToken(sp.getString("connecTionId", ""),
//					picFileFullName, new UploadFileResponseHandler());

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	/**
	 * 上传文件回调
	 * @author LongWei
	 *
	 */
	class UploadFileResponseHandler extends TextHttpResponseHandler {
		String fileType = "";
		FromToMessage fromToMessage;

		public UploadFileResponseHandler(String fileType,
				FromToMessage fromToMessage) {
			this.fileType =  fileType;
			this.fromToMessage = fromToMessage;
		}

		@Override
		public void onFailure(int statusCode, Header[] headers,
				String responseString, Throwable throwable) {
//			Toast.makeText(ChatActivity.this, "上传7牛失败了", Toast.LENGTH_SHORT).show();;
			MessageDao.getInstance().updateFailedMsgToDao(fromToMessage);
			sendMsgsToMessageFragment(fromToMessage);
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
				String responseString) {
			// TODO Auto-generated method stub
			String succeed = HttpParser.getSucceed(responseString);
			String message = HttpParser.getMessage(responseString);
			if ("true".equals(succeed)) {
				String upToken = HttpParser.getUpToken(responseString);
				// qiniu SDK自带方法上传
				UploadManager uploadManager = new UploadManager();
				if ("img".equals(fileType)) {// 图片
//					final String imgFileKey = UUID.randomUUID().toString();
					String fileName = UUID.randomUUID().toString();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
					String date = sdf.format(new Date());
					final String imgFileKey = user.account+"/image/"+date + "/"+ System.currentTimeMillis()+"/"+fileName;

					uploadManager.put(fromToMessage.filePath, imgFileKey, upToken,
							new UpCompletionHandler() {


								@Override
								public void complete(String key,
										ResponseInfo info, JSONObject response) {
									// TODO Auto-generated method stub
//									System.out.println("上传图片成功了");
									System.out.println(key + "     " + info
											+ "      " + response);

									fromToMessage.message = RequestUrl.QiniuHttp + imgFileKey;
//									System.out.println("图片在服务器上的位置是："+fromToMessage.message);
									MessageDao.getInstance().updateMsgToDao(fromToMessage);
									//发送新消息给服务器
									HttpManager.newMsgToServer(sp.getString("connecTionId", ""),
											fromToMessage, new NewMessageResponseHandler(fromToMessage));
								}
							},null);

				} else if ("ly".equals(fileType)) {// 音频文件
//					System.out.println("上传录音");
//					final String fileKey = UUID.randomUUID().toString();
					String fileName = UUID.randomUUID().toString();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
					String date = sdf.format(new Date());
					final String fileKey = user.account+"/sound/"+date + "/"+ System.currentTimeMillis()+"/"+fileName;


					uploadManager.put(fromToMessage.filePath, fileKey, upToken,
							new UpCompletionHandler() {
								@Override
								public void complete(String key,
										ResponseInfo info, JSONObject response) {
									// TODO Auto-generated method stub
//									System.out.println("上传录音成功了");
									System.out.println(key + "     " + info
											+ "      " + response);
									//设置获得的url
									fromToMessage.message = RequestUrl.QiniuHttp + fileKey;
//									System.out.println("录音在服务器上的位置是："+fromToMessage.message);
									MessageDao.getInstance().updateMsgToDao(fromToMessage);
									//发送新消息给服务器
									HttpManager.newMsgToServer(sp.getString("connecTionId", ""),
											fromToMessage, new NewMessageResponseHandler(fromToMessage));
								}
							}, null);
				}
			} else {
//				Toast.makeText(ChatActivity.this, message, 3000).show();
			}

		}
	}

	// 获取字符
	public String getRealPathFromURI(Uri contentUri) {
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			// Do not call Cursor.close() on a cursor obtained using this
			// method,
			// because the activity will do that for you at the appropriate time
			Cursor cursor = this.managedQuery(contentUri, proj, null, null,
					null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} catch (Exception e) {
			return contentUri.getPath();
		}
	}

	// 覆盖手机返回键
	@Override
	public void onBackPressed() {
		if (mMore.getVisibility() == View.VISIBLE) {
			mMore.setVisibility(View.GONE);
			mChatEmojiNormal.setVisibility(View.VISIBLE);
			mChatEmojiChecked.setVisibility(View.INVISIBLE);
		} else {
			finish();
			super.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		readflag = false;
		MediaManager.relese();

		MobileApplication.getInstance().remove(this);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		readflag = true;

		int mode = myPreferences.getInt("mode", AudioManager.MODE_NORMAL);
		if(AudioManager.MODE_NORMAL == mode) {
			isSpeaker = true;
			audioManager.setMode(AudioManager.MODE_NORMAL);
			editor.putInt("mode", AudioManager.MODE_NORMAL);
			editor.commit();
			title_iv_voice_mode.setVisibility(View.GONE);
		}else if(AudioManager.MODE_IN_CALL == mode){
			isSpeaker = false;
			audioManager.setMode(AudioManager.MODE_IN_CALL);
			editor.putInt("mode", AudioManager.MODE_IN_CALL);
			editor.commit();
			title_iv_voice_mode.setVisibility(View.VISIBLE);
		}
		updateMessage();
		MediaManager.resume();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		audioManager.setMode(AudioManager.MODE_NORMAL);
		MediaManager.pause();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		updateMessage();
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

	@Override
	public void onRecordFinished(float mTime, String filePath) {
		//录音完成了
		//界面显示
		ArrayList fromTomsgs = new ArrayList<FromToMessage>();
		//构建一条新消息
		FromToMessage fromToMessage = new FromToMessage();
		fromToMessage.message = "";
		fromToMessage.msgType = FromToMessage.MSG_TYPE_AUDIO;
		fromToMessage.userType = "0";
		fromToMessage.when = System.currentTimeMillis();
		fromToMessage.recordTime = mTime;
		fromToMessage.voiceSecond = Math.round(mTime) + "";
		System.out.println("回调函数里的filepath是:"+ filePath);
		fromToMessage.filePath = filePath;

		if("User".equals(type)) {
			LogUtil.d("ChatActivity", "个人聊天");
			fromToMessage.sessionId = sessionId;
			fromToMessage.tonotify  = _id;
			fromToMessage.type = "User";
			fromToMessage.from = _id;
		}else if("Group".equals(type)){
			LogUtil.d("ChatActivity", "群组聊天");
			fromToMessage.sessionId = sessionId;
			fromToMessage.type = "Group";
		}else if("Discussion".equals(type)) {
			LogUtil.d("ChatActivity", "讨论组聊天");
			fromToMessage.sessionId = sessionId;
			fromToMessage.type = "Discussion";
		}

		fromTomsgs.add(fromToMessage);
		list.addAll(fromTomsgs);
		mChatList.setAdapter(chatAdapter);
		chatAdapter.notifyDataSetChanged();
		mChatList.setSelection(list.size());
		
		fromToMessage.sendState = "sending";
		MessageDao.getInstance().insertSendMsgsToDao(fromToMessage);
		
		//获取7牛token
//		System.out.println("从服务器获取七牛的token");
		HttpManager.getQiNiuToken(sp.getString("connecTionId", ""),
				fromToMessage.filePath, new UploadFileResponseHandler("ly", fromToMessage));
		//上传7牛
		//发送新消息到服务器
	}

}
