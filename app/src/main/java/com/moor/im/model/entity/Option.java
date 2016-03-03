package com.moor.im.model.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by longwei on 2016/2/22.
 */
public class Option implements Serializable{


    public String _id;
    public String name;
    public String state;
    public String key;
    public List<Option> options;
}
