package com.moor.im.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.moor.im.R;

/**
 * listview右侧字母列表索引
 * @author LongWei
 *
 */
public class SideBar extends View{
	/**
	 * 当字母被触摸时的回调接口
	 */
	private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
	
	public static String[] b = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
			"J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
			"W", "X", "Y", "Z", "#" };
	//选中的位置
	private int choose = 0;
	private Paint paint = new Paint();

	private TextView mTextDialog;
	
	private Resources resources = getResources();

	public void setTextView(TextView mTextDialog) {
		this.mTextDialog = mTextDialog;
	}


	public SideBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SideBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SideBar(Context context) {
		super(context);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		int height = getHeight(); 
		int width = getWidth(); 
		int singleHeight = height / b.length;//获得每个字母的高度
		for (int i = 0; i < b.length; i++) {
			paint.setTypeface(Typeface.DEFAULT);
			paint.setAntiAlias(true);
			paint.setTextSize(resources.getDimension(R.dimen.sidebar_lettersize));
			paint.setColor(resources.getColor(R.color.sidebar_letter_normal));
			if (i == choose) {
				paint.setColor(resources.getColor(R.color.sidebar_letter_pressed));
				paint.setFakeBoldText(false);
				
				float xPos = width / 2 - paint.measureText(b[i]) / 2;
				float yPos = singleHeight * i + singleHeight;
				canvas.drawText(b[i], xPos, yPos, paint);
				paint.reset();
				continue;
			}else{
			
			float xPos = width / 2 - paint.measureText(b[i]) / 2;
			float yPos = singleHeight * i + singleHeight;
			canvas.drawText(b[i], xPos, yPos, paint);
			paint.reset();
			}
		}

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final float y = event.getY();//
		final int oldChoose = choose;
		final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
		final int c = (int) (y / getHeight() * b.length);//

		switch (action) {
		case MotionEvent.ACTION_UP:
			setBackgroundDrawable(new ColorDrawable(0x00000000));
			choose = c;
			if (mTextDialog != null && mTextDialog.getVisibility() == View.VISIBLE) {
				mTextDialog.setVisibility(View.INVISIBLE);
			}
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			if (oldChoose != c) {
				if (c >= 0 && c < b.length) {
					if (listener != null) {
						listener.onTouchingLetterChanged(b[c]);
					}
					if (mTextDialog != null) {
						mTextDialog.setText(b[c]);
						mTextDialog.setVisibility(View.VISIBLE);
					}
					choose = c;
					invalidate();
				}
			}
			break;
		default:
			if (mTextDialog != null && mTextDialog.getVisibility() == View.VISIBLE) {
				mTextDialog.setVisibility(View.INVISIBLE);
			}
			break;
		}
		return true;
	}

	
	public void setOnTouchingLetterChangedListener(
			OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
		this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
	}

	
	public interface OnTouchingLetterChangedListener {
		public void onTouchingLetterChanged(String s);
	}

	
	public void setBar(String letter) {
		int amount = b.length;
		for (int i = 0; i < amount; i++) {
			if (letter.equals(b[i])) {
				choose = i;
			}
		}
		invalidate();
	}

}
