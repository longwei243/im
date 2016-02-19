package com.moor.im.ui.fragment.mobileassitant;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.CacheKey;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.dao.ContactsDao;
import com.moor.im.db.dao.UserDao;
import com.moor.im.http.MobileHttpManager;
import com.moor.im.model.entity.MACallLog;
import com.moor.im.model.entity.MACallLogData;
import com.moor.im.model.entity.User;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.model.parser.MobileAssitantParser;
import com.moor.im.ui.activity.MYCallHighQueryActivity;
import com.moor.im.ui.adapter.MyCallAdapter;
import com.moor.im.ui.dialog.LoadingFragmentDialog;
import com.moor.im.ui.view.pulltorefresh.PullToRefreshBase;
import com.moor.im.ui.view.pulltorefresh.PullToRefreshListView;

import org.apache.http.Header;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by longwei on 2016/2/17.
 */
public class MyCallFragment extends Fragment{

    private static final String MYCALLQUERYTYPE = "myCallQueryType";

    private List<MACallLogData> maCallLogs;
    private PullToRefreshListView mPullRefreshListView;
    private MyCallAdapter mAdapter;

    private User user = UserDao.getInstance().getUser();

    private LoadingFragmentDialog loadingFragmentDialog;

    private int page = 2;

    private View view;
    private TextView mycall_tv_hignquery;
    private EditText mycall_et_numquery;
    private ImageButton mycall_ib_search;
    private Spinner mycall_sp_quickquery;

    private View footerView;

    private SharedPreferences myCallSp;
    private SharedPreferences.Editor myCallEditor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mycall, null);
        myCallSp = getActivity().getSharedPreferences(getResources().getString(R.string.mobileAssistant), 0);
        myCallEditor = myCallSp.edit();
