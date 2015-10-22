package com.moor.im.model.entity;

import java.util.List;

/**
 * Created by long on 2015/7/22.
 */
public class Discussion {

    private static final long serialVersionUID = 1L;
    public String _id;
    public String account;
    public String creator;
    public String title;
    public Long create_date;
    public String curete_date_display;
    public Long last_update;
    public String last_update_display;
    public List member;
}
