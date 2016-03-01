package com.moor.im.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;

import com.moor.im.R;
import com.moor.im.ui.fragment.mobileassitant.AllCallFragment;
import com.moor.im.ui.fragment.mobileassitant.MyCallFragment;
import com.moor.im.ui.fragment.mobileassitant.RoalUnDealOrderFragment;
import com.moor.im.ui.fragment.mobileassitant.UserUnDealOrderFragment;
import com.moor.im.ui.view.ChangeColorTabItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by longwei on 2016/2/17.
 */
public class MAErpActivity extends FragmentActivity implements
        ViewPager.OnPageChangeListener, View.OnClickListener {

    private ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;
    private List<Fragment> mTabsFragment = new ArrayList<Fragment>();
    private List<ChangeColorTabItem> mTabItem = new ArrayList<ChangeColorTabItem>();
    private Fragment fragment_roal_undeal;
    private Fragment fragment_user_undeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_maerp);

        mViewPager = (ViewPager) findViewById(R.id.ma_viewpager);
        findViewById(R.id.chat_btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        initDatas();

        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(this);
    }

    /**
     * 初始化Fragment以及fragmentadapter
     */
    private void initDatas() {
        fragment_roal_undeal = new RoalUnDealOrderFragment();
        mTabsFragment.add(fragment_roal_undeal);

        fragment_user_undeal = new UserUnDealOrderFragment();
        mTabsFragment.add(fragment_user_undeal);

        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public int getCount() {
                return mTabsFragment.size();
            }

            @Override
            public Fragment getItem(int arg0) {
                return mTabsFragment.get(arg0);
            }
        };

        initTabIndicator();

    }

    /**
     * 初始化底部tab，设置监听
     */
    private void initTabIndicator() {
        ChangeColorTabItem tab_item_mycall = (ChangeColorTabItem) findViewById(R.id.id_ma_mycall);
        ChangeColorTabItem tab_item_allcall = (ChangeColorTabItem) findViewById(R.id.id_ma_allcall);

        mTabItem.add(tab_item_mycall);
        mTabItem.add(tab_item_allcall);

        tab_item_mycall.setOnClickListener(this);
        tab_item_allcall.setOnClickListener(this);

        tab_item_mycall.setIconAlpha(1.0f);
    }

    /**
     * 重置所有tab
     */
    private void resetOtherTabs() {
        for (int i = 0; i < mTabItem.size(); i++) {
            mTabItem.get(i).setIconAlpha(0);
        }
    }

    @Override
    public void onClick(View v) {

        resetOtherTabs();

        switch (v.getId()) {
            case R.id.id_ma_mycall:
                mTabItem.get(0).setIconAlpha(1.0f);
                mViewPager.setCurrentItem(0, false);
                break;
            case R.id.id_ma_allcall:
                mTabItem.get(1).setIconAlpha(1.0f);
                mViewPager.setCurrentItem(1, false);
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
                               int positionOffsetPixels) {
        if (positionOffset > 0) {
            ChangeColorTabItem left = mTabItem.get(position);
            ChangeColorTabItem right = mTabItem.get(position + 1);
            left.setIconAlpha(1 - positionOffset);
            right.setIconAlpha(positionOffset);
        }

    }

    @Override
    public void onPageSelected(int arg0) {

    }
}
