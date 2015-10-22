package com.moor.im.model.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 组织结构
 * @author LongWei
 *
 */
public class Department implements Serializable{

	private static final long serialVersionUID = 1L;
	public String _id;
	public String Name;
	public String Description;
	public boolean Root;
	public String Account;
	public String Create_user;
	public String Create_date;
	public String Lastupdate;
	public String Update_user;
	public List Members;
	public List Subdepartments;
	
}
