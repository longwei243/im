package com.moor.im.model.parser;

import com.moor.im.app.CacheKey;
import com.moor.im.app.MobileApplication;
import com.moor.im.model.entity.Discussion;
import com.moor.im.model.entity.Group;
import com.moor.im.ui.activity.DiscussionSettingActivity;

import java.util.List;

/**
 * Created by long on 2015/7/17.
 */
public class DiscussionParser {

    private static DiscussionParser discussionParser;

    private DiscussionParser() {}

    public static DiscussionParser getInstance() {
        if(discussionParser == null) {
            discussionParser = new DiscussionParser();
        }
        return discussionParser;
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
        Discussion discussion = getDiscussionById(id);
        if(discussion != null) {
            name = discussion.title;
        }

        return name;
    }

    /**
     * 根据群组ID获取该群组
     * @param id
     * @return
     */
    public Discussion getDiscussionById(String id) {
        Discussion discussion = null;
        if(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_DISCUSSION) == null) {
            return null;
        }
        List<Discussion> discussions = HttpParser.getDiscussion(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_DISCUSSION));
        if(discussions != null) {
            for (int i=0; i<discussions.size(); i++) {
                if(id.equals(discussions.get(i)._id)) {
                    discussion = discussions.get(i);
                    return discussion;
                }
            }
        }

        return null;
    }
}
