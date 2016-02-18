package com.moor.im.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间日期工具类
 * @author LongWei
 *
 */
public class TimeUtil {

	/**
	 * 将时间戳转换为友好显示的时间
	 * @param time
	 * @return
	 */
	public static String convertTimeToFriendly(long time) {
		String str = "";
		long currentTime = System.currentTimeMillis();
		long dxTime = currentTime - time;
		if(dxTime < 60 * 1000) {
			//几秒前
			str = "刚刚";
		}else if(60 * 1000 < dxTime && dxTime < 60*60*1000){
			//几分钟前
			str = (int)(dxTime / (60*1000)) + "分钟前";
		}else if(dxTime > 60*60*1000){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date d = new Date(time);
			Date cd = new Date(currentTime);
			String currentStr = sdf.format(cd);
			String timeStr = sdf.format(d);
			if(currentStr.split(" ")[0].equals(timeStr.split(" ")[0])) {
				str = "今天"+timeStr.substring(11);
			}else if(Integer.parseInt(currentStr.substring(8, 9)) - Integer.parseInt(timeStr.substring(8, 9)) == 1) {
				str = "昨天"+timeStr.substring(11);
			}else if(Integer.parseInt(currentStr.substring(8, 9)) - Integer.parseInt(timeStr.substring(8, 9)) == 2) {
				str = "前天"+timeStr.substring(11);
			}else {
				str = timeStr.substring(5);
			}
		}
		return str;
	}
	/**
	 * 将时间戳转换为友好显示的时间,用于聊天
	 * @param time
	 * @return
	 */
	public static String convertTimeToFriendlyForChat(long time) {
		String str = "";
		long currentTime = System.currentTimeMillis();
		long dxTime = currentTime - time;
		if(dxTime < 60 * 1000) {
			//几秒前
			str = "";
		}else if(60 * 1000 < dxTime && dxTime < 60*60*1000){
			//几分钟前
			str = (int)(dxTime / (60*1000)) + "分钟前";
		}else if(dxTime > 60*60*1000){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date d = new Date(time);
			Date cd = new Date(currentTime);
			String currentStr = sdf.format(cd);
			String timeStr = sdf.format(d);
			if(currentStr.split(" ")[0].equals(timeStr.split(" ")[0])) {
				str = "今天"+timeStr.substring(11);
			}else if(Integer.parseInt(currentStr.substring(8, 9)) - Integer.parseInt(timeStr.substring(8, 9)) == 1) {
				str = "昨天"+timeStr.substring(11);
			}else if(Integer.parseInt(currentStr.substring(8, 9)) - Integer.parseInt(timeStr.substring(8, 9)) == 2) {
				str = "前天"+timeStr.substring(11);
			}else {
				str = timeStr.substring(5);
			}
		}
		return str;
	}

	/**
	 * 获得当前时间
	 * @return
	 */
	public static String getCurrentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		Date d = new Date();
		String currentStr = sdf.format(d);
		return currentStr;
	}

	/**
	 * 获取通话记录显示的时长
	 * @param seconds
	 * @return
	 */
	public static String getContactsLogTime(long seconds) {
		String time = "";
		if(seconds < 0) {
			time = "0";
		}else if(seconds < 60) {
			time = seconds + "";
		}else if(seconds > 60) {
			int min = (int)(seconds / 60);
			int sec = (int)(seconds % 60);
			time = min +"分"+sec;
		}
		return time;
	}

	public static String getShortTime(String time) {
		String _d= time.substring(0, 10);
		String _t= time.substring(11,16);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		String now = sdf.format(date);
		String d = now.substring(0, 10);
		if(_d.equals(d)) {
			return _t;
		}else {
			return _d;
		}
	}
}
