package com.moor.im.ui.adapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.app.RequestUrl;
import com.moor.im.db.dao.ContactsDao;
import com.moor.im.db.dao.MessageDao;
import com.moor.im.db.dao.UserDao;
import com.moor.im.http.HttpManager;
import com.moor.im.model.entity.Contacts;
import com.moor.im.model.entity.FromToMessage;
import com.moor.im.model.entity.User;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.ui.activity.ImageViewLookActivity;
import com.moor.im.ui.base.MyBaseAdapter;
import com.moor.im.ui.view.RoundImageView;
import com.moor.im.ui.view.recordbutton.MediaManager;
import com.moor.im.ui.view.squareprogressview.SquareProgressView;
import com.moor.im.utils.AnimatedGifDrawable;
import com.moor.im.utils.AnimatedImageSpan;
import com.moor.im.utils.FaceConversionUtil;
import com.moor.im.utils.LogUtil;
import com.moor.im.utils.TimeUtil;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;


/**
 * 聊天的适配器
 * 
 * @author LongWei
 * 
 */
public class ChatAdapter extends MyBaseAdapter {
	private Context context;// 上下文
	private List<FromToMessage> messageList;// 聊天信息的集合
	private ViewHodler holder;
	private SharedPreferences sp;
	
	private int mMinRecordLength;
	private int mMaxRecordLength;
	
	View chat_to_recorder_anim;
	View chat_from_recorder_anim;
	
	Handler handler;

	private String im_icon;

	private User user = UserDao.getInstance().getUser();

