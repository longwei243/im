package com.moor.im.model.entity;

import java.io.Serializable;

/**
 * Created by longwei on 2016/2/22.
 */
public class QueryData implements Serializable{

    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


}
