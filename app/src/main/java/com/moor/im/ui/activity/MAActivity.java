package com.moor.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.moor.im.R;


/**
 * Created by longwei on 2016/5/12.
 */
public class MAActivity extends Activity{

    LinearLayout ma_cdr, ma_erp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_ma);
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ma_cdr = (LinearLayout) findViewById(R.id.ma_cdr);
        ma_erp = (LinearLayout) findViewById(R.id.ma_erp);

        ma_cdr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mobileIntent = new Intent(MAActivity.this, MACdrActivity.class);
				startActivity(mobileIntent);
            }
        });
        ma_erp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent erpIntent = new Intent(MAActivity.this, MAErpActivity.class);
				startActivity(erpIntent);
            }
        });
    }


}
