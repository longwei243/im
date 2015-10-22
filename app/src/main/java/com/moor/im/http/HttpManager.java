package com.moor.im.http;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Environment;
import android.view.View;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.app.RequestUrl;
import com.moor.im.model.entity.FromToMessage;
import com.moor.im.utils.JSONWriter;
import com.moor.im.utils.Utils;

/**
 * 请求服务器方法类
 * 
 */
public class HttpManager {

	/**
	 * 获取个人信息
	 * 
	 * @param ConnectionId
	 * @param responseHandler
	 */
	public static void getUserInfo(String connectionId,
			ResponseHandlerInterface responseHandler) {

		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("Action", "getUserInfo");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RequestParams params = new RequestParams();
		params.add("data", json + "");
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);

	}
	/**
	 * 修改个人信息
	 */
	public static void editUserInfo(String connectionId,String _id, String name, 
			String email, String mobile, String product,
			ResponseHandlerInterface responseHandler) {
		
		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("Action", "editUserInfo");
			json.put("_id", _id);
			json.put("DisplayName", name);
			json.put("Email", email);
			json.put("Mobile", mobile);
			json.put("Product", product);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RequestParams params = new RequestParams();
		params.add("data", json + "");
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);
		
	}

	/**
	 * 取所有联系人
	 * 
	 * @param ConnectionId
	 * @param responseHandler
	 */
	public static void getContacts(String connectionId,
			ResponseHandlerInterface responseHandler) {

		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("Action", "getContacts");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RequestParams params = new RequestParams();
		params.add("data", json + "");
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);

	}

	/**
	 * 注销
	 * 
	 * @param ConnectionId
	 * @param responseHandler
	 */
	public static void loginOff(String connectionId,
			ResponseHandlerInterface responseHandler) {

		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("Action", "logoff");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RequestParams params = new RequestParams();
		params.add("data", json + "");
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);

	}

	/**
	 * 获取组织结构
	 * 
	 * @param ConnectionId
	 * @param responseHandler
	 */
	public static void getDepartments(String connectionId,
			ResponseHandlerInterface responseHandler) {

		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("Action", "getDepartments");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RequestParams params = new RequestParams();
		params.add("data", json + "");
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);

	}
	/**
	 * 获取版本号
	 * 
	 * @param ConnectionId
	 * @param responseHandler
	 */
	public static void getVersion(String connectionId,
			ResponseHandlerInterface responseHandler) {
		
		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("Action", "getDepartmentAndContactsVersion");
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RequestParams params = new RequestParams();
		params.add("data", json + "");
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);
		
	}

	/**
	 * 发送新消息到服务器
	 * @param connectionId
	 * @param fromToMessage
	 * @param responseHandler
	 */
	public static void newMsgToServer(String connectionId, FromToMessage fromToMessage, ResponseHandlerInterface responseHandler) {
		
		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("SessionId", fromToMessage.sessionId);
			json.put("MsgType", fromToMessage.msgType);
			json.put("Platform", "android");
			json.put("Type", fromToMessage.type);
			json.put("Message", fromToMessage.message);
			json.put("VoiceSecond", fromToMessage.voiceSecond);
			json.put("DeviceInfo", "android device");
			json.put("Action", "newMsg");
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RequestParams params = new RequestParams();
		params.add("data", json + "");
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);
		
	}

	/**
	 * 取消息
	 * 
	 * @param ConnectionId
	 * @param ReceivedMsgIds
	 * @param responseHandler
	 */
	public static void getMsg(String connectionId, ArrayList array,
			ResponseHandlerInterface responseHandler) {
		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ConnectionId", Utils.replaceBlank(connectionId));
		map.put("ReceivedMsgIds", array);
		map.put("Action", "getMsg");
		JSONWriter jw = new JSONWriter();
		jw.write(map);

		RequestParams params = new RequestParams();
		params.add("data", jw.write(map));
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);

	}
	/**
	 * 取大量的消息
	 * 
	 * @param ConnectionId
	 * @param largeMsgIdarray
	 * @param responseHandler
	 */
	public static void getLargeMsgs(String connectionId, ArrayList largeMsgIdarray,
			ResponseHandlerInterface responseHandler) {
		
		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ConnectionId", Utils.replaceBlank(connectionId));
		map.put("LargeMsgId", largeMsgIdarray);
		map.put("Action", "getLargeMsg");
		JSONWriter jw = new JSONWriter();
		jw.write(map);
		
		RequestParams params = new RequestParams();
		params.add("data", jw.write(map));
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);
		
	}

	/**
	 * 获取7牛的token
	 * 
	 * @param connectionId
	 * @param img
	 * @param responseHandler
	 */
	public static void getQiNiuToken(String connectionId, String fileName,
			ResponseHandlerInterface responseHandler) {
		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("Action", "qiniu.getUptoken");
			json.put("fileName", fileName);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RequestParams params = new RequestParams();
		params.add("data", json + "");
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);
	}

	/**
	 * 修改个人头像
	 * @param connectionId
	 * @param iconUrl
	 * @param responseHandler
	 */
	public static void updateUserIcon(String connectionId, String iconUrl,
									 ResponseHandlerInterface responseHandler) {
		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("Action", "uploadIcon");
			json.put("IconUrl", iconUrl);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RequestParams params = new RequestParams();
		params.add("data", json + "");
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);
	}
	
	/**
	 * 添加一个部门
	 * @param connectionId
	 * @param members 成员
	 * @param subDept 子部门
	 * @param name 部门名称
	 * @param desc 部门描述
	 * @param root 是否是根部门
	 * @param responseHandler
	 */
	public static void addDepartment(String connectionId, ArrayList members, 
			ArrayList subDept, String name, String desc, boolean root,
			ResponseHandlerInterface responseHandler) {

		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ConnectionId", Utils.replaceBlank(connectionId));
		map.put("Members", members);
		map.put("Subdepartments", subDept);
		map.put("Name", name);
		map.put("Description", desc);
		map.put("Root", root);
		map.put("Action", "addDepartment");
		JSONWriter jw = new JSONWriter();
		jw.write(map);

		RequestParams params = new RequestParams();
		params.add("data", jw.write(map));
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);

	}
	/**
	 * 更新一个部门
	 * @param connectionId
	 * @param _id 该部门的Id
	 * @param members 成员
	 * @param subDept 子部门
	 * @param name 部门名称
	 * @param desc 部门描述
	 * @param root 是否是根部门
	 * @param responseHandler
	 */
	public static void updateDepartment(String connectionId, String _id, ArrayList members, 
			ArrayList subDept, String name, String desc, boolean root,
			ResponseHandlerInterface responseHandler) {
		
		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ConnectionId", Utils.replaceBlank(connectionId));
		map.put("_id", _id);
		map.put("Members", members);
		map.put("Subdepartments", subDept);
		map.put("Name", name);
		map.put("Description", desc);
		map.put("Root", root);
		map.put("Action", "updateDepartment");
		JSONWriter jw = new JSONWriter();
		jw.write(map);
		
		RequestParams params = new RequestParams();
		params.add("data", jw.write(map));
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);
		
	}

	/**
	 * 删除根部门,注意若有子部门时不能删除
	 * @param connectionId
	 * @param id 要删除的部门id
	 * @param responseHandler
	 */
	public static void deteleDepartment(String connectionId, String id,
			ResponseHandlerInterface responseHandler) {

		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("_id", id);
			json.put("Action", "delDepartment");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RequestParams params = new RequestParams();
		params.add("data", json + "");
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);

	}
	/**
	 * 删除子部门,注意若有子部门时不能删除
	 * @param connectionId
	 * @param id 要删除的部门id
	 * @param responseHandler
	 */
	public static void deteleSubDepartment(String connectionId, String id, String parentId,
			ResponseHandlerInterface responseHandler) {
		
		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("_id", id);
			json.put("ParentId", parentId);
			json.put("Action", "delDepartment");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RequestParams params = new RequestParams();
		params.add("data", json + "");
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);
		
	}

	/**
	 * 添加一个群
	 * @param connectionId
	 * @param admins 管理员
	 * @param members 成员
	 * @param name 群名称
	 * @param responseHandler
	 */
	public static void createGroup(String connectionId, ArrayList admins,
								   ArrayList members, String name,
								   ResponseHandlerInterface responseHandler) {

		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ConnectionId", Utils.replaceBlank(connectionId));
		map.put("Member", members);
		map.put("Admin", admins);
		map.put("Title", name);
		map.put("Action", "createGroup");
		JSONWriter jw = new JSONWriter();
		jw.write(map);

		RequestParams params = new RequestParams();
		params.add("data", jw.write(map));
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);

	}

	/**
	 * 获取群
	 *
	 * @param ConnectionId
	 * @param responseHandler
	 */
	public static void getGroupByUser(String connectionId,
									  ResponseHandlerInterface responseHandler) {

		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("Action", "getGroupByUser");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RequestParams params = new RequestParams();
		params.add("data", json + "");
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);

	}
	/**
	 * 修改群名称
	 *
	 * @param connectionId
	 * @param responseHandler
	 */
	public static void updateGroupTitle(String connectionId, String id, String title,
									  ResponseHandlerInterface responseHandler) {

		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("Action", "editGroup");
			json.put("Title", title);
			json.put("_id", id);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RequestParams params = new RequestParams();
		params.add("data", json + "");
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);

	}

	/**
	 * 删除该群
	 *
	 * @param connectionId
	 * @param responseHandler
	 */
	public static void deleteGroup(String connectionId, String id,
										ResponseHandlerInterface responseHandler) {

		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("Action", "removeGroup");
			json.put("_id", id);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RequestParams params = new RequestParams();
		params.add("data", json + "");
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);

	}

	/**
	 * 添加群组管理员
	 * @param connectionId
	 * @param _id
	 * @param admin
	 * @param responseHandler
	 */
	public static void addGroupAdmin(String connectionId, String _id, ArrayList admin,
										ResponseHandlerInterface responseHandler) {

		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ConnectionId", Utils.replaceBlank(connectionId));
		map.put("_id", _id);
		map.put("Admin", admin);
		map.put("Action", "addGroupAdmin");
		JSONWriter jw = new JSONWriter();
		jw.write(map);

		RequestParams params = new RequestParams();
		params.add("data", jw.write(map));
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);

	}

	/**
	 * 添加群组管理员
	 * @param connectionId
	 * @param _id
	 * @param member
	 * @param responseHandler
	 */
	public static void addGroupMember(String connectionId, String _id, ArrayList member,
									 ResponseHandlerInterface responseHandler) {

		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ConnectionId", Utils.replaceBlank(connectionId));
		map.put("_id", _id);
		map.put("Member", member);
		map.put("Action", "addGroupMember");
		JSONWriter jw = new JSONWriter();
		jw.write(map);

		RequestParams params = new RequestParams();
		params.add("data", jw.write(map));
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);

	}

	/**
	 * 删除群组的管理员
	 * @param connectionId
	 * @param _id
	 * @param admin
	 * @param responseHandler
	 */
	public static void deleteGroupAdmin(String connectionId, String _id, ArrayList admin,
									  ResponseHandlerInterface responseHandler) {

		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ConnectionId", Utils.replaceBlank(connectionId));
		map.put("_id", _id);
		map.put("Admin", admin);
		map.put("Action", "removeGroupAdmin");
		JSONWriter jw = new JSONWriter();
		jw.write(map);

		RequestParams params = new RequestParams();
		params.add("data", jw.write(map));
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);

	}

	/**
	 * 删除群组成员
	 * @param connectionId
	 * @param _id
	 * @param member
	 * @param responseHandler
	 */
	public static void deleteGroupMember(String connectionId, String _id, ArrayList member,
										ResponseHandlerInterface responseHandler) {

		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ConnectionId", Utils.replaceBlank(connectionId));
		map.put("_id", _id);
		map.put("Member", member);
		map.put("Action", "removeGroupMember");
		JSONWriter jw = new JSONWriter();
		jw.write(map);

		RequestParams params = new RequestParams();
		params.add("data", jw.write(map));
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);

	}


	/**
	 * 创建讨论组
	 * @param connectionId
	 * @param members
	 * @param name
	 * @param responseHandler
	 */
	public static void createDiscussion(String connectionId,
								   ArrayList members, String name,
								   ResponseHandlerInterface responseHandler) {

		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ConnectionId", Utils.replaceBlank(connectionId));
		map.put("Member", members);
		map.put("Title", name);
		map.put("Action", "createDiscussion");
		JSONWriter jw = new JSONWriter();
		jw.write(map);

		RequestParams params = new RequestParams();
		params.add("data", jw.write(map));
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);

	}

	public static void getDiscussionByUser(String connectionId,
									  ResponseHandlerInterface responseHandler) {

		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("Action", "getDiscussionByUser");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RequestParams params = new RequestParams();
		params.add("data", json + "");
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);

	}


	/**
	 * 删除讨论组成员
	 * @param connectionId
	 * @param _id
	 * @param member
	 * @param responseHandler
	 */
	public static void deleteDiscussionMember(String connectionId, String _id, ArrayList member,
										 ResponseHandlerInterface responseHandler) {

		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ConnectionId", Utils.replaceBlank(connectionId));
		map.put("_id", _id);
		map.put("Member", member);
		map.put("Action", "removeDiscussionMember");
		JSONWriter jw = new JSONWriter();
		jw.write(map);

		RequestParams params = new RequestParams();
		params.add("data", jw.write(map));
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);

	}


	/**
	 * 修改讨论组名称
	 * @param connectionId
	 * @param id
	 * @param title
	 * @param responseHandler
	 */
	public static void updateDiscussionTitle(String connectionId, String id, String title,
										ResponseHandlerInterface responseHandler) {

		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("Action", "editDiscussion");
			json.put("Title", title);
			json.put("_id", id);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RequestParams params = new RequestParams();
		params.add("data", json + "");
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);

	}

	/**
	 * 删除讨论组
	 * @param connectionId
	 * @param id
	 * @param responseHandler
	 */
	public static void deleteDiscussion(String connectionId, String id,
								   ResponseHandlerInterface responseHandler) {

		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("Action", "removeDiscussion");
			json.put("_id", id);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RequestParams params = new RequestParams();
		params.add("data", json + "");
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);

	}

	/**
	 * 添加讨论组成员
	 * @param connectionId
	 * @param _id
	 * @param member
	 * @param responseHandler
	 */
	public static void addDiscussionMember(String connectionId, String _id, ArrayList member,
									  ResponseHandlerInterface responseHandler) {

		AsyncHttpClient httpclient = MobileApplication.httpclient;
		JSONObject json = new JSONObject();

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ConnectionId", Utils.replaceBlank(connectionId));
		map.put("_id", _id);
		map.put("Member", member);
		map.put("Action", "addDiscussionMember");
		JSONWriter jw = new JSONWriter();
		jw.write(map);

		RequestParams params = new RequestParams();
		params.add("data", jw.write(map));
		httpclient.post(RequestUrl.baseHttp1, params, responseHandler);

	}

}
