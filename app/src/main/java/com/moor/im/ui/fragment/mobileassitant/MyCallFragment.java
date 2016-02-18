package com.moor.im.ui.fragment.mobileassitant;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.db.dao.ContactsDao;
import com.moor.im.db.dao.UserDao;
import com.moor.im.http.MobileHttpManager;
import com.moor.im.model.entity.MACallLog;
import com.moor.im.model.entity.MACallLogData;
import com.moor.im.model.entity.User;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.model.parser.MobileAssitantParser;
import com.moor.im.ui.adapter.MyCallAdapter;
import com.moor.im.ui.dialog.LoadingFragmentDialog;
import com.moor.im.ui.view.pulltorefresh.PullToRefreshBase;
import com.moor.im.ui.view.pulltorefresh.PullToRefreshListView;

import org.apache.http.Header;

import java.util.HashMap;
import java.util.List;

/**
 * Created by longwei on 2016/2/17.
 */
public class MyCallFragment extends Fragment{
    private List<MACallLogData> maCallLogs;
    private PullToRefreshListView mPullRefreshListView;
    private MyCallAdapter mAdapter;

    private User user = UserDao.getInstance().getUser();

    private LoadingFragmentDialog loadingFragmentDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mycall, null);
        initViews(view);
        return view;
    }

    private void initViews(View view) {

        loadingFragmentDialog = new LoadingFragmentDialog();

        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.my_ptl);
        mPullRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);

        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase refreshView) {
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase refreshView) {
                Toast.makeText(getActivity(), "上拉加载更多", Toast.LENGTH_SHORT).show();
            }
        });
        loadingFragmentDialog.show(getActivity().getFragmentManager(), "");
//        MobileHttpManager.getCdr(user._id, new GetCdrResponseHandler());
        HashMap<String, String> datas = new HashMap<>();
        datas.put("ACCOUNT_ID", user.account);
        MobileHttpManager.queryCdr(user._id, datas, new GetCdrResponseHandler());

    }

    class GetCdrResponseHandler extends TextHttpResponseHandler {
        @Override
        public void onFailure(int statusCode, Header[] headers,
                              String responseString, Throwable throwable) {
//			Toast.makeText(getActivity(), "请检查您的网络问题！！！", 3000).show();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers,
                              String responseString) {
            String succeed = HttpParser.getSucceed(responseString);
            if ("true".equals(succeed)) {

                BackTask backTask = new BackTask();
                backTask.execute(responseString);
//                maCallLogs = MobileAssitantParser.getCdrs(responseString);
//                mAdapter = new MyCallAdapter(getActivity(), maCallLogs);
//                mPullRefreshListView.setAdapter(mAdapter);
            } else {
//				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT)
//						.show();
            }
        }
    }


    class BackTask extends AsyncTask<String, Void, List<MACallLogData>> {

        @Override
        protected List<MACallLogData> doInBackground(String[] params) {
            maCallLogs = MobileAssitantParser.getCdrs(params[0]);
            return maCallLogs;
        }

        @Override
        protected void onPostExecute(List<MACallLogData> maCallLogDatas) {
            super.onPostExecute(maCallLogDatas);
            loadingFragmentDialog.dismiss();
            mAdapter = new MyCallAdapter(getActivity(), maCallLogDatas);
            mPullRefreshListView.setAdapter(mAdapter);
        }
    }
}
