package com.moor.im.tcpservice.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.CacheKey;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.dao.ContactsDao;
import com.moor.im.db.dao.MessageDao;
import com.moor.im.db.dao.NewMessageDao;
import com.moor.im.event.LoginEvent;
import com.moor.im.event.NewMessgeEvent;
import com.moor.im.event.ReconnectEvent;
import com.moor.im.event.SocketEvent;
import com.moor.im.http.HttpManager;
import com.moor.im.model.entity.Discussion;
import com.moor.im.model.entity.FromToMessage;
import com.moor.im.model.entity.Group;
import com.moor.im.model.entity.NewMessage;
import com.moor.im.model.parser.DiscussionParser;
import com.moor.im.model.parser.GroupParser;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.receiver.AlarmManagerReceiver;
import com.moor.im.tcpservice.manager.HeartBeatManager;
import com.moor.im.tcpservice.manager.LoginManager;
import com.moor.im.tcpservice.manager.SocketManager;
import com.moor.im.tcpservice.tcp.SocketManagerStatus;
import com.moor.im.ui.activity.MainActivity;
import com.moor.im.ui.fragment.MessageFragment;
import com.moor.im.utils.LogUtil;
import com.moor.im.utils.NetUtils;
import com.moor.im.utils.TimeUtil;
import com.moor.im.utils.VibratorUtil;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import de.greenrobot.event.EventBus;
/**
 * im服务,进行tcp的连接的管理
 * @author LongWei
 *
 */
public class IMService extends Service{

	private Context context;
	private SharedPreferences.Editor editor;
	/**binder*/
	private IMServiceBinder binder = new IMServiceBinder();
    public class IMServiceBinder extends IMServiceInterface.Stub {
        public IMService getService() {
            return IMService.this;
        }
		@Override
		public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

		}
		@Override
		public void logoff() throws RemoteException {
			loginMgr.loginOff();
		}
		@Override
		public void join(IBinder token) throws RemoteException {
			System.out.println("----------join----------"+token);
			AppClient client = new AppClient(token);
			token.linkToDeath(client, 0);
			mClient = client;
		}
		@Override
		public void leave() throws RemoteException {
			System.out.println("----------leave----------");
			mClient.mToken.unlinkToDeath(mClient, 0);
			mClient = null;
		}
		@Override
		public void login(String username, String passwd) throws RemoteException{
			System.out.println("----------login----"+username+"---"+passwd+"---");
			editor.putString("loginName", username);
			editor.putString("loginPass", passwd);
			editor.commit();
			socketMgr.login();
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	private SocketManager socketMgr;
	private LoginManager loginMgr;
	private HeartBeatManager heartBeatMgr;

	private AppClient mClient = null;
	public class AppClient implements IBinder.DeathRecipient {
		public final IBinder mToken;
		public AppClient(IBinder token) {
			mToken = token;
		}
		@Override
		public void binderDied() {
			// 客户端死掉，执行此回调
			MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "-----------------client died: " + mToken);
			mClient = null;
		}
	}

	
	@Override
	public void onCreate() {
		super.onCreate();
		sp = this.getSharedPreferences("SP", 0);
		editor = sp.edit();

		EventBus.getDefault().register(this, 10);
//		startForeground((int) System.currentTimeMillis(), new Notification());
		LogUtil.d("IMService", "进入了onCreate方法");
//        MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "IMService ，进入了onCreate方法");

		//start AlarmManager
		Intent amintent = new Intent(IMService.this,
				AlarmManagerReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(
				MobileApplication.getInstance(), 0, amintent, 0);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, 5 * 60);
		// Schedule the alarm!
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP,
				calendar.getTimeInMillis(), 5 * 60 * 1000, sender);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		context = getApplicationContext();
		//进行管理类的初始化
		socketMgr = SocketManager.getInstance(context);
		loginMgr = LoginManager.getInstance(context);
		heartBeatMgr = HeartBeatManager.getInstance(context);
		MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "IMService，进入了onStartCommand方法" + socketMgr.getStatus());

		if(socketMgr.getStatus().equals(SocketManagerStatus.BREAK) && NetUtils.hasDataConnection(context)){
			if(!loginMgr.isKickout() && !loginMgr.isLoginOff() && loginMgr.isStoreUsernamePasswordRight()){
				sp = context.getSharedPreferences("SP", 0);
				String name = sp.getString("loginName", "");
				String password = sp.getString("loginPass", "");
				if(name != null && password !=null && !name.equals("") && !password.equals("")){
					MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "进入了onStartCommand方法,发现socket处于BREAK状态开始重连");
					socketMgr.login();
				}
			}
		}
		//内存不足被杀死，当内存又有的时候，service又被重新创建，但是不保证每次都被创建
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		Intent intent = new Intent("com.moor.im.IMServiceDown");
		sendBroadcast(intent);

		EventBus.getDefault().unregister(this);
