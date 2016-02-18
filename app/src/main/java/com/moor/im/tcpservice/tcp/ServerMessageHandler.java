package com.moor.im.tcpservice.tcp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.moor.im.app.CacheKey;
import com.moor.im.app.MobileApplication;
import com.moor.im.event.LoginEvent;
import com.moor.im.event.SocketEvent;
import com.moor.im.tcpservice.manager.LoginManager;
import com.moor.im.tcpservice.manager.SocketManager;
import com.moor.im.utils.LogUtil;
import com.moor.im.utils.TimeUtil;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;

import java.nio.charset.Charset;

import de.greenrobot.event.EventBus;


/**
 * tcp数据的处理器,接收到对应的数据后将对应的事件发送出去
 * @author LongWei
 *
 */
public class ServerMessageHandler extends IdleStateAwareChannelHandler {

	private Context context;
	
	private SharedPreferences sp; 
	private Editor editor;
	private long lastHeartBeatReceivedTime;

	
	public ServerMessageHandler(Context context) {
		this.context = context;
		sp = context.getSharedPreferences("SP", 0);
		editor = sp.edit();

	}
	
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		super.channelConnected(ctx, e);
		System.out.println("连接tcp服务");
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
        /**
         * 1. 已经与远程主机建立的连接，远程主机主动关闭连接，或者网络异常连接被断开的情况
         2. 已经与远程主机建立的连接，本地客户机主动关闭连接的情况
         3. 本地客户机在试图与远程主机建立连接时，遇到类似与connection refused这样的异常，未能连接成功时
         而只有当本地客户机已经成功的与远程主机建立连接（connected）时，连接断开的时候才会触发channelDisconnected事件，即对应上述的1和2两种情况。
         *
         **/
  		super.channelDisconnected(ctx, e);
//		MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "tcp 链接断开了：");
		LogUtil.d("ServerMessageHandler", "已经与Server断开连接。。。。");
		if(SocketManager.getInstance(MobileApplication.getInstance()).getSocketThread() != null
				&& SocketManager.getInstance(MobileApplication.getInstance()).getSocketThread().getChannel() != null){
			if(ctx.getChannel().getId().equals(SocketManager.getInstance(MobileApplication.getInstance()).getSocketThread().getChannel().getId())){
//				MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "tcp1 开始连：");
				//发送tcp服务器连接断开的事件
				EventBus.getDefault().postSticky(SocketEvent.MSG_SERVER_DISCONNECTED);
				LogUtil.d("ServerMessageHandler", "发送tcp服务器连接断开的事件");
			}else{
				System.out.println("发现了 old tcp channel 断开");
			}
		}else{

//			MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "tcp2 开始连：");
			//发送tcp服务器连接断开的事件
			EventBus.getDefault().postSticky(SocketEvent.MSG_SERVER_DISCONNECTED);
			LogUtil.d("ServerMessageHandler", "发送tcp服务器连接断开的事件");
		}
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		super.messageReceived(ctx, e);
		ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
        String result = buffer.toString(Charset.defaultCharset());
		System.out.println("服务器返回的数据是：" + result);
        LogUtil.d("ServerMessageHandler", "服务器返回的数据是：" + result);
		MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "ServerMessageHandler，服务器返回的数据是："+ result);

        if ("3".equals(result)) {
        	//心跳管理器负责
		} else if ("4".equals(result)) {
			//被踢了
			//发送被踢了的事件
			EventBus.getDefault().postSticky(LoginEvent.LOGIN_KICKED);
			SocketManager.getInstance(MobileApplication.getInstance()).setStatus(SocketManagerStatus.BREAK);
		}else if ("100".equals(result)) {
			//有新消息之后的处理
			EventBus.getDefault().postSticky(LoginEvent.NEW_MSG);
		} else if ("400".equals(result)) {
			//登录失败，用户名或密码错误
			//发送登录失败的事件
			MobileApplication.cacheUtil.put(CacheKey.CACHE_CHANGED_PASSWORD, "false", 9);
			LoginManager.getInstance(MobileApplication.getInstance()).setIsStoreUsernamePasswordRight(false);
			EventBus.getDefault().post(LoginEvent.LOGIN_FAILED);
			SocketManager.getInstance(MobileApplication.getInstance()).setStatus(SocketManagerStatus.CONNECTED);
		} else if(result.startsWith("200")) {
			String connectionid = result.replace("200", "");
			editor.putString("connecTionId", connectionid + "");
			editor.commit();

			MobileApplication.cacheUtil.put(CacheKey.CACHE_CHANGED_PASSWORD, "true", 9);

			//发送登录成功的事件
			Intent intnet = new Intent("com.moor.im.LOGIN_SUCCESS_FOR_RECEIVER");
			intnet.putExtra("connecTionId", connectionid + "");
			context.sendBroadcast(intnet);

			SocketManager.getInstance(MobileApplication.getInstance()).setStatus(SocketManagerStatus.LOGINED);
			MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "登陆成功了：connecTionId被保存起来了：" + connectionid);
		} else {
			MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "ServerMessageHandler，服务器返回的数据是："+ result+" 未知的标示");
		}
	}

    /**
	 *
     * */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
//        super.exceptionCaught(ctx, e);
        LogUtil.d("ServerMessageHandler", "exceptionCaught被调用了，直接断开连接");
		MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "ServerMessageHandler,exceptionCaught被调用了，发送MSG_SERVER_DISCONNECTED的事件");
        //有异常时直接断开连接
		EventBus.getDefault().postSticky(SocketEvent.MSG_SERVER_DISCONNECTED);
		Channel ch = e.getChannel();
		ch.close();
    }


	@Override
	public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
		super.channelIdle(ctx, e);

		switch (e.getState()) {
			case READER_IDLE:
				LogUtil.d("ServerMessageHandler", "读取通道空闲了");
				MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "ServerMessageHandler,读取通道空闲了,发送MSG_SERVER_DISCONNECTED的事件");
				EventBus.getDefault().postSticky(SocketEvent.MSG_SERVER_DISCONNECTED);
				Channel ch = e.getChannel();
				ch.close();
				break;
		}
	}
}
