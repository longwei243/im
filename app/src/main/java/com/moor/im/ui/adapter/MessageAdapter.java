package com.moor.im.ui.adapter;

import java.util.List;

import com.bumptech.glide.Glide;
import com.csipsimple.utils.Log;
import com.moor.im.R;
import com.moor.im.db.dao.ContactsDao;
import com.moor.im.db.dao.MessageDao;
import com.moor.im.db.dao.NewMessageDao;
import com.moor.im.model.entity.Contacts;
import com.moor.im.model.entity.Discussion;
import com.moor.im.model.entity.NewMessage;
import com.moor.im.model.parser.DiscussionParser;
import com.moor.im.model.parser.GroupParser;
import com.moor.im.ui.base.MyBaseAdapter;
import com.moor.im.ui.view.RoundImageView;
import com.moor.im.utils.LogUtil;
import com.moor.im.utils.TimeUtil;
import com.moor.im.utils.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 消息页面适配器
 * 
 * @author Mr.li
 * 
 */

public class MessageAdapter extends MyBaseAdapter {
	private ViewHolder holder;
	private Context context;
	private List<NewMessage> messagelist;
	private NewMessage message;

	public MessageAdapter(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.context = context;

	}

	@Override
	public View getMyView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		if (convertView == null) {// 判断convertView是否为空
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.fragment_message_list_item,
					null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView
					.findViewById(R.id.message_people_name);
			holder.time = (TextView) convertView
					.findViewById(R.id.message_time);
			holder.content = (TextView) convertView
					.findViewById(R.id.message_content);
			holder.img = (ImageView) convertView
					.findViewById(R.id.message_icon);
			holder.unreadcount = (TextView) convertView
					.findViewById(R.id.message_unreadcount);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		messagelist = getAdapterData();
		message = messagelist.get(position);

		if("User".equals(message.type)) {
			String im_icon = ContactsDao.getInstance().getContactsIcon(message.from);
			if(!"".equals(im_icon) && im_icon != null) {
				Glide.with(context).load(im_icon+"?imageView2/0/w/100/h/100").asBitmap().placeholder(R.drawable.head_default_local).into(holder.img);
			}else {
				Glide.with(context).load(R.drawable.head_default_local).asBitmap().into(holder.img);
			}
		}else if("Group".equals(message.type)) {
			Glide.with(context).load(R.drawable.ic_addfriend_group).asBitmap().into(holder.img);
		}else if("Discussion".equals(message.type)) {
			Glide.with(context).load(R.drawable.ic_addfriend_discuss).asBitmap().into(holder.img);
		}else if("System".equals(message.type)) {
			Glide.with(context).load(R.drawable.ic_launcher).asBitmap().into(holder.img);
		}




		// 半角符转全角符
		String content = Utils.ToDBC(message.message);

		if("User".equals(message.type)) {
			if(message.fromName != null && !"".equals(message.fromName)) {
				holder.name.setText(message.fromName);
			}
			if("0".equals(message.msgType)) {
				holder.content.setText(content);
			}else if("2".equals(message.msgType)) {
				holder.content.setText("[语音]");
			}else if("1".equals(message.msgType)) {
				holder.content.setText("[图片]");
			}
		}else if("Group".equals(message.type)) {
			if(message.fromName != null && !"".equals(message.fromName)) {
				holder.name.setText(message.fromName);
			}else {
				if(message.sessionId != null && !"".equals(message.sessionId)) {
					holder.name.setText(GroupParser.getInstance().getNameById(message.sessionId));
				}

			}
			String name = "";
			if(ContactsDao.getInstance().getContactsName(message.from) != null) {
				name = ContactsDao.getInstance().getContactsName(message.from);
			}
			if("0".equals(message.msgType)) {
				if(!"".equals(name)) {
					holder.content.setText(name+":"+content);
				}else {
					holder.content.setText(content);
				}

			}else if("2".equals(message.msgType)) {
				if(!"".equals(name)) {
					holder.content.setText(name+":"+"[语音]");
				}else {
					holder.content.setText("[语音]");
				}
			}else if("1".equals(message.msgType)) {
				if(!"".equals(name)) {
					holder.content.setText(name+":"+"[图片]");
				}else {
					holder.content.setText("[图片]");
				}
			}
		}else if("Discussion".equals(message.type)) {
			if(message.fromName != null && !"".equals(message.fromName)) {
				holder.name.setText(message.fromName);
			}else {
				if(message.sessionId != null && !"".equals(message.sessionId)) {
					holder.name.setText(DiscussionParser.getInstance().getNameById(message.sessionId));
				}

			}
			String name = "";
			if(ContactsDao.getInstance().getContactsName(message.from) != null) {
				name = ContactsDao.getInstance().getContactsName(message.from);
			}
			if("0".equals(message.msgType)) {
				if(!"".equals(name)) {
					holder.content.setText(name+":"+content);
				}else {
					holder.content.setText(content);
				}

			}else if("2".equals(message.msgType)) {
				if(!"".equals(name)) {
					holder.content.setText(name+":"+"[语音]");
				}else {
					holder.content.setText("[语音]");
				}
			}else if("1".equals(message.msgType)) {
				if(!"".equals(name)) {
					holder.content.setText(name+":"+"[图片]");
				}else {
					holder.content.setText("[图片]");
				}
			}
		}else if("System".equals(message.type)) {
			holder.name.setText("系统通知");
			holder.content.setText(content);
		}

		holder.time.setText(TimeUtil.convertTimeToFriendly(message.time));

		if (message.unReadCount == 0) {
			holder.unreadcount.setVisibility(View.GONE);
		} else if (message.unReadCount >= 99) {
			holder.unreadcount.setVisibility(View.VISIBLE);
			holder.unreadcount.setText(99 + "");
		} else {
			holder.unreadcount.setVisibility(View.VISIBLE);
			holder.unreadcount.setText(message.unReadCount + "");
		}
		return convertView;
	}

	public class ViewHolder {

		public ImageView img;
		public TextView name;
		public TextView time;
		public TextView content;
		public TextView unreadcount;

	}

}