//        stopForeground(true);
//        reconnectMgr.reset();
        heartBeatMgr.reset();
//        reconnectMgr.reset();
        LogUtil.d("IMService", "进入了onDestroy方法， 重置了管理类");

		MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "IMService 执行了onDestroy方法，我们再次启动IMService");
		Intent imserviceIntent = new Intent(context, IMService.class);
		context.startService(imserviceIntent);

		super.onDestroy();
	}

	private static final Random random = new Random(System.currentTimeMillis());
	private NotificationManager notificationManager;
	private SharedPreferences sp;
	private List<FromToMessage> fromToMessage;
	private String largeMsgId;

	// EventBus 登录事件驱动,接收到事件后调用manager中的方法进行具体处理
    public void onEventMainThread(LoginEvent loginEvent){
    	LogUtil.d("IMService", "进入了登录事件驱动的方法中，进行相应的处理");
       switch (loginEvent){
           case LOGIN_SUCCESS:
               onLoginSuccess();
               break;
           case LOGIN_FAILED:
               onLoginFailed();
               break;
           case  LOGIN_KICKED:
               onLoginKicked();
               break;
		   case  NEW_MSG:
			   onNewMessageReceived();
			   break;
           default:
        	   break;
       }
    }

	/**
	 * 接收到新消息的处理
	 */
	private void onNewMessageReceived() {
		this.notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		sp = context.getSharedPreferences("SP", 0);
		//获取服务器的新消息
		LogUtil.d("IMService", "接收到100，发送http请求获取新消息");
		ArrayList<String> array = MessageDao.getInstance()
				.getUnReadDao();
		HttpManager.getMsg(sp.getString("connecTionId", ""), array,
				new getMsgResponseHandler(context));

	}

	public boolean isAppForground(Context mContext) {
		ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			if (!topActivity.getPackageName().equals(mContext.getPackageName())) {
				return false;
			}
		}
		return true;

			// Returns a list of application processes that are running on the
			// device
