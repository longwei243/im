package com.moor.im.db.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.DataBaseHelper;
import com.moor.im.model.entity.Contacts;

/**
 * 联系人数据库操作
 * 
 * @author LongWei
 * 
 */
public class ContactsDao {

	private static ContactsDao instance;

	private Dao<Contacts, Integer> dao;

	private DataBaseHelper helper = DataBaseHelper.getHelper(MobileApplication
			.getInstance());

	private ContactsDao() {
		try {
			dao = helper.getDao(Contacts.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static ContactsDao getInstance() {
		if (instance == null) {
			instance = new ContactsDao();
		}
		return instance;
	}

	/**
	 * 存储所有联系人信息
	 * 
	 * @param contacts
	 */
	public void saveContacts(List<Contacts> contacts) {
		for (int i = 0; i < contacts.size(); i++) {
			try {
				dao.create(contacts.get(i));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 读取所有联系人信息
	 * 
	 * @return
	 */
	public List<Contacts> getContacts() {
		try {
			List<Contacts> list = dao.queryForAll();
			return list;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 模糊查询
	 * @return
	 */
	public List<Contacts> getContactsByMoHu(String key) {
		try {
			List<Contacts> list = dao.queryBuilder().where().like("displayName", "%"+key+"%").or().like("exten", "%" + key + "%").or().like("pinyin", "%" + key + "%").query();
			return list;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 通过id取到显示名字
	 * 
	 * @param _id
	 * @return
	 */
	public String getContactsName(String _id) {
		String name = "";
		try {
			Contacts contact = dao.queryBuilder().where().eq("_id", _id)
					.queryForFirst();
			if (contact != null) {
				name = contact.displayName;
				return name;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 通过id取到显示名字
	 *
	 * @param _id
	 * @return
	 */
	public String getContactsIcon(String _id) {
		String icon = "";
		try {
			Contacts contact = dao.queryBuilder().where().eq("_id", _id)
					.queryForFirst();
			if (contact != null) {
				icon = contact.im_icon;
				return icon;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 通过id取到该联系人
	 * 
	 * @param _id
	 * @return
	 */
	public Contacts getContactById(String _id) {
		Contacts contact = new Contacts();
		try {
			contact = dao.queryBuilder().where().eq("_id", _id)
					.queryForFirst();
			if (contact != null) {
				return contact;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return contact;
	}
	/**
	 * 清空联系人数据库
	 */
	public void clear() {
		try {
			dao.delete(dao.queryForAll());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
