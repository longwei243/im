package com.moor.im.tcpservice.manager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.moor.im.app.MobileApplication;
import com.moor.im.app.RequestUrl;
import com.moor.im.event.SocketEvent;
import com.moor.im.tcpservice.tcp.ServerMessageHandler;
import com.moor.im.tcpservice.tcp.SocketManagerStatus;
import com.moor.im.tcpservice.tcp.SocketThread;
import com.moor.im.utils.LogUtil;
import com.moor.im.utils.TimeUtil;

import de.greenrobot.event.EventBus;

/**
 * tcp连接的管理类，这里进行真正的连接等操作
 * @author LongWei
 *
 */
public class SocketManager {
	
	private static SocketManager socketManager;

	private Context context;

	private HeartBeatManager heartBeatManager;
	
	private SocketManager(Context context) {
		this.context = context;
		heartBeatManager = HeartBeatManager.getInstance(context);
	}

	private SocketManagerStatus Status = SocketManagerStatus.BREAK;

	public synchronized void setStatus(SocketManagerStatus status){
		System.out.println("切换状态为"+status);
		MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "切换状态为"+status);
		this.Status = status;
	}

	public SocketManagerStatus getStatus(){
			return this.Status;
	}

	public static SocketManager getInstance(Context context){
		if(socketManager == null) {
			socketManager = new SocketManager(context);
			EventBus.getDefault().register(socketManager);
		}
		return socketManager;
	}
	
	private SocketThread socketThread;

	/**
	 * 连接tcp服务器
	 */
	public void login() {
		MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "tcp 开始重连, 进行登录操作");
		if (socketThread != null) {
			socketThread.setConnecting(false);
			socketThread.close();
			socketThread = null;
        }
		socketThread = new SocketThread(RequestUrl.baseTcpHost, RequestUrl.baseTcpPort, new ServerMessageHandler(context));
		socketThread.start();
	}

	/**
	 * 断开tcp连接
	 */
	public void onServerDisconn(){
		disconnectServer();
    }
	
	 /**
     * 断开与tcp服务器的链接
     */
    public void disconnectServer() {
        if (socketThread != null) {
        	socketThread.close();
        	socketThread = null;
        }
    }

    /**
     * 向服务器发送数据
     * @param data
     */
    public void sendData(String data) {
		if(socketThread != null) {
			try {
				socketThread.sendData(data);
			}catch (Exception e) {
				LogUtil.d("SocketManger", "向服务器发送数据异常");
			}
		}

    }
    
    /**判断链接是否处于断开状态*/
    public boolean isSocketConnect(){
        if(socketThread == null || socketThread.isClose()){
            return false;
        }
        return true;
    }

	public void onEvent(SocketEvent socketEvent){
    	LogUtil.d("IMService", "进入了socket事件驱动的方法中");
    	switch (socketEvent){
    	case NONE:
    		//什么也没干呢
    		break;
		case NETWORK_OK:
			handlerNetWorkOk();
			break;
    	case MSG_SERVER_DISCONNECTED:
			handlerDisconnected();
    		break;
		case NETWORK_DOWN:
			handlerNetWorkDown();
			break;
    	default:
    		break;
    	}
    }

	private void handlerNetWorkOk(){
		LogUtil.d("IMService", "网络恢复了，tcp开始重连");
		if (Status.equals(SocketManagerStatus.LOGINED)) {
			LogUtil.d("SocketManager","网络恢复了， 登录状态是成功，不用进行重连");
			return;
		}
		login();
	}

	private void handlerNetWorkDown(){
		setStatus(SocketManagerStatus.BREAK);
		if(socketThread != null) {
			socketThread.setConnecting(false);
			socketThread = null;
			heartBeatManager.reset();
		}

	}

	private void handlerDisconnected(){
		MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "接收到tcp断开的事件，准备进行响应操作");
	 	if( Status.equals(SocketManagerStatus.CONNECTED) ||
			Status.equals(SocketManagerStatus.LOGINED) ||
			Status.equals(SocketManagerStatus.WAIT_LOGIN) ||
			Status.equals(SocketManagerStatus.CONNECTING)){
			if(!LoginManager.getInstance(MobileApplication.getInstance()).isLoginOff()
				&& !LoginManager.getInstance(MobileApplication.getInstance()).isKickout()) {
				setStatus(SocketManagerStatus.BREAK);
				heartBeatManager.reset();
				/**检测网络状态*/
				ConnectivityManager nw = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo netinfo = nw.getActiveNetworkInfo();
				if (netinfo != null && netinfo.isConnected()) {
					LogUtil.d("IMService", "tcp连接被断开了,但是有网，开始重连");
					login();

				}
			}
		}
	}

	public SocketThread getSocketThread(){
		return socketThread;
	}

}