//        myCallEditor.clear();
//        myCallEditor.commit();
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        footerView = LayoutInflater.from(getActivity()).inflate(R.layout.footer, null);

        mycall_tv_hignquery = (TextView) view.findViewById(R.id.mycall_tv_hignquery);
        mycall_tv_hignquery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MYCallHighQueryActivity.class);
                startActivityForResult(intent, 0x999);
            }
        });


        mycall_et_numquery = (EditText) view.findViewById(R.id.mycall_et_numquery);
        mycall_ib_search = (ImageButton) view.findViewById(R.id.mycall_ib_search);
        mycall_ib_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = mycall_et_numquery.getText().toString().trim();
                if (!"".equals(num)) {
                    HashMap<String, String> datas = new HashMap<String, String>();
                    datas.put("DISPOSAL_AGENT", user._id);
                    datas.put("NUMBER", num);
                    MobileHttpManager.queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    myCallEditor.putString(MYCALLQUERYTYPE, "number");
                    myCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_MyCallQueryData, datas);
                    loadingFragmentDialog.show(getActivity().getFragmentManager(), "");
                } else {
                    Toast.makeText(getActivity(), "请输入号码后查询", Toast.LENGTH_SHORT).show();
                }
            }
        });
        loadingFragmentDialog = new LoadingFragmentDialog();

        mycall_sp_quickquery = (Spinner) view.findViewById(R.id.mycall_sp_quickquery);
        final String[] quickDatas = getResources().getStringArray(R.array.mycall);
        ArrayAdapter<String> spAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_text, R.id.sp_tv, quickDatas) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item_layout,
                        null);
                TextView label = (TextView) view
                        .findViewById(R.id.spinner_item_label);
                label.setText(quickDatas[position]);
                if (mycall_sp_quickquery.getSelectedItemPosition() == position) {
                    view.setBackgroundColor(getResources().getColor(
                            R.color.maincolor));
                } else {
                    view.setBackgroundColor(getResources().getColor(
                            R.color.maincolordark));
                }
                return view;
            }
        };
        mycall_sp_quickquery.setAdapter(spAdapter);
        mycall_sp_quickquery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    myCallEditor.clear();
                    myCallEditor.commit();
                    HashMap<String, String> datas = new HashMap<>();
                    datas.put("DISPOSAL_AGENT", user._id);
                    MobileHttpManager.queryCdr(user._id, datas, new QueryCdrResponseHandler());
                }else if(position == 1) {
                    HashMap<String, String> datas = new HashMap<>();
                    datas.put("DISPOSAL_AGENT", user._id);
                    datas.put("STATUS", "dealing");
                    MobileHttpManager.queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    myCallEditor.putString(MYCALLQUERYTYPE, "quick");
                    myCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_MyCallQueryData, datas);
                }else if(position == 2) {
                    HashMap<String, String> datas = new HashMap<>();
                    datas.put("DISPOSAL_AGENT", user._id);
                    datas.put("STATUS", "notDeal");
                    MobileHttpManager.queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    myCallEditor.putString(MYCALLQUERYTYPE, "quick");
                    myCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_MyCallQueryData, datas);
                }else if(position == 3) {
                    HashMap<String, String> datas = new HashMap<>();
                    datas.put("DISPOSAL_AGENT", user._id);
                    datas.put("STATUS", "queueLeak");
                    MobileHttpManager.queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    myCallEditor.putString(MYCALLQUERYTYPE, "quick");
                    myCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_MyCallQueryData, datas);
                }else if(position == 4) {
                    HashMap<String, String> datas = new HashMap<>();
                    datas.put("DISPOSAL_AGENT", user._id);
                    datas.put("STATUS", "voicemail");
                    MobileHttpManager.queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    myCallEditor.putString(MYCALLQUERYTYPE, "quick");
                    myCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_MyCallQueryData, datas);
                }else if(position == 5) {
                    HashMap<String, String> datas = new HashMap<>();
                    datas.put("DISPOSAL_AGENT", user._id);
                    datas.put("STATUS", "leak");
                    MobileHttpManager.queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    myCallEditor.putString(MYCALLQUERYTYPE, "quick");
                    myCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_MyCallQueryData, datas);
                }else if(position == 6) {
                    HashMap<String, String> datas = new HashMap<>();
                    datas.put("DISPOSAL_AGENT", user._id);
                    datas.put("STATUS", "blackList");
                    MobileHttpManager.queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    myCallEditor.putString(MYCALLQUERYTYPE, "quick");
                    myCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_MyCallQueryData, datas);
                }
                loadingFragmentDialog.show(getActivity().getFragmentManager(), "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    class QueryCdrResponseHandler extends TextHttpResponseHandler {
        @Override
        public void onFailure(int statusCode, Header[] headers,
                              String responseString, Throwable throwable) {
            loadingFragmentDialog.dismiss();
            Toast.makeText(getActivity(), "请检查您的网络问题！！！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers,
                              String responseString) {
            String succeed = HttpParser.getSucceed(responseString);
            if ("true".equals(succeed)) {
                BackTask backTask = new BackTask();
                backTask.execute(responseString);
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
            mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.my_ptl);
            mPullRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);

            mPullRefreshListView.getRefreshableView().removeFooterView(footerView);

            mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2() {

                @Override
                public void onPullDownToRefresh(PullToRefreshBase refreshView) {
                }

                @Override
                public void onPullUpToRefresh(PullToRefreshBase refreshView) {
                    loadDatasMore();
                }
            });
            mAdapter = new MyCallAdapter(getActivity(), maCallLogDatas);
            mPullRefreshListView.setAdapter(mAdapter);

            if(maCallLogDatas.size() < 10) {
                mPullRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
                mPullRefreshListView.getRefreshableView().addFooterView(footerView);
            }

            page = 2;
        }
    }


    /**
     * 加载更多数据
     */
    private void loadDatasMore() {

        String type = myCallSp.getString(MYCALLQUERYTYPE, "");
        if("".equals(type)) {
            HashMap<String, String> datas = new HashMap<>();
            datas.put("DISPOSAL_AGENT", user._id);
            datas.put("page", page + "");
            MobileHttpManager.queryCdr(user._id, datas, new GetCdrMoreResponseHandler());
        }else if("number".equals(type)) {
            HashMap<String, String> datas = (HashMap<String, String>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MyCallQueryData);
            datas.put("page", page + "");
            MobileHttpManager.queryCdr(user._id, datas, new GetCdrMoreResponseHandler());
        }else if("quick".equals(type)) {
            HashMap<String, String> datas = (HashMap<String, String>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MyCallQueryData);
            datas.put("page", page + "");
            MobileHttpManager.queryCdr(user._id, datas, new GetCdrMoreResponseHandler());
        }else if("high".equals(type)) {
            HashMap<String, String> datas = (HashMap<String, String>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MyCallQueryData);
            datas.put("page", page + "");
            MobileHttpManager.queryCdr(user._id, datas, new GetCdrMoreResponseHandler());
        }


    }
    class GetCdrMoreResponseHandler extends TextHttpResponseHandler {
        @Override
        public void onFailure(int statusCode, Header[] headers,
                              String responseString, Throwable throwable) {
            mPullRefreshListView.onRefreshComplete();
			Toast.makeText(getActivity(), "请检查您的网络问题！！！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers,
                              String responseString) {
            String succeed = HttpParser.getSucceed(responseString);
            if ("true".equals(succeed)) {

                BackTaskMore backTask = new BackTaskMore();
                backTask.execute(responseString);
            } else {
//				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT)
//						.show();
            }
        }
    }

    class BackTaskMore extends AsyncTask<String, Void, List<MACallLogData>> {

        @Override
        protected List<MACallLogData> doInBackground(String[] params) {
            List<MACallLogData> callLogs = MobileAssitantParser.getCdrs(params[0]);
            return callLogs;
        }

        @Override
        protected void onPostExecute(List<MACallLogData> maCallLogDatas) {
            super.onPostExecute(maCallLogDatas);
            if(maCallLogDatas.size() < 10) {
                //是最后一页了
                maCallLogs.addAll(maCallLogDatas);
                mAdapter.notifyDataSetChanged();
                mPullRefreshListView.onRefreshComplete();

                mPullRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
                mPullRefreshListView.getRefreshableView().addFooterView(footerView);
            }else {
                maCallLogs.addAll(maCallLogDatas);
                mAdapter.notifyDataSetChanged();
                mPullRefreshListView.onRefreshComplete();
                page++;
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0x999 && resultCode == Activity.RESULT_OK) {
            if(data.getSerializableExtra("highQueryData") != null) {
                loadingFragmentDialog.show(getActivity().getFragmentManager(), "");
                HashMap<String, String> datas = (HashMap<String, String>) data.getSerializableExtra("highQueryData");
                datas.put("DISPOSAL_AGENT", user._id);
                MobileHttpManager.queryCdr(user._id, datas, new QueryCdrResponseHandler());
                myCallEditor.putString(MYCALLQUERYTYPE, "high");
                myCallEditor.commit();
                MobileApplication.cacheUtil.put(CacheKey.CACHE_MyCallQueryData, datas);
            }
        }
    }
}
