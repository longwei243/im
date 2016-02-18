package com.moor.im.ui.fragment;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.dao.UserDao;
import com.moor.im.event.DialEvent;
import com.moor.im.model.entity.CallLogModel;
import com.moor.im.model.entity.User;
import com.moor.im.ui.activity.CallingActivity;
import com.moor.im.ui.adapter.CallLogAdapter;
import com.moor.im.ui.base.BaseLazyFragment;
import com.moor.im.utils.Utils;

import de.greenrobot.event.EventBus;

/**
 * 打电话界面
 * 
 * @author LongWei
 * 
 */
public class DialFragment extends BaseLazyFragment {
	/**
	 * 拨号盘布局
	 */
	private LinearLayout dialplate_layout;
	
	private ListView calllog_listview;
	CallLogAdapter adapter;

	private EditText editText_phone_number;

	private Button btn_dialnum_1;
	private Button btn_dialnum_2;
	private Button btn_dialnum_3;
	private Button btn_dialnum_4;
	private Button btn_dialnum_5;
	private Button btn_dialnum_6;
	private Button btn_dialnum_7;
	private Button btn_dialnum_8;
	private Button btn_dialnum_9;
	private Button btn_dialnum_0;
	private Button btn_dialnum_delete;
	private Button btn_dialnum_call;
	
	
	private AsyncQueryHandler asyncQuery;  
    private List<CallLogModel> callLogs;

	private User user = UserDao.getInstance().getUser();
	
	/**
	 * 拨号盘高度
	 */
	private int dialplate_layout_height;
	private SharedPreferences sp;

	public interface OnMakeCallListener{
		void makeCall(String callee);
	}
	
