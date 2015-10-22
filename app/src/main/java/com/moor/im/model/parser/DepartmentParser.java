package com.moor.im.model.parser;

import java.util.ArrayList;
import java.util.List;

import com.moor.im.db.dao.ContactsDao;
import com.moor.im.model.entity.Contacts;
import com.moor.im.model.entity.Department;
/**
 * 部门解析层级关系
 * @author LongWei
 *
 */
public class DepartmentParser {

	List<Department> departments;
	
	public DepartmentParser(List<Department> departments) {
		this.departments = departments;
	}

	/**
	 * 获取根部门
	 * @param departments 传入所有的部门信息
	 * @return
	 */
	public List<Department> getRootDepartments() {
		
		List<Department> rootDepartments = new ArrayList<Department>();
		for (int i = 0; i < departments.size(); i++) {
			if(departments.get(i).Root){
				rootDepartments.add(departments.get(i));
			}
		}
		return rootDepartments;
	}
	
	/**
	 * 通过部门id查询该部门
	 * @param id 部门id
	 * @return
	 */
	public Department getDepartmentById(String id) {
		Department d = null;
		for (int i = 0; i < departments.size(); i++) {
			if(id.equals(departments.get(i)._id)) {
				d = departments.get(i);
				return d;
			}
		}
		return null;
	}
	
	/**
	 * 根据根部门获取下级部门
	 * @return
	 */
	public List<Department> getSecondDepartments(Department rootDepartment) {
		
		List<Department> secondDepartment = new ArrayList<Department>();
		
		if(rootDepartment.Subdepartments.size() != 0) {
			for (int i = 0; i < rootDepartment.Subdepartments.size(); i++) {
				String id = (String) rootDepartment.Subdepartments.get(i);
				Department d = getDepartmentById(id);
				secondDepartment.add(d);
			}
			return secondDepartment;
		}
		
		return secondDepartment;
	}
	/**
	 * 根据根部门获取成员
	 * @return
	 */
	public List<Contacts> getMembers(Department rootDepartment) {
		
		List<Contacts> members = new ArrayList<Contacts>();
		
		if(rootDepartment.Members.size() != 0) {
			for (int i = 0; i < rootDepartment.Members.size(); i++) {
				String id = (String) rootDepartment.Members.get(i);
				Contacts contact = ContactsDao.getInstance().getContactById(id);
				if(contact != null) {
					members.add(contact);
				}
			}
			return members;
		}
		
		return members;
	}
	/**
	 * 获取所有子部门
	 * @return
	 */
	public List<Department> getAllSubDepartments() {
		
		List<Department> allSubDepartment = new ArrayList<Department>();
		
		for (int i = 0; i < departments.size(); i++) {
			if(!departments.get(i).Root) {
				//不是根部门就是子部门
				allSubDepartment.add(departments.get(i));
			}
		}
		
		return allSubDepartment;
	}
	
	/**
	 * 该部门是否有子部门
	 * @param department
	 * @return
	 */
	public boolean hasSubDepartment(Department department) {
		if(department.Subdepartments.size() == 0) {
			return false;
		}else {
			return true;
		}
	}
	/**
	 * 该部门是否有子部门
	 * @param department
	 * @return
	 */
	public boolean hasSubDepartment(String id) {
		if(getDepartmentById(id).Subdepartments.size() == 0) {
			return false;
		}else {
			return true;
		}
	}
	/**
	 * 该部门是否有成员
	 * @param department
	 * @return
	 */
	public boolean hasMembers(Department department) {
		if(department.Members.size() == 0) {
			return false;
		}else {
			return true;
		}
	}
	/**
	 * 该部门是否有成员
	 * @param department
	 * @return
	 */
	public boolean hasMembers(String id) {
		if(getDepartmentById(id).Members.size() == 0) {
			return false;
		}else {
			return true;
		}
	}
}
