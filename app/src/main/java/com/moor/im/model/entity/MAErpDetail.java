package com.moor.im.model.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by longwei on 2016/3/4.
 */
public class MAErpDetail implements Serializable{

    public String _id;
    public String flow;
    public String flowId;
    public String step;
    public String stepId;
    public String lastUpdateUser;
    public String lastUpdateTime;

    public List<MAAction> actions;
    public List<FieldData> fieldDatas;
    public List<MAErpHistory> historyList;
}
