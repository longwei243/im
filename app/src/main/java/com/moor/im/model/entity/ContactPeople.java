package com.moor.im.model.entity;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 联系人数据,使用ormlite映射到了数据库中
 * @author LongWei
 *
 */
@DatabaseTable
public class ContactPeople implements Serializable{

	private static final long serialVersionUID = 1L;
	
	/**
	 * 数据库中的主键
	 */
	@DatabaseField(generatedId=true)
	private Integer _id;
	/**
	 * 姓名
	 */
	@DatabaseField
	private String name;
	/**
	 * 排序字母
	 */
	@DatabaseField
	private String sortLetters;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSortLetters() {
		return sortLetters;
	}
	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}
	public Integer get_id() {
		return _id;
	}
	public void set_id(Integer _id) {
		this._id = _id;
	}
}
