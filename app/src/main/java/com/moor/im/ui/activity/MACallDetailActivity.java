package com.moor.im.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.csipsimple.api.ISipService;
import com.csipsimple.api.SipProfile;
import com.moor.im.R;
import com.moor.im.db.dao.UserDao;
import com.moor.im.model.entity.MACallLogData;
import com.moor.im.model.entity.User;
import com.moor.im.ui.view.MP3PlayerView;
import com.moor.im.ui.view.recordbutton.MediaManager;
import com.moor.im.utils.NullUtil;
import com.moor.im.utils.Utils;

/**
 * Created by longwei on 2016/2/24.
 */
public class MACallDetailActivity extends Activity{

    private MACallLogData callLogData;

    private TextView mycalldetail_tv_callno, mycalldetail_tv_calledno,
            mycalldetail_tv_province, mycalldetail_tv_district,
            mycalldetail_tv_investigate, mycalldetail_tv_offeringtime,
            mycalldetail_tv_begintime, mycalldetail_tv_timelength,
            mycalldetail_tv_statusdesc, mycalldetail_tv_agent,mycalldetail_tv_queue,
            mycalldetail_tv_status;

    private ImageView mycalldetail_iv_status;

    private Button mycalldetail_btn_call;

    private MP3PlayerView mycalldetail_mp3player;