	public ChatAdapter(Context context, Handler handler, String im_icon) {
		super(context);
		this.context = context;
		this.handler = handler;
		this.im_icon = im_icon;
		sp = context.getSharedPreferences("SP", 4);
		
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics );
		mMinRecordLength = (int) (outMetrics.widthPixels * 0.25f);
		mMaxRecordLength = (int) (outMetrics.widthPixels * 0.7f);
	}

	/* (non-Javadoc)
	 * @see com.moor.im.ui.base.MyBaseAdapter#getMyView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getMyView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		if (convertView == null) {
			holder = new ViewHodler();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.list_chat, null);

			// 两侧的头像
			holder.fromIcon = (ImageView) convertView
					.findViewById(R.id.chatfrom_icon);
			holder.toIcon = (ImageView) convertView
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
			holder = (ViewHodler) convertView.getTag();
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
			// 发送消息 to显示
			holder.toContainer.setVisibility(View.VISIBLE);
			holder.fromContainer.setVisibility(View.GONE);

			//自己的
			if(user.im_icon != null && !"".equals(user.im_icon)) {
				Glide.with(context).load(user.im_icon+"?imageView2/0/w/100/h/100").asBitmap().placeholder(R.drawable.head_default_local).into(holder.toIcon);
			}else {
				Glide.with(context).load(R.drawable.head_default_local).asBitmap().into(holder.toIcon);
			}
			
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
							sendMsgToNet(message);
							
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
								sendMsgToNet(message);
								failureMsgs.setVisibility(View.GONE);
							}else {
								//重新上传7牛
								HttpManager.getQiNiuToken(sp.getString("connecTionId", ""),
										message.filePath, new UploadFileResponseHandler("ly", message));
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
							Intent intent = new Intent(context, ImageViewLookActivity.class);
							intent.putExtra("imagePath", message.filePath);
							context.startActivity(intent);
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
									sendMsgToNet(message);
									failureMsgs.setVisibility(View.GONE);
								}else {
									//重新上传7牛
									HttpManager.getQiNiuToken(sp.getString("connecTionId", ""),
											message.filePath, new UploadFileResponseHandler("img", message));
								}
							}
						});
					}

				}

		} else if("1".equals(message.userType)){// 接收的消息
			// 收到消息 from显示
			holder.toContainer.setVisibility(View.GONE);
			holder.fromContainer.setVisibility(View.VISIBLE);

			//对方的
			if("User".equals(message.type)) {
				if(!"".equals(im_icon)) {
					Glide.with(context).load(im_icon + "?imageView2/0/w/100/h/100").asBitmap().placeholder(R.drawable.head_default_local).into(holder.fromIcon);
				}else {
					Glide.with(context).load(R.drawable.head_default_local).asBitmap().into(holder.fromIcon);
				}
			}else if("Group".equals(message.type)) {

				if("System".equals(message.from)) {
					Glide.with(context).load(R.drawable.head_default_local).asBitmap().into(holder.fromIcon);
				}else {
					if(message.from != null && !"".equals(message.from)) {
						holder.chatfrom_tv_name.setVisibility(View.VISIBLE);
						holder.chatfrom_tv_name.setText(ContactsDao.getInstance().getContactsName(message.from));
						String imicon = ContactsDao.getInstance().getContactsIcon(message.from);
						if(!"".equals(imicon)) {
							Glide.with(context).load(imicon + "?imageView2/0/w/100/h/100").asBitmap().placeholder(R.drawable.head_default_local).into(holder.fromIcon);
						}else {
							Glide.with(context).load(R.drawable.head_default_local).asBitmap().into(holder.fromIcon);
						}
					}

				}
			}else if("Discussion".equals(message.type)) {
				if("System".equals(message.from)) {
					Glide.with(context).load(R.drawable.head_default_local).asBitmap().into(holder.fromIcon);
				}else {
					if(message.from != null && !"".equals(message.from)) {
						holder.chatfrom_tv_name.setVisibility(View.VISIBLE);
						holder.chatfrom_tv_name.setText(ContactsDao.getInstance().getContactsName(message.from));
						String imicon = ContactsDao.getInstance().getContactsIcon(message.from);
						if(!"".equals(imicon)) {
							Glide.with(context).load(imicon + "?imageView2/0/w/100/h/100").asBitmap().placeholder(R.drawable.head_default_local).into(holder.fromIcon);
						}else {
							Glide.with(context).load(R.drawable.head_default_local).asBitmap().into(holder.fromIcon);
						}
					}

				}
			}



			if(FromToMessage.MSG_TYPE_TEXT.equals(message.msgType)) {
				//文本消息
				holder.fromContent.setVisibility(View.VISIBLE);
				holder.chat_from_recorder_length.setVisibility(View.GONE);

				holder.chat_from_layout_img.setVisibility(View.GONE);
				// 对内容做处理
				SpannableStringBuilder content = handler(holder.fromContent,
						message.message);
				SpannableString spannableString = FaceConversionUtil.getInstace()
						.getExpressionString(context, content + "");
				holder.fromContent.setText(spannableString);// 给对话内容赋值
			}else if(FromToMessage.MSG_TYPE_AUDIO.equals(message.msgType)) {
				//接收到录音
				holder.fromContent.setVisibility(View.GONE);
				holder.chat_from_recorder_length.setVisibility(View.VISIBLE);

				holder.chat_from_layout_img.setVisibility(View.GONE);
				String url = message.message;
				if("".equals(message.filePath) || message.filePath == null) {
					AsyncHttpClient httpclient = MobileApplication.httpclient;
					final String dirStr = Environment.getExternalStorageDirectory() + File.separator + "m7/downloadfile/";
					
					File dir = new File(dirStr);
					if(!dir.exists()) {
						dir.mkdirs();
						
					}
					File file = new File(dir, "7moor_record_"+UUID.randomUUID()+".amr");
					
					if(file.exists()) {
						file.delete();
					}
					final String filePath = file.getAbsolutePath();
					LogUtil.d("接收到录音", "请求的下载地址是："+url);
					httpclient.get(url, new FileAsyncHttpResponseHandler(file) {
						
						@Override
						public void onSuccess(int statusCode, Header[] headers, File file) {
							System.out.println("下载文件成功了");
							//界面显示
							if(!"".equals(filePath)) {
								message.filePath = filePath;
								//更新数据库
								MessageDao.getInstance().updateMsgToDao(message);
							}else {
								message.filePath = "";
							}
							
							LogUtil.d("接收到录音", "保存到本地的文件路径是："+message.filePath);
							holder.fromContent.setVisibility(View.GONE);
							
							holder.chat_from_recorder_length.setVisibility(View.VISIBLE);
							holder.chat_from_recorder_time.setVisibility(View.VISIBLE);
							
							holder.chat_from_recorder_time.setText(message.voiceSecond + "\"");
							LayoutParams lp = holder.chat_from_recorder_length.getLayoutParams();
							lp.width = (int) (mMinRecordLength + (mMaxRecordLength / 60 * Integer.parseInt(message.voiceSecond )));
							
							holder.chat_from_recorder_length.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									if(chat_from_recorder_anim != null) {
										chat_from_recorder_anim.setBackgroundResource(R.drawable.adj_left);
										chat_from_recorder_anim = null;
									}
									//播放动画
									chat_from_recorder_anim = v.findViewById(R.id.chat_from_recorder_anim);
									chat_from_recorder_anim.setBackgroundResource(R.drawable.recorder_play_anim_left);
									AnimationDrawable anim = (AnimationDrawable) chat_from_recorder_anim.getBackground();
									anim.start();
									//播放声音
									System.out.println("adapter中的message.filePath是:"+message.filePath);
									MediaManager.playSound(message.filePath, new MediaPlayer.OnCompletionListener() {
										
										@Override
										public void onCompletion(MediaPlayer mp) {
											chat_from_recorder_anim.setBackgroundResource(R.drawable.adj_left);

										}
									});
								}
							});
							handler.sendEmptyMessage(0x88);
						}

						@Override
						public void onProgress(int bytesWritten, int totalSize) {
							// TODO Auto-generated method stub
							super.onProgress(bytesWritten, totalSize);
							System.out.println("当前进度是："+bytesWritten);
						}

						@Override
						public void onFailure(int statusCode, Header[] headers,
								Throwable throwable, File file) {
							System.out.println("下载文件失败了");
							message.filePath = "";
						}
					});  
					
					

				}else {
					//直接显示

					holder.chat_from_recorder_time.setText(message.voiceSecond + "\"");
					LayoutParams lp = holder.chat_from_recorder_length.getLayoutParams();
					lp.width = (int) (mMinRecordLength + (mMaxRecordLength / 60 * Integer.parseInt(message.voiceSecond )));
					
					holder.chat_from_recorder_length.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							if(chat_from_recorder_anim != null) {
								chat_from_recorder_anim.setBackgroundResource(R.drawable.adj_left);
								chat_from_recorder_anim = null;
							}
							//播放动画
							chat_from_recorder_anim = v.findViewById(R.id.chat_from_recorder_anim);
							chat_from_recorder_anim.setBackgroundResource(R.drawable.recorder_play_anim_left);
							AnimationDrawable anim = (AnimationDrawable) chat_from_recorder_anim.getBackground();
							anim.start();
							//播放声音
							System.out.println("adapter中的message.filePath是:"+message.filePath);
							MediaManager.playSound(message.filePath, new MediaPlayer.OnCompletionListener() {
								
								@Override
								public void onCompletion(MediaPlayer mp) {
									chat_from_recorder_anim.setBackgroundResource(R.drawable.adj_left);

								}
							});
						}
					});
				}
								
			}else if(FromToMessage.MSG_TYPE_IMAGE.equals(message.msgType)) {
				//接受到图片
				holder.fromContent.setVisibility(View.GONE);
				holder.chat_from_recorder_length.setVisibility(View.GONE);

				holder.chat_from_layout_img.setVisibility(View.VISIBLE);

				LogUtil.d("ChatAdapter", "加载网络图片URL是:"+message.message+"?imageView2/0/w/200/h/140");
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

	static class ViewHodler {
		ImageView fromIcon, toIcon;// 头像
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

	
	private void sendMsgToNet(FromToMessage fromToMessage) {
		HttpManager.newMsgToServer(sp.getString("connecTionId", ""),
				fromToMessage, new NewMessageResponseHandler(fromToMessage));
	}
	
	
	class NewMessageResponseHandler extends TextHttpResponseHandler {
		FromToMessage fromToMessage;
		public NewMessageResponseHandler(FromToMessage fromToMessage) {
			this.fromToMessage = fromToMessage;
		}
		
		@Override
		public void onFailure(int statusCode, Header[] headers,
				String responseString, Throwable throwable) {
			MessageDao.getInstance().updateFailedMsgToDao(fromToMessage);
			handler.sendEmptyMessage(0x88);
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
				String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			String message = HttpParser.getMessage(responseString);
			
			if ("true".equals(succeed)) {
				System.out.println("消息发送成功了");
				
				MessageDao.getInstance().updateSucceedMsgToDao(fromToMessage);
				handler.sendEmptyMessage(0x88);
			}
		}
	}
	
	class UploadFileResponseHandler extends TextHttpResponseHandler {
		String fileType = "";
		FromToMessage fromToMessage;

		public UploadFileResponseHandler(String fileType,
				FromToMessage fromToMessage) {
			this.fileType =  fileType;
			this.fromToMessage = fromToMessage;
		}

		@Override
		public void onFailure(int statusCode, Header[] headers,
				String responseString, Throwable throwable) {
//			Toast.makeText(ChatActivity.this, "上传7牛失败了", Toast.LENGTH_SHORT).show();;
			MessageDao.getInstance().updateFailedMsgToDao(fromToMessage);
			handler.sendEmptyMessage(0x88);
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
				String responseString) {
			// TODO Auto-generated method stub
			String succeed = HttpParser.getSucceed(responseString);
			String message = HttpParser.getMessage(responseString);
			if ("true".equals(succeed)) {
				String upToken = HttpParser.getUpToken(responseString);
				// qiniu SDK自带方法上传
				UploadManager uploadManager = new UploadManager();
				if ("img".equals(fileType)) {// 图片
//					final String imgFileKey = UUID.randomUUID().toString();
					String fileName = UUID.randomUUID().toString();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
					String date = sdf.format(new Date());
					final String imgFileKey = user.account+"/image/"+date + "/"+ System.currentTimeMillis()+"/"+fileName;

					uploadManager.put(fromToMessage.filePath, imgFileKey, upToken,
							new UpCompletionHandler() {
								@Override
								public void complete(String key,
													 ResponseInfo info, JSONObject response) {
									// TODO Auto-generated method stub
									System.out.println("上传图片成功了");
									System.out.println(key + "     " + info
											+ "      " + response);

									fromToMessage.message = RequestUrl.QiniuHttp + imgFileKey;
									System.out.println("图片在服务器上的位置是："+fromToMessage.message);
									MessageDao.getInstance().updateMsgToDao(fromToMessage);
									//发送新消息给服务器
									HttpManager.newMsgToServer(sp.getString("connecTionId", ""),
											fromToMessage, new NewMessageResponseHandler(fromToMessage));
								}
							}, null);
				} else if ("ly".equals(fileType)) {// 音频文件
					System.out.println("上传录音");
//					final String fileKey = UUID.randomUUID().toString();
					String fileName = UUID.randomUUID().toString();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
					String date = sdf.format(new Date());
					final String fileKey = user.account+"/sound/"+date + "/"+ System.currentTimeMillis()+"/"+fileName;

					uploadManager.put(fromToMessage.filePath, fileKey, upToken,

							new UpCompletionHandler() {
								@Override
								public void complete(String key,
										ResponseInfo info, JSONObject response) {
									// TODO Auto-generated method stub
									System.out.println("上传录音成功了");
									System.out.println(key + "     " + info
											+ "      " + response);
									//设置获得的url
									fromToMessage.message = RequestUrl.QiniuHttp + fileKey;
									System.out.println("录音在服务器上的位置是："+fromToMessage.message);
									MessageDao.getInstance().updateMsgToDao(fromToMessage);
									//发送新消息给服务器
									HttpManager.newMsgToServer(sp.getString("connecTionId", ""),
											fromToMessage, new NewMessageResponseHandler(fromToMessage));
								}
							}, null);
				}
			}

		}
	}
}
