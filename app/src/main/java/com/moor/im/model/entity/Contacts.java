package com.moor.im.model.entity;

import java.io.Serializable;
import java.util.List;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 联系人列表实体类
 * 
 * @author Mr.li
 * 
 */
@DatabaseTable(tableName = "contacts")
public class Contacts implements Serializable{
	public Contacts() {
	}

	// 主键 id 自增长
	@DatabaseField(generatedId = true)
	public int id;
	@DatabaseField
	public String _id;
	@DatabaseField
	public String account;
	@DatabaseField
	public String pbx;
	@DatabaseField
	public String type;
	@DatabaseField
	public String mobile;
	@DatabaseField
	public String email;
	@DatabaseField
	public String status;
	@DatabaseField
	public String product;
	@DatabaseField
	public String uversion;
	public List role;
	@DatabaseField
	public String lastUpdate;
	@DatabaseField
	public String callerIDNum;
	@DatabaseField
	public String exten;
	@DatabaseField
	public String password;
	@DatabaseField
	public String displayName;
	@DatabaseField
	public String loginName;
	@DatabaseField
	public String pinyin;
	@DatabaseField
	public String sipExten;
	@DatabaseField
	public String im_icon;

}
