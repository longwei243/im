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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.CacheKey;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.dao.UserDao;
import com.moor.im.http.MobileHttpManager;
import com.moor.im.model.entity.MAAgent;
import com.moor.im.model.entity.MACallLogData;
import com.moor.im.model.entity.MAOption;
import com.moor.im.model.entity.MAQueue;
import com.moor.im.model.entity.Option;
import com.moor.im.model.entity.User;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.model.parser.MobileAssitantParser;
import com.moor.im.ui.activity.AllCallHighQueryActivity;
import com.moor.im.ui.activity.MACallDetailActivity;
import com.moor.im.ui.adapter.MyCallAdapter;
import com.moor.im.ui.base.BaseLazyFragment;
import com.moor.im.ui.dialog.LoadingFragmentDialog;
import com.moor.im.ui.view.pulltorefresh.PullToRefreshBase;
import com.moor.im.ui.view.pulltorefresh.PullToRefreshListView;
import com.moor.im.utils.CacheUtils;
import com.moor.im.utils.MobileAssitantCache;

import org.apache.http.Header;

import java.util.HashMap;
import java.util.List;

/**
 * Created by longwei on 2016/2/17.
 */
public class AllCallFragment extends BaseLazyFragment{
    private static final String ALLCALLQUERYTYPE = "allCallQueryType";

    private List<MACallLogData> maCallLogs;
    private PullToRefreshListView mPullRefreshListView;
    private MyCallAdapter mAdapter;

    private User user = UserDao.getInstance().getUser();

    private LoadingFragmentDialog loadingFragmentDialog;

    private int page = 2;

    private View view;
    private TextView allcall_tv_hignquery;
    private EditText allcall_et_numquery;
    private ImageButton allcall_ib_search;
    private Spinner allcall_sp_quickquery;

    private View footerView;

    private SharedPreferences allCallSp;
    private SharedPreferences.Editor allCallEditor;

