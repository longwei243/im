package com.moor.im.receiver;

import com.moor.im.app.MobileApplication;
import com.moor.im.event.ReconnectEvent;
import com.moor.im.event.SocketEvent;
import com.moor.im.tcpservice.manager.HeartBeatManager;
import com.moor.im.utils.LogUtil;
import com.moor.im.utils.TimeUtil;

import de.greenrobot.event.EventBus;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * 网络状态监听器
 * @author LongWei
 *
 */
public class NetWorkReceiver extends BroadcastReceiver{

	private boolean isNetConnected = false;

	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		int tempStatus = -1;
		if(info != null && info.isConnected()) {
			//网络连接上了
			System.out.println("网络连接上了");
			isNetConnected = true;

			if(info.getType() == ConnectivityManager.TYPE_WIFI) {
				//wifi
//				HeartBeatManager.heartInterval = 10;
//				System.out.println("设置了wifi网络下的心跳时长"+TcpManager.heartInterval);
			}else if(info.getType() == ConnectivityManager.TYPE_MOBILE){
				//手机网络
				switch(info.getSubtype()) {
				case TelephonyManager.NETWORK_TYPE_GPRS:
				case TelephonyManager.NETWORK_TYPE_EDGE:
				case TelephonyManager.NETWORK_TYPE_IDEN:
				case TelephonyManager.NETWORK_TYPE_CDMA:
				case TelephonyManager.NETWORK_TYPE_1xRTT:
					//2g
//					HeartBeatManager.heartInterval = 300;
//					System.out.println("设置了2g网络下的心跳时长"+TcpManager.heartInterval);
					break;
				case TelephonyManager.NETWORK_TYPE_EVDO_B:
				case TelephonyManager.NETWORK_TYPE_EHRPD:
				case TelephonyManager.NETWORK_TYPE_HSPA:
				case TelephonyManager.NETWORK_TYPE_HSPAP:
				case TelephonyManager.NETWORK_TYPE_UMTS:
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
				case TelephonyManager.NETWORK_TYPE_HSDPA:
				case TelephonyManager.NETWORK_TYPE_HSUPA:
					//3g
//					HeartBeatManager.heartInterval = 120;
//					System.out.println("设置了3g网络下的心跳时长"+TcpManager.heartInterval);
					break;
				case TelephonyManager.NETWORK_TYPE_LTE:
					//4g
//					HeartBeatManager.heartInterval = 80;
//					System.out.println("设置了4g网络下的心跳时长"+TcpManager.heartInterval);
					break;
				
				}
			}

			if(isNetConnected) {
				//重新连接网络后启动断线重连
				LogUtil.d("NetWorkReceiver", "网络重新连接上启动断线重连，发送了启动断线重连的事件");
//				MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "NetWorkReceiver,网络重新连接上启动断线重连，发送了启动断线重连的事件");

				EventBus.getDefault().postSticky(SocketEvent.NETWORK_OK);
				Intent netConnectedintent = new Intent("netchanged");
				netConnectedintent.putExtra("netstate", "connected");
				context.sendBroadcast(netConnectedintent);
			}
			
		}else {
			//网络断了
			System.out.println("NetWorkReceiver网络断了");
			isNetConnected = false;
			EventBus.getDefault().postSticky(SocketEvent.NETWORK_DOWN);
			//弹出提示栏appManager
			Intent netClosedintent = new Intent("netchanged");
			netClosedintent.putExtra("netstate", "closed");
			context.sendBroadcast(netClosedintent);
		}
	}

}
