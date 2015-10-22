package com.moor.im.ui.adapter;

import java.util.List;

import com.moor.im.R;
import com.moor.im.model.entity.ChatEmoji;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * 
 * @author Mr.li 表情填充器
 */
public class FaceAdapter extends BaseAdapter {

	private List<ChatEmoji> data;

	private LayoutInflater inflater;

	private int size = 0;

	private ViewHolder viewHolder = null;

	private Context context;

	public FaceAdapter(Context context, List<ChatEmoji> list) {
		this.inflater = LayoutInflater.from(context);
		this.data = list;
		this.size = list.size();
		this.context = context;
	}

	@Override
	public int getCount() {
		return this.size;
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ChatEmoji emoji = data.get(position);
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(R.layout.viewpager_item_face, null);
			viewHolder.mIvFace = (ImageView) convertView
					.findViewById(R.id.item_iv_face);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if (emoji.getId() == R.drawable.face_del_icon) {
			convertView.setBackgroundDrawable(null);
			viewHolder.mIvFace.setImageResource(emoji.getId());
		} else if (TextUtils.isEmpty(emoji.getCharacter())) {
			convertView.setBackgroundDrawable(null);
			viewHolder.mIvFace.setImageDrawable(null);
		} else {
			viewHolder.mIvFace.setTag(emoji);
			viewHolder.mIvFace.setImageResource(emoji.getId());
		}

		return convertView;
	}

	class ViewHolder {
		public ImageView mIvFace;
	}
}