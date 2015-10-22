package com.moor.im.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;

import com.moor.im.app.MobileApplication;

public class KickedReceiver extends BroadcastReceiver {
    public KickedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("============okokook");
        if (intent.getAction() == "kicked") {
            System.out.println("========接受到kicked广播");
            Message msg = new Message();
            msg.what = 0x111;
            MobileApplication.getInstance().getHandler().sendMessage(msg);
//            handler.sendMessage(msg);
        }
    }
}
