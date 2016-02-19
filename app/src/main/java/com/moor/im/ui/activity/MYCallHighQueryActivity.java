package com.moor.im.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.moor.im.R;

import java.util.HashMap;


/**
 * Created by longwei on 2016/2/19.
 */
public class MYCallHighQueryActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mycall_highquery);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> datas = new HashMap<String, String>();
                datas.put("STATUS", "leak");

                Intent dataIntent = new Intent();
                dataIntent.putExtra("highQueryData", datas);
                setResult(Activity.RESULT_OK, dataIntent);
                finish();
            }
        });
    }
}
