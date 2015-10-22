package com.moor.im.ui.dialog;

import com.moor.im.app.MobileApplication;
import com.moor.im.tcpservice.manager.SocketManager;
import com.moor.im.tcpservice.service.IMService;
import com.moor.im.tcpservice.service.IMServiceConnector;
import com.moor.im.tcpservice.service.IMServiceInterface;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
/**
 * 被踢了显示的界面，主要是显示被T对话框
 * @author LongWei
 *
 */
public class KickedActicity extends Activity{
	private KickedDialog dialog;
	private IMService imService;
	private SocketManager socketManager;
	private SharedPreferences sp;
	private IMServiceConnector imServiceConnector = new IMServiceConnector(){

		@Override
		public void onIMServiceConnected() {
			imService = imServiceConnector.getIMService();
			if(imService != null) {
				socketManager = imService.getSocketMgr();
			}
		}

		@Override
		public void onServiceDisconnected() {

		}

	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		dialog = new KickedDialog(KickedActicity.this, onComfirmClickListener, onCancelClickListener);
		dialog.show();
		imServiceConnector.connect(KickedActicity.this);
		sp = MobileApplication.getInstance().getSharedPreferences("SP", 0);
	}
	
	private android.view.View.OnClickListener onComfirmClickListener = new android.view.View.OnClickListener() {
		
		@Override
		public void onClick(View view) {
			//进行重连
//			socketManager.login();
			dialog.cancel();
			finish();
			String name = sp.getString("loginName", "");
			String password = sp.getString("loginPass", "");
			try {
				imServiceConnector.getBinder().login(name, password);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
//			dialog.cancel();
//			finish();
		}
	};
	private android.view.View.OnClickListener onCancelClickListener = new android.view.View.OnClickListener() {
		
		@Override
		public void onClick(View view) {
			MobileApplication.getInstance().exit();
			dialog.cancel();
			finish();
		}
	};


	@Override
	protected void onDestroy() {
		super.onDestroy();
		imServiceConnector.disconnect(KickedActicity.this);
	}
}
