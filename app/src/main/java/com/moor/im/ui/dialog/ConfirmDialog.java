package com.moor.im.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.moor.im.R;

/**
 * 确认操作对话框
 * Created by long on 2015/7/30.
 */
public class ConfirmDialog extends ParentDialog{
    private Context context;
    private static ConfirmDialog dialog;

    public ConfirmDialog(Context context, android.view.View.OnClickListener onComfirmClickListener) {
        super(context, onComfirmClickListener, onCancelClickListener);
        this.context = context;
        setContentView();
        dialog = this;
    }

    public void setContentView() {
        View view = LayoutInflater.from(context).inflate(
                R.layout.dialog_confirm, null);
        super.setContentView(view);
    }

    private static android.view.View.OnClickListener onCancelClickListener = new android.view.View.OnClickListener() {

        @Override
        public void onClick(View view) {
            dialog.cancel();
        }
    };



}