//			ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
//			String packageName = getApplicationContext().getPackageName();
//			List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
//			.getRunningAppProcesses();
//			if (appProcesses == null)
//			return false;
//			for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
//			// The name of the process that this object is associated with.
//				if (appProcess.processName.equals(packageName)
//				&& appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
//					return true;
//					}
//				}
//			return false;
	}


	class getMsgResponseHandler extends TextHttpResponseHandler {
		Context context;
		private Notification notification = new Notification();
		public getMsgResponseHandler(Context context) {
			this.context = context;
		}

		@Override
		public void onFailure(int statusCode, Header[] headers,
							  String responseString, Throwable throwable) {
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
							  String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			String message = HttpParser.getMessage(responseString);
			boolean isLargeMsg = HttpParser.isLargeMsg(responseString);
			System.out.println("isLargeMsg的值为："+isLargeMsg);
			// 获取数据成功并且不是大量数据
			if ("true".equals(succeed)) {
				if(isLargeMsg) {
					//有大量的数据
					LogUtil.d("消息接收器", "有大量消息要来了");
					getLargeMsgsFromNet(largeMsgId);
				}else {
					//没有大量的数据
					fromToMessage = HttpParser.getMsgs(responseString);
					if(MessageDao.getInstance().contains(fromToMessage)) {
						return;
					}
					// 判断数据是否被读取、及时更新
					MessageDao.getInstance().updateMsgsIdDao();
					// 存入手机数据库
					MessageDao.getInstance().insertGetMsgsToDao(fromToMessage);
					LogUtil.d("消息接收器", "消息存到了数据库中");
					// 不等于0说明有新消息
					if (fromToMessage.size() != 0) {
						String fromStr = "";
						String messageStr = "";
						if("User".equals(fromToMessage.get(0).type)) {
							 fromStr = ContactsDao.getInstance().getContactsName(
									fromToMessage.get(0).from);
							if(FromToMessage.MSG_TYPE_TEXT.equals(fromToMessage.get(0).msgType)) {
								messageStr = fromToMessage.get(0).message;
							}else if(FromToMessage.MSG_TYPE_IMAGE.equals(fromToMessage.get(0).msgType)) {
								messageStr = "[图片]";
							}else if(FromToMessage.MSG_TYPE_AUDIO.equals(fromToMessage.get(0).msgType)) {
								messageStr = "[语音]";
							}
						}else if("Group".equals(fromToMessage.get(0).type)) {
							fromStr = GroupParser.getInstance().getNameById(fromToMessage.get(0).sessionId);
							String fromName = ContactsDao.getInstance().getContactsName(
									fromToMessage.get(0).from);
							if(FromToMessage.MSG_TYPE_TEXT.equals(fromToMessage.get(0).msgType)) {
								messageStr = fromName + ":"+ fromToMessage.get(0).message;
							}else if(FromToMessage.MSG_TYPE_IMAGE.equals(fromToMessage.get(0).msgType)) {
								messageStr = fromName + ":"+ "[图片]";
							}else if(FromToMessage.MSG_TYPE_AUDIO.equals(fromToMessage.get(0).msgType)) {
								messageStr = fromName + ":"+ "[语音]";
							}
						}else if("Discussion".equals(fromToMessage.get(0).type)) {
							fromStr = DiscussionParser.getInstance().getNameById(fromToMessage.get(0).sessionId);
							String fromName = ContactsDao.getInstance().getContactsName(
									fromToMessage.get(0).from);
							if(FromToMessage.MSG_TYPE_TEXT.equals(fromToMessage.get(0).msgType)) {
								messageStr = fromName + ":"+ fromToMessage.get(0).message;
							}else if(FromToMessage.MSG_TYPE_IMAGE.equals(fromToMessage.get(0).msgType)) {
								messageStr = fromName + ":"+ "[图片]";
							}else if(FromToMessage.MSG_TYPE_AUDIO.equals(fromToMessage.get(0).msgType)) {
								messageStr = fromName + ":"+ "[语音]";
							}
						}

						notification.icon = R.drawable.ic_launcher;
						notification.defaults = Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND;
						notification.flags |= Notification.FLAG_AUTO_CANCEL;
						notification.when = System.currentTimeMillis();
						notification.tickerText = "有新消息来了";

						Intent it = new Intent(context,
								MainActivity.class);
						it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						it.setAction(Intent.ACTION_MAIN);
						it.addCategory(Intent.CATEGORY_LAUNCHER);
						PendingIntent contentIntent = PendingIntent.getActivity(context, 1,
								it, PendingIntent.FLAG_UPDATE_CURRENT);
						notification.setLatestEventInfo(context, fromStr, messageStr,
								contentIntent);



						// 查询是否有某个人的消息如果没有则插入，如果有则先删除在插入
						for (int i = 0; i < fromToMessage.size(); i++) {

						LogUtil.d("IMService", "消息的类型是:"+fromToMessage.get(i).type);
							if("User".equals(fromToMessage.get(i).type)) {
								LogUtil.d("IMService", "接收到个人发的消息");
								List<NewMessage> newMsgs = NewMessageDao.getInstance().isQueryMessage(
										fromToMessage.get(i).sessionId);
								if (newMsgs.size() == 0) {

									NewMessageDao.getInstance().insertNewMsgs(
											fromToMessage.get(i).sessionId,
											fromToMessage.get(i).message,
											fromToMessage.get(i).msgType,
											ContactsDao.getInstance().getContactsName(
													fromToMessage.get(i).from)
													+ "", fromToMessage.get(i).when, 1, fromToMessage.get(i).type, fromToMessage.get(i).from);
								} else {

									NewMessage nm = NewMessageDao.getInstance().getNewMsg(fromToMessage.get(i).sessionId);
									nm.message = fromToMessage.get(i).message;
									nm.msgType = fromToMessage.get(i).msgType;
									nm.fromName = ContactsDao.getInstance().getContactsName(
											fromToMessage.get(i).from);
									nm.time = fromToMessage.get(i).when;
									nm.unReadCount = nm.unReadCount + 1;
									nm.type = fromToMessage.get(i).type;
									nm.from = fromToMessage.get(i).from;
									NewMessageDao.getInstance().updateMsg(nm);

								}
							}else if("Group".equals(fromToMessage.get(i).type)) {
								// 群组的最新消息
								LogUtil.d("IMService", "接收到群组发的消息");
								if("System".equals(fromToMessage.get(i).from)) {
									if(fromToMessage.get(i).message.contains("解散")) {
										NewMessageDao.getInstance().deleteMsgById(fromToMessage.get(i).sessionId);
									}
									fromToMessage.get(i).sessionId = "System";
									fromToMessage.get(i).type = "System";
									MessageDao.getInstance().updateMsgToDao(fromToMessage.get(i));
									LogUtil.d("IMService", "接收到了群组的系统通知");
									//获取一次群组的数据
									getGroupDataFromNet();
								}
								List<NewMessage> newMsgs = NewMessageDao.getInstance().isQueryMessage(
										fromToMessage.get(i).sessionId);
								if (newMsgs.size() == 0) {
									LogUtil.d("IMService", "新消息存入了数据库中");
									NewMessageDao.getInstance().insertNewMsgs(
											fromToMessage.get(i).sessionId,
											fromToMessage.get(i).message,
											fromToMessage.get(i).msgType,
											GroupParser.getInstance().getNameById(fromToMessage.get(i).sessionId)
													+ "", fromToMessage.get(i).when, 1, fromToMessage.get(i).type, fromToMessage.get(i).from);
								} else {

									NewMessage nm = NewMessageDao.getInstance().getNewMsg(fromToMessage.get(i).sessionId);
									nm.message = fromToMessage.get(i).message;
									nm.msgType = fromToMessage.get(i).msgType;
									nm.fromName = GroupParser.getInstance().getNameById(fromToMessage.get(i).sessionId);
									nm.time = fromToMessage.get(i).when;
									nm.unReadCount = nm.unReadCount + 1;
									nm.type = fromToMessage.get(i).type;
									nm.from = fromToMessage.get(i).from;
									NewMessageDao.getInstance().updateMsg(nm);

								}
							}else if("Discussion".equals(fromToMessage.get(i).type)) {
								// 讨论组的最新消息
								LogUtil.d("IMService", "接收到讨论组发的消息");
								if("System".equals(fromToMessage.get(i).from)) {
									if(fromToMessage.get(i).message.contains("解散")) {
										NewMessageDao.getInstance().deleteMsgById(fromToMessage.get(i).sessionId);
									}
									fromToMessage.get(i).sessionId = "System";
									fromToMessage.get(i).type = "System";
									MessageDao.getInstance().updateMsgToDao(fromToMessage.get(i));
									getDiscussionDataFromNet();
								}
								List<NewMessage> newMsgs = NewMessageDao.getInstance().isQueryMessage(
										fromToMessage.get(i).sessionId);
								if (newMsgs.size() == 0) {

									NewMessageDao.getInstance().insertNewMsgs(
											fromToMessage.get(i).sessionId,
											fromToMessage.get(i).message,
											fromToMessage.get(i).msgType,
											DiscussionParser.getInstance().getNameById(fromToMessage.get(i).sessionId)
													+ "", fromToMessage.get(i).when, 1, fromToMessage.get(i).type, fromToMessage.get(i).from);
								} else {
//
									NewMessage nm = NewMessageDao.getInstance().getNewMsg(fromToMessage.get(i).sessionId);
									nm.message = fromToMessage.get(i).message;
									nm.msgType = fromToMessage.get(i).msgType;
									nm.fromName = DiscussionParser.getInstance().getNameById(fromToMessage.get(i).sessionId);
									nm.time = fromToMessage.get(i).when;
									nm.unReadCount = nm.unReadCount + 1;
									nm.type = fromToMessage.get(i).type;
									nm.from = fromToMessage.get(i).from;
									NewMessageDao.getInstance().updateMsg(nm);

								}
							}


						}
					}

				}



				Intent intnet = new Intent("com.moor.im.NEW_MSG");
				// 收到消息通知页面
				if(isAppForground(context))	 {
					//应用在前台
					Message msg = new Message();
					msg.obj = "msg";
					MobileApplication.getHandler().sendMessage(msg);
				}else {
					notificationManager.notify(1, notification);
					// 发送消息通知上个页面更新数据
					Message backMsg = new Message();
					backMsg.obj = "backMsg";
					MobileApplication.getHandler().sendMessage(backMsg);
				}

				if (fromToMessage.size() != 0) {
					// 通知聊天页面查询数据库更新数据
					long[] lo = new long[] { 0l, 300l, 100l, 300l };
				 	//震动提示
					VibratorUtil.Vibrate(context, lo, true);

					Message msg = new Message();
					msg.what = 1;
					msg.obj = fromToMessage.get(0).sessionId + "";
					MessageFragment.chatHandler.sendMessage(msg);

					intnet.putExtra("obj", fromToMessage.get(0).sessionId + "");
				}
				context.sendBroadcast(intnet);
			}

		}
	}

	/**
	 * 从网络获取大量消息数据
	 */
	public void getLargeMsgsFromNet(String largeMsgId) {
		LogUtil.d("获取大量消息数据：", "largeMsgId是："+largeMsgId);
		ArrayList largeMsgIdarray = new ArrayList();
		largeMsgIdarray.add(largeMsgId);
		HttpManager.getLargeMsgs(sp.getString("connecTionId", ""), largeMsgIdarray, new GetLargeMsgsResponseHandler());
	}

	// 取大量消息
	class GetLargeMsgsResponseHandler extends TextHttpResponseHandler {



		@Override
		public void onFailure(int statusCode, Header[] headers,
							  String responseString, Throwable throwable) {
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
							  String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			String message = HttpParser.getMessage(responseString);
			largeMsgId = HttpParser.getLargeMsgId(responseString);
			boolean hasMore = HttpParser.hasMoreMsgs(responseString);
			fromToMessage.clear();
			if("true".equals(succeed)) {
				fromToMessage = HttpParser.getMsgs(responseString);
				LogUtil.d("获取大量数据", "获取到的消息数为："+fromToMessage.size());

				// 判断数据是否被读取、及时更新
				MessageDao.getInstance().updateMsgsIdDao();
				// 存入手机数据库
				MessageDao.getInstance().insertGetMsgsToDao(fromToMessage);
				// 不等于0说明有新消息
				if (fromToMessage.size() != 0) {
					// 查询是否有某个人的消息如果没有则插入，如果有则先删除在插入
					for (int i = 0; i < fromToMessage.size(); i++) {

						if("User".equals(fromToMessage.get(i).type)) {
							LogUtil.d("IMService", "接收到个人发的消息");
							List<NewMessage> newMsgs = NewMessageDao.getInstance().isQueryMessage(
									fromToMessage.get(i).from);

							// 每个人的最新消息
							if (newMsgs.size() == 0) {
								NewMessageDao.getInstance().insertNewMsgs(
										fromToMessage.get(i).from,
										fromToMessage.get(i).message,
										fromToMessage.get(i).msgType,
										ContactsDao.getInstance().getContactsName(
												fromToMessage.get(i).from)
												+ "", fromToMessage.get(i).when, 1, fromToMessage.get(i).type, fromToMessage.get(i).from);
							} else {

								NewMessage nm = NewMessageDao.getInstance().getNewMsg(fromToMessage.get(i).sessionId);
								nm.message = fromToMessage.get(i).message;
								nm.msgType = fromToMessage.get(i).msgType;
								nm.fromName = ContactsDao.getInstance().getContactsName(
										fromToMessage.get(i).from);
								nm.time = fromToMessage.get(i).when;
								nm.unReadCount = nm.unReadCount + 1;
								nm.type = fromToMessage.get(i).type;
								nm.from = fromToMessage.get(i).from;
								NewMessageDao.getInstance().updateMsg(nm);

							}
						}else if("Group".equals(fromToMessage.get(i).type)) {
							LogUtil.d("IMService", "接收到群组发的消息");
							if("System".equals(fromToMessage.get(i).from)) {

								if(fromToMessage.get(i).message.contains("解散")) {
									NewMessageDao.getInstance().deleteMsgById(fromToMessage.get(i).sessionId);
								}
								fromToMessage.get(i).sessionId = "System";
								fromToMessage.get(i).type = "System";
								MessageDao.getInstance().updateMsgToDao(fromToMessage.get(i));
								LogUtil.d("IMService", "接收到了群组的系统通知");
								//获取一次群组的数据
								getGroupDataFromNet();
							}
							List<NewMessage> newMsgs = NewMessageDao.getInstance().isQueryMessage(
									fromToMessage.get(i).sessionId);

							// 每个人的最新消息
							if (newMsgs.size() == 0) {
								NewMessageDao.getInstance().insertNewMsgs(
										fromToMessage.get(i).sessionId,
										fromToMessage.get(i).message,
										fromToMessage.get(i).msgType,
										GroupParser.getInstance().getNameById(fromToMessage.get(i).sessionId)
												+ "", fromToMessage.get(i).when, 1, fromToMessage.get(i).type, fromToMessage.get(i).from);
							} else {

								NewMessage nm = NewMessageDao.getInstance().getNewMsg(fromToMessage.get(i).sessionId);
								nm.message = fromToMessage.get(i).message;
								nm.msgType = fromToMessage.get(i).msgType;
								nm.fromName = GroupParser.getInstance().getNameById(fromToMessage.get(i).sessionId);
								nm.time = fromToMessage.get(i).when;
								nm.unReadCount = nm.unReadCount + 1;
								nm.type = fromToMessage.get(i).type;
								nm.from = fromToMessage.get(i).from;
								NewMessageDao.getInstance().updateMsg(nm);

							}
						}else if("Discussion".equals(fromToMessage.get(i).type)) {
							// 讨论组的最新消息
							LogUtil.d("IMService", "接收到讨论组发的消息");
							if("System".equals(fromToMessage.get(i).from)) {
								if(fromToMessage.get(i).message.contains("解散")) {
									NewMessageDao.getInstance().deleteMsgById(fromToMessage.get(i).sessionId);
								}
								fromToMessage.get(i).sessionId = "System";
								fromToMessage.get(i).type = "System";
								MessageDao.getInstance().updateMsgToDao(fromToMessage.get(i));
								getDiscussionDataFromNet();
							}
							List<NewMessage> newMsgs = NewMessageDao.getInstance().isQueryMessage(
									fromToMessage.get(i).sessionId);
							if (newMsgs.size() == 0) {

								NewMessageDao.getInstance().insertNewMsgs(
										fromToMessage.get(i).sessionId,
										fromToMessage.get(i).message,
										fromToMessage.get(i).msgType,
										DiscussionParser.getInstance().getNameById(fromToMessage.get(i).sessionId)
												+ "", fromToMessage.get(i).when, 1, fromToMessage.get(i).type, fromToMessage.get(i).from);
							} else {

								NewMessage nm = NewMessageDao.getInstance().getNewMsg(fromToMessage.get(i).sessionId);
								nm.message = fromToMessage.get(i).message;
								nm.msgType = fromToMessage.get(i).msgType;
								nm.fromName = DiscussionParser.getInstance().getNameById(fromToMessage.get(i).sessionId);
								nm.time = fromToMessage.get(i).when;
								nm.unReadCount = nm.unReadCount + 1;
								nm.type = fromToMessage.get(i).type;
								nm.from = fromToMessage.get(i).from;
								NewMessageDao.getInstance().updateMsg(nm);
							}
						}

					}
				}

				if(hasMore) {
					//还有更多的消息，继续去取
					getLargeMsgsFromNet(largeMsgId);
				}else {
					//没有了，刷新界面
					LogUtil.d("获取大量消息数据", "没有更多的数据了");
				}

			}
		}
	}



	/**
     * 被踢了
     */
	private void onLoginKicked() {
		LogUtil.d("IMService", "被踢了");
		loginMgr.onKickedOff();
		Intent kickedIntent = new Intent();
		kickedIntent.setAction("kicked");
		context.sendBroadcast(kickedIntent);
	}

	/**
	 * 登录失败
	 */
	private void onLoginFailed() {
		LogUtil.d("IMService", "登录失败");
		loginMgr.setIsStoreUsernamePasswordRight(false);
		Intent intnet = new Intent("com.moor.im.LOGIN_FAILED");
		context.sendBroadcast(intnet);
	}

	/**
	 * 登录成功
	 */
	private void onLoginSuccess() {
		LogUtil.d("IMService", "登录成功");
		loginMgr.setIsStoreUsernamePasswordRight(true);
		loginMgr.setLoginOff(false);
		//登录成功了发送心跳
		heartBeatMgr.onloginSuccess();
	}

	public LoginManager getLoginMgr() {
		return loginMgr;
	}
	public SocketManager getSocketMgr() {
		return socketMgr;
	}


	public void getGroupDataFromNet() {
		HttpManager.getGroupByUser(sp.getString("connecTionId", ""),
				new GetGroupResponseHandler());
	}


	class GetGroupResponseHandler extends TextHttpResponseHandler {

		@Override
		public void onFailure(int statusCode, Header[] headers,
							  String responseString, Throwable throwable) {
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
							  String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			String message = HttpParser.getMessage(responseString);
			LogUtil.d("IMService", "获取群组返回结果:" + responseString);
			if ("true".equals(succeed)) {
				//将数据存到本地
				MobileApplication.cacheUtil.put(CacheKey.CACHE_GROUP, responseString);
				Message msg = new Message();
				msg.obj = "msg";
				MobileApplication.getHandler().sendMessage(msg);
				LogUtil.d("IMService", "通知消息页更新一下");
			}
		}
	}


	public void getDiscussionDataFromNet() {
		HttpManager.getDiscussionByUser(sp.getString("connecTionId", ""),
				new GetDiscussionResponseHandler());
	}

	class GetDiscussionResponseHandler extends TextHttpResponseHandler {

		@Override
		public void onFailure(int statusCode, Header[] headers,
							  String responseString, Throwable throwable) {
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
							  String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			String message = HttpParser.getMessage(responseString);
			LogUtil.d("IMService", "获取讨论组返回结果:" + responseString);
			if ("true".equals(succeed)) {
				//将数据存到本地
				MobileApplication.cacheUtil.put(CacheKey.CACHE_DISCUSSION, responseString);

				//通知消息页重新刷新一次数据
				Message msg = new Message();
				msg.obj = "msg";
				MobileApplication.getHandler().sendMessage(msg);
			} else {
			}
		}
	}

}
