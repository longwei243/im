package com.moor.im.ui.activity;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;

/**
 * Created by long on 2015/7/30.
 */
public class AboutMeActivity extends Activity{


    ImageView btn_back;

    TextView aboutme_tv_version;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        MobileApplication.getInstance().add(this);
        setContentView(R.layout.activity_aboutme);

        btn_back = (ImageView) findViewById(R.id.btn_back);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        aboutme_tv_version = (TextView) findViewById(R.id.aboutme_tv_version);

        String versionStr = "容联七陌v"+getVersion();

        aboutme_tv_version.setText(versionStr);
    }

    /**
     * 获取应用版本号
     * @return
     */
    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MobileApplication.getInstance().remove(this);
    }
}
