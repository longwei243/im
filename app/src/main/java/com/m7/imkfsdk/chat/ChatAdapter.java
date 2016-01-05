package com.m7.imkfsdk.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.m7.imkfsdk.recordbutton.MediaManager;
import com.m7.imkfsdk.utils.FaceConversionUtil;
import com.moor.im.R;
import com.moor.im.ui.activity.ImageViewLookActivity;
import com.moor.imkf.ChatListener;
import com.moor.imkf.IMChat;
import com.moor.imkf.model.entity.FromToMessage;
import com.moor.imkf.utils.AnimatedGifDrawable;
import com.moor.imkf.utils.AnimatedImageSpan;
import com.moor.imkf.utils.LogUtil;
import com.moor.imkf.utils.TimeUtil;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 聊天的适配器
 * 
 * @author LongWei
 * 
 */
public class ChatAdapter extends MyBaseAdapter {
	private Context context;
	private List<FromToMessage> messageList;
	private ViewHolder holder;

	private int mMinRecordLength;
	private int mMaxRecordLength;

	View chat_to_recorder_anim;
	View chat_from_recorder_anim;

	Handler handler;

	public ChatAdapter(Context context, Handler handler) {
		super(context);
		this.context = context;
		this.handler = handler;

		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics );
		mMinRecordLength = (int) (outMetrics.widthPixels * 0.25f);
		mMaxRecordLength = (int) (outMetrics.widthPixels * 0.7f);
	}

	/* (non-Javadoc)
	 * @see ui.base.MyBaseAdapter#getMyView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getMyView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.list_chat_kf, null);

			// 两侧的头像
			holder.fromIcon = (RoundImageView) convertView
					.findViewById(R.id.chatfrom_icon);
			holder.toIcon = (RoundImageView) convertView
					.findViewById(R.id.chatto_icon);

			// 两侧的线性布局
			holder.fromContainer = (ViewGroup) convertView
					.findViewById(R.id.chart_from_container);
			holder.toContainer = (ViewGroup) convertView
					.findViewById(R.id.chart_to_container);

			// 两侧的对话信息
			holder.fromContent = (TextView) convertView
					.findViewById(R.id.chatfrom_content);
			holder.toContent = (TextView) convertView
					.findViewById(R.id.chatto_content);

			holder.time = (TextView) convertView.findViewById(R.id.chat_time);// 时间

			holder.chat_to_text_layout = (FrameLayout) convertView.findViewById(R.id.chat_to_text_layout);

			holder.chat_to_recorder_length = (RelativeLayout) convertView.findViewById(R.id.chat_to_recorder_length);
			holder.chat_to_recorder_time = (TextView) convertView.findViewById(R.id.chat_to_recorder_time);
			holder.chat_from_recorder_length = (RelativeLayout) convertView.findViewById(R.id.chat_from_recorder_length);
			holder.chat_from_recorder_time = (TextView) convertView.findViewById(R.id.chat_from_recorder_time);

			holder.chat_to_layout_img = (FrameLayout) convertView.findViewById(R.id.chat_to_layout_img);
			holder.chat_to_iv_img = (ImageView) convertView.findViewById(R.id.chat_to_iv_img);

			holder.chat_from_layout_img = (FrameLayout) convertView.findViewById(R.id.chat_from_layout_img);
			holder.chat_from_iv_img = (ImageView) convertView.findViewById(R.id.chat_from_iv_img);

			holder.chatfrom_tv_name = (TextView) convertView.findViewById(R.id.chatfrom_tv_name);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		messageList = getAdapterData();
		final FromToMessage message = messageList.get(position);

		//根据时间戳来进行时间的显示
		boolean showTimer = false;
		if(position == 0) {
			showTimer = true;
		}
		if(position != 0) {
			FromToMessage previousItem = (FromToMessage)getItem(position - 1);
			if((message.when - previousItem.when >= 180000L)) {
				showTimer = true;

			}
		}


		if(showTimer) {
			holder.time.setVisibility(View.VISIBLE);
			holder.time.setText(TimeUtil.convertTimeToFriendlyForChat(message.when));
		}else {
			holder.time.setVisibility(View.GONE);
		}

		// 发出的信息
		if ("0".equals(message.userType)) {
			holder.toContainer.setVisibility(View.VISIBLE);
			holder.fromContainer.setVisibility(View.GONE);

			//自己的
			Glide.with(context).load(R.drawable.head_default_local).asBitmap().into(holder.toIcon);

			if(FromToMessage.MSG_TYPE_TEXT.equals(message.msgType)) {

				holder.chat_to_text_layout.setVisibility(View.VISIBLE);

				holder.chat_to_recorder_length.setVisibility(View.GONE);

				holder.chat_to_layout_img.setVisibility(View.GONE);
				//文本消息
				// 对内容做处理
				SpannableStringBuilder content = handler(holder.toContent,
						message.message);
				SpannableString spannableString = FaceConversionUtil.getInstace()
						.getExpressionString(context, content + "");
				holder.toContent.setText(spannableString);// 给对话内容赋值

				final ImageView failureMsgs = (ImageView) convertView
						.findViewById(R.id.failure_msgs);
				final ProgressBar progressBar = (ProgressBar) convertView
						.findViewById(R.id.progressBar);
				progressBar.setVisibility(View.VISIBLE);
				if ("true".equals(message.sendState)) {
					failureMsgs.setVisibility(View.GONE);
					progressBar.setVisibility(View.GONE);
				} else if ("false".equals(message.sendState)) {
					progressBar.setVisibility(View.GONE);
					failureMsgs.setVisibility(View.VISIBLE);
					failureMsgs.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							System.out.println("点击了重发消息");
							progressBar.setVisibility(View.VISIBLE);
							failureMsgs.setVisibility(View.GONE);
							reSendMsgToNet(message);

						}
					});
				}
			}else if(FromToMessage.MSG_TYPE_AUDIO.equals(message.msgType)) {
				//录音
				holder.chat_to_text_layout.setVisibility(View.GONE);

				holder.chat_to_recorder_length.setVisibility(View.VISIBLE);

				holder.chat_to_layout_img.setVisibility(View.GONE);

				holder.chat_to_recorder_time.setText(Math.round(message.recordTime) + "\"");
				LayoutParams lp = holder.chat_to_recorder_length.getLayoutParams();
				lp.width = (int) (mMinRecordLength + (mMaxRecordLength / 60 * message.recordTime));

				holder.chat_to_recorder_length.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if(chat_to_recorder_anim != null) {
							chat_to_recorder_anim.setBackgroundResource(R.drawable.adj);
							chat_to_recorder_anim = null;
						}
						//播放动画
						chat_to_recorder_anim = v.findViewById(R.id.chat_to_recorder_anim);
						chat_to_recorder_anim.setBackgroundResource(R.drawable.recorder_play_anim);
						AnimationDrawable anim = (AnimationDrawable) chat_to_recorder_anim.getBackground();
						anim.start();
						//播放声音
						System.out.println("adapter中的message.filePath是:"+message.filePath);
						MediaManager.playSound(message.filePath, new MediaPlayer.OnCompletionListener() {

							@Override
							public void onCompletion(MediaPlayer mp) {
								chat_to_recorder_anim.setBackgroundResource(R.drawable.adj);

							}
						});
					}
				});

				final ImageView failureMsgs = (ImageView) convertView
						.findViewById(R.id.failure_msgs);
				final ProgressBar progressBar = (ProgressBar) convertView
						.findViewById(R.id.progressBar);
				progressBar.setVisibility(View.VISIBLE);
				if ("true".equals(message.sendState)) {
					failureMsgs.setVisibility(View.GONE);
					progressBar.setVisibility(View.GONE);
				} else if ("false".equals(message.sendState)) {
					progressBar.setVisibility(View.GONE);
					failureMsgs.setVisibility(View.VISIBLE);

					final String messageStr = message.message;
					failureMsgs.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							System.out.println("点击了重发消息");
							if(!"".equals(messageStr)) {
								failureMsgs.setVisibility(View.GONE);
							}else {
								reSendMsgToNet(message);
							}
						}
					});
				}


			}else if(FromToMessage.MSG_TYPE_IMAGE.equals(message.msgType)) {
				//发送的图片

				holder.chat_to_text_layout.setVisibility(View.GONE);

				holder.chat_to_recorder_length.setVisibility(View.GONE);

				holder.chat_to_layout_img.setVisibility(View.VISIBLE);

					Glide.with(context).load(message.filePath)
							.centerCrop()
							.crossFade()
							.into(holder.chat_to_iv_img);

					holder.chat_to_layout_img.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							//点击查看原图

						}
					});

					final ImageView failureMsgs = (ImageView) convertView
							.findViewById(R.id.failure_msgs);
					final ProgressBar progressBar = (ProgressBar) convertView
							.findViewById(R.id.progressBar);
					progressBar.setVisibility(View.VISIBLE);
					if ("true".equals(message.sendState)) {
						failureMsgs.setVisibility(View.GONE);
						progressBar.setVisibility(View.GONE);

					} else if ("false".equals(message.sendState)) {
						progressBar.setVisibility(View.GONE);
						failureMsgs.setVisibility(View.VISIBLE);

						final String messageStr = message.message;
						failureMsgs.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								System.out.println("点击了重发消息");
								if(!"".equals(messageStr)) {
									failureMsgs.setVisibility(View.GONE);
								}else {
									//重新发送
									reSendMsgToNet(message);
								}
							}
						});
					}

				}

		} else if("1".equals(message.userType)) {// 接收的消息
			// 收到消息 from显示
			holder.toContainer.setVisibility(View.GONE);
			holder.fromContainer.setVisibility(View.VISIBLE);
			Glide.with(context).load(R.drawable.head_default_robot).asBitmap().into(holder.fromIcon);

			if (FromToMessage.MSG_TYPE_TEXT.equals(message.msgType)) {
				//文本消息
				holder.fromContent.setVisibility(View.VISIBLE);
				holder.chat_from_recorder_length.setVisibility(View.GONE);

				holder.chat_from_layout_img.setVisibility(View.GONE);
				// 对内容做处理
				if(message.showHtml) {
					// 给对话内容赋值
					URLImageParser p = new URLImageParser(holder.fromContent, context);
					holder.fromContent.setText(Html.fromHtml(message.message, p ,null));
					holder.fromContent.setMovementMethod(LinkMovementMethod.getInstance());

				}else {
					SpannableStringBuilder content = handler(holder.fromContent,
							message.message);
					SpannableString spannableString = FaceConversionUtil.getInstace()
							.getExpressionString(context, content + "");
					holder.fromContent.setText(spannableString);// 给对话内容赋值
				}

			} else if (FromToMessage.MSG_TYPE_AUDIO.equals(message.msgType)) {
				//接收到录音


			} else if (FromToMessage.MSG_TYPE_IMAGE.equals(message.msgType)) {
				//接受到图片
				holder.fromContent.setVisibility(View.GONE);
				holder.chat_from_recorder_length.setVisibility(View.GONE);

				holder.chat_from_layout_img.setVisibility(View.VISIBLE);

				Glide.with(context).load(message.message+"?imageView2/0/w/200/h/140")
						.centerCrop()
						.crossFade()
						.placeholder(R.drawable.pic_thumb_bg)
						.error(R.drawable.image_download_fail_icon)
						.into(holder.chat_from_iv_img);

				holder.chat_from_layout_img.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						//点击查看原图
						Intent intent = new Intent(context, ImageViewLookActivity.class);
						intent.putExtra("imagePath", message.message);
						context.startActivity(intent);
					}
				});
			}
		}

		return convertView;
	}





	private SpannableStringBuilder handler(final TextView gifTextView,
			String content) {
		SpannableStringBuilder sb = new SpannableStringBuilder(content);
		String regex = "(\\#\\[face/png/f_static_)\\d{3}(.png\\]\\#)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(content);
		while (m.find()) {
			String tempText = m.group();
			try {
				String num = tempText.substring(
						"#[face/png/f_static_".length(), tempText.length()
								- ".png]#".length());
				String gif = "face/gif/f" + num + ".gif";
				/**
				 * 如果open这里不抛异常说明存在gif，则显示对应的gif 否则说明gif找不到，则显示png
				 * */
				InputStream is = context.getAssets().open(gif);
				sb.setSpan(new AnimatedImageSpan(new AnimatedGifDrawable(is,
						new AnimatedGifDrawable.UpdateListener() {
							@Override
							public void update() {
								gifTextView.postInvalidate();
							}
						})), m.start(), m.end(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				is.close();
			} catch (Exception e) {
				String png = tempText.substring("#[".length(),
						tempText.length() - "]#".length());
				try {
					sb.setSpan(
							new ImageSpan(context,
									BitmapFactory.decodeStream(context
											.getAssets().open(png))),
							m.start(), m.end(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
		return sb;
	}

	static class ViewHolder {
		RoundImageView fromIcon, toIcon;// 头像
		TextView fromContent, toContent, time;// 内容，时间
		ViewGroup fromContainer, toContainer;// 线性布局
		
		FrameLayout chat_to_text_layout;
		
		RelativeLayout chat_to_recorder_length;
		TextView chat_to_recorder_time;
		RelativeLayout chat_from_recorder_length;
		TextView chat_from_recorder_time;

		FrameLayout chat_to_layout_img;
		ImageView chat_to_iv_img;
		FrameLayout chat_from_layout_img;
		ImageView chat_from_iv_img;

		TextView chatfrom_tv_name;

	}

	/**
	 * 屏蔽listitem的所有事件
	 * */
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	
	private void reSendMsgToNet(FromToMessage fromToMessage) {
		IMChat.getInstance().reSendMessage(fromToMessage, new ChatListener() {
			@Override
			public void onSuccess() {
				handler.sendEmptyMessage(0x88);
			}

			@Override
			public void onFailed() {
				handler.sendEmptyMessage(0x88);
			}

			@Override
			public void onProcess() {

			}
		});
	}



	public class URLImageParser implements Html.ImageGetter {
		Context c;
		View container;

		/***
		 * Construct the URLImageParser which will execute AsyncTask and refresh the container
		 * @param t
		 * @param c
		 */
		public URLImageParser(View t, Context c) {
			this.c = c;
			this.container = t;
		}

		public Drawable getDrawable(String source) {
			URLDrawable urlDrawable = new URLDrawable();

			// get the actual source
			ImageGetterAsyncTask asyncTask =
					new ImageGetterAsyncTask( urlDrawable);

			asyncTask.execute(source);

			// return reference to URLDrawable where I will change with actual image from
			// the src tag
			return urlDrawable;
		}

		public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
			URLDrawable urlDrawable;

			public ImageGetterAsyncTask(URLDrawable d) {
				this.urlDrawable = d;
			}

			@Override
			protected Drawable doInBackground(String... params) {
				String source = params[0];
				return fetchDrawable(source);
			}

			@Override
			protected void onPostExecute(Drawable result) {
				// set the correct bound according to the result from HTTP call
				urlDrawable.setBounds(0, 0, result.getIntrinsicWidth(), result.getIntrinsicHeight());

				// change the reference of the current drawable to the result
				// from the HTTP call
				urlDrawable.drawable = result;

				// redraw the image by invalidating the container
				URLImageParser.this.container.invalidate();
			}

			/***
			 * Get the Drawable from URL
			 * @param urlString
			 * @return
			 */
			public Drawable fetchDrawable(String urlString) {
				try {
					InputStream is = fetch(urlString);
					Drawable drawable = Drawable.createFromStream(is, "src");
					drawable.setBounds(0, 0, 25 + drawable.getIntrinsicWidth(), 25
							+ drawable.getIntrinsicHeight());
					return drawable;
				} catch (Exception e) {
					return null;
				}
			}

			private InputStream fetch(String urlString) throws MalformedURLException, IOException {
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpGet request = new HttpGet(urlString);
				HttpResponse response = httpClient.execute(request);
				return response.getEntity().getContent();
			}
		}
	}

	public class URLDrawable extends BitmapDrawable {
		// the drawable that you need to set, you could set the initial drawing
		// with the loading image if you need to
		protected Drawable drawable;

		@Override
		public void draw(Canvas canvas) {
			// override the draw to facilitate refresh function later
			if(drawable != null) {
				drawable.draw(canvas);
			}
		}
	}
}
