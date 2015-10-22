package com.moor.im.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.moor.im.app.MobileApplication;
import com.moor.im.tcpservice.service.IMService;
import com.moor.im.utils.LogUtil;
import com.moor.im.utils.TimeUtil;

public class BootBroadcastReceiver extends BroadcastReceiver {
    public BootBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "BootBroadcastReceiver 收到广播,"+intent.getAction()+"，启动service：");

        Intent imserviceIntent = new Intent(context, IMService.class);
        context.startService(imserviceIntent);

        LogUtil.d("MobileApplication", "BootBroadcastReceiver 收到广播, 启动IMService");
    }
}
