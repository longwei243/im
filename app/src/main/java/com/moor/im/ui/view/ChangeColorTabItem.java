package com.moor.im.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.moor.im.R;

/**
 * 底部颜色跟随滑动变化的tab
 * @author LongWei
 *
 */
public class ChangeColorTabItem extends View{
	private Bitmap mBitmap;
	private Canvas mCanvas;
	private Paint mPaint;
	/**
	 * 图标颜色
	 */
	private int mColor = 0x21b38a;
	/**
	 * 透明度
	 */
	private float mAlpha = 0f;
	/**
	 * 图标图片
	 */
	private Bitmap mIconBitmap;
	/**
	 * 图标区域
	 */
	private Rect mIconRect;
	/**
	 * 图标下的文字
	 */
	private String mText = "图标文字";
	/**
	 * 文字大小
	 */
	private int mTextSize = (int) TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics());
	/**
	 * 文字画笔
	 */
	private Paint mTextPaint;
	/**
	 * 文字的区域
	 */
	private Rect mTextBound = new Rect();

	public ChangeColorTabItem(Context context)
	{
		super(context);
	}

	/**
	 * 构造方法，初始化
	 * 
	 * @param context
	 * @param attrs
	 */
	public ChangeColorTabItem(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		// 获得自定义属性
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ChangeColorIconView);

		int n = a.getIndexCount();
		for (int i = 0; i < n; i++)
		{

			int attr = a.getIndex(i);
			switch (attr)
			{
			case R.styleable.ChangeColorIconView_tabicon:
				BitmapDrawable drawable = (BitmapDrawable) a.getDrawable(attr);
				mIconBitmap = drawable.getBitmap();
				break;
			case R.styleable.ChangeColorIconView_color:
				mColor = a.getColor(attr, 0x21b38a);
				break;
			case R.styleable.ChangeColorIconView_text:
				mText = a.getString(attr);
				break;
			case R.styleable.ChangeColorIconView_text_size:
				mTextSize = (int) a.getDimension(attr, TypedValue
						.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10,
								getResources().getDisplayMetrics()));
				break;

			}
		}

		a.recycle();

		mTextPaint = new Paint();
		mTextPaint.setTextSize(mTextSize);
		mTextPaint.setColor(Color.parseColor("#7e7f81"));
		mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBound);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// 图片宽度取最小的
		int bitmapWidth = Math.min(getMeasuredWidth() - getPaddingLeft()
				- getPaddingRight(), getMeasuredHeight() - getPaddingTop()
				- getPaddingBottom() - mTextBound.height());

		int left = getMeasuredWidth() / 2 - bitmapWidth / 2;
		int top = (getMeasuredHeight() - mTextBound.height()) / 2 - bitmapWidth
				/ 2;
		// 
		mIconRect = new Rect(left, top, left + bitmapWidth, top + bitmapWidth);

	}

	@Override
	protected void onDraw(Canvas canvas)
	{

		int alpha = (int) Math.ceil((255 * mAlpha));
		canvas.drawBitmap(mIconBitmap, null, mIconRect, null);
		setupTargetBitmap(alpha);
		drawSourceText(canvas, alpha);
		drawTargetText(canvas, alpha);
		canvas.drawBitmap(mBitmap, 0, 0, null);

	}
	
	private void setupTargetBitmap(int alpha)
	{
		mBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(),
				Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		mPaint = new Paint();
		mPaint.setColor(mColor);
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setAlpha(alpha);
		mCanvas.drawRect(mIconRect, mPaint);
		mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		mPaint.setAlpha(255);
		mCanvas.drawBitmap(mIconBitmap, null, mIconRect, mPaint);
	}

	private void drawSourceText(Canvas canvas, int alpha)
	{
		mTextPaint.setTextSize(mTextSize);
		mTextPaint.setColor(0xff7e7f81);
		mTextPaint.setAlpha(255 - alpha);
		canvas.drawText(mText, mIconRect.left + mIconRect.width() / 2
				- mTextBound.width() / 2,
				mIconRect.bottom + mTextBound.height(), mTextPaint);
	}
	
	private void drawTargetText(Canvas canvas, int alpha)
	{
		mTextPaint.setColor(mColor);
		mTextPaint.setAlpha(alpha);
		canvas.drawText(mText, mIconRect.left + mIconRect.width() / 2
				- mTextBound.width() / 2,
				mIconRect.bottom + mTextBound.height(), mTextPaint);
		
	}

	public void setIconAlpha(float alpha)
	{
		this.mAlpha = alpha;
		invalidateView();
	}

	private void invalidateView()
	{
		if (Looper.getMainLooper() == Looper.myLooper())
		{
			invalidate();
		} else
		{
			postInvalidate();
		}
	}

	public void setIconColor(int color)
	{
		mColor = color;
	}

	public void setIcon(int resId)
	{
		this.mIconBitmap = BitmapFactory.decodeResource(getResources(), resId);
		if (mIconRect != null)
			invalidateView();
	}

	public void setIcon(Bitmap iconBitmap)
	{
		this.mIconBitmap = iconBitmap;
		if (mIconRect != null)
			invalidateView();
	}

	private static final String INSTANCE_STATE = "instance_state";
	private static final String STATE_ALPHA = "state_alpha";

	@Override
	protected Parcelable onSaveInstanceState()
	{
		Bundle bundle = new Bundle();
		bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
		bundle.putFloat(STATE_ALPHA, mAlpha);
		return bundle;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state)
	{
		if (state instanceof Bundle)
		{
			Bundle bundle = (Bundle) state;
			mAlpha = bundle.getFloat(STATE_ALPHA);
			super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
		} else
		{
			super.onRestoreInstanceState(state);
		}

	}

}
