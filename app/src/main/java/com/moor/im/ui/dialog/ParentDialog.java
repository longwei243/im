package com.moor.im.ui.dialog;


import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.moor.im.R;

/**
 * 父对话框-所有对话框继承此对话框
 * @author LongWei
 *
 */
public class ParentDialog extends Dialog {

	private Context context;
	private View parentView;
	private android.view.View.OnClickListener onComfirmClickListener;
	
	private android.view.View.OnClickListener onCancelClickListener;
	
	public ParentDialog(Context context, android.view.View.OnClickListener onComfirmClickListener, android.view.View.OnClickListener onCancelClickListener) {
		super(context, R.style.parentDialog);
		this.setCanceledOnTouchOutside(false);
		parentView = LayoutInflater.from(context).inflate(R.layout.parentdialog, null);
		this.context = context;
		this.onComfirmClickListener = onComfirmClickListener;
		this.onCancelClickListener = onCancelClickListener;
		setTitle(R.string.parentdialog_title);
	}
	
	@Override
	public void setContentView(View view) {
		FrameLayout parentdialog_content_FrameLayout = (FrameLayout) parentView.findViewById(R.id.parentdialog_content_FrameLayout);
		parentdialog_content_FrameLayout.addView(view);
		Button cancel = (Button) parentView.findViewById(R.id.parentdialog_cancel_Button);
		Button confirm = (Button) parentView.findViewById(R.id.parentdialog_confirm_Button);
		
		if (onComfirmClickListener != null) {
			confirm.setOnClickListener(onComfirmClickListener);
		} 
		if (onCancelClickListener != null) {
			cancel.setOnClickListener(onCancelClickListener);
		} 
		super.setContentView(parentView);
	}

	@Override
	public void setTitle(CharSequence title) {
		TextView textView = (TextView) parentView.findViewById(R.id.parentdialog_title_TextView);
		textView.setText(title);
	}

	@Override
	public void setTitle(int titleId) {
		TextView textView = (TextView) parentView.findViewById(R.id.parentdialog_title_TextView);
		textView.setText(titleId);
	}
	
	public void setIcon(int iconId) {
		TextView textView = (TextView) parentView.findViewById(R.id.parentdialog_title_TextView);
		textView.setCompoundDrawables(context.getResources().getDrawable(iconId), null, null, null);
	}
	

}
