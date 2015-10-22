package com.moor.im.ui.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * 延迟加载的fragment的基类,使用了v4包下的fragment
 * @author LongWei
 *
 */
public class BaseLazyFragment extends Fragment{

	/**
	 * 是否准备好
	 */
	private boolean isPrepared;
	
	/**
	 * 是否第一次调用onResume()
	 */
	private boolean isFirstResume = true;
	
	private boolean isFirstVisible = true;
	private boolean isFirstInvisible = true;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		initPrepare();
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(isFirstResume) {
			isFirstResume = false;
			return;
		}
		
		if(getUserVisibleHint()) {
			onUserVisible();
		}
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(getUserVisibleHint()) {
			onUserVisible();
		}
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
             if (isFirstVisible) {
                 isFirstVisible = false;
                initPrepare();
            } else {
                onUserVisible();
           }
	    } else {
            if (isFirstInvisible) {
                isFirstInvisible = false;
              onFirstUserInvisible();
            } else {
                onUserInvisible();
            }
        }
	}
	
	public synchronized void initPrepare() {
	    if (isPrepared) {
	        onFirstUserVisible();
	    } else {
	        isPrepared = true;
	    }
	}
	
	/**
	 * fragment不可见（切换掉或者onPause）
	 */
	public void onUserInvisible() {}

	/**
	 * 第一次fragment不可见（不建议在此处理事件）
	 */
	public void onFirstUserInvisible() {}

	
	
	/**
	* 第一次fragment可见（进行初始化工作）
	*/
	public void onFirstUserVisible() {}
	
	/**
	 *  fragment可见（切换回来或者onResume）
	 */
	public void onUserVisible() {}
}
