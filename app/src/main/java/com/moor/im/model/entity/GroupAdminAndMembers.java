package com.moor.im.model.entity;

/**
 * Created by long on 2015/7/21.
 */
public class GroupAdminAndMembers {

    private String _id;

    private String type;

    private String imicon;

    private String name;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }
    public String getImicon() {
        return imicon;
    }

    public void setImicon(String imicon) {
        this.imicon = imicon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
