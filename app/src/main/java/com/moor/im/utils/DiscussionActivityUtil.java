package com.moor.im.utils;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by long on 2015/7/22.
 */
public class DiscussionActivityUtil {

    private static DiscussionActivityUtil instance;

    public static DiscussionActivityUtil getInstance() {

        if (instance == null) {
            instance = new DiscussionActivityUtil();
        }
        return instance;
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
