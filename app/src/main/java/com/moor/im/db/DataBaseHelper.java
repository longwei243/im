package com.moor.im.db;

import java.sql.SQLException;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.moor.im.model.entity.Contacts;
import com.moor.im.model.entity.FromToMessage;
import com.moor.im.model.entity.NewMessage;
import com.moor.im.model.entity.User;
import com.moor.im.model.entity.UserRole;

/**
 * 操作数据库的帮助类，使用了OrmLite框架
 * 
 * @author LongWei
 * 
 */
public class DataBaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "qmoor.db";
	private static final int DATABASE_VERSION = 8;
	private Dao<User, Integer> userDao = null;
	private Dao<Contacts, Integer> contactsDao = null;
	private Dao<FromToMessage, Integer> fromToMessageDao = null;
	private Dao<NewMessage, Integer> newMessageDao = null;
	private Dao<UserRole, Integer> userRoleDao = null;

	private DataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, User.class);
			TableUtils.createTable(connectionSource, Contacts.class);
			TableUtils.createTable(connectionSource, FromToMessage.class);
			TableUtils.createTable(connectionSource, NewMessage.class);
			TableUtils.createTable(connectionSource, UserRole.class);
			// contactsDao = getContactsDao();
			userDao = getUserDao();
			fromToMessageDao = getFromMessageDao();
			newMessageDao = getNewMessageDao();
			userRoleDao = getUserRoleDao();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
			int oldVer, int newVer) {
		try {
			TableUtils.dropTable(connectionSource, Contacts.class, true);
			TableUtils.dropTable(connectionSource, User.class, true);
			TableUtils.dropTable(connectionSource, FromToMessage.class, true);
			TableUtils.dropTable(connectionSource, NewMessage.class, true);
			TableUtils.dropTable(connectionSource, UserRole.class, true);
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获得联系人数据库表的dao
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Dao<Contacts, Integer> getContactsDao() throws SQLException {
		if (contactsDao == null) {
			contactsDao = getDao(Contacts.class);
		}
		return contactsDao;
	}

	/**
	 * 获取用户信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Dao<User, Integer> getUserDao() throws SQLException {
		if (userDao == null) {
			userDao = getDao(User.class);
		}
		return userDao;
	}

	/**
	 * 消息列表
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Dao<FromToMessage, Integer> getFromMessageDao() throws SQLException {
		if (fromToMessageDao == null) {
			fromToMessageDao = getDao(FromToMessage.class);
		}
		return fromToMessageDao;
	}

	/**
	 * 所有人最新消息列表
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Dao<NewMessage, Integer> getNewMessageDao() throws SQLException {
		if (newMessageDao == null) {
			newMessageDao = getDao(NewMessage.class);
		}
		return newMessageDao;
	}

	public Dao<UserRole, Integer> getUserRoleDao() throws SQLException {
		if (userRoleDao == null) {
			userRoleDao = getDao(UserRole.class);
		}
		return userRoleDao;
	}

	private static DataBaseHelper instance;

	/**
	 * 单例获取该Helper
	 * 
	 * @param context
	 * @return
	 */
	public static synchronized DataBaseHelper getHelper(Context context) {
		if (instance == null) {
			synchronized (DataBaseHelper.class) {
				if (instance == null)
					instance = new DataBaseHelper(context);
			}
		}

		return instance;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		super.close();
		contactsDao = null;
		userDao = null;
		fromToMessageDao = null;
		newMessageDao = null;
	}
}
