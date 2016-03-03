package com.moor.im.model.entity;

import java.io.Serializable;

/**
 * Created by longwei on 2016/3/3.
 */
public class FieldData implements Serializable{

    private String type;
    private String name;
    private String value;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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
