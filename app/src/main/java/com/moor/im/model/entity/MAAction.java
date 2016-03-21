package com.moor.im.model.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by longwei on 2016/3/1.
 */
public class MAAction implements Serializable{

    public String _id;
    public String name;
    public String jumpTo;
    public String actionRole;
    public List<MAActionFields> actionFields;
}