	private OnMakeCallListener makeCallListener;
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		makeCallListener = (OnMakeCallListener) activity;
	}

	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
		asyncQuery = new MyAsyncQueryHandler(getActivity().getContentResolver()); 
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_dial, null);
		initViews(view);
		sp = getActivity().getSharedPreferences("SP", 4);
		return view;
	}

	@Override
	public void onFirstUserVisible() {

	}

	private void initViews(View view) {


		dialplate_layout = (LinearLayout) view
				.findViewById(R.id.dialplate_layout);
		editText_phone_number = (EditText) view
				.findViewById(R.id.dialplate_edittext_phonenum);

		if (android.os.Build.VERSION.SDK_INT <= 10) {
			editText_phone_number.setInputType(InputType.TYPE_NULL);
		} else {
			getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			try {
				Class<EditText> cls = EditText.class;
				Method setSoftInputShownOnFocus;
				setSoftInputShownOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
				setSoftInputShownOnFocus.setAccessible(true);
				setSoftInputShownOnFocus.invoke(editText_phone_number, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		btn_dialnum_1 = (Button) view.findViewById(R.id.dialNum1);
		btn_dialnum_2 = (Button) view.findViewById(R.id.dialNum2);
		btn_dialnum_3 = (Button) view.findViewById(R.id.dialNum3);
		btn_dialnum_4 = (Button) view.findViewById(R.id.dialNum4);
		btn_dialnum_5 = (Button) view.findViewById(R.id.dialNum5);
		btn_dialnum_6 = (Button) view.findViewById(R.id.dialNum6);
		btn_dialnum_7 = (Button) view.findViewById(R.id.dialNum7);
		btn_dialnum_8 = (Button) view.findViewById(R.id.dialNum8);
		btn_dialnum_9 = (Button) view.findViewById(R.id.dialNum9);
		btn_dialnum_0 = (Button) view.findViewById(R.id.dialNum0);
		btn_dialnum_delete = (Button) view.findViewById(R.id.dial_delete);
		btn_dialnum_call = (Button) view.findViewById(R.id.dial_call);

		btn_dialnum_1.setOnClickListener(clickListener);
		btn_dialnum_2.setOnClickListener(clickListener);
		btn_dialnum_3.setOnClickListener(clickListener);
		btn_dialnum_4.setOnClickListener(clickListener);
		btn_dialnum_5.setOnClickListener(clickListener);
		btn_dialnum_6.setOnClickListener(clickListener);
		btn_dialnum_7.setOnClickListener(clickListener);
		btn_dialnum_8.setOnClickListener(clickListener);
		btn_dialnum_9.setOnClickListener(clickListener);
		btn_dialnum_0.setOnClickListener(clickListener);
		btn_dialnum_delete.setOnClickListener(clickListener);
		btn_dialnum_call.setOnClickListener(clickListener);
		btn_dialnum_delete.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				editText_phone_number.setText("");
				return false;
			}
		});
		// 获取拨号盘的高度
		dialplate_layout.measure(0, 0);
		dialplate_layout_height = dialplate_layout.getMeasuredHeight();

		
		calllog_listview = (ListView) view.findViewById(R.id.dialplate_listview);
		calllog_listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				CallLogModel clm = (CallLogModel) parent.getAdapter().getItem(position);
				String number = clm.getNumber();
				callingDialog(number);
			}
		});
		
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		init();
	}
	
	 private void init() {  
	        Uri uri = android.provider.CallLog.Calls.CONTENT_URI;  
	        // 查询的列  
	        String[] projection = { CallLog.Calls.DATE, // 日期  
	                CallLog.Calls.NUMBER, // 号码  
	                CallLog.Calls.TYPE, // 类型  
	                CallLog.Calls.CACHED_NAME, // 名字  
	                CallLog.Calls.DURATION, // 时长  
	                CallLog.Calls._ID, // id  
	        };  
	        
	        asyncQuery.startQuery(0, null, uri, projection, null, null,  
	                CallLog.Calls.DEFAULT_SORT_ORDER+ " limit "+100);  
	    }  
	  
	    private class MyAsyncQueryHandler extends AsyncQueryHandler {  
	  
	        public MyAsyncQueryHandler(ContentResolver cr) {  
	            super(cr);  
	        }  
	  
	        @Override  
	        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {  
	            if (cursor != null && cursor.getCount() > 0) {  
	                callLogs = new ArrayList<CallLogModel>();  
	                
	                cursor.moveToFirst(); // 游标移动到第一项  
	                for (int i = 0; i < cursor.getCount(); i++) {  
	                    cursor.moveToPosition(i);  
	                    long date = cursor.getLong(cursor  
	                            .getColumnIndex(CallLog.Calls.DATE));  
	                    long duration = cursor.getLong(cursor  
	                    		.getColumnIndex(CallLog.Calls.DURATION));  
	                    String number = cursor.getString(cursor  
	                            .getColumnIndex(CallLog.Calls.NUMBER));  
	                    String type = cursor.getString(cursor  
	                            .getColumnIndex(CallLog.Calls.TYPE));  
	                    String cachedName = cursor.getString(cursor  
	                            .getColumnIndex(CallLog.Calls.CACHED_NAME));// 缓存的名称与电话号码，如果它的存在  
	                    long id = cursor.getLong(cursor  
	                            .getColumnIndex(CallLog.Calls._ID));  
	  
	                    CallLogModel callLogBean = new CallLogModel();  
	                    callLogBean.set_id(id);  
	                    callLogBean.setNumber(number);  
	                    callLogBean.setDuration(duration);  
	                    callLogBean.setDisplayName(cachedName);  
	                    if (null == cachedName || "".equals(cachedName)) {  
	                        callLogBean.setDisplayName(number);  
	                    }  
	                    String typeStr = "";
	    				if(type.equals("1")) {
	    					typeStr = "呼入";
	    				}else if(type.equals("2")){
	    					typeStr = "呼出";
	    				}else if(type.equals("3")){
	    					typeStr = "未接听";
	    				}
	                    callLogBean.setType(typeStr);  
	                    callLogBean.setDate(date);  
	  
	                    callLogs.add(callLogBean);  
	                }  
	                if (callLogs.size() > 0) {  
	                    setAdapter(callLogs);  
	                }  
	                cursor.close();
	            }  
	            super.onQueryComplete(token, cookie, cursor);  
	        }  
	    }  
	  
	    private void setAdapter(List<CallLogModel> callLogs) {  
	        adapter = new CallLogAdapter(getActivity(), callLogs);  
	        calllog_listview.setAdapter(adapter);  
	    }  

	OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			StringBuffer sb = new StringBuffer(editText_phone_number.getText()
					.toString());
			switch (v.getId()) {
			case R.id.dialNum1:
				sb.append("1");
				break;
			case R.id.dialNum2:
				sb.append("2");
				break;
			case R.id.dialNum3:
				sb.append("3");
				break;
			case R.id.dialNum4:
				sb.append("4");
				break;
			case R.id.dialNum5:
				sb.append("5");
				break;
			case R.id.dialNum6:
				sb.append("6");
				break;
			case R.id.dialNum7:
				sb.append("7");
				break;
			case R.id.dialNum8:
				sb.append("8");
				break;
			case R.id.dialNum9:
				sb.append("9");
				break;
			case R.id.dialNum0:
				sb.append("0");
				break;
			case R.id.dial_delete:
				if (editText_phone_number.getText().length() != 0) {
					//找到光标位置
					int index = editText_phone_number.getSelectionStart();
					if(index > 0) {
						sb.deleteCharAt(index - 1);
					}

				}
				break;
			case R.id.dial_call:
				if(!"".equals(editText_phone_number.getText().toString().trim())) {
					callingDialog(editText_phone_number.getText().toString().trim());
				}else{
					Toast.makeText(getActivity(), "请输入号码后拨打", Toast.LENGTH_SHORT).show();
				}

				break;

			default:
				break;
			}
			if (sb.toString() != null) {
				editText_phone_number.setText(sb.toString());
				editText_phone_number.setSelection(sb.length());
			}
		}
	};

	public void callingDialog(final String number) {
		LayoutInflater myInflater = LayoutInflater.from(getActivity());
		final View myDialogView = myInflater.inflate(R.layout.calling_dialog,
				null);
		final Builder dialog = new AlertDialog.Builder(getActivity())
				.setView(myDialogView);
		final AlertDialog alert = dialog.show();
		alert.setCanceledOnTouchOutside(true);// 设置点击Dialog外部任意区域关闭Dialog
		alert.getWindow().setGravity(Gravity.BOTTOM);

		// 直播
		LinearLayout mDirectSeeding = (LinearLayout) myDialogView
				.findViewById(R.id.direct_seeding_linear);
		mDirectSeeding.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				try {
					if (Utils.isNetWorkConnected(getActivity())) {
						makeCallListener.makeCall(number);
					} else {
						Toast.makeText(getActivity(), "网络错误，请重试！",
								Toast.LENGTH_LONG).show();
					}
					alert.dismiss();
//					editText_phone_number.setText("");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		// 回拨
		LinearLayout mCallReturn = (LinearLayout) myDialogView
				.findViewById(R.id.call_return_linear);
//		if("zj".equals(user.product)) {
//			mCallReturn.setVisibility(View.GONE);
//		}
		mCallReturn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				String mobile = user.mobile;
				if(mobile == null || "".equals(mobile)) {
					Toast.makeText(getActivity(), "未绑定手机，不能进行回拨", Toast.LENGTH_SHORT).show();
					
				}else {
					// TODO Auto-generated method stub
					if (Utils.isNetWorkConnected(getActivity())) {
						// 跳转到正在通话页面
						Intent calling = new Intent(getActivity(),
								CallingActivity.class);
						calling.putExtra("phone_number", number);
						startActivity(calling);
						editText_phone_number.setText("");
					} else {
						Toast.makeText(getActivity(), "网络错误，请重试！",
								Toast.LENGTH_LONG).show();
					}
					editText_phone_number.setText("");
					alert.dismiss();
				}

			}
		});
	
		// 普通电话
		LinearLayout mOrdinaryCall = (LinearLayout) myDialogView
				.findViewById(R.id.ordinary_call_linear);
		mOrdinaryCall.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_CALL, Uri
						.parse("tel://"
								+ number));
				startActivity(intent);
				alert.dismiss();
				editText_phone_number.setText("");
			}
		});
		// 取消
		LinearLayout mCancelLinear = (LinearLayout) myDialogView
				.findViewById(R.id.cancel_linear);
		mCancelLinear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alert.dismiss();
				editText_phone_number.setText("");
			}
		});
	}

	private SharedPreferences settings;

	/**
	 * 拨号盘关闭
	 */
	private void dialplateClose() {
		ObjectAnimator
				.ofFloat(dialplate_layout, "translationY", 0,
						dialplate_layout_height).setDuration(600).start();
	}

	/**
	 * 拨号盘打开
	 */
	private void dialplateOpen() {
		ObjectAnimator
				.ofFloat(dialplate_layout, "translationY",
						dialplate_layout_height, 0).setDuration(600).start();
	}

	public void onEventMainThread(DialEvent de) {
		settings = getActivity().getSharedPreferences(
				MobileApplication.getInstance().getResources()
						.getString(R.string.spname), Activity.MODE_PRIVATE);
		final String dia_key = settings.getString("ClickState", "").toString()
				.trim();
		final String dia_t1 = settings.getString("moveState", "").toString()
				.trim();
		if (!dia_t1.equals("STATE_MOVE")) {
			if (dia_key.equals("") | dia_key.equals("STATE_SHOW")) {
				dialplateOpen();
			} else {
				dialplateClose();
			}
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

}
