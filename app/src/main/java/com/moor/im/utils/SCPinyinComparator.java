package com.moor.im.utils;

import com.moor.im.model.entity.ContactBean;
import com.moor.im.model.entity.Contacts;

import java.util.Comparator;

/**
 *
 */
public class SCPinyinComparator implements Comparator<ContactBean> {

	@Override
	public int compare(ContactBean o1, ContactBean o2) {
		if (o1.getPinyin().toUpperCase().substring(0, 1).equals("@")
				|| o2.getPinyin().toUpperCase().substring(0, 1).equals("#")) {
			return -1;
		} else if (o1.getPinyin().toUpperCase().substring(0, 1).equals("#")
				|| o2.getPinyin().toUpperCase().substring(0, 1).equals("@")) {
			return 1;
		} else {
			return o1.getPinyin().toUpperCase().substring(0, 1).compareTo(o2.getPinyin().toUpperCase().substring(0, 1));
		}
	}

}
