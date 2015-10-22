package com.moor.im.utils;

import com.moor.im.model.entity.Contacts;

import java.util.Comparator;

/**
 *
 */
public class PinyinComparator implements Comparator<Contacts> {

	@Override
	public int compare(Contacts o1, Contacts o2) {
		if (o1.pinyin.toUpperCase().substring(0, 1).equals("@")
				|| o2.pinyin.toUpperCase().substring(0, 1).equals("#")) {
			return -1;
		} else if (o1.pinyin.toUpperCase().substring(0, 1).equals("#")
				|| o2.pinyin.toUpperCase().substring(0, 1).equals("@")) {
			return 1;
		} else {
			return o1.pinyin.toUpperCase().substring(0, 1).compareTo(o2.pinyin.toUpperCase().substring(0, 1));
		}
	}

}
