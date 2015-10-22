package com.moor.im.model.entity;

/**
 * 通话记录
 * @author LongWei
 *
 */
public class CallLog {

	/**
	 * _id
	 */
	private long _id;
	/**
	 * 号码
	 */
	private String number;
	/**
	 * 日期
	 */
	private long date;
	/**
	 * 通话时间
	 */
	private long duration;
	/**
	 * 显示的名字
	 */
	private String displayName;
	/**
	 * 呼叫类型
	 */
	private String type;
	
	public long get_id() {
		return _id;
	}
	public long getDate() {
		return date;
	}
	public String getDisplayName() {
		return displayName;
	}
	public long getDuration() {
		return duration;
	}
	public String getNumber() {
		return number;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public void setDate(long date) {
		this.date = date;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
