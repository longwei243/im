package com.moor.im.model.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by longwei on 2016/2/22.
 */
public class MAOption implements Serializable{

    public String _id;
    public String name;
    public String accountId;
    public Integer cascade;
    public List<String> headers;
    public List<Option> options;
}
