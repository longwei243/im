package com.m7.imkfsdk.recordbutton;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.moor.im.R;


/**
 * Created by LongWei
 */
public class DialogManager {

    private Dialog mDialog;

    private ImageView mIcon;
    private ImageView mVoice;
    private TextView textView;

    private Context context;

    public DialogManager(Context context) {
        this.context = context;
    }

    public void showDialog() {
        mDialog = new Dialog(context, R.style.Theme_AudioDialog);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_recorder, null);
        mDialog.setContentView(view);
        mIcon = (ImageView) view.findViewById(R.id.dialog_recorder_iv_rd);
        mVoice = (ImageView) view.findViewById(R.id.dialog_recorder_iv_voice);
        textView = (TextView) view.findViewById(R.id.dialog_recorder_tv);

        mDialog.show();
    }

    public void recording() {
        if(mDialog != null && mDialog.isShowing()) {
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);

            mIcon.setImageResource(R.drawable.recorder);
            textView.setText("手指上滑 取消发送");
        }
    }

    public void wantToCancel() {
        if(mDialog != null && mDialog.isShowing()) {
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);

            mIcon.setImageResource(R.drawable.cancel);
            textView.setText("松开手指 取消发送");
        }
    }

    public void tooShort() {
        if(mDialog != null && mDialog.isShowing()) {
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);

            mIcon.setImageResource(R.drawable.voice_to_short);
            textView.setText("录音时间太短");
        }
    }

    public void dismissDialog() {
        if(mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public void updateVoiceLevel(int level) {
        if(mDialog != null && mDialog.isShowing()) {
            int resId = context.getResources().getIdentifier("v"+level, "drawable", context.getPackageName());
            mVoice.setImageResource(resId);
        }
    }

}
