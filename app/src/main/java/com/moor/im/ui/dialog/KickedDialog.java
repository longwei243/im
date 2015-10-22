package com.moor.im.ui.dialog;

import com.moor.im.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

/**
 * 被T了的对话框
 * @author LongWei
 *
 */
public class KickedDialog extends ParentDialog{

	private Context context;
	private static KickedDialog dialog;
	
	public KickedDialog(Context context, android.view.View.OnClickListener onComfirmClickListener, android.view.View.OnClickListener onCancelClickListener) {
		super(context, onComfirmClickListener, onCancelClickListener);
		this.context = context;
		setContentView();
		dialog = this;
	}
	
	public void setContentView() {
		View setting_netaddress = LayoutInflater.from(context).inflate(
				R.layout.dialog_kicked, null);
		super.setContentView(setting_netaddress);
	}

}
