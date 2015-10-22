package com.moor.im.model.parser;

import com.moor.im.app.CacheKey;
import com.moor.im.app.MobileApplication;
import com.moor.im.model.entity.Group;
import java.util.List;

/**
 * Created by long on 2015/7/17.
 */
public class GroupParser {

    private static GroupParser groupParser;

    private GroupParser() {}

    public static GroupParser getInstance() {
        if(groupParser == null) {
            groupParser = new GroupParser();
        }
        return groupParser;
    }

    /**
     * 根据该群组ID获取名字
     * @param id
     * @return
     */
    public String getNameById(String id) {

        if("System".equals(id)) {
            return "系统通知";
        }
        String name = "";
        Group group = getGroupById(id);
        if(group != null) {
            name = group.title;
        }

        return name;
    }

    /**
     * 根据群组ID获取该群组
     * @param id
     * @return
     */
    public Group getGroupById(String id) {
        Group group = null;
        if(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_GROUP) == null) {
            return null;
        }
        List<Group> groups = HttpParser.getGroups(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_GROUP));
        if(groups != null) {
            for (int i=0; i<groups.size(); i++) {
                if(id.equals(groups.get(i)._id)) {
                    group = groups.get(i);
                    return group;
                }
            }
        }

        return null;
    }
}
