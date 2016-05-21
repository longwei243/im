package com.moor.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.ui.view.TouchImageView;
import com.moor.im.utils.LogUtil;

import org.apache.http.Header;

import java.io.File;
import java.util.UUID;

/**
 * Created by long on 2015/7/3.
 */
public class ImageViewLookActivity extends Activity{

    private TouchImageView touchImageView;
    private Button image_btn_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        MobileApplication.getInstance().add(this);
        setContentView(R.layout.activity_image_look);
        touchImageView = (TouchImageView) findViewById(R.id.matrixImageView);
        image_btn_save = (Button) findViewById(R.id.image_btn_save);

        Intent intent = getIntent();
        final String imgPath = intent.getStringExtra("imagePath");

        if(imgPath != null && !"".equals(imgPath)) {
            Glide.with(this).load(imgPath)
                    .placeholder(R.drawable.pic_thumb_bg)
                    .error(R.drawable.image_download_fail_icon)
                    .into(touchImageView);
        }else {
            finish();
        }

        touchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if(imgPath.startsWith("http")) {
            image_btn_save.setVisibility(View.VISIBLE);
        }

        image_btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //保存图片到本地
                Toast.makeText(ImageViewLookActivity.this, "图片开始下载了", Toast.LENGTH_SHORT).show();
                downLoadImg(imgPath);
            }
        });
    }

    /**
     * 下载图片
     * @param url
     */
    public void downLoadImg(String url) {
        AsyncHttpClient httpclient = MobileApplication.httpclient;
        final String dirStr = Environment.getExternalStorageDirectory() + File.separator + "m7/downloadImagefile/";

        File dir = new File(dirStr);
        if(!dir.exists()) {
            dir.mkdirs();

        }

        File file = new File(dir, UUID.randomUUID()+".png");
        if(file.exists()) {
            return;
        }

        final String filePath = file.getAbsolutePath();


        httpclient.get(url, new FileAsyncHttpResponseHandler(file) {

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                LogUtil.d("ImageViewLookActivity", "图片已保存到"+filePath);
                Toast.makeText(ImageViewLookActivity.this, "图片以保存到"+filePath, Toast.LENGTH_SHORT).show();
            }


            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                // TODO Auto-generated method stub
                super.onProgress(bytesWritten, totalSize);
//
            }


            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  Throwable throwable, File file) {
                Toast.makeText(ImageViewLookActivity.this, "图片下载失败了", Toast.LENGTH_SHORT).show();
            }
        });

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        MobileApplication.getInstance().remove(this);
    }
}
