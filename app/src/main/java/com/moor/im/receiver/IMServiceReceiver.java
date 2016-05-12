package com.moor.im.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Message;

import com.moor.im.app.MobileApplication;
import com.moor.im.event.LoginEvent;
import com.moor.im.event.NewOrderEvent;
import com.moor.im.ui.fragment.MessageFragment;
import com.moor.im.utils.TimeUtil;

import de.greenrobot.event.EventBus;

public class IMServiceReceiver extends BroadcastReceiver {
    public IMServiceReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "IMServiceReceiver 收到广播," + intent.getAction());

        switch (intent.getAction()){
            case "com.moor.im.LOGIN_SUCCESS_FOR_RECEIVER":
                handlerLoginSuccess(context, intent);
                break;
            case "com.moor.im.NEW_MSG":
                handlerNewMessage(context, intent);
                break;
            case "com.moor.im.LOGIN_FAILED":
                EventBus.getDefault().postSticky(LoginEvent.LOGIN_FAILED);
                break;
            case "com.moor.im.NEW_ORDER":
                EventBus.getDefault().post(new NewOrderEvent());
                break;
        }
    }

    private void handlerLoginSuccess(Context context, Intent intent){
        SharedPreferences sp = context.getSharedPreferences("SP", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("connecTionId", intent.getExtras().get("connecTionId") + "");
        editor.putString("isStoreUsernamePasswordRight", "true");
        editor.commit();
        EventBus.getDefault().postSticky(LoginEvent.LOGIN_SUCCESS);
    }

    private void handlerNewMessage(Context context, Intent intent){
        //更新列表
        Message msg = new Message();
        msg.obj = "msg";
        MobileApplication.getInstance().getHandler().sendMessage(msg);
        //更新聊天框
        if(intent.getExtras() != null){
            if(intent.getExtras().get("obj")!=null){
                Message msg1 = new Message();
                msg1.what = 1;
                msg1.obj = intent.getExtras().get("obj").toString();
                MessageFragment.chatHandler.sendMessage(msg1);
            }
        }
    }
}
