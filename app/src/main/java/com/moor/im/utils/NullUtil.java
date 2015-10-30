package com.moor.im.utils;

/**
 * 检测值是否为空
 * Created by longwei on 2015/10/30.
 */
public class NullUtil {

    /**
     * 检测字符串是否是空的，若是空的则返回空字符串，避免空指针错误
     * @param str
     * @return
     */
    public static String checkNull(String str) {
            if(str != null && !"".equals(str)) {
                return str;
            }else {
                return "";
            }
    }
}
