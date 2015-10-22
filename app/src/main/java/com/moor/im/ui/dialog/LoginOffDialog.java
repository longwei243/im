package com.moor.im.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.moor.im.R;
/**
 * 注销对话框
 * @author LongWei
 *
 */
public class LoginOffDialog extends ParentDialog{
	
	private Context context;
	private static LoginOffDialog dialog;
	
	public LoginOffDialog(Context context, android.view.View.OnClickListener onComfirmClickListener) {
		super(context, onComfirmClickListener, onCancelClickListener);
		this.context = context;
		setContentView();
		dialog = this;
		
		
	}
	
	public void setContentView() {
		View view = LayoutInflater.from(context).inflate(
				R.layout.dialog_loginoff, null);
		super.setContentView(view);
	}

	private static android.view.View.OnClickListener onCancelClickListener = new android.view.View.OnClickListener() {
		
		@Override
		public void onClick(View view) {
			dialog.cancel();
		}
	};
	


}
