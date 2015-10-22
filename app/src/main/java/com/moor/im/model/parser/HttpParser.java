package com.moor.im.model.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.moor.im.app.CacheKey;
import com.moor.im.app.MobileApplication;
import com.moor.im.model.entity.Contacts;
import com.moor.im.model.entity.Department;
import com.moor.im.model.entity.Discussion;
import com.moor.im.model.entity.FromToMessage;
import com.moor.im.model.entity.Group;
import com.moor.im.model.entity.User;
import com.moor.im.utils.LogUtil;

/**
 * 用户解析类
 * 
 * @author Mr.li
 * 
 */
public class HttpParser {

	/**
	 * 获取返回成功状态
	 * 
	 * @param responseString
	 * @return
	 */
	public static String getSucceed(String responseString) {
		String succeed = "";
		try {
			JSONObject o = new JSONObject(responseString);
			succeed = o.getString("Succeed");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return succeed;
	}

	/**
	 * 获取返回消息
	 * 
	 * @param responseString
	 * @return
	 */
	public static String getMessage(String responseString) {
		String message = "";
		try {
			JSONObject o = new JSONObject(responseString);
			message = o.getString("Message");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return message;
	}
	/**
	 * 获取大量消息的id
	 * 
	 * @param responseString
	 * @return
	 */
	public static String getLargeMsgId(String responseString) {
		String largeMsgId = "";
		try {
			JSONObject o = new JSONObject(responseString);
			largeMsgId = o.getString("LargeMsgId");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return largeMsgId;
	}

	/**
	 * 用户信息
	 * 
	 * @param responseString
	 * @return
	 */
	public static User getUserInfo(String responseString) {
		User user = new User();

		try {

			JSONObject o = new JSONObject(responseString);
			JSONObject o1 = o.getJSONObject("UserInfo");

			Gson gson = new Gson();
			// TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
			user = gson.fromJson(o1.toString(), new TypeToken<User>() {
			}.getType());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return user;
	}
	/**
	 * 获取一个部门
	 * 
	 * @param responseString
	 * @return
	 */
	public static Department getDepartmentInfo(String responseString) {
		Department department = new Department();
		
		try {
			
			JSONObject o = new JSONObject(responseString);
			JSONObject o1 = o.getJSONObject("Department");
			
			Gson gson = new Gson();
			// TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
			department = gson.fromJson(o1.toString(), new TypeToken<Department>() {
			}.getType());
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return department;
	}

	/**
	 * 获取一个聊天群组
	 *
	 * @param responseString
	 * @return
	 */
	public static Group getGroupInfo(String responseString) {
		Group group = new Group();

		try {

			JSONObject o = new JSONObject(responseString);
			JSONObject o1 = o.getJSONObject("Group");

			Gson gson = new Gson();
			// TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
			group = gson.fromJson(o1.toString(), new TypeToken<Group>() {
			}.getType());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return group;
	}

	/**
	 * 获取联系人列表
	 * 
	 * @param responseString
	 * @return
	 */
	public static List<Contacts> getContacts(String responseString) {
		List<Contacts> contacts = new ArrayList<Contacts>();

		try {
			JSONObject o = new JSONObject(responseString);
			JSONArray o1 = o.getJSONArray("Contacts");

			Gson gson = new Gson();
			// TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
			contacts = gson.fromJson(o1.toString(),
					new TypeToken<List<Contacts>>() {
					}.getType());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return contacts;
	}

	
	/**
	 * 有网络数据获取组织结构
	 * 
	 * @param responseString
	 * @return
	 */
	public static List<Department> getDepartments(String responseString) {
		List<Department> departments = new ArrayList<Department>();
		try {
			JSONObject o = new JSONObject(responseString);
			JSONArray o1 = o.getJSONArray("Departments");
			
			Gson gson = new Gson();
			// TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
			departments = gson.fromJson(o1.toString(),
					new TypeToken<List<Department>>() {
			}.getType());
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return departments;
	}

	/**
	 * 取消息
	 * 
	 * @param responseString
	 * @return
	 */
	public static List<FromToMessage> getMsgs(String responseString) {
		List<FromToMessage> newMessage = new ArrayList<FromToMessage>();
		LogUtil.i("取消息的返回数据:", responseString);
		try {
			JSONObject o = new JSONObject(responseString);
			JSONArray o1 = o.getJSONArray("Msgs");
			Gson gson = new Gson();
			// TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
			newMessage = gson.fromJson(o1.toString(),
					new TypeToken<List<FromToMessage>>() {
					}.getType());
			//收到的新消息逆序
			Collections.reverse(newMessage);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return newMessage;
	}
	/**
	 * 是否有大量的消息
	 * 
	 * @param responseString
	 * @return
	 */
	public static boolean isLargeMsg(String responseString) {
		try {
			JSONObject o = new JSONObject(responseString);
			boolean isLargeMsg = o.getBoolean("HasLargeMsgs");
			if(isLargeMsg) {
				return true;
			}
			
		} catch (JSONException e) {
			return false;
		}
		
		return false;
	}
	/**
	 * 是否还有大量的消息
	 * 
	 * @param responseString
	 * @return
	 */
	public static boolean hasMoreMsgs(String responseString) {
		try {
			JSONObject o = new JSONObject(responseString);
			boolean isHasMore = o.getBoolean("HasMore");
			if(isHasMore) {
				return true;
			}
			
		} catch (JSONException e) {
			return false;
		}
		
		return false;
	}

	/**
	 * 获取uptoken
	 * 
	 * @param responseString
	 * @return
	 */
	public static String getUpToken(String responseString) {
		String s = "";
		try {
			JSONObject o = new JSONObject(responseString);
			s = o.getString("uptoken");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;

	}


	/**
	 * 有网络数据获取群组
	 *
	 * @param responseString
	 * @return
	 */
	public static List<Group> getGroups(String responseString) {
		if(responseString == null || "".equals(responseString)) {
			return null;
		}
		List<Group> groups = new ArrayList<Group>();
		try {
			JSONObject o = new JSONObject(responseString);
			LogUtil.d("HttpParser", "获取群组数据:"+responseString);
			JSONArray o1 = o.getJSONArray("Groups");



			Gson gson = new Gson();
			// TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
			groups = gson.fromJson(o1.toString(),
					new TypeToken<List<Group>>() {
					}.getType());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return groups;
	}

	/**
	 * 有网络数据获取讨论组
	 *
	 * @param responseString
	 * @return
	 */
	public static List<Discussion> getDiscussion(String responseString) {
		if(responseString == null || "".equals(responseString)) {
			return null;
		}
		List<Discussion> discussions = new ArrayList<Discussion>();
		try {
			JSONObject o = new JSONObject(responseString);
			JSONArray o1 = o.getJSONArray("Discussions");

			Gson gson = new Gson();
			// TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
			discussions = gson.fromJson(o1.toString(),
					new TypeToken<List<Discussion>>() {
					}.getType());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return discussions;
	}
}
