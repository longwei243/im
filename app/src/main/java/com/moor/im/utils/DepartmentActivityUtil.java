package com.moor.im.utils;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

public class DepartmentActivityUtil {
	
	private static DepartmentActivityUtil departmentActivityUtil;
	
	public static DepartmentActivityUtil getInstance() {

		if (departmentActivityUtil == null) {
			departmentActivityUtil = new DepartmentActivityUtil();
		}
		return departmentActivityUtil;
	}
	

	private List<Activity> activities = new ArrayList<Activity>();
	
	public void add(Activity a) {
		activities.add(a);
	}
	
	public void exit() {
		for (int i = 0; i < activities.size(); i++) {
			activities.get(i).finish();
		}
	}
}
