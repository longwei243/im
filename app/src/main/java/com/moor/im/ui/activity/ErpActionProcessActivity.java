package com.moor.im.ui.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.app.RequestUrl;
import com.moor.im.db.dao.MessageDao;
import com.moor.im.db.dao.UserDao;
import com.moor.im.event.ErpExcuteSuccess;
import com.moor.im.http.HttpManager;
import com.moor.im.http.MobileHttpManager;
import com.moor.im.model.entity.FromToMessage;
import com.moor.im.model.entity.MAAction;
import com.moor.im.model.entity.MAActionFields;
import com.moor.im.model.entity.MAAgent;
import com.moor.im.model.entity.MABusinessField;
import com.moor.im.model.entity.MABusinessFlow;
import com.moor.im.model.entity.MABusinessStep;
import com.moor.im.model.entity.MACol;
import com.moor.im.model.entity.MAErpDetail;
import com.moor.im.model.entity.MAFields;
import com.moor.im.model.entity.MAOption;
import com.moor.im.model.entity.Option;
import com.moor.im.model.entity.User;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.model.parser.MobileAssitantParser;
import com.moor.im.ui.adapter.ErpAgentSpAdapter;
import com.moor.im.ui.adapter.ErpCBAdapter;
import com.moor.im.ui.adapter.ErpSpAdapter;
import com.moor.im.ui.dialog.UploadFileDialog;
import com.moor.im.ui.view.GridViewInScrollView;
import com.moor.im.utils.MobileAssitantCache;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;

/**
 * Created by longwei on 2016/3/16.
 */
public class ErpActionProcessActivity extends Activity{

    private String actionId;
    private MAErpDetail business;

    private LinearLayout erp_action_pro_field;
    private Button erp_action_pro_btn;

    private RelativeLayout erp_action_pro_agent;
    private Spinner erp_action_pro_sp_agent;

    private User user = UserDao.getInstance().getUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        MobileApplication.getInstance().add(this);
        setContentView(R.layout.activity_erp_action_process);

