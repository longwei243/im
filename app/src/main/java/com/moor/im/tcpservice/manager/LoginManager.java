package com.moor.im.tcpservice.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.moor.im.app.MobileApplication;
import com.moor.im.tcpservice.tcp.SocketManagerStatus;
import com.moor.im.utils.TimeUtil;
import com.moor.im.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 登录管理
 * @author LongWei
 *
 */
public class LoginManager {

	SocketManager socketManager;
	
	private static LoginManager loginManager;
	
	private boolean isKickout = false;
	private boolean isLoginOff = false;
	private boolean isStoreUsernamePasswordRight = true;

	private Context context;

	private SharedPreferences sp;

	
	private LoginManager(Context context) {
		this.context = context;
		sp = context.getSharedPreferences("SP", 0);
		socketManager = SocketManager.getInstance(context);
	}
	
	public static LoginManager getInstance(Context context) {
		if(loginManager == null) {
			loginManager = new LoginManager(context);
		}
		return loginManager;
	}
	/**
	 * 登录tcp服务器
	 */
	public void login() {
		/**
		 * 连接tcp服务器成功再去登录
		 */
		System.out.println("进入发送登陆方法" + SocketManager.getInstance(MobileApplication.getInstance()).getStatus());
		if(SocketManager.getInstance(MobileApplication.getInstance()).getStatus().equals(SocketManagerStatus.CONNECTED)) {
			System.out.println("登陆方法判断到了 CONNECTED 状态");
			int rom = (int) (Math.random() * 900) + 100;

			String name = sp.getString("loginName", "");
			String password = sp.getString("loginPass", "");

			JSONObject jb = new JSONObject();
			try {
				jb.put("Action", "login");
				jb.put("LoginName", name);
				jb.put("Platform", "android");
				jb.put("RandomKey", rom + "");
				jb.put("MD5", Utils.getMD5(name + rom + password));
			} catch (JSONException e) {
				e.printStackTrace();
			}

			String str = "1" + jb.toString() + "\n";

			MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "发送登陆请求" + name + "  " + password);
			socketManager.sendData(str);
			SocketManager.getInstance(MobileApplication.getInstance()).setStatus(SocketManagerStatus.WAIT_LOGIN);

			isKickout = false;
			isLoginOff = false;
			isStoreUsernamePasswordRight = false;
		}else {
			SocketManager.getInstance(MobileApplication.getInstance()).login();
		}
	}
	
	/**
	 * 注销
	 */
	public void loginOff() {
		this.setLoginOff(true);
		socketManager.sendData("quit\n");
		socketManager.disconnectServer();
	}
	
	/**
	 * 被踢了
	 */
	public void onKickedOff() {
		isKickout=true;
		socketManager.sendData("quit\n");
        socketManager.onServerDisconn();
	}
	
	public boolean isKickout() {
        return isKickout;
    }

	public boolean isLoginOff() {
		return isLoginOff;
	}
	
	public void setLoginOff(boolean isLoginOff) {
		this.isLoginOff = isLoginOff;
	}

	public boolean isStoreUsernamePasswordRight() {
		return isStoreUsernamePasswordRight;
	}

	public void setIsStoreUsernamePasswordRight(boolean isStoreUsernamePasswordRight) {
		this.isStoreUsernamePasswordRight = isStoreUsernamePasswordRight;
	}
}
