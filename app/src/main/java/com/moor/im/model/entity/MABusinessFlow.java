package com.moor.im.model.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by longwei on 2016/3/1.
 */
public class MABusinessFlow implements Serializable{

    public String _id;
    public String name;
    public String account;
    public String createUser;
    public String lastUpdateTime;
    public String status;
    public List<MABusinessStep> steps;
    public List<MABusinessField> fields;

}