        erp_action_pro_field = (LinearLayout) findViewById(R.id.erp_action_pro_field);
        erp_action_pro_btn = (Button) findViewById(R.id.erp_action_pro_btn);
        erp_action_pro_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitProcess();
            }
        });

        erp_action_pro_agent = (RelativeLayout) findViewById(R.id.erp_action_pro_agent);
        erp_action_pro_sp_agent = (Spinner) findViewById(R.id.erp_action_pro_sp_agent);

        Intent intent = getIntent();
        actionId = intent.getStringExtra("actionId");

        business = (MAErpDetail) intent.getSerializableExtra("business");

        MABusinessFlow flow = MobileAssitantCache.getInstance().getBusinessFlow(business.flowId);
        MABusinessStep step = MobileAssitantCache.getInstance().getBusinessStep(business.stepId);

        MAAction action = getFlowStepActionById(step.actions, actionId);
        TextView erp_action_process_tv_name = (TextView) findViewById(R.id.erp_action_process_tv_name);
        erp_action_process_tv_name.setText("处理工单-"+action.name);


        List<MAActionFields> fields = action.actionFields;

        String nextStepId = action.jumpTo;
        MABusinessStep nextStep = MobileAssitantCache.getInstance().getBusinessStep(nextStepId);
        if("sys".equals(nextStep.type)) {
            //隐藏坐席
            erp_action_pro_agent.setVisibility(View.GONE);
        }else {
            //处理下一步坐席权限
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
            //显示showAgents
            erp_action_pro_agent.setVisibility(View.VISIBLE);
            erp_action_pro_sp_agent.setAdapter(new ErpAgentSpAdapter(ErpActionProcessActivity.this, showAgents));
        }
        //显示自定义字段
        createFlowCustomFields(fields, flow.fields, business, erp_action_pro_field);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * 提交
     */
    private void submitProcess() {
        HashMap<String, String> datas = new HashMap<>();
        HashMap<String, JSONArray> jadatas = new HashMap<>();
        int childSize = erp_action_pro_field.getChildCount();
        for(int i=0; i<childSize; i++) {
            RelativeLayout childView = (RelativeLayout) erp_action_pro_field.getChildAt(i);
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
                            Toast.makeText(ErpActionProcessActivity.this, fieldName + "是必填项", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ErpActionProcessActivity.this, fieldName_multi + "是必填项", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ErpActionProcessActivity.this, fieldName_number + "是必填项", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ErpActionProcessActivity.this, fieldName_data + "是必填项", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ErpActionProcessActivity.this, fieldName_radio + "是必选项", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ErpActionProcessActivity.this, fieldName_checkbox + "是必选项", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ErpActionProcessActivity.this, fieldName_file+"是必上传项", Toast.LENGTH_SHORT).show();
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
        MAAgent agent = (MAAgent) erp_action_pro_sp_agent.getSelectedItem();
        String agentId = "";
        if(agent != null) {
            agentId = agent._id;
        }

        datas.put("_id", business._id);
        datas.put("actionId", actionId);
        datas.put("master", agentId);
        MobileHttpManager.excuteBusinessStepAction(user._id, datas, jadatas, new ExcuteBusHandler());
    }

//    private boolean isFieldRequired(RelativeLayout childView) {
//        TextView tv_single_required = (TextView) childView.getChildAt(0);
//        String fieldName = tv_single_required.getText().toString();
//        String required = (String) tv_single_required.getTag();
//        if("no".equals(required)) {
//            return false;
//        }
//        return true;
//    }

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
                Toast.makeText(ErpActionProcessActivity.this, "执行成功", Toast.LENGTH_SHORT).show();
                EventBus.getDefault().post(new ErpExcuteSuccess());
                finish();
            }else if("403".equals(HttpParser.getErrorCode(s))) {
                Toast.makeText(ErpActionProcessActivity.this, "该工单步骤已被执行或您已无权限执行", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(ErpActionProcessActivity.this, "执行失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 创建不同字段界面
     * @param fields
     * @param flowFields
     * @param business
     * @param pane
     */
    private void createFlowCustomFields(List<MAActionFields> fields, List<MABusinessField> flowFields, MAErpDetail business, LinearLayout pane) {
        for(int i=0; i<fields.size(); i++) {
            MAActionFields row = fields.get(i);
            for(int j=0; j<row.cols.size(); j++) {
                MACol col = row.cols.get(j);
                for(int k=0; k<col.fields.size(); k++) {
                    MAFields maField = col.fields.get(k);
                    MABusinessField cacheField = getFieldById(flowFields, maField._id);
                    if(cacheField != null) {
                        System.out.println("requier is:"+cacheField.required);
                        switch (cacheField.type) {
                            case "single":
                                RelativeLayout singleView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.erp_field_single, null);
                                singleView.setTag("single");
                                TextView erp_field_single_tv_name = (TextView) singleView.findViewById(R.id.erp_field_single_tv_name);
                                erp_field_single_tv_name.setText(cacheField.name);
                                erp_field_single_tv_name.setTag(cacheField.required);
                                EditText erp_field_single_et_value = (EditText) singleView.findViewById(R.id.erp_field_single_et_value);
                                erp_field_single_et_value.setTag(cacheField._id);
                                pane.addView(singleView);
                                break;
                            case "multi":
                                RelativeLayout multiView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.erp_field_multi, null);
                                multiView.setTag("multi");
                                TextView erp_field_multi_tv_name = (TextView) multiView.findViewById(R.id.erp_field_multi_tv_name);
                                erp_field_multi_tv_name.setText(cacheField.name);
                                erp_field_multi_tv_name.setTag(cacheField.required);
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
                                erp_field_number_tv_name.setTag(cacheField.required);
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
        erp_field_dropdown_tv_name.setTag(cacheField.required);
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
                    ErpSpAdapter adapter = new ErpSpAdapter(ErpActionProcessActivity.this, firstOption);
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
                    final ErpSpAdapter adapter = new ErpSpAdapter(ErpActionProcessActivity.this, firstOption);
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
                            ErpSpAdapter adapter = new ErpSpAdapter(ErpActionProcessActivity.this, secondOptions);
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
                    final ErpSpAdapter adapter = new ErpSpAdapter(ErpActionProcessActivity.this, firstOption);
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
                            ErpSpAdapter adapter = new ErpSpAdapter(ErpActionProcessActivity.this, secondOptions);
                            erp_field_dropdown_item_sp_value2.setAdapter(adapter);

                            erp_field_dropdown_item_sp_value2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    Option o = (Option) parent.getAdapter().getItem(position);
                                    List<Option> threeOptions = getOptionsByKey(secondOptions, o.key);
                                    ErpSpAdapter adapter = new ErpSpAdapter(ErpActionProcessActivity.this, threeOptions);
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
        erp_field_checkbox_tv_name.setTag(cacheField.required);
        GridViewInScrollView checkbox_gv = (GridViewInScrollView) checkboxView.findViewById(R.id.erp_field_checkbox_gv_value);
        checkbox_gv.setTag(cacheField._id);
        if(cacheField.dic != null) {
            MAOption maoption = MobileAssitantCache.getInstance().getMAOption(cacheField.dic);
            if(maoption != null) {
                List<Option> options = maoption.options;
                final ErpCBAdapter adapter = new ErpCBAdapter(ErpActionProcessActivity.this, options);
                checkbox_gv.setAdapter(adapter);
//                for (int i=0; i<options.size(); i++) {
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
        erp_field_radio_tv_name.setTag(cacheField.required);
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
                    RadioButton rb = new RadioButton(ErpActionProcessActivity.this);
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
        DatePickerDialog dpd = new DatePickerDialog(ErpActionProcessActivity.this, new DatePickerDialog.OnDateSetListener() {
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
        erp_field_file_tv_name.setTag(cacheField.required);
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
                    Toast.makeText(ErpActionProcessActivity.this, "上传文件不能大于10M", Toast.LENGTH_SHORT).show();
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

    public interface OnFileUploadCompletedListener {
        void onCompleted(String fileName, String key);
    }

    OnFileUploadCompletedListener  fileUploadCompletedListener = new OnFileUploadCompletedListener() {

        @Override
        public void onCompleted(String fileName, String key) {
            final RelativeLayout rl = (RelativeLayout) LayoutInflater.from(ErpActionProcessActivity.this).inflate(R.layout.erp_field_file_already, null);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MobileApplication.getInstance().remove(this);
    }
}
