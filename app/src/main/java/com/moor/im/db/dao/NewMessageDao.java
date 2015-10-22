package com.moor.im.db.dao;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.j256.ormlite.dao.Dao;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.DataBaseHelper;
import com.moor.im.model.entity.NewMessage;

/**
 * 所有人的最新消息实体类
 * 
 * @author Mr.li
 * 
 */
public class NewMessageDao {
	private Dao<NewMessage, Integer> newMessageDao = null;
	private DataBaseHelper helper = DataBaseHelper.getHelper(MobileApplication
			.getInstance());
	private static NewMessageDao instance;

	private NewMessageDao() {
		try {
			newMessageDao = helper.getDao(NewMessage.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static NewMessageDao getInstance() {
		if (instance == null) {
			instance = new NewMessageDao();
		}
		return instance;
	}

	/**
	 * 插入一条最新的消息
	 * 
	 * @param sessionId
	 * @param message
	 * @param fromName
	 */
	public void insertNewMsgs(String sessionId, String message, String msgType, String fromName,
			Long when, int unReadCount, String type, String from) {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm:ss");
		String time = sDateFormat.format(new java.util.Date());
		SimpleDateFormat sFormat = new SimpleDateFormat("yyyyMMddhhmmss");
		String date = sFormat.format(new java.util.Date());

		Random random = new Random();
		int x = random.nextInt(899999);
		int y = x + 100000;
		String s = date + y;
		NewMessage newMsgs = new NewMessage();
		newMsgs._id = s + "";
		newMsgs.fromName = fromName;
		newMsgs.message = message;
		newMsgs.msgType = msgType;
		newMsgs.sessionId = sessionId;
		newMsgs.time = when;
		newMsgs.img = "";
		newMsgs.unReadCount = unReadCount;
		newMsgs.type = type;
		newMsgs.from = from;
		try {
			newMessageDao.create(newMsgs);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 获取所有最新消息的列表
	 * 
	 * @return
	 */
	public List<NewMessage> queryMessage() {
		List<NewMessage> newMessage = new ArrayList<NewMessage>();
		try {
			newMessage = newMessageDao.queryBuilder().orderBy("time", false)
					.query();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newMessage;
	}

	/**
	 * 查询是否有这个人的消息
	 * 
	 * @param fromId
	 * @return
	 */
	public List<NewMessage> isQueryMessage(String fromId) {
		List<NewMessage> newMessage = new ArrayList<NewMessage>();
		try {
			newMessage = newMessageDao.queryBuilder().where()
					.eq("sessionId", fromId).query();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newMessage;
	}

	/**
	 * 删除某条消息
	 * 
	 * @param newMessage
	 */
	public void deleteOneMessage(List<NewMessage> newMessage) {
		try {
			newMessageDao.delete(newMessage);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 修改未读数位0
	 * 
	 */
	public void updateUnReadCount(String id) {
		List<NewMessage> newMessage = new ArrayList<NewMessage>();
		try {
			newMessage = newMessageDao.queryBuilder().where().eq("sessionId", id)
					.query();
			for (int i = 0; i < newMessage.size(); i++) {
				newMessage.get(i).unReadCount = 0;
				newMessageDao.update(newMessage.get(i));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 获取所有的未读消息数
	 * 
	 * @return
	 */
	public int getAllUnReadCount() {
		List<NewMessage> newMessage = new ArrayList<NewMessage>();
		int allUnReadCount = 0;
		try {
			newMessage = newMessageDao.queryForAll();
			for (int i = 0; i < newMessage.size(); i++) {
				allUnReadCount += newMessage.get(i).unReadCount;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return allUnReadCount;
	}
	/**
	 * 删除所有消息
	 */
	public void deleteAllMsgs() {
		try {
			newMessageDao.delete(newMessageDao.queryForAll());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void deleteMsgById(String id) {
		try {
			NewMessage msg = newMessageDao.queryBuilder().where().eq("sessionId", id)
					.queryForFirst();
			newMessageDao.delete(msg);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 更新该消息
	 * @param msg
	 */
	public void updateMsg(NewMessage msg) {
		try {
			NewMessage newMessage = newMessageDao.queryBuilder().where().eq("sessionId", msg.sessionId)
					.queryForFirst();
			newMessage = msg;
			newMessageDao.update(newMessage);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public NewMessage getNewMsg(String sessionId) {
		NewMessage newMessage = null;
		try {
			newMessage = newMessageDao.queryBuilder().where().eq("sessionId", sessionId).queryForFirst();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return newMessage;
	}
}