    private TextView allcall_tv_queryitem;
    private ImageView allcall_btn_queryitem;
    private RelativeLayout allcall_rl_queryitem;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_allcall, null);
        allCallSp = getActivity().getSharedPreferences(getResources().getString(R.string.mobileAssistant), 0);
        allCallEditor = allCallSp.edit();
        allCallEditor.clear();
        allCallEditor.commit();

        return view;
    }

    @Override
    public void onFirstUserVisible() {
        initViews(view);
    }

    private void initViews(View view) {
        footerView = LayoutInflater.from(getActivity()).inflate(R.layout.footer, null);

        allcall_tv_hignquery = (TextView) view.findViewById(R.id.allcall_tv_hignquery);
        allcall_tv_hignquery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AllCallHighQueryActivity.class);
                startActivityForResult(intent, 0x888);
            }
        });

        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.all_ptl);

        allcall_et_numquery = (EditText) view.findViewById(R.id.allcall_et_numquery);
        allcall_ib_search = (ImageButton) view.findViewById(R.id.allcall_ib_search);
        allcall_ib_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = allcall_et_numquery.getText().toString().trim();
                if (!"".equals(num)) {
                    HashMap<String, String> datas = new HashMap<String, String>();
                    datas.put("ACCOUNT_ID", user.account);
                    datas.put("NUMBER", num);
                    MobileHttpManager.queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    allCallEditor.putString(ALLCALLQUERYTYPE, "number");
                    allCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_AllCallQueryData, datas, CacheUtils.TIME_HOUR * 2);
                    loadingFragmentDialog.show(getActivity().getFragmentManager(), "");
                    allcall_rl_queryitem.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getActivity(), "请输入号码后查询", Toast.LENGTH_SHORT).show();
                }
            }
        });
        loadingFragmentDialog = new LoadingFragmentDialog();

        allcall_sp_quickquery = (Spinner) view.findViewById(R.id.allcall_sp_quickquery);
        final String[] quickDatas = getResources().getStringArray(R.array.mycall);
        ArrayAdapter<String> spAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_text, R.id.sp_tv, quickDatas) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item_layout,
                        null);
                TextView label = (TextView) view
                        .findViewById(R.id.spinner_item_label);
                label.setText(quickDatas[position]);
                if (allcall_sp_quickquery.getSelectedItemPosition() == position) {
                    view.setBackgroundColor(getResources().getColor(
                            R.color.maincolor));
                } else {
                    view.setBackgroundColor(getResources().getColor(
                            R.color.maincolordark));
                }
                return view;
            }
        };
        allcall_sp_quickquery.setAdapter(spAdapter);
        allcall_sp_quickquery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    allCallEditor.clear();
                    allCallEditor.commit();
                    HashMap<String, String> datas = new HashMap<>();
                    datas.put("ACCOUNT_ID", user.account);
                    MobileHttpManager.queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    loadingFragmentDialog.show(getActivity().getFragmentManager(), "");
                    allcall_rl_queryitem.setVisibility(View.GONE);
                } else if (position == 2) {
                    HashMap<String, String> datas = new HashMap<>();
                    datas.put("ACCOUNT_ID", user.account);
                    datas.put("STATUS", "dealing");
                    MobileHttpManager.queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    allCallEditor.putString(ALLCALLQUERYTYPE, "quick");
                    allCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_AllCallQueryData, datas, CacheUtils.TIME_HOUR * 2);
                    loadingFragmentDialog.show(getActivity().getFragmentManager(), "");
                    allcall_rl_queryitem.setVisibility(View.GONE);
                } else if (position == 3) {
                    HashMap<String, String> datas = new HashMap<>();
                    datas.put("ACCOUNT_ID", user.account);
                    datas.put("STATUS", "notDeal");
                    MobileHttpManager.queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    allCallEditor.putString(ALLCALLQUERYTYPE, "quick");
                    allCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_AllCallQueryData, datas, CacheUtils.TIME_HOUR * 2);
                    loadingFragmentDialog.show(getActivity().getFragmentManager(), "");
                    allcall_rl_queryitem.setVisibility(View.GONE);
                } else if (position == 4) {
                    HashMap<String, String> datas = new HashMap<>();
                    datas.put("ACCOUNT_ID", user.account);
                    datas.put("STATUS", "queueLeak");
                    MobileHttpManager.queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    allCallEditor.putString(ALLCALLQUERYTYPE, "quick");
                    allCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_AllCallQueryData, datas, CacheUtils.TIME_HOUR * 2);
                    loadingFragmentDialog.show(getActivity().getFragmentManager(), "");
                    allcall_rl_queryitem.setVisibility(View.GONE);
                } else if (position == 5) {
                    HashMap<String, String> datas = new HashMap<>();
                    datas.put("ACCOUNT_ID", user.account);
                    datas.put("STATUS", "voicemail");
                    MobileHttpManager.queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    allCallEditor.putString(ALLCALLQUERYTYPE, "quick");
                    allCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_AllCallQueryData, datas, CacheUtils.TIME_HOUR * 2);
                    loadingFragmentDialog.show(getActivity().getFragmentManager(), "");
                    allcall_rl_queryitem.setVisibility(View.GONE);
                } else if (position == 6) {
                    HashMap<String, String> datas = new HashMap<>();
                    datas.put("ACCOUNT_ID", user.account);
                    datas.put("STATUS", "leak");
                    MobileHttpManager.queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    allCallEditor.putString(ALLCALLQUERYTYPE, "quick");
                    allCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_AllCallQueryData, datas, CacheUtils.TIME_HOUR * 2);
                    loadingFragmentDialog.show(getActivity().getFragmentManager(), "");
                    allcall_rl_queryitem.setVisibility(View.GONE);
                } else if (position == 7) {
                    HashMap<String, String> datas = new HashMap<>();
                    datas.put("ACCOUNT_ID", user.account);
                    datas.put("STATUS", "blackList");
                    MobileHttpManager.queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    allCallEditor.putString(ALLCALLQUERYTYPE, "quick");
                    allCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_AllCallQueryData, datas, CacheUtils.TIME_HOUR * 2);
                    loadingFragmentDialog.show(getActivity().getFragmentManager(), "");
                    allcall_rl_queryitem.setVisibility(View.GONE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        allcall_rl_queryitem = (RelativeLayout) view.findViewById(R.id.allcall_rl_queryitem);
        allcall_tv_queryitem = (TextView) view.findViewById(R.id.allcall_tv_queryitem);
        allcall_btn_queryitem = (ImageView) view.findViewById(R.id.allcall_btn_queryitem);
        allcall_btn_queryitem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allcall_rl_queryitem.setVisibility(View.GONE);
                loadingFragmentDialog.show(getActivity().getFragmentManager(), "");
                allCallEditor.clear();
                allCallEditor.commit();
                HashMap<String, String> datas = new HashMap<>();
                datas.put("ACCOUNT_ID", user.account);
                MobileHttpManager.queryCdr(user._id, datas, new QueryCdrResponseHandler());

            }
        });

        HashMap<String, String> datas = new HashMap<>();
        datas.put("ACCOUNT_ID", user.account);
        MobileHttpManager.queryCdr(user._id, datas, new QueryCdrResponseHandler());
        loadingFragmentDialog.show(getActivity().getFragmentManager(), "");

    }

    class QueryCdrResponseHandler extends TextHttpResponseHandler {
        @Override
        public void onFailure(int statusCode, Header[] headers,
                              String responseString, Throwable throwable) {
            loadingFragmentDialog.dismiss();
            mPullRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
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
            mPullRefreshListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MACallLogData maCallLogData = (MACallLogData) parent.getAdapter().getItem(position);
                    if (maCallLogData != null) {
                        Intent intent = new Intent(getActivity(), MACallDetailActivity.class);
                        intent.putExtra("calllogdata", maCallLogData);
                        startActivity(intent);
                    }
                }
            });
        }
    }


    /**
     * 加载更多数据
     */
    private void loadDatasMore() {

        String type = allCallSp.getString(ALLCALLQUERYTYPE, "");
        if("".equals(type)) {
            HashMap<String, String> datas = new HashMap<>();
            datas.put("ACCOUNT_ID", user.account);
            datas.put("page", page + "");
            MobileHttpManager.queryCdr(user._id, datas, new GetCdrMoreResponseHandler());
        }else if("number".equals(type)) {
            HashMap<String, String> datas = (HashMap<String, String>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_AllCallQueryData);
            datas.put("page", page + "");
            MobileHttpManager.queryCdr(user._id, datas, new GetCdrMoreResponseHandler());
        }else if("quick".equals(type)) {
            HashMap<String, String> datas = (HashMap<String, String>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_AllCallQueryData);
            datas.put("page", page + "");
            MobileHttpManager.queryCdr(user._id, datas, new GetCdrMoreResponseHandler());
        }else if("high".equals(type)) {
            HashMap<String, String> datas = (HashMap<String, String>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_AllCallQueryData);
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
        if(requestCode == 0x888 && resultCode == Activity.RESULT_OK) {
            if(data.getSerializableExtra("highQueryData") != null) {
                loadingFragmentDialog.show(getActivity().getFragmentManager(), "");
                allcall_sp_quickquery.setSelection(0);
                HashMap<String, String> datas = (HashMap<String, String>) data.getSerializableExtra("highQueryData");
                //显示查询的条件
                showQueryItem(datas);
                datas.put("ACCOUNT_ID", user.account);
                MobileHttpManager.queryCdr(user._id, datas, new QueryCdrResponseHandler());
                allCallEditor.putString(ALLCALLQUERYTYPE, "high");
                allCallEditor.commit();
                MobileApplication.cacheUtil.put(CacheKey.CACHE_AllCallQueryData, datas, CacheUtils.TIME_HOUR * 2);
            }
        }
    }

    private void showQueryItem(HashMap<String, String> datas) {
        StringBuilder sb = new StringBuilder();
        sb.append("查询条件:");
        for(String key : datas.keySet()) {
            sb.append(" ");
            if("CONNECT_TYPE".equals(key)) {
                String connectType = "";
                if("normal".equals(datas.get(key))) {
                    connectType = "普通来电";
                }else if("normal".equals(datas.get(key))) {
                    connectType = "外呼去电";
                }else if("transfer".equals(datas.get(key))) {
                    connectType = "来电转接";
                }else if("dialTransfer".equals(datas.get(key))) {
                    connectType = "外呼转接";
                }
                sb.append(connectType);
                continue;
            }
            if("STATUS".equals(key)) {
                String status = "";
                if("leak".equals(datas.get(key))) {
                    status = "IVR";
                }else if("dealing".equals(datas.get(key))) {
                    status = "已接听";
                }else if("notDeal".equals(datas.get(key))) {
                    status = "振铃未接听";
                }else if("queueLeak".equals(datas.get(key))) {
                    status = "排队放弃";
                }else if("voicemail".equals(datas.get(key))) {
                    status = "已留言";
                }else if("blackList".equals(datas.get(key))) {
                    status = "黑名单";
                }
                sb.append(status);
                continue;
            }

            if("DISPOSAL_AGENT".equals(key)) {
                MAAgent agent = MobileAssitantCache.getInstance().getAgentById(datas.get(key));
                String agentName = agent.displayName;
                sb.append(agentName);
                continue;
            }

            if("ERROR_MEMO".equals(key)) {
                MAQueue queue = MobileAssitantCache.getInstance().getQueueByExten(datas.get(key));
                String queueName = queue.DisplayName;
                sb.append(queueName);
                continue;
            }

            if("INVESTIGATE".equals(key)) {
                if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAOption) != null) {
                    HashMap<String, MAOption> optionMap = (HashMap<String, MAOption>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAOption);
                    for(String optionKey : optionMap.keySet()) {
                        if("满意度调查选项".equals(optionKey)) {
                            List<Option> investigates = optionMap.get(optionKey).options;
                            for(int i=0; i<investigates.size(); i++) {
                                if(datas.get(key).equals(investigates.get(i).options.get(0).name)) {
                                    String investigateName = investigates.get(i).name;
                                    sb.append(investigateName);
                                    break;
                                }
                            }
                        }
                    }
                }
                continue;
            }
            sb.append(datas.get(key));
        }
        allcall_rl_queryitem.setVisibility(View.VISIBLE);
        allcall_tv_queryitem.setText(sb.toString());
    }

}