    User user = UserDao.getInstance().getUser();
    private ISipService service;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = ISipService.Stub.asInterface(arg1);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            service = null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_macalldetail);

        Intent intent = getIntent();
        if(intent.getSerializableExtra("calllogdata") != null) {
            callLogData = (MACallLogData) intent.getSerializableExtra("calllogdata");
        }
        initViews();
        initData();

        bindService(new Intent().setComponent(new ComponentName("com.moor.im", "com.csipsimple.service.SipService"))
                , connection,
                Context.BIND_AUTO_CREATE);
    }

    private void initViews() {
        mycalldetail_tv_callno = (TextView) findViewById(R.id.mycalldetail_tv_callno);
        mycalldetail_tv_calledno = (TextView) findViewById(R.id.mycalldetail_tv_calledno);
        mycalldetail_tv_province = (TextView) findViewById(R.id.mycalldetail_tv_province);
        mycalldetail_tv_district = (TextView) findViewById(R.id.mycalldetail_tv_district);
        mycalldetail_tv_investigate = (TextView) findViewById(R.id.mycalldetail_tv_investigate);
        mycalldetail_tv_offeringtime = (TextView) findViewById(R.id.mycalldetail_tv_offeringtime);
        mycalldetail_tv_begintime = (TextView) findViewById(R.id.mycalldetail_tv_begintime);
        mycalldetail_tv_timelength = (TextView) findViewById(R.id.mycalldetail_tv_timelength);
        mycalldetail_tv_statusdesc = (TextView) findViewById(R.id.mycalldetail_tv_statusdesc);
        mycalldetail_tv_agent = (TextView) findViewById(R.id.mycalldetail_tv_agent);
        mycalldetail_tv_queue = (TextView) findViewById(R.id.mycalldetail_tv_queue);
        mycalldetail_tv_status = (TextView) findViewById(R.id.mycalldetail_tv_status);
        mycalldetail_iv_status = (ImageView) findViewById(R.id.mycalldetail_iv_status);


        mycalldetail_mp3player = (MP3PlayerView) findViewById(R.id.mycalldetail_mp3player);
        mycalldetail_btn_call = (Button) findViewById(R.id.mycalldetail_btn_call);


    }

    private void initData() {
        if(callLogData == null) {
            return;
        }
        mycalldetail_tv_callno.setText(NullUtil.checkNull(callLogData.CALL_NO));
        mycalldetail_tv_calledno.setText(NullUtil.checkNull(callLogData.CALLED_NO));
        mycalldetail_tv_province.setText(NullUtil.checkNull(callLogData.PROVINCE));
        mycalldetail_tv_district.setText(NullUtil.checkNull(callLogData.DISTRICT));
        mycalldetail_tv_investigate.setText(NullUtil.checkNull(callLogData.INVESTIGATE));
        mycalldetail_tv_offeringtime.setText(NullUtil.checkNull(callLogData.OFFERING_TIME));
        mycalldetail_tv_begintime.setText(NullUtil.checkNull(callLogData.BEGIN_TIME));
        if(!"0秒".equals(NullUtil.checkNull(callLogData.shortCallTimeLength))) {
            mycalldetail_tv_timelength.setText(NullUtil.checkNull(callLogData.shortCallTimeLength));
        }
        mycalldetail_tv_statusdesc.setText(NullUtil.checkNull(callLogData.status));
        mycalldetail_tv_agent.setText(NullUtil.checkNull(callLogData.agent));
        mycalldetail_tv_queue.setText(NullUtil.checkNull(callLogData.queue));

        String dialType = NullUtil.checkNull(callLogData.dialType);
        String number = "";
        if("outbound".equals(dialType)) {
            mycalldetail_tv_status.setText("外呼去电");
            mycalldetail_iv_status.setImageResource(R.drawable.outcall_icon);
            number = NullUtil.checkNull(callLogData.CALLED_NO);
        }else if("inbound".equals(dialType)) {
            mycalldetail_tv_status.setText("接听来电");
            mycalldetail_iv_status.setImageResource(R.drawable.incall_icon);
            number = NullUtil.checkNull(callLogData.CALL_NO);
        }

        final String finalNumber = number;
        mycalldetail_btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callingDialog(finalNumber);
            }
        });

        if("success".equals(callLogData.statusClass)) {
            final String urlPath = callLogData.FILE_SERVER+"/"+callLogData.RECORD_FILE_NAME;
            mycalldetail_mp3player.setVisibility(View.VISIBLE);
            mycalldetail_mp3player.setUrlPath(urlPath);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mycalldetail_mp3player.stop();
        unbindService(connection);
    }

    public void callingDialog(final String number) {
        LayoutInflater myInflater = LayoutInflater.from(MACallDetailActivity.this);
        final View myDialogView = myInflater.inflate(R.layout.calling_dialog,
                null);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MACallDetailActivity.this)
                .setView(myDialogView);
        final AlertDialog alert = dialog.show();
        alert.setCanceledOnTouchOutside(true);// 设置点击Dialog外部任意区域关闭Dialog
        alert.getWindow().setGravity(Gravity.BOTTOM);

        // 直播
        LinearLayout mDirectSeeding = (LinearLayout) myDialogView
                .findViewById(R.id.direct_seeding_linear);
        mDirectSeeding.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                try {
                    if (Utils.isNetWorkConnected(MACallDetailActivity.this)) {
                        makeCall(number);
                    } else {
                        Toast.makeText(MACallDetailActivity.this, "网络错误，请重试！",
                                Toast.LENGTH_LONG).show();
                    }
                    alert.dismiss();
//					editText_phone_number.setText("");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
        // 回拨
        LinearLayout mCallReturn = (LinearLayout) myDialogView
                .findViewById(R.id.call_return_linear);
//		if("zj".equals(user.product)) {
//			mCallReturn.setVisibility(View.GONE);
//		}
        mCallReturn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String mobile = user.mobile;
                if(mobile == null || "".equals(mobile)) {
                    Toast.makeText(MACallDetailActivity.this, "未绑定手机，不能进行回拨", Toast.LENGTH_SHORT).show();

                }else {
                    // TODO Auto-generated method stub
                    if (Utils.isNetWorkConnected(MACallDetailActivity.this)) {
                        // 跳转到正在通话页面
                        Intent calling = new Intent(MACallDetailActivity.this,
                                CallingActivity.class);
                        calling.putExtra("phone_number", number);
                        startActivity(calling);
                    } else {
                        Toast.makeText(MACallDetailActivity.this, "网络错误，请重试！",
                                Toast.LENGTH_LONG).show();
                    }
                    alert.dismiss();
                }

            }
        });

        // 普通电话
        LinearLayout mOrdinaryCall = (LinearLayout) myDialogView
                .findViewById(R.id.ordinary_call_linear);
        mOrdinaryCall.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(Intent.ACTION_CALL, Uri
                        .parse("tel://"
                                + number));
                startActivity(intent);
                alert.dismiss();
            }
        });
        // 取消
        LinearLayout mCancelLinear = (LinearLayout) myDialogView
                .findViewById(R.id.cancel_linear);
        mCancelLinear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                alert.dismiss();
            }
        });
    }

    /**
     * 拨打直拨电话
     * @param callee
     */
    public void makeCall(String callee) {
        //TODO 获取id
        Long id = -1L;
        Cursor c = getContentResolver().query(SipProfile.ACCOUNT_URI, null, null, null, null);
        if(c != null) {
            while(c.moveToNext()) {
                id = c.getLong(c.getColumnIndex("id"));
            }
        }
//		System.out.println("sip账户ID是："+id);
        try {
            service.makeCall(callee, id.intValue());
        } catch (RemoteException e) {
            Toast.makeText(MACallDetailActivity.this, "拨打电话失败", Toast.LENGTH_SHORT).show();
        }
    }
}
