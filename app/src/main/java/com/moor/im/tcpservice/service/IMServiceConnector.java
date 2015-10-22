package com.moor.im.tcpservice.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

import com.moor.im.tcpservice.service.IMService.IMServiceBinder;

/**
 * IMService绑定
 * 1. 供上层使用【activity】
 * 同层次的manager没有必要使用。
 */
public abstract class IMServiceConnector {

    public abstract void onIMServiceConnected();
    public abstract void onServiceDisconnected();

	private IMService imService;
	public IMService getIMService() {
		return imService;
	}
	private IMServiceInterface binder;
	private IBinder mToken = new Binder();
	public IMServiceInterface getBinder() {
		return binder;
	}

	private ServiceConnection imServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// todo eric when to unbind the service?
			// TODO Auto-generated method stub
			try {
				binder.leave();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			IMServiceConnector.this.onServiceDisconnected();
		}
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			if (imService == null) {
//				IMServiceBinder binder = (IMServiceBinder) service;
//				imService = binder.getService();

				binder = IMServiceInterface.Stub.asInterface(service);
				try {
					binder.join(mToken);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				if (imService == null) {
					return;
				}
			}
            IMServiceConnector.this.onIMServiceConnected();
		}
	};

    public boolean connect(Context ctx) {
		return bindService(ctx);
	}

    public void disconnect(Context ctx) {
		unbindService(ctx);
        IMServiceConnector.this.onServiceDisconnected();
	}

	public boolean bindService(Context ctx) {

		Intent intent = new Intent();
		intent.setComponent(new ComponentName("com.moor.im", "com.moor.im.tcpservice.service.IMService"));
//		intent.setClass(ctx, IMService.class);
		if (!ctx.bindService(intent, imServiceConnection, Context.BIND_AUTO_CREATE)) {
			return false;
		} else {
			return true;
		}
	}

	public void unbindService(Context ctx) {
		try {
			// todo eric .check the return value .check the right place to call it
			ctx.unbindService(imServiceConnection);
		} catch (IllegalArgumentException exception) {
		}
	}

}
