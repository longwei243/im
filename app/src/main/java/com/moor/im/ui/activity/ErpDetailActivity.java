package com.moor.im.ui.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.app.RequestUrl;
import com.moor.im.db.dao.UserDao;
import com.moor.im.event.ErpExcuteSuccess;
import com.moor.im.http.MobileHttpManager;
import com.moor.im.model.entity.FieldData;
import com.moor.im.model.entity.MAAction;
import com.moor.im.model.entity.MAActionFields;
import com.moor.im.model.entity.MAAgent;
import com.moor.im.model.entity.MABusinessField;
import com.moor.im.model.entity.MABusinessFlow;
import com.moor.im.model.entity.MABusinessStep;
import com.moor.im.model.entity.MACol;
import com.moor.im.model.entity.MAErpDetail;
import com.moor.im.model.entity.MAErpHistory;
import com.moor.im.model.entity.MAFields;
import com.moor.im.model.entity.MAOption;
import com.moor.im.model.entity.MAStepFields;
import com.moor.im.model.entity.Option;
import com.moor.im.model.entity.QueryData;
import com.moor.im.model.entity.User;
import com.moor.im.model.entity.UserRole;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.ui.adapter.ErpAgentSpAdapter;
import com.moor.im.ui.adapter.ErpCBAdapter;
import com.moor.im.ui.adapter.ErpSpAdapter;
import com.moor.im.ui.adapter.SPAdapter;
import com.moor.im.ui.dialog.LoadingFragmentDialog;
import com.moor.im.ui.dialog.UploadFileDialog;
import com.moor.im.ui.view.GridViewInScrollView;
import com.moor.im.ui.view.RoundImageView;
import com.moor.im.utils.MobileAssitantCache;
import com.moor.im.utils.NullUtil;
import com.moor.im.utils.WindowUtils;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by longwei on 2016/3/2.
 */
public class ErpDetailActivity extends Activity{

    private User user = UserDao.getInstance().getUser();

    private TextView erpdetail_tv_customerName, erpdetail_tv_flow, erpdetail_tv_step,
            erpdetail_tv_lastUpdateUser, erpdetail_tv_lastUpdateTime;
    private LinearLayout erpdetail_ll_fields, erpdetail_ll_history, erp_detail_start_ll;
    private LoadingFragmentDialog loadingFragmentDialog;
    private Spinner erpdetail_sp_action;
    private ScrollView erpdetail_sv;
    private EditText erpdetail_et_backinfo;
    private TextView erpdetail_btn_save;
    private RoundImageView erpdetail_iv_imicon;

    String type;

    MAErpDetail detail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        MobileApplication.getInstance().add(this);
        setContentView(R.layout.activity_erpdetail);
        EventBus.getDefault().register(this);
        Intent intent = getIntent();
        String busId = intent.getStringExtra("busId");
        String customerName = intent.getStringExtra("customerName");
        final String customerId = intent.getStringExtra("customerId");
        type = intent.getStringExtra("type");
        erpdetail_sv = (ScrollView) findViewById(R.id.erpdetail_sv);
        erpdetail_tv_customerName = (TextView) findViewById(R.id.erpdetail_tv_customerName);
        erpdetail_tv_customerName.setText(customerName);

        erpdetail_tv_flow = (TextView) findViewById(R.id.erpdetail_tv_flow);
        erpdetail_tv_step = (TextView) findViewById(R.id.erpdetail_tv_step);
        erpdetail_tv_lastUpdateUser = (TextView) findViewById(R.id.erpdetail_tv_lastUpdateUser);
        erpdetail_tv_lastUpdateTime = (TextView) findViewById(R.id.erpdetail_tv_lastUpdateTime);
        erpdetail_iv_imicon = (RoundImageView) findViewById(R.id.erpdetail_iv_imicon);
        erpdetail_ll_fields = (LinearLayout) findViewById(R.id.erpdetail_ll_fields);
        erpdetail_ll_history = (LinearLayout) findViewById(R.id.erpdetail_ll_history);
        erp_detail_start_ll = (LinearLayout) findViewById(R.id.erp_detail_start_ll);

        erpdetail_sp_action = (Spinner) findViewById(R.id.erpdetail_sp_action);

        loadingFragmentDialog = new LoadingFragmentDialog();
        loadingFragmentDialog.show(getFragmentManager(), "");

