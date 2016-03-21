package com.moor.im.model.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.moor.imkf.model.entity.*;

import java.util.List;

/**
 * Created by longwei on 2016/3/16.
 */
@DatabaseTable(tableName = "userrole")
public class UserRole {

    // 主键 id 自增长
    @DatabaseField(generatedId = true)
    public int id;
    @DatabaseField
    public String role;

    @DatabaseField(canBeNull = true, foreign = true, foreignAutoRefresh = true)
    public User user;
}
