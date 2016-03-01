package com.moor.im.model.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by longwei on 2016/3/1.
 */
public class MABusinessStep implements Serializable{

    public String _id;
    public String flow_id;
    public String name;
    public String master;
    public String type;
    public String createUser;
    public String createTime;
    public String systemFn;
    public String lastUpdateTime;
    public Boolean isBegin;
    public List<MAStepFields> stepFields;
    public List<MAAction> actions;
}
