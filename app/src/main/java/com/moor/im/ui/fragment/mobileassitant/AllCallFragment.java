package com.moor.im.ui.fragment.mobileassitant;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moor.im.R;
import com.moor.im.ui.view.pulltorefresh.PullToRefreshListView;

/**
 * Created by longwei on 2016/2/17.
 */
public class AllCallFragment extends Fragment{
    private PullToRefreshListView mPullRefreshListView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_allcall, null);
        return view;
    }
}
