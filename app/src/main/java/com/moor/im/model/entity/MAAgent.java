package com.moor.im.model.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by longwei on 2016/2/18.
 */
public class MAAgent implements Serializable{
        public String account;
        public String pbx;
        public String type;
        public String mobile;
        public String email;
        public String status;
        public String product;
        public String uversion;
        public List<String> role;
        public String lastUpdate;
        public String _id;
        public String callerIDNum;
        public String exten;
        public String password;
        public String displayName;
        public String loginName;
        public String pinyin;
        public String sipExten;
}
