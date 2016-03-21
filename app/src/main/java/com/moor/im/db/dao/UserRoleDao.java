package com.moor.im.db.dao;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.DataBaseHelper;
import com.moor.im.model.entity.User;
import com.moor.im.model.entity.UserRole;

import java.sql.SQLException;

/**
 * 用户dao方法
 **
 */
public class UserRoleDao {
	private Context context;
	private Dao<UserRole, Integer> userDao = null;
	private DataBaseHelper helper = DataBaseHelper.getHelper(MobileApplication
			.getInstance());
	private static UserRoleDao instance;

	private UserRoleDao() {
		try {
			userDao = helper.getDao(UserRole.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static UserRoleDao getInstance() {
		if (instance == null) {
			instance = new UserRoleDao();
		}
		return instance;
	}

	/**
	 * 存用户信息
	 */
	public void insertUserRole(UserRole role) {
		try {
			userDao.create(role);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * 删除用户信息
	 */
	public void deleteUserRole() {
		try {
			userDao.delete(userDao.queryForAll());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
