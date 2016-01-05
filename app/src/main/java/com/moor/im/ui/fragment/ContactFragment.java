package com.moor.im.ui.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Inflater;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncStatusObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.dao.ContactsDao;
import com.moor.im.db.dao.UserDao;
import com.moor.im.http.HttpManager;
import com.moor.im.model.entity.Contacts;
import com.moor.im.model.entity.User;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.ui.activity.ContactDetailActivity;
import com.moor.im.ui.activity.ContactSystemActicity;
import com.moor.im.ui.activity.DepartmentActivity;
import com.moor.im.ui.activity.DiscussionActivity;
import com.moor.im.ui.activity.GroupActivity;
import com.moor.im.ui.adapter.ContactListViewAdapter;
import com.moor.im.ui.base.BaseLazyFragment;
import com.moor.im.ui.view.ListViewInScrollView;
import com.moor.im.ui.view.SideBar;
import com.moor.im.ui.view.SideBar.OnTouchingLetterChangedListener;
import com.moor.im.utils.LogUtil;
import com.moor.im.utils.NullUtil;
import com.moor.im.utils.PingYinUtil;
import com.moor.im.utils.PinyinComparator;

/**
 * 通讯录界面
 * 
 * @author LongWei
 * 
 */
public class ContactFragment extends BaseLazyFragment {

	private ListView listView;
	private SideBar sideBar;
	private TextView dialog;
	private ContactListViewAdapter adapter;
	private int firstItem = 0;

	private List<Contacts> contacts;

	private SharedPreferences sp;

	private LinearLayout department_layout, group_layout, discuss_layout, phone_contact_layout;

	SharedPreferences.Editor editor;

	SharedPreferences myPreferences;
	SharedPreferences.Editor myeditor;

	private PinyinComparator pinyinComparator = new PinyinComparator();

	private EditText contact_et_search;

	private User user = UserDao.getInstance().getUser();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_contact, null);
		initViews(view);

		sp = getActivity().getSharedPreferences("SP", 4);
		editor = sp.edit();

		myPreferences = getActivity().getSharedPreferences(MobileApplication.getInstance()
						.getResources().getString(R.string.spname),
				Activity.MODE_PRIVATE);
		myeditor = myPreferences.edit();

		return view;
	}

	class getContactsResponseHandler extends TextHttpResponseHandler {
		@Override
		public void onFailure(int statusCode, Header[] headers,
				String responseString, Throwable throwable) {
//			Toast.makeText(getActivity(), "请检查您的网络问题！！！", 3000).show();
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
				String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			if ("true".equals(succeed)) {

				ContactsDao.getInstance().clear();
				contacts = HttpParser.getContacts(responseString);

				ContactsDao.getInstance().saveContacts(contacts);

				SetLetterTask slt = new SetLetterTask();
				slt.execute(contacts);

//				initDatas(contacts);
			} else {
//				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT)
//						.show();
			}
		}
	}

	class SetLetterTask extends AsyncTask<List<Contacts>, Void, List<Contacts>> {

		@Override
		protected List<Contacts> doInBackground(List<Contacts>... params) {
			List<Contacts> cbs = params[0];
			if(cbs != null && cbs.size() > 0 && user != null) {
				setLetter(cbs);
				Collections.sort(cbs, pinyinComparator);
				for (int i = 0; i < cbs.size(); i++) {
					if (cbs.get(i)._id.equals(user._id)) {
						cbs.remove(i);
					}
				}
			}
			return cbs;
		}

		@Override
		protected void onPostExecute(List<Contacts> contacts) {

			initDatas(contacts);

			super.onPostExecute(contacts);
		}
	}

	/**
	 * 第一次出现界面时在加载数据
	 */
	@Override
	public void onFirstUserVisible() {
		List<Contacts> list = getContactsFromDB();
		if (list != null && list.size() != 0) {
			SetLetterTask slt = new SetLetterTask();
			slt.execute(list);
//			initDatas(list);
		}
		
		getVersionFromNet();
	}

	/**
	 * 从网络上加载联系人
	 */
	private void getContactsFromNet() {
		System.out.println("从网络加载联系人");
		HttpManager.getContacts(sp.getString("connecTionId", ""),
				new getContactsResponseHandler());
	}

	/**
	 * 从数据库读取所有联系人
	 */
	private List<Contacts> getContactsFromDB() {
		System.out.println("从数据库加载联系人");
		return ContactsDao.getInstance().getContacts();
	}

	/**
	 * 加载数据
	 */
	private void initDatas(final List<Contacts> contacts) {

//		if(contacts != null && contacts.size() > 0 && user != null) {
//			setLetter(contacts);
//			Collections.sort(contacts, pinyinComparator);
//			// 将自己在联系人中移除
//			for (int i = 0; i < contacts.size(); i++) {
//				if (contacts.get(i)._id.equals(user._id)) {
//					contacts.remove(i);
//				}
//			}

			adapter = new ContactListViewAdapter(getActivity(), contacts);
			listView.setAdapter(adapter);

			sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

				@Override
				public void onTouchingLetterChanged(String s) {

					int position = adapter.getPositionForSection(s.charAt(0));
					if (position != -1) {
						listView.setSelection(position);
					}

				}
			});

			listView.setOnScrollListener(new OnScrollListener() {//

				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
					switch (scrollState) {

					}
				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
									 int visibleItemCount, int totalItemCount) {
					firstItem = firstVisibleItem;
					String py = ((Contacts) adapter.getItem(firstItem)).pinyin;
					if (py != null && !"".equals(py)) {
						String letter = py.toUpperCase().substring(0, 1);
						sideBar.setBar(letter);
					}


				}
			});
			listView.getParent().requestDisallowInterceptTouchEvent(true);

			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
										int position, long id) {
					Contacts messageWall = (Contacts) parent.getAdapter().getItem(position);
					if(messageWall != null) {
						Intent intent = new Intent(getActivity(),
								ContactDetailActivity.class);
						intent.putExtra("_id", NullUtil.checkNull(messageWall._id + ""));
						intent.putExtra("otherName", NullUtil.checkNull(messageWall.displayName + ""));
						intent.putExtra("contact", messageWall);
						startActivity(intent);
					}

				}
			});
