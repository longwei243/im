package com.moor.im.model.parser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 打电话解析数据
 * 
 * @author Mr.li
 * 
 */
public class CallingParser {

	public static String getMessage(String response) {
		JSONObject o;
		String message = "";
		try {
			o = new JSONObject(response);
			message = o.getString("Message");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return message;

	}
}
