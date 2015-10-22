package com.moor.im.model.entity;

import java.io.Serializable;
/**
 * 部门和成员
 * @author LongWei
 *
 */
public class DeptAndMember implements Serializable{


	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	private String id;
	/**
	 * 显示的名字
	 */
	private String name;
	/**
	 * 类型，部门为dept,成员为member
	 */
	private String type;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