//		}

	}

	private void initViews(View view) {

		listView = (ListView) view.findViewById(R.id.contact_listview);
		sideBar = (SideBar) view.findViewById(R.id.contact_sidebar);
		dialog = (TextView) view.findViewById(R.id.contact_textview_dialog);
		sideBar.setTextView(dialog);
		contact_et_search = (EditText) view.findViewById(R.id.contact_et_search);
		contact_et_search.setVisibility(View.GONE);
//		contact_et_search.addTextChangedListener(new TextWatcher() {
//
//			@Override
//			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
//									  int arg3) {
//				String key = contact_et_search.getText().toString().trim();
//				if(user != null && user.exten.equals(key)) {
//					return;
//				}else if(!"".equals(key)) {
//					SearchContactsTask searchContactsTask = new SearchContactsTask();
//					searchContactsTask.execute(key);
//				}else {
//					List<Contacts> list = getContactsFromDB();
//					if (list != null && list.size() != 0) {
//						initDatas(list);
//					}
//				}
//
//			}
//
//			@Override
//			public void beforeTextChanged(CharSequence arg0, int arg1,
//										  int arg2, int arg3) {
//			}
//
//			@Override
//			public void afterTextChanged(Editable arg0) {
//
//			}
//		});

		View viewheader = LayoutInflater.from(getActivity()).inflate(R.layout.contact_listview_header, null);
		listView.addHeaderView(viewheader);

		department_layout = (LinearLayout) viewheader.findViewById(R.id.department_layout);
		department_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//点击进入组织架构页面
				Intent intent = new Intent(getActivity(), DepartmentActivity.class);
				startActivity(intent);
			}
		});
		group_layout = (LinearLayout) viewheader.findViewById(R.id.group_layout);
		group_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//点击进入群组页面
				Intent intent = new Intent(getActivity(), GroupActivity.class);
				startActivity(intent);
			}
		});
		discuss_layout = (LinearLayout) viewheader.findViewById(R.id.discuss_layout);
		discuss_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//点击进入讨论组页面
				Intent intent = new Intent(getActivity(), DiscussionActivity.class);
				startActivity(intent);
			}
		});
		phone_contact_layout = (LinearLayout) viewheader.findViewById(R.id.phone_contact_layout);
		phone_contact_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//点击进入手机联系人页面
				Intent intent = new Intent(getActivity(), ContactSystemActicity.class);
				startActivity(intent);
			}
		});

	}
	
	/**
	 * 从网络获取版本号
	 */
	public void getVersionFromNet() {
		System.out.println("从网络获取版本号");
		HttpManager.getVersion(sp.getString("connecTionId", ""),
				new GetVersionResponseHandler());
	}
	
	class GetVersionResponseHandler extends TextHttpResponseHandler {

		@Override
		public void onFailure(int statusCode, Header[] headers,
				String responseString, Throwable throwable) {
//			Toast.makeText(getActivity(), "请检查您的网络问题！！！", 3000).show();
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
				String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			if ("true".equals(succeed)) {
				try {
					JSONObject jsonObject = new JSONObject(responseString);
					Long contactsVersion = jsonObject.getLong("ContactsVersion");
					if(!"".equals(myPreferences.getString("contactsVersion", ""))) {
						if(!myPreferences.getString("contactsVersion", "").equals(contactsVersion + "")) {
							//需更新
							getContactsFromNet();
						}else {
//							List<Contacts> list = getContactsFromDB();
//
//							if (list != null && list.size() != 0) {
////								System.out.println("联系人列表不是空的");
//								initDatas(list);
//							}
						}
					
					}else{
//						List<Contacts> list = getContactsFromDB();
//
//						if (list != null && list.size() != 0) {
//							System.out.println("联系人列表不是空的");
//							initDatas(list);
//						}
					}
					
					if(contactsVersion != null && !"".equals(contactsVersion)) {
						myeditor.putString("contactsVersion", contactsVersion + "");
						myeditor.commit();

					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
//				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT)
//						.show();
			}
		}
	}

	private void setLetter(List<Contacts> contacts) {
		for (int i=0; i<contacts.size(); i++) {
			if(contacts.get(i).pinyin == null || "".equals(contacts.get(i).pinyin)) {
				contacts.get(i).pinyin = "#";
			}
			if(!contacts.get(i).pinyin.toUpperCase().substring(0, 1).matches("[A-Z]")) {
				contacts.get(i).pinyin = "#";
			}
		}
	}

//	class SearchContactsTask extends AsyncTask<String, Void, List<Contacts>> {
//
//		@Override
//		protected List<Contacts> doInBackground(String[] params) {
//			String key = params[0];
//			List<Contacts> contactsList = ContactsDao.getInstance().getContactsByMoHu(key);
//
//			List<Contacts> searchContacts = new ArrayList<Contacts>();
//
//			if(contactsList != null && contactsList.size() != 0) {
//				for (int i=0; i<contactsList.size(); i++) {
//					Contacts contacts = contactsList.get(i);
//					searchContacts.add(contacts);
//
//				}
//			}
//			return searchContacts;
//		}
//
//		@Override
//		protected void onPostExecute(List<Contacts> contactses) {
//			super.onPostExecute(contactses);
//
//			initDatas(contactses);
//		}
//	}
}