        erpdetail_et_backinfo = (EditText) findViewById(R.id.erpdetail_et_backinfo);
        erpdetail_btn_save = (TextView) findViewById(R.id.erpdetail_btn_save);
        erpdetail_btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String backInfoStr = erpdetail_et_backinfo.getText().toString().trim();
                if (!"".equals(backInfoStr)) {
                    saveBackInfo(backInfoStr);
                } else {
                    Toast.makeText(ErpDetailActivity.this, "请输入备注内容", Toast.LENGTH_SHORT).show();
                }
            }
        });

        MobileHttpManager.getBusinessDetailById(user._id, busId, new GetBusinessDetailResponseHandler());

        findViewById(R.id.chat_btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        RelativeLayout erpdetail_rl_customer = (RelativeLayout) findViewById(R.id.erpdetail_rl_customer);
        erpdetail_rl_customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent customerIntent = new Intent(ErpDetailActivity.this, ErpCustomerDetailActivity.class);
//                customerIntent.putExtra("customerId", customerId);
//                startActivity(customerIntent);
            }
        });
    }

    /**
     * 保存备注
     * @param backInfoStr
     */
    private void saveBackInfo(String backInfoStr) {
        MobileHttpManager.saveBusinessBackInfo(user._id, detail._id, backInfoStr, new SaveBackInfoResponseHandler(backInfoStr));
    }

    class SaveBackInfoResponseHandler extends TextHttpResponseHandler {

        private String backinfoStr;

        public SaveBackInfoResponseHandler(String backinfoStr) {
            this.backinfoStr = backinfoStr;
        }

        @Override
        public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
            Toast.makeText(ErpDetailActivity.this, "保存备注失败", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess(int i, Header[] headers, String s) {
            String succeed = HttpParser.getSucceed(s);
            if ("true".equals(succeed)) {
                erpdetail_et_backinfo.setText("");

                //填充备注布局
                LinearLayout backInfoView = (LinearLayout) LayoutInflater.from(ErpDetailActivity.this).inflate(R.layout.erp_backinfo_item, null);
                TextView erp_backinfo_item_tv = (TextView) backInfoView.findViewById(R.id.erp_backinfo_item_tv);
                erp_backinfo_item_tv.setText(backinfoStr);

                TextView erp_history_tv_name = (TextView) backInfoView.findViewById(R.id.erp_history_tv_name);
                erp_history_tv_name.setText(user.displayName);

                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateStr = sdf.format(date);
                TextView erp_history_tv_time = (TextView) backInfoView.findViewById(R.id.erp_history_tv_time);
                erp_history_tv_time.setText(dateStr);

                TextView erp_history_tv_info = (TextView) backInfoView.findViewById(R.id.erp_history_tv_info);
                erp_history_tv_info.setText("添加 新的备注！");

                RoundImageView erp_history_iv_imicon = (RoundImageView) backInfoView.findViewById(R.id.erp_history_iv_imicon);
                if(user.im_icon != null && !"".equals(user.im_icon)) {
                    Glide.with(MobileApplication.getInstance()).load(user.im_icon+"?imageView2/0/w/100/h/100").asBitmap().placeholder(R.drawable.ic_person_grey600_48dp).into(erp_history_iv_imicon);
                }else {
                    Glide.with(MobileApplication.getInstance()).load(R.drawable.ic_person_grey600_48dp).asBitmap().into(erp_history_iv_imicon);
                }

                erpdetail_ll_history.addView(backInfoView, 0);
            }
        }
    }

    class GetBusinessDetailResponseHandler extends TextHttpResponseHandler {

        @Override
        public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
            loadingFragmentDialog.dismiss();
        }

        @Override
        public void onSuccess(int i, Header[] headers, String s) {
            String succeed = HttpParser.getSucceed(s);
            if ("true".equals(succeed)) {
                BackTask backTask = new BackTask();
                backTask.execute(s);
            }
        }
    }

    class BackTask extends AsyncTask<String, Void, MAErpDetail> {

        @Override
        protected MAErpDetail doInBackground(String[] params) {
            return initDatas(params[0]);
        }

        @Override
        protected void onPostExecute(MAErpDetail detail) {
            super.onPostExecute(detail);
            initDetailViews(detail);

        }
    }

    /**
     * 填充数据视图
     * @param detail
     */
    private void initDetailViews(final MAErpDetail detail) {
        erpdetail_tv_flow.setText(detail.flow);
        erpdetail_tv_step.setText(detail.step);
        erpdetail_tv_lastUpdateUser.setText(detail.lastUpdateUser);
        erpdetail_tv_lastUpdateTime.setText(detail.lastUpdateTime);
        if(!"".equals(detail.imIcon)) {
            Glide.with(MobileApplication.getInstance()).load(detail.imIcon+"?imageView2/0/w/100/h/100").asBitmap().placeholder(R.drawable.ic_person_grey600_48dp).into(erpdetail_iv_imicon);
        }else {
            Glide.with(MobileApplication.getInstance()).load(R.drawable.ic_person_grey600_48dp).asBitmap().into(erpdetail_iv_imicon);
        }
        boolean isBegin = MobileAssitantCache.getInstance().getBusinessStep(detail.stepId).isBegin;
        if(isBegin) {
            //是开始步骤，退回过的工单
            initStartStepViews(detail);
        }else {
            //上面字段信息
            List<FieldData> fdList = detail.fieldDatas;
            LinearLayout parentLinearLayout = new LinearLayout(ErpDetailActivity.this);
            parentLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            parentLinearLayout.setOrientation(LinearLayout.VERTICAL);
            for(int i=0; i<fdList.size(); i++) {
                //填充布局
                LinearLayout fieldLL = new LinearLayout(ErpDetailActivity.this);
                fieldLL.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                fieldLL.setOrientation(LinearLayout.HORIZONTAL);

                fieldLL.setPadding(WindowUtils.dip2px(16), 0, 0, 0);

                final FieldData fd = fdList.get(i);
                if("file".equals(fd.getType())) {
                    //附件
                    TextView tv_field_name = new TextView(ErpDetailActivity.this);
                    tv_field_name.setText(fd.getName());
                    tv_field_name.setTextColor(getResources().getColor(R.color.all_black));
                    LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    fieldLL.addView(tv_field_name, mLayoutParams);
                    TextView tv_field_value = new TextView(ErpDetailActivity.this);
                    tv_field_value.setText(fdList.get(i).getValue());
                    tv_field_value.setTextColor(getResources().getColor(R.color.abs__holo_blue_light));
                    tv_field_value.setPadding(WindowUtils.dip2px(16), 0, WindowUtils.dip2px(4), 0);
                    tv_field_value.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String urlStr = RequestUrl.QiniuHttpMobile + fd.getId();
                            Uri  uri = Uri.parse(urlStr);
                            Intent  intent = new  Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }
                    });
                    fieldLL.addView(tv_field_value, mLayoutParams);
                }else {
                    TextView tv_field_name = new TextView(ErpDetailActivity.this);
                    tv_field_name.setText(fdList.get(i).getName());
                    tv_field_name.setTextColor(getResources().getColor(R.color.all_black));
                    LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    fieldLL.addView(tv_field_name, mLayoutParams);
                    TextView tv_field_value = new TextView(ErpDetailActivity.this);
                    tv_field_value.setText(fdList.get(i).getValue());
                    tv_field_value.setTextColor(getResources().getColor(R.color.grey));
                    tv_field_value.setPadding(WindowUtils.dip2px(16), 0, WindowUtils.dip2px(4), 0);
                    fieldLL.addView(tv_field_value, mLayoutParams);
                }

                parentLinearLayout.addView(fieldLL);
            }
            erpdetail_ll_fields.addView(parentLinearLayout);
        }

        //历史
        List<MAErpHistory> historyList = detail.historyList;
        for (int h=0; h<historyList.size(); h++) {
            MAErpHistory historyData = historyList.get(h);
            View infoView = LayoutInflater.from(ErpDetailActivity.this).inflate(R.layout.erp_history_info, null);
            TextView erp_history_tv_name = (TextView) infoView.findViewById(R.id.erp_history_tv_name);
            erp_history_tv_name.setText(historyData.name);
            TextView erp_history_tv_time = (TextView) infoView.findViewById(R.id.erp_history_tv_time);
            erp_history_tv_time.setText(historyData.time);
            TextView erp_history_tv_info = (TextView) infoView.findViewById(R.id.erp_history_tv_info);
            erp_history_tv_info.setText(historyData.info);
            RoundImageView erp_history_iv_imicon = (RoundImageView) infoView.findViewById(R.id.erp_history_iv_imicon);
            if(!"".equals(historyData.imIcon)) {
                Glide.with(MobileApplication.getInstance()).load(historyData.imIcon+"?imageView2/0/w/100/h/100").asBitmap().placeholder(R.drawable.ic_person_grey600_48dp).into(erp_history_iv_imicon);
            }else {
                Glide.with(MobileApplication.getInstance()).load(R.drawable.ic_person_grey600_48dp).asBitmap().into(erp_history_iv_imicon);
            }
            erpdetail_ll_history.addView(infoView);

            LinearLayout history_field_ll = new LinearLayout(ErpDetailActivity.this);
            history_field_ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            history_field_ll.setOrientation(LinearLayout.VERTICAL);
            history_field_ll.setPadding(WindowUtils.dip2px(50), 0, 0, 0);
            List<FieldData> historyFieldDatas = historyData.historyData;
            for(int f=0; f<historyFieldDatas.size(); f++) {

                LinearLayout historyFieldLL = new LinearLayout(ErpDetailActivity.this);
                historyFieldLL.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                historyFieldLL.setOrientation(LinearLayout.HORIZONTAL);
                historyFieldLL.setPadding(WindowUtils.dip2px(16), 0, WindowUtils.dip2px(4), 0);
                final FieldData fd = historyFieldDatas.get(f);
                if("file".equals(fd.getType())) {
                    //附件
                    TextView tv_field_name = new TextView(ErpDetailActivity.this);
                    tv_field_name.setText(fd.getName());
                    tv_field_name.setTextColor(getResources().getColor(R.color.all_black));
                    LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    historyFieldLL.addView(tv_field_name, mLayoutParams);
                    TextView tv_field_value = new TextView(ErpDetailActivity.this);
                    tv_field_value.setText(fd.getValue());
                    tv_field_value.setTextColor(getResources().getColor(R.color.abs__holo_blue_light));
                    tv_field_value.setPadding(WindowUtils.dip2px(16), 0, WindowUtils.dip2px(4), 0);
                    tv_field_value.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String urlStr = RequestUrl.QiniuHttpMobile + fd.getId();
                            Uri  uri = Uri.parse(urlStr);
                            Intent  intent = new  Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }
                    });
                    historyFieldLL.addView(tv_field_value, mLayoutParams);
                }else {
                    TextView tv_field_name = new TextView(ErpDetailActivity.this);
                    tv_field_name.setText(fd.getName());
                    tv_field_name.setTextColor(getResources().getColor(R.color.all_black));
                    LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    historyFieldLL.addView(tv_field_name, mLayoutParams);
                    TextView tv_field_value = new TextView(ErpDetailActivity.this);
                    tv_field_value.setText(fd.getValue());
                    tv_field_value.setTextColor(getResources().getColor(R.color.grey));
                    tv_field_value.setPadding(WindowUtils.dip2px(16), 0, WindowUtils.dip2px(4), 0);
                    historyFieldLL.addView(tv_field_value, mLayoutParams);
                }
                history_field_ll.addView(historyFieldLL);
            }
            erpdetail_ll_history.addView(history_field_ll);

            if(h != historyList.size()-1) {
                View v = new View(ErpDetailActivity.this);
                LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 2);
                v.setBackgroundColor(getResources().getColor(R.color.grey));
                v.setPadding(0,2,0,2);
                erpdetail_ll_history.addView(v, vlp);
            }
        }

        //动作,注意角色权限控制
        List<MAAction> actionList = detail.actions;
        List<QueryData> actionDatas = new ArrayList<>();
        QueryData qd = new QueryData();
        qd.setName("开始操作");
        qd.setValue("");
        actionDatas.add(qd);
        Collection<UserRole> userRoles = user.userRoles;
        List<String> roles = new ArrayList<>();
        if(userRoles != null && userRoles.size() > 0) {
            for (UserRole ur : userRoles) {
                roles.add(ur.role);
            }
        }

        for (int c=0; c<actionList.size(); c++) {
            MAAction action = actionList.get(c);

            if(arrayContainsStr(roles, action.actionRole)) {
                QueryData qd1 = new QueryData();
                qd1.setName(action.name);
                qd1.setValue(action._id);
                actionDatas.add(qd1);
            }
        }

        QueryData qd_back = new QueryData();
        qd_back.setName("退回");
        qd_back.setValue("退回");
        actionDatas.add(qd_back);
        SPAdapter actionAdapter = new SPAdapter(ErpDetailActivity.this, actionDatas);
        erpdetail_sp_action.setAdapter(actionAdapter);
        erpdetail_sp_action.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                QueryData queryData = (QueryData) parent.getAdapter().getItem(position);

                String actionId = queryData.getValue();
                MAErpDetail business = detail;
                String stepId = business.stepId;
                MAAction action = MobileAssitantCache.getInstance().getBusinessStepAction(stepId, actionId);

                if (action != null) {
                    String nextStepId = action.jumpTo;
                    MABusinessStep nextStep = MobileAssitantCache.getInstance().getBusinessStep(nextStepId);
                    if ("sys".equals(nextStep.type)) {
                        //下一步是系统步骤且没有配置界面，直接执行
                        MABusinessStep step = MobileAssitantCache.getInstance().getBusinessStep(stepId);
                        MAAction act = getFlowStepActionById(step.actions, actionId);
                        if (act != null) {
                            List<MAActionFields> actionFields = act.actionFields;
                            if (actionFields.size() == 0) {
                                //执行操作
                                HashMap<String, String> datas = new HashMap<String, String>();
                                datas.put("_id", business._id);
                                datas.put("actionId", act._id);
                                datas.put("master", "sys");

                                MobileHttpManager.excuteBusinessStepAction(user._id, datas, new HashMap<String, JSONArray>(),new ExcuteBusHandler());
                            } else {
                                Intent intent = new Intent(ErpDetailActivity.this, ErpActionProcessActivity.class);
                                intent.putExtra("actionId", actionId);
                                intent.putExtra("business", business);
                                startActivity(intent);
                            }
                        }
                    } else {
                        Intent intent = new Intent(ErpDetailActivity.this, ErpActionProcessActivity.class);
                        intent.putExtra("actionId", actionId);
                        intent.putExtra("business", business);
                        startActivity(intent);
                    }
                }else if("退回".equals(actionId)) {
                    Intent intent = new Intent(ErpDetailActivity.this, ErpActionBackActivity.class);
                    intent.putExtra("business", business);
                    startActivity(intent);
                }
                erpdetail_sp_action.setSelection(0);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //判断是否需要显示动作操作
        if("roalundeal".equals(type)) {
            //隐藏
            erpdetail_sp_action.setVisibility(View.GONE);
        }else if("userundeal".equals(type)) {
            erpdetail_sp_action.setVisibility(View.VISIBLE);
            //已完成的也隐藏
            if("complete".equals(detail.status) || "cancel".equals(detail.status) || isBegin) {
                erpdetail_sp_action.setVisibility(View.GONE);
            }
        }
        erpdetail_sv.setVisibility(View.VISIBLE);
        loadingFragmentDialog.dismiss();
    }

    LinearLayout erp_detail_start_ll_fields;
    Spinner erp_detail_start_sp_nextagent;
    Spinner erp_detail_start_sp_type;
    Spinner erp_detail_start_sp_nextstep;
    /**
     * 创建第一步的界面
     */
    private void initStartStepViews(final MAErpDetail business) {
        erp_detail_start_ll.setVisibility(View.VISIBLE);

        erp_detail_start_sp_type = (Spinner) findViewById(R.id.erp_detail_start_sp_type);
        erp_detail_start_sp_nextstep = (Spinner) findViewById(R.id.erp_detail_start_sp_nextstep);
        erp_detail_start_sp_nextagent = (Spinner) findViewById(R.id.erp_detail_start_sp_nextagent);
        erp_detail_start_ll_fields = (LinearLayout) findViewById(R.id.erp_detail_start_ll_fields);
        Button erp_detail_start_btn_submit = (Button) findViewById(R.id.erp_detail_start_btn_submit);
        erp_detail_start_btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitProcess(business);
            }
        });

        List<QueryData> typeDatas = new ArrayList<>();
        List<MABusinessFlow> flows = MobileAssitantCache.getInstance().getBusinessFlows();
        for(int i=0; i<flows.size(); i++) {
            MABusinessFlow flow = flows.get(i);
            if(hasAccessForFlow(flow)) {
                QueryData qd = new QueryData();
                qd.setName(flow.name);
                qd.setValue(flow._id);
                typeDatas.add(qd);
            }
        }

        SPAdapter typeAdatper = new SPAdapter(ErpDetailActivity.this, typeDatas);
        erp_detail_start_sp_type.setAdapter(typeAdatper);
        erp_detail_start_sp_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                QueryData qd = (QueryData) parent.getAdapter().getItem(position);
                String currentFlowId = qd.getValue();
                MABusinessFlow currentFlow = MobileAssitantCache.getInstance().getBusinessFlow(currentFlowId);
                List<MABusinessStep> steps = currentFlow.steps;
                MABusinessStep step = null;
                for(int j=0; j<steps.size(); j++) {
                    if(steps.get(j).isBegin) {
                        step = steps.get(j);
                        break;
                    }
                }

                if(step != null) {
                    List<MAAction> actions = step.actions;
                    List<QueryData> actionDatas = new ArrayList<>();
                    for(int c=0; c<actions.size(); c++) {
                        QueryData actionQD = new QueryData();
                        actionQD.setName(actions.get(c).name);
                        actionQD.setValue(actions.get(c)._id);
                        actionDatas.add(actionQD);
                    }

                    SPAdapter actionAdapter = new SPAdapter(ErpDetailActivity.this, actionDatas);
                    erp_detail_start_sp_nextstep.setAdapter(actionAdapter);
                    erp_detail_start_sp_nextstep.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            QueryData acQd = (QueryData) parent.getAdapter().getItem(position);
                            String actionId = acQd.getValue();
                            QueryData flowQd = (QueryData) erp_detail_start_sp_type.getSelectedItem();
                            String flowId = flowQd.getValue();
                            MABusinessFlow flow = MobileAssitantCache.getInstance().getBusinessFlow(flowId);
                            List<MABusinessStep> steps = flow.steps;
                            MABusinessStep beginStep = null;
                            for(int j=0; j<steps.size(); j++) {
                                if(steps.get(j).isBegin) {
                                    beginStep = steps.get(j);
                                    break;
                                }
                            }

                            if(beginStep != null) {
                                MAAction action = getFlowStepActionById(beginStep.actions, actionId);
                                String nextStepId = action.jumpTo;
                                MABusinessStep nextStep = MobileAssitantCache.getInstance().getBusinessStep(nextStepId);

                                List<String> roles = new ArrayList<>();
                                List<MAAction> actions = nextStep.actions;
                                for (int i=0; i<actions.size(); i++) {
                                    MAAction a = actions.get(i);
                                    roles.add(a.actionRole);
                                }
                                List<MAAgent> agents = MobileAssitantCache.getInstance().getAgents();
                                List<String> ids = new ArrayList<>();
                                List<MAAgent> showAgents = new ArrayList<>();
                                MAAgent autoMaster = new MAAgent();
                                autoMaster.displayName = "自动分配";
                                autoMaster._id = "";
                                showAgents.add(autoMaster);
                                for(int j=0; j<roles.size(); j++) {
                                    String roleId = roles.get(j);
                                    for(int k=0; k<agents.size(); k++) {
                                        MAAgent a = agents.get(k);
                                        if(arrayContainsStr(a.role, roleId)) {
                                            if(!arrayContainsStr(ids, a._id)) {
                                                showAgents.add(a);
                                                ids.add(a._id);
                                            }
                                        }
                                    }
                                }
                                erp_detail_start_sp_nextagent.setAdapter(new ErpAgentSpAdapter(ErpDetailActivity.this, showAgents));
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                }
                erp_detail_start_ll_fields.removeAllViews();
                //自定义字段界面
                createFlowCustomFields(step.stepFields, currentFlow.fields, business, erp_detail_start_ll_fields);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private boolean hasAccessForFlow(MABusinessFlow flow) {
        if("disable".equals(flow.status)) {
            return false;
        }

        List<MABusinessStep> steps = flow.steps;
        MABusinessStep step = null;
        for(int j=0; j<steps.size(); j++) {
            if(steps.get(j).isBegin) {
                step = steps.get(j);
                break;
            }
        }
        if(step == null) {
            return false;
        }

        List<MAAction> actions = step.actions;
        List<String> actionRoles = new ArrayList<>();
        for (int i=0; i<actions.size(); i++) {
            actionRoles.add(actions.get(i).actionRole);
        }

        Collection<UserRole> userRoles = user.userRoles;
        List<String> roles = new ArrayList<>();
        if(userRoles != null && userRoles.size() > 0) {
            for (UserRole ur : userRoles) {
                roles.add(ur.role);
            }
        }

        for(int i=0; i<actionRoles.size(); i++) {
            if(arrayContainsStr(roles, actionRoles.get(i))) {
                return true;
            }
        }
        return false;
    }

    private MAAction getFlowStepActionById(List<MAAction> actions, String actionId) {
        if(actions != null && actionId != null) {
            for(int i=0; i<actions.size(); i++) {
                MAAction a = actions.get(i);
                if(actionId.equals(a._id)) {
                    return a;
                }
            }
        }
        return null;
    }

    private boolean arrayContainsStr(List<String> arr, String str) {

        if(arr != null && arr.size() > 0 && str != null) {
            for (int i=0; i<arr.size(); i++) {
                if(arr.get(i).equals(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    private MAErpDetail initDatas(String data) {
        detail = new MAErpDetail();
        try {
            JSONObject jo = new JSONObject(data);
            JSONObject jsonObject = jo.getJSONObject("data");
            String _id = jsonObject.getString("_id");
            String flowId = jsonObject.getString("flow");
            String stepId = jsonObject.getString("step");
            String lastUpdateUserId = jsonObject.getString("lastUpdateUser");
            String lastUpdateTime = jsonObject.getString("lastUpdateTime");
            String status = jsonObject.getString("status");
            MABusinessFlow flow = MobileAssitantCache.getInstance().getBusinessFlow(flowId);
            if(flow != null) {
                detail.flow = flow.name;
            }
            MABusinessStep step = MobileAssitantCache.getInstance().getBusinessStep(stepId);
            if(step != null) {
                detail.step = step.name;
            }
            detail.flowId = flowId;
            detail.stepId = stepId;
            detail.status = status;

            MAAgent agent = MobileAssitantCache.getInstance().getAgentById(lastUpdateUserId);
            if(agent != null) {
                detail.lastUpdateUser = agent.displayName;
                if(agent.im_icon != null && !"".equals(agent.im_icon)) {
                    detail.imIcon = agent.im_icon;
                }else {
                    detail.imIcon = "";
                }

            }
            detail._id = _id;
            detail.lastUpdateTime = lastUpdateTime;

            //填充字段
            List<FieldData> fdList = initFieldData(jsonObject);
            detail.fieldDatas = fdList;

            List<MAAction> actionsList = step.actions;
            detail.actions = actionsList;

            //填充历史
            List<MAErpHistory> historyList = new ArrayList<>();
            JSONArray historyArray = jsonObject.getJSONArray("history");
            for (int j=0; j<historyArray.length(); j++) {
                MAErpHistory history = new MAErpHistory();
                JSONObject historyItem = historyArray.getJSONObject(j);
                String action = historyItem.getString("action");
                if(action != null && "complete".equals(action)) {
                    continue;
                }
                String historyMaster = historyItem.getString("master");
                if(action != null && "backIn".equals(action)){
                    historyMaster = historyItem.getString("excuteUser");
                }
                String time = historyItem.getString("time");
                if(MobileAssitantCache.getInstance().getAgentById(historyMaster) != null) {
                    String username = MobileAssitantCache.getInstance().getAgentById(historyMaster).displayName;
                    history.name = username;
                    history.imIcon = NullUtil.checkNull(MobileAssitantCache.getInstance().getAgentById(historyMaster).im_icon);
                }else {
                    history.name = "";
                    history.imIcon = "";
                }
                history.time = time;
                //历史信息
                String infoResult = initHistoryInfo(action, historyItem);
                history.info = infoResult;
                //历史字段
                JSONObject excuteData = historyItem.getJSONObject("excuteData");
                List<FieldData> historyDataList = initHistoryFieldData(excuteData, action);
                history.historyData = historyDataList;
                historyList.add(history);
            }
            detail.historyList = historyList;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return detail;
    }

    /**
     * 显示自定义字段
     * @return
     */
    private List<FieldData> initFieldData(JSONObject jsonObject) {
        List<FieldData> fdList = new ArrayList<>();
        try{
            Iterator<String> iterator = jsonObject.keys();
            while(iterator.hasNext()) {
                String key = iterator.next();
                String fieldKey = key;
                if("_".equals(key.substring(key.length() - 2, key.length() - 1))) {
                    fieldKey = key.substring(0, key.length() - 2);
                }
                MABusinessField field = MobileAssitantCache.getInstance().getBusinessField(fieldKey);
                if(field != null) {
                    if ("dropdown".equals(field.type)) {
                        //后面_1,_2的
                        if(!"_".equals(key.substring(key.length() - 2, key.length() - 1))) {
                            FieldData fd_dropdown = createDropDownData(field, jsonObject, key);
                            fdList.add(fd_dropdown);
                        }

                    }else if ("checkbox".equals(field.type)) {
                        //数组
                        String value = MobileAssitantCache.getInstance().getDicById(jsonObject.getString(key));
                        String type = "normal";
                        String name = field.name;

                        if(value != null && !"".equals(value)) {
                            FieldData fd = new FieldData();
                            fd.setType(type);
                            fd.setName(name);
                            fd.setValue(value);
                            fdList.add(fd);
                        }else {
                            JSONArray defaultValue = jsonObject.getJSONArray(key + "_default");
                            if(defaultValue != null && defaultValue.length() > 0) {
                                StringBuilder sb = new StringBuilder();
                                for (int i=0; i<defaultValue.length(); i++) {
                                    sb.append(defaultValue.getString(i)+" ");
                                }
                                FieldData fd = new FieldData();
                                fd.setType(type);
                                fd.setName(name);
                                fd.setValue(sb.toString());
                                fdList.add(fd);
                            }
                        }
                    }else if ("radio".equals(field.type)) {
                        //只有一个值
                        String value = MobileAssitantCache.getInstance().getDicById(jsonObject.getString(key));
                        String type = "normal";
                        String name = field.name;

                        if(value != null && !"".equals(value)) {
                            FieldData fd = new FieldData();
                            fd.setType(type);
                            fd.setName(name);
                            fd.setValue(value);
                            fdList.add(fd);
                        }
                    }else if ("file".equals(field.type)) {
                        //数组
                        JSONArray attachArray = jsonObject.getJSONArray(key);
                        for(int i=0; i< attachArray.length(); i++) {
                            JSONObject attach = attachArray.getJSONObject(i);
                            FieldData fd = new FieldData();
                            fd.setType("file");
                            fd.setName(field.name);
                            fd.setValue(attach.getString("name"));
                            fd.setId(attach.getString("id"));
                            fdList.add(fd);
                        }
                    } else {
                        String type = "normal";
                        String name = field.name;
                        String value = jsonObject.getString(key);
                        if(value != null && !"".equals(value)) {
                            FieldData fd = new FieldData();
                            fd.setType(type);
                            fd.setName(name);
                            fd.setValue(value);
                            fdList.add(fd);
                        }
                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return fdList;
    }

    private FieldData createDropDownData(MABusinessField field, JSONObject jsonObject, String k) {
        FieldData fd_dropdown = new FieldData();
        try{
            String fdValue = "";
            String type = "normal";
            String name = field.name;

            Iterator<String> iterator = jsonObject.keys();
            while(iterator.hasNext()) {
                String key = iterator.next();
                String fieldKey = key;
                if ("_".equals(key.substring(key.length() - 2, key.length() - 1))) {
                    fieldKey = key.substring(0, key.length() - 2);
                }
                MABusinessField f = MobileAssitantCache.getInstance().getBusinessField(fieldKey);
                if (f != null) {
                    if ("dropdown".equals(f.type)) {
                        //后面_1,_2的
                        String v = MobileAssitantCache.getInstance().getDicById(jsonObject.getString(key));
                        if(k.equals(fieldKey)) {
                            fdValue = v + "-->"+fdValue;
                        }
                    }
                }
            }

            fdValue = fdValue.substring(0, fdValue.lastIndexOf("-->"));
            fd_dropdown.setType(type);
            fd_dropdown.setName(name);
            fd_dropdown.setValue(fdValue);
        }catch (Exception e) {

        }
        return fd_dropdown;
    }


    /**
     * 历史操作信息
     * @param action
     * @param historyItem
     * @return
     */
    private String initHistoryInfo(String action, JSONObject historyItem) {
        String infoResult = "";
        try{
            if (action != null && "create".equals(action)) {
                infoResult = "创建 工单！";
            }else if(action != null && "transformIn".equals(action)) {
                String stepStr = MobileAssitantCache.getInstance().getBusinessStep(historyItem.getString("step")).name;
                String actionName = MobileAssitantCache.getInstance().getBusinessStepAction(historyItem.getString("fromStep"), historyItem.getString("excuteAction")).name;
                infoResult = "执行动作【" + actionName + "】状态变更为【" + stepStr + "】";
            }else if(action != null && "backIn".equals(action)) {
                String stepStr = MobileAssitantCache.getInstance().getBusinessStep(historyItem.getString("step")).name;
                infoResult = "执行动作【退回工单】状态变更为【" + stepStr + "】";
            }else if(action != null && "recreate".equals(action)) {
                infoResult = "重新提交 工单！";
            }else if(action != null && "comment".equals(action)) {
                infoResult = "添加 新的备注！";
            }else if(action != null && "assign".equals(action)) {
                String mastername ="";
                if(MobileAssitantCache.getInstance().getAgentById(historyItem.getJSONObject("excuteData").getString("master")) != null) {
                    mastername = MobileAssitantCache.getInstance().getAgentById(historyItem.getJSONObject("excuteData").getString("master")).displayName;
                    if (mastername == null || "".equals(mastername)) {
                        mastername = "自动分配";
                    }
                    infoResult = "变更 工单处理人为【" + mastername + "】";
                }else {
                    mastername = "自动分配";
                    infoResult = "变更 工单处理人为【" + mastername + "】";
                }

            }else {
                infoResult = "未知的动作！";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return infoResult;
    }

    /**
     * 历史字段
     * @param excuteData
     * @param action
     * @return
     */
    private List<FieldData> initHistoryFieldData(JSONObject excuteData, String action) {
        List<FieldData> historyDataList = new ArrayList<>();
        try{
            Iterator<String> historyDataIterator = excuteData.keys();
            while(historyDataIterator.hasNext()) {
                String key = historyDataIterator.next();
                String fieldKey = key;
                if("_".equals(key.substring(key.length() - 2, key.length() - 1))) {
                    fieldKey = key.substring(0, key.length() - 2);
                }
                MABusinessField field = MobileAssitantCache.getInstance().getBusinessField(fieldKey);

                if(field != null) {
                    if ("dropdown".equals(field.type)) {
                        //后面_1,_2的
                        if(!"_".equals(key.substring(key.length() - 2, key.length() - 1))) {
                            FieldData fd_dropdown = createDropDownData(field, excuteData, key);
                            historyDataList.add(fd_dropdown);
                        }

                    }else if ("checkbox".equals(field.type)) {
                        //数组
                        String value = MobileAssitantCache.getInstance().getDicById(excuteData.getString(key));
                        String type = "normal";
                        String name = field.name;

                        if(value != null && !"".equals(value)) {
                            FieldData fd = new FieldData();
                            fd.setType(type);
                            fd.setName(name);
                            fd.setValue(value);
                            historyDataList.add(fd);
                        }else {
                            JSONArray defaultValue = excuteData.getJSONArray(key + "_default");
                            if(defaultValue != null && defaultValue.length() > 0) {
                                StringBuilder sb = new StringBuilder();
                                for (int i=0; i<defaultValue.length(); i++) {
                                    sb.append(defaultValue.getString(i)+" ");
                                }
                                FieldData fd = new FieldData();
                                fd.setType(type);
                                fd.setName(name);
                                fd.setValue(sb.toString());
                                historyDataList.add(fd);
                            }
                        }
                    }else if ("radio".equals(field.type)) {
                        //只有一个值
                        String value = MobileAssitantCache.getInstance().getDicById(excuteData.getString(key));
                        String type = "normal";
                        String name = field.name;

                        if(value != null && !"".equals(value)) {
                            FieldData fd = new FieldData();
                            fd.setType(type);
                            fd.setName(name);
                            fd.setValue(value);
                            historyDataList.add(fd);
                        }
                    }else if ("file".equals(field.type)) {
                        JSONArray attachArray = excuteData.getJSONArray(fieldKey);
                        for(int i=0; i< attachArray.length(); i++) {
                            JSONObject attach = attachArray.getJSONObject(i);
                            FieldData fd = new FieldData();
                            fd.setType("file");
                            fd.setName(field.name);
                            fd.setValue(attach.getString("name"));
                            fd.setId(attach.getString("id"));
                            historyDataList.add(fd);
                        }
                    } else {
                        String type = "normal";
                        String name = field.name;
                        String value = excuteData.getString(key);
                        if(value != null && !"".equals(value)) {
                            FieldData fd = new FieldData();
                            fd.setType(type);
                            fd.setName(name);
                            fd.setValue(value);
                            historyDataList.add(fd);
                        }
                    }

                }else if("number".equals(key)){
                    FieldData fd = new FieldData();
                    fd.setType("normal");
                    fd.setName("工单编号");
                    fd.setValue(excuteData.getString(key));
                    historyDataList.add(fd);
                }

                if (action.equals("backIn")) {
                    FieldData fd = new FieldData();
                    fd.setType("normal");
                    fd.setName("退回原因");
                    fd.setValue(excuteData.getString("backInfo"));
                    historyDataList.add(fd);
                } else if (action.equals("comment")) {
                    FieldData fd = new FieldData();
                    fd.setType("normal");
                    fd.setName("备注内容");
                    fd.setValue(excuteData.getString("backInfo"));
                    historyDataList.add(fd);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return historyDataList;
    }

    public void onEventMainThread(ErpExcuteSuccess event) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        MobileApplication.getInstance().remove(this);

    }



    /**
     * 创建不同字段界面
     * @param fields
     * @param flowFields
     * @param business
     * @param pane
     */
    private void createFlowCustomFields(List<MAStepFields> fields, List<MABusinessField> flowFields, MAErpDetail business, LinearLayout pane) {
        for(int i=0; i<fields.size(); i++) {
            MAStepFields row = fields.get(i);
            for(int j=0; j<row.cols.size(); j++) {
                MACol col = row.cols.get(j);
                for(int k=0; k<col.fields.size(); k++) {
                    MAFields maField = col.fields.get(k);
                    MABusinessField cacheField = getFieldById(flowFields, maField._id);
                    if(cacheField != null) {
                        switch (cacheField.type) {
                            case "single":
                                RelativeLayout singleView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.erp_field_single, null);
                                singleView.setTag("single");
                                TextView erp_field_single_tv_name = (TextView) singleView.findViewById(R.id.erp_field_single_tv_name);
                                erp_field_single_tv_name.setText(cacheField.name);
                                EditText erp_field_single_et_value = (EditText) singleView.findViewById(R.id.erp_field_single_et_value);
                                erp_field_single_et_value.setTag(cacheField._id);
                                pane.addView(singleView);
                                break;
                            case "multi":
                                RelativeLayout multiView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.erp_field_multi, null);
                                multiView.setTag("multi");
                                TextView erp_field_multi_tv_name = (TextView) multiView.findViewById(R.id.erp_field_multi_tv_name);
                                erp_field_multi_tv_name.setText(cacheField.name);
                                EditText erp_field_multi_et_value = (EditText) multiView.findViewById(R.id.erp_field_multi_et_value);
                                erp_field_multi_et_value.setTag(cacheField._id);
                                pane.addView(multiView);
                                break;
                            case "date":
                                initDateView(cacheField, pane);
                                break;
                            case "number":
                                RelativeLayout numberView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.erp_field_number, null);
                                numberView.setTag("number");
                                TextView erp_field_number_tv_name = (TextView) numberView.findViewById(R.id.erp_field_number_tv_name);
                                erp_field_number_tv_name.setText(cacheField.name);
                                EditText erp_field_number_et_value = (EditText) numberView.findViewById(R.id.erp_field_number_et_value);
                                erp_field_number_et_value.setTag(cacheField._id);
                                pane.addView(numberView);
                                break;
                            case "dropdown":
                                initDropDownView(cacheField, pane);
                                break;
                            case "checkbox":
                                initCheckBoxView(cacheField, pane);
                                break;
                            case "radio":
                                initRadioView(cacheField, pane);
                                break;
                            case "file":
                                initFileView(cacheField, pane);
                                break;

                        }
                    }
                }
            }
        }
    }


    /**
     * 下拉框界面
     * @param cacheField
     * @param pane
     */
    private void initDropDownView(MABusinessField cacheField, LinearLayout pane) {
        RelativeLayout dropDownView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.erp_field_dropdown, null);
        dropDownView.setTag("dropdown");
        TextView erp_field_dropdown_tv_name = (TextView) dropDownView.findViewById(R.id.erp_field_dropdown_tv_name);
        erp_field_dropdown_tv_name.setText(cacheField.name);
        LinearLayout erp_field_dropdown_ll = (LinearLayout) dropDownView.findViewById(R.id.erp_field_dropdown_ll);
        if(cacheField.dic != null) {
            MAOption maoption = MobileAssitantCache.getInstance().getMAOption(cacheField.dic);
            if(maoption != null) {
                if(maoption.cascade == 1) {
                    String fieldName = maoption.headers.get(0);
                    List<Option> firstOption = maoption.options;
                    RelativeLayout firstItemRL = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.erp_field_dropdown_item1, null);
                    TextView erp_field_dropdown_item_tv_name = (TextView) firstItemRL.findViewById(R.id.erp_field_dropdown_item_tv_name);
                    erp_field_dropdown_item_tv_name.setText(fieldName);

                    Spinner erp_field_dropdown_item_sp_value = (Spinner) firstItemRL.findViewById(R.id.erp_field_dropdown_item_sp_value);
                    erp_field_dropdown_item_sp_value.setTag(cacheField._id);
                    ErpSpAdapter adapter = new ErpSpAdapter(ErpDetailActivity.this, firstOption);
                    erp_field_dropdown_item_sp_value.setAdapter(adapter);

                    erp_field_dropdown_ll.addView(firstItemRL);
                }else if(maoption.cascade == 2) {
                    String fieldName = maoption.headers.get(0);
                    final List<Option> firstOption = maoption.options;
                    RelativeLayout firstItemRL = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.erp_field_dropdown_item1, null);
                    TextView erp_field_dropdown_item_tv_name = (TextView) firstItemRL.findViewById(R.id.erp_field_dropdown_item_tv_name);
                    erp_field_dropdown_item_tv_name.setText(fieldName);

                    final Spinner erp_field_dropdown_item_sp_value = (Spinner) firstItemRL.findViewById(R.id.erp_field_dropdown_item_sp_value);
                    erp_field_dropdown_item_sp_value.setTag(cacheField._id);
                    final ErpSpAdapter adapter = new ErpSpAdapter(ErpDetailActivity.this, firstOption);
                    erp_field_dropdown_item_sp_value.setAdapter(adapter);
                    erp_field_dropdown_ll.addView(firstItemRL);
                    String fieldName2 = maoption.headers.get(1);
                    RelativeLayout secondItemRL = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.erp_field_dropdown_item2, null);
                    TextView erp_field_dropdown_item_tv_name2 = (TextView) secondItemRL.findViewById(R.id.erp_field_dropdown_item2_tv_name);
                    erp_field_dropdown_item_tv_name2.setText(fieldName2);

                    final Spinner erp_field_dropdown_item_sp_value2 = (Spinner) secondItemRL.findViewById(R.id.erp_field_dropdown_item2_sp_value);
                    erp_field_dropdown_item_sp_value2.setTag(cacheField._id + "_1");
                    erp_field_dropdown_ll.addView(secondItemRL);

                    erp_field_dropdown_item_sp_value.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Option o = (Option) parent.getAdapter().getItem(position);
                            List<Option> secondOptions = getOptionsByKey(firstOption, o.key);
                            ErpSpAdapter adapter = new ErpSpAdapter(ErpDetailActivity.this, secondOptions);
                            erp_field_dropdown_item_sp_value2.setAdapter(adapter);
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }else if(maoption.cascade == 3) {
                    String fieldName = maoption.headers.get(0);
                    final List<Option> firstOption = maoption.options;
                    RelativeLayout firstItemRL = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.erp_field_dropdown_item1, null);
                    TextView erp_field_dropdown_item_tv_name = (TextView) firstItemRL.findViewById(R.id.erp_field_dropdown_item_tv_name);
                    erp_field_dropdown_item_tv_name.setText(fieldName);

                    final Spinner erp_field_dropdown_item_sp_value = (Spinner) firstItemRL.findViewById(R.id.erp_field_dropdown_item_sp_value);
                    erp_field_dropdown_item_sp_value.setTag(cacheField._id);
                    final ErpSpAdapter adapter = new ErpSpAdapter(ErpDetailActivity.this, firstOption);
                    erp_field_dropdown_item_sp_value.setAdapter(adapter);
                    erp_field_dropdown_ll.addView(firstItemRL);

                    String fieldName2 = maoption.headers.get(1);
                    RelativeLayout secondItemRL = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.erp_field_dropdown_item2, null);
                    TextView erp_field_dropdown_item_tv_name2 = (TextView) secondItemRL.findViewById(R.id.erp_field_dropdown_item2_tv_name);
                    erp_field_dropdown_item_tv_name2.setText(fieldName2);

                    final Spinner erp_field_dropdown_item_sp_value2 = (Spinner) secondItemRL.findViewById(R.id.erp_field_dropdown_item2_sp_value);
                    erp_field_dropdown_item_sp_value2.setTag(cacheField._id + "_1");
                    erp_field_dropdown_ll.addView(secondItemRL);

                    String fieldName3 = maoption.headers.get(2);
                    RelativeLayout threeItemRL = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.erp_field_dropdown_item3, null);
                    TextView erp_field_dropdown_item_tv_name3 = (TextView) threeItemRL.findViewById(R.id.erp_field_dropdown_item3_tv_name);
                    erp_field_dropdown_item_tv_name3.setText(fieldName3);
                    final Spinner erp_field_dropdown_item_sp_value3 = (Spinner) threeItemRL.findViewById(R.id.erp_field_dropdown_item3_sp_value);
                    erp_field_dropdown_item_sp_value3.setTag(cacheField._id + "_2");
                    erp_field_dropdown_ll.addView(threeItemRL);


                    erp_field_dropdown_item_sp_value.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Option o = (Option) parent.getAdapter().getItem(position);
                            final List<Option> secondOptions = getOptionsByKey(firstOption, o.key);
                            ErpSpAdapter adapter = new ErpSpAdapter(ErpDetailActivity.this, secondOptions);
                            erp_field_dropdown_item_sp_value2.setAdapter(adapter);

                            erp_field_dropdown_item_sp_value2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    Option o = (Option) parent.getAdapter().getItem(position);
                                    List<Option> threeOptions = getOptionsByKey(secondOptions, o.key);
                                    ErpSpAdapter adapter = new ErpSpAdapter(ErpDetailActivity.this, threeOptions);
                                    erp_field_dropdown_item_sp_value3.setAdapter(adapter);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });



                }
            }
        }
        pane.addView(dropDownView);
    }

    private List<Option> getOptionsByKey(List<Option> o, String key) {
        for(int i=0; i<o.size(); i++) {
            if(key.equals(o.get(i).key)) {
                return o.get(i).options;
            }
        }
        return null;
    }

    /**
     * 多选框界面
     * @param cacheField
     * @param pane
     */
    private void initCheckBoxView(MABusinessField cacheField, LinearLayout pane) {
        RelativeLayout checkboxView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.erp_field_checkbox, null);
        checkboxView.setTag("checkbox");
        TextView erp_field_checkbox_tv_name = (TextView) checkboxView.findViewById(R.id.erp_field_checkbox_tv_name);
        erp_field_checkbox_tv_name.setText(cacheField.name);
        GridViewInScrollView checkbox_gv = (GridViewInScrollView) checkboxView.findViewById(R.id.erp_field_checkbox_gv_value);
        checkbox_gv.setTag(cacheField._id);
        if(cacheField.dic != null) {
            MAOption maoption = MobileAssitantCache.getInstance().getMAOption(cacheField.dic);
            if(maoption != null) {
                List<Option> options = maoption.options;
                final ErpCBAdapter adapter = new ErpCBAdapter(ErpDetailActivity.this, options);
//                for (int i=0; i<options.size(); i++) {
                    checkbox_gv.setAdapter(adapter);
                    checkbox_gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            ErpCBAdapter.ViewHolder holder = (ErpCBAdapter.ViewHolder) view.getTag();
                            holder.cb.toggle();
                            if (holder.cb.isChecked()) {
                                adapter.getIsSelected().put(position, true);
                            } else {
                                adapter.getIsSelected().put(position, false);
                            }
                        }
                    });
//                }
            }
        }
        pane.addView(checkboxView);
    }

    /**
     * 单选按钮界面
     * @param cacheField
     * @param pane
     */
    private void initRadioView(MABusinessField cacheField, LinearLayout pane) {
        RelativeLayout radioView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.erp_field_radio, null);
        radioView.setTag("radio");
        TextView erp_field_radio_tv_name = (TextView) radioView.findViewById(R.id.erp_field_radio_tv_name);
        erp_field_radio_tv_name.setText(cacheField.name);
        RadioGroup radioGroup = (RadioGroup) radioView.findViewById(R.id.erp_field_radio_rg_value);
        radioGroup.setTag(cacheField._id);
        if(cacheField.dic != null) {
            MAOption maoption = MobileAssitantCache.getInstance().getMAOption(cacheField.dic);
            if(maoption != null) {
                List<Option> options = maoption.options;
                if(options.size() > 2) {
                    radioGroup.setOrientation(RadioGroup.VERTICAL);
                }
                for (int i=0; i<options.size(); i++) {
                    Option o = options.get(i);
                    RadioButton rb = new RadioButton(ErpDetailActivity.this);
                    ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    rb.setLayoutParams(lp);
                    rb.setText(o.name);
                    rb.setTag(o.key);
                    radioGroup.addView(rb);
                }
            }
        }
        pane.addView(radioView);
    }

    /**
     * 时间界面
     * @param cacheField
     * @param pane
     */
    private void initDateView(MABusinessField cacheField, LinearLayout pane) {
        RelativeLayout dataView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.erp_field_data, null);
        dataView.setTag("date");
        TextView erp_field_data_tv_name = (TextView) dataView.findViewById(R.id.erp_field_data_tv_name);
        erp_field_data_tv_name.setText(cacheField.name);
        final EditText erp_field_data_et_value = (EditText) dataView.findViewById(R.id.erp_field_data_et_value);
        erp_field_data_et_value.setTag(cacheField._id);
        erp_field_data_et_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePiker(erp_field_data_et_value);
            }
        });
        pane.addView(dataView);
    }

    /**
     * 显示选择时间框
     * @param et
     */
    private void showDatePiker(final EditText et) {
        Calendar d = Calendar.getInstance(Locale.CHINA);
        //创建一个日历引用d，通过静态方法getInstance() 从指定时区 Locale.CHINA 获得一个日期实例
        Date myDate = new Date();
        //创建一个Date实例
        d.setTime(myDate);
        //设置日历的时间，把一个新建Date实例myDate传入
        int year = d.get(Calendar.YEAR);
        int month = d.get(Calendar.MONTH);
        int day = d.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(ErpDetailActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String monthStr  = "";
                if((monthOfYear+1) < 10) {
                    monthStr = "0"+(monthOfYear+1);
                }else {
                    monthStr = (monthOfYear+1) + "";
                }
                String dayStr  = "";
                if(dayOfMonth < 10) {
                    dayStr = "0"+dayOfMonth;
                }else {
                    dayStr = dayOfMonth + "";
                }
                final String data = year+"-"+monthStr+"-"+dayStr;
                et.setText(data);
            }
        }, year, month, day) {
            @Override
            protected void onStop() {
//                super.onStop();
            }
        };
        dpd.show();
    }



    private MABusinessField getFieldById(List<MABusinessField> flowFields, String id) {
        if(flowFields != null && flowFields.size() > 0 && id != null) {
            for(int i=0; i<flowFields.size(); i++) {
                MABusinessField field = flowFields.get(i);
                if(field._id.equals(id)) {
                    return field;
                }
            }
        }
        return null;
    }

    LinearLayout erp_field_file_ll_already;
    /**
     * 上传文件界面
     * @param cacheField
     * @param pane
     */
    private void initFileView(MABusinessField cacheField, LinearLayout pane) {
        RelativeLayout fileView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.erp_field_file, null);
        fileView.setTag("file");

        TextView erp_field_file_tv_name = (TextView) fileView.findViewById(R.id.erp_field_file_tv_name);
        erp_field_file_tv_name.setText(cacheField.name);
        erp_field_file_ll_already = (LinearLayout) fileView.findViewById(R.id.erp_field_file_ll_already);
        erp_field_file_ll_already.setTag(cacheField._id);
        Button erp_field_file_btn_uploadfile = (Button) fileView.findViewById(R.id.erp_field_file_btn_uploadfile);
        erp_field_file_btn_uploadfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");//设置类型
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                }else {
                    intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                }
                startActivityForResult(intent, 0x1111);
            }
        });

        pane.addView(fileView);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x1111 && resultCode == Activity.RESULT_OK) {//是否选择，没选择就不会继续
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            String path = "";
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                String[] projection = { "_data" };
                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(uri, projection,null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow("_data");
                    if (cursor.moveToFirst()) {
                        path =  cursor.getString(column_index);
                    }
                } catch (Exception e) {

                }
            }
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                path =  uri.getPath();
            }

            File file = new File(path);
            String fileSizeStr = "";
            if(file.exists()) {
                long fileSize = file.length();
                System.out.println("文件大小为:" + fileSize);
                if((fileSize / 1024 / 1024) > 10.0) {
                    //大于10M不能上传
                    Toast.makeText(ErpDetailActivity.this, "上传文件不能大于10M", Toast.LENGTH_SHORT).show();
                }else {
                    if(fileSize / 1024 < 1.0) {
                        //B
                        fileSizeStr = (int)(fileSize/1024) + "B";
                    }else if(fileSize / 1024 > 1.0 && fileSize / 1024 / 1024 < 1.0) {
                        //KB
                        fileSizeStr = (int)(fileSize/1024) + "KB";
                    }else if(fileSize / 1024 / 1024 < 10.0) {
                        //MB
                        fileSizeStr = (int)(fileSize/1024/1024) + "MB";
                    }

                    String fileName = path.substring(path.lastIndexOf("/") + 1);
                    System.out.println("filename is:"+fileName);
                    UploadFileDialog fileDialog = new UploadFileDialog();
                    Bundle b = new Bundle();
                    b.putString("fileName", fileName);
                    b.putString("fileSize", fileSizeStr);
                    b.putSerializable("file", file);
                    fileDialog.setArguments(b);
                    fileDialog.setOnFileUploadCompletedListener(fileUploadCompletedListener);
                    fileDialog.show(getFragmentManager(), "");
                }
            }
        }
    }

    ErpActionProcessActivity.OnFileUploadCompletedListener fileUploadCompletedListener = new ErpActionProcessActivity.OnFileUploadCompletedListener() {

        @Override
        public void onCompleted(String fileName, String key) {
            final RelativeLayout rl = (RelativeLayout) LayoutInflater.from(ErpDetailActivity.this).inflate(R.layout.erp_field_file_already, null);
            String type = "other";
            if(fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase().equals("png") ||
                    fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase().equals("jpg") ||
                    fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase().equals("jpeg") ||
                    fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase().equals("gif") ||
                    fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase().equals("bmp")) {
                type = "img";
            }
            rl.setTag(type);
            TextView erp_field_file_upload_already_tv_filename = (TextView) rl.findViewById(R.id.erp_field_file_upload_already_tv_filename);
            erp_field_file_upload_already_tv_filename.setText(fileName);
            erp_field_file_upload_already_tv_filename.setTag(key);

            ImageView erp_field_file_upload_already_btn_delete = (ImageView) rl.findViewById(R.id.erp_field_file_upload_already_btn_delete);
            erp_field_file_upload_already_btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //删除附件
                    erp_field_file_ll_already.removeView(rl);
                }
            });

            erp_field_file_ll_already.addView(rl);
        }
    };


    /**
     * 提交
     */
    private void submitProcess(MAErpDetail business) {
        HashMap<String, String> datas = new HashMap<>();
        HashMap<String, JSONArray> jadatas = new HashMap<>();
        int childSize = erp_detail_start_ll_fields.getChildCount();
        for(int i=0; i<childSize; i++) {
            RelativeLayout childView = (RelativeLayout) erp_detail_start_ll_fields.getChildAt(i);
            String type = (String) childView.getTag();
            switch(type) {
                case "single":

                    EditText et = (EditText) childView.getChildAt(1);
                    String id = (String) et.getTag();
                    String value = et.getText().toString().trim();
                    TextView tv_single_required = (TextView) childView.getChildAt(0);
                    String fieldName = tv_single_required.getText().toString();
                    String required = (String) tv_single_required.getTag();
                    if("required".equals(required)) {
                        if("".equals(value)) {
                            Toast.makeText(ErpDetailActivity.this, fieldName + "是必填项", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    datas.put(id, value);
                    System.out.println("id is:" + id + "," + "value is:" + value);
                    break;
                case "multi":

                    EditText et_multi = (EditText) childView.getChildAt(1);
                    String id_multi = (String) et_multi.getTag();
                    String value_multi = et_multi.getText().toString().trim();

                    TextView tv_multi_required = (TextView) childView.getChildAt(0);
                    String fieldName_multi = tv_multi_required.getText().toString();
                    String required_multi = (String) tv_multi_required.getTag();
                    if("required".equals(required_multi)) {
                        if("".equals(value_multi)) {
                            Toast.makeText(ErpDetailActivity.this, fieldName_multi + "是必填项", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    datas.put(id_multi, value_multi);
                    System.out.println("id_multi is:"+id_multi+","+"value_multi is:"+value_multi);
                    break;
                case "number":
                    EditText et_number = (EditText) childView.getChildAt(1);
                    String id_number = (String) et_number.getTag();
                    String value_number = et_number.getText().toString().trim();
                    TextView tv_number_required = (TextView) childView.getChildAt(0);
                    String fieldName_number = tv_number_required.getText().toString();
                    String required_number = (String) tv_number_required.getTag();
                    if("required".equals(required_number)) {
                        if("".equals(value_number)) {
                            Toast.makeText(ErpDetailActivity.this, fieldName_number + "是必填项", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    datas.put(id_number, value_number);
                    System.out.println("id_number is:"+id_number+","+"value_number is:"+value_number);
                    break;
                case "date":

                    EditText et_data = (EditText) childView.getChildAt(1);
                    String id_data = (String) et_data.getTag();
                    String value_data = et_data.getText().toString().trim();

                    TextView tv_data_required = (TextView) childView.getChildAt(0);
                    String fieldName_data = tv_data_required.getText().toString();
                    String required_data = (String) tv_data_required.getTag();
                    if("required".equals(required_data)) {
                        if("".equals(value_data)) {
                            Toast.makeText(ErpDetailActivity.this, fieldName_data + "是必填项", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    datas.put(id_data, value_data);
                    System.out.println("id_data is:"+id_data+","+"value_number is:"+value_data);
                    break;
                case "radio":
                    RadioGroup radioGroup = (RadioGroup) childView.getChildAt(1);
                    int selectId = radioGroup.getCheckedRadioButtonId();

                    TextView tv_radio_required = (TextView) childView.getChildAt(0);
                    String fieldName_radio = tv_radio_required.getText().toString();
                    String required_radio = (String) tv_radio_required.getTag();
                    if("required".equals(required_radio)) {
                        if(selectId == -1) {
                            Toast.makeText(ErpDetailActivity.this, fieldName_radio + "是必选项", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    if(selectId != -1) {
                        RadioButton rb = (RadioButton) radioGroup.findViewById(selectId);
                        String id_radio = (String) radioGroup.getTag();
                        String value_radio = (String) rb.getTag();
                        datas.put(id_radio, value_radio);
                        System.out.println("id_radio is:"+id_radio+","+"value_radio is:"+value_radio);
                    }
                    break;
                case "checkbox":
                    //数组

                    JSONArray jsonArray = new JSONArray();
                    JSONArray jsonArray_default = new JSONArray();
                    GridViewInScrollView gv = (GridViewInScrollView) childView.getChildAt(1);
                    String cbFieldId = (String) gv.getTag();
                    List<Option> options = ((ErpCBAdapter)gv.getAdapter()).getOptions();
                    HashMap<Integer, Boolean> selected = ((ErpCBAdapter)gv.getAdapter()).getIsSelected();
                    TextView tv_checkbox_required = (TextView) childView.getChildAt(0);
                    String fieldName_checkbox = tv_checkbox_required.getText().toString();
                    String required_checkbox = (String) tv_checkbox_required.getTag();
                    if("required".equals(required_checkbox)) {
                        if(selected.size() == 0) {
                            Toast.makeText(ErpDetailActivity.this, fieldName_checkbox + "是必选项", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    for (int o = 0; o < selected.size(); o++) {
                        if(selected.get(o)) {
                            Option option = options.get(o);
                            jsonArray.put(option.key);
                            jsonArray_default.put(option.name);
                            System.out.println("checkbox name is:"+option.name);
                        }
                    }
                    jadatas.put(cbFieldId, jsonArray);
                    jadatas.put(cbFieldId+"_default", jsonArray_default);
                    break;
                case "dropdown":
                    //后面_1,_2
                    LinearLayout ll = (LinearLayout) childView.getChildAt(1);
                    int ll_child_count = ll.getChildCount();

                    if(ll_child_count == 1) {
                        Spinner sp1 = (Spinner) ((RelativeLayout)(ll.getChildAt(0))).getChildAt(1);
                        String id_dropdown1 = (String) sp1.getTag();
                        String value_dropdown1 = ((Option)sp1.getSelectedItem()).key;
                        datas.put(id_dropdown1, value_dropdown1);
                        datas.put(id_dropdown1+"_default", ((Option)sp1.getSelectedItem()).name);
                        System.out.println("id_dropdown1 is:"+id_dropdown1+",value_dropdown1"+value_dropdown1);
                    }else if(ll_child_count == 2) {
                        Spinner sp1 = (Spinner) ((RelativeLayout)(ll.getChildAt(0))).getChildAt(1);
                        String id_dropdown1 = (String) sp1.getTag();
                        String value_dropdown1 = ((Option)sp1.getSelectedItem()).key;
                        datas.put(id_dropdown1, value_dropdown1);
                        datas.put(id_dropdown1+"_default", ((Option)sp1.getSelectedItem()).name);

                        System.out.println("id_dropdown1 is:"+id_dropdown1+",value_dropdown1"+value_dropdown1);

                        Spinner sp2 = (Spinner) ((RelativeLayout)(ll.getChildAt(1))).getChildAt(1);
                        String id_dropdown2 = (String) sp2.getTag();
                        String value_dropdown2 = ((Option)sp2.getSelectedItem()).key;
                        datas.put(id_dropdown2, value_dropdown2);
                        datas.put(id_dropdown2+"_default", ((Option)sp2.getSelectedItem()).name);
                        System.out.println("id_dropdown2 is:"+id_dropdown2+",value_dropdown2"+value_dropdown2);
                    }else if(ll_child_count == 3) {
                        Spinner sp1 = (Spinner) ((RelativeLayout)(ll.getChildAt(0))).getChildAt(1);
                        String id_dropdown1 = (String) sp1.getTag();
                        String value_dropdown1 = ((Option)sp1.getSelectedItem()).key;
                        datas.put(id_dropdown1, value_dropdown1);
                        datas.put(id_dropdown1+"_default", ((Option)sp1.getSelectedItem()).name);

                        System.out.println("id_dropdown1 is:"+id_dropdown1+",value_dropdown1"+value_dropdown1);

                        Spinner sp2 = (Spinner) ((RelativeLayout)(ll.getChildAt(1))).getChildAt(1);
                        String id_dropdown2 = (String) sp2.getTag();
                        String value_dropdown2 = ((Option)sp2.getSelectedItem()).key;
                        datas.put(id_dropdown2, value_dropdown2);
                        datas.put(id_dropdown2+"_default", ((Option)sp2.getSelectedItem()).name);

                        System.out.println("id_dropdown2 is:"+id_dropdown2+",value_dropdown2"+value_dropdown2);

                        Spinner sp3 = (Spinner) ((RelativeLayout)(ll.getChildAt(2))).getChildAt(1);
                        String id_dropdown3 = (String) sp3.getTag();
                        String value_dropdown3 = ((Option)sp3.getSelectedItem()).key;
                        datas.put(id_dropdown3, value_dropdown3);
                        datas.put(id_dropdown2+"_default", ((Option)sp2.getSelectedItem()).name);
                        datas.put(id_dropdown3+"_default", ((Option)sp3.getSelectedItem()).name);
                        System.out.println("id_dropdown3 is:"+id_dropdown3+",value_dropdown3"+value_dropdown3);

                    }
                    break;
                case "file":
                    LinearLayout ll_file = (LinearLayout) childView.getChildAt(0);
                    TextView tv_file_required = (TextView) ll_file.getChildAt(0);
                    String fieldName_file = tv_file_required.getText().toString();
                    String required_file = (String) tv_file_required.getTag();

                    LinearLayout filell = (LinearLayout) childView.getChildAt(1);
                    String fileFieldId = (String) filell.getTag();
                    System.out.println("附件的字段id是:"+fileFieldId);
                    int fileCount = filell.getChildCount();
                    if("required".equals(required_file)) {
                        if(fileCount == 0) {
                            Toast.makeText(ErpDetailActivity.this, fieldName_file+"是必上传项", Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }
                    JSONArray ja = new JSONArray();
                    for(int f=0; f<fileCount; f++) {
                        JSONObject jb = new JSONObject();
                        RelativeLayout rl = (RelativeLayout) filell.getChildAt(f);
                        String filetype = (String) rl.getTag();
                        TextView fileNameTv = (TextView) rl.getChildAt(1);
                        String fileName = fileNameTv.getText().toString().trim();
                        String fileKey = (String) fileNameTv.getTag();
                        try {
                            jb.put("id", fileKey);
                            jb.put("name", fileName);
                            jb.put("type", filetype);

                            ja.put(jb);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        System.out.println("文件type is:"+filetype+",name is:"+fileName+",key is:"+fileKey);
                    }

                    jadatas.put(fileFieldId, ja);
                    break;
            }
        }

        //提交坐席数据
        MAAgent agent = (MAAgent) erp_detail_start_sp_nextagent.getSelectedItem();
        String agentId = "";
        if(agent != null) {
            agentId = agent._id;
        }
        datas.put("nextUser", agentId);
        datas.put("_id", business._id);

        QueryData nextStepQd = (QueryData) erp_detail_start_sp_nextstep.getSelectedItem();
        String nextStepId = nextStepQd.getValue();
        datas.put("nextAction", nextStepId);

        QueryData flowQd = (QueryData) erp_detail_start_sp_type.getSelectedItem();
        String flowId = flowQd.getValue();
        datas.put("flow", flowId);

        MobileHttpManager.reSaveBusiness(user._id, datas, jadatas, new ExcuteBusHandler());
    }

    class ExcuteBusHandler extends TextHttpResponseHandler{

        @Override
        public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

        }

        @Override
        public void onSuccess(int i, Header[] headers, String s) {
            System.out.println("执行动作返回结果:"+s);
            String succeed = HttpParser.getSucceed(s);
            if("true".equals(succeed)) {
                //执行成功
                EventBus.getDefault().post(new ErpExcuteSuccess());
                finish();
            }else if("403".equals(HttpParser.getErrorCode(s))) {
                Toast.makeText(ErpDetailActivity.this, "该工单步骤已被执行或您已无权限执行", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(ErpDetailActivity.this, "执行失败", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
