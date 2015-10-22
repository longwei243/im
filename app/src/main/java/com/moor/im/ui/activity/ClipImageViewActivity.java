package com.moor.im.ui.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.RequestUrl;
import com.moor.im.db.dao.MessageDao;
import com.moor.im.db.dao.UserDao;
import com.moor.im.event.UserIconUpdate;
import com.moor.im.http.HttpManager;
import com.moor.im.model.entity.FromToMessage;
import com.moor.im.model.entity.User;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.ui.dialog.LoadingFragmentDialog;
import com.moor.im.ui.view.clipimageview.ClipImageLayout;
import com.moor.im.utils.LogUtil;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import de.greenrobot.event.EventBus;

/**
 * Created by long on 2015/7/6.
 */
public class ClipImageViewActivity extends Activity{

    ClipImageLayout clipImageLayout;

    ImageView clipimagelayout_iv_ok;

    Drawable drawable;

    private SharedPreferences sp;

    LoadingFragmentDialog loadingFragmentDialog;

    User user = UserDao.getInstance().getUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clipimageview);
        sp = this.getSharedPreferences("SP", 4);

        ActionBar ab = getActionBar();
        if(ab != null) {
            ab.setDisplayShowHomeEnabled(false);
            ab.setTitle("裁剪头像");
        }


        loadingFragmentDialog = new LoadingFragmentDialog();

        Intent intent = getIntent();
        String imagePath = intent.getStringExtra("imagePath");
        LogUtil.d("ClipImageViewActivity", "传过来的图片路径是:"+imagePath);

        clipImageLayout = (ClipImageLayout) findViewById(R.id.clipimagelayout);
        clipimagelayout_iv_ok = (ImageView) findViewById(R.id.clipimagelayout_iv_ok);

        final Bitmap bitmap = optimizeBitmap(imagePath, 800, 800);
        drawable = new BitmapDrawable(bitmap);

        clipImageLayout.setmDrawable(drawable);
        LogUtil.d("ClipImageViewActivity", "把图像设置到了裁剪区域");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.icon_clip, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.clip_icon:

                loadingFragmentDialog.show(getFragmentManager(), "");
                //点了确定就上传吧
                Bitmap clipBitmap = clipImageLayout.clip();
                ProcessBitmapTask pbt = new ProcessBitmapTask();
                pbt.execute(clipBitmap);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public Bitmap optimizeBitmap(String pathName, int maxWidth,
                                        int maxHeight) {
        Bitmap result = null;
        // 图片配置对象，该对象可以配置图片加载的像素获取个数
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 表示加载图像的原始宽高
        options.inJustDecodeBounds = true;
        result = BitmapFactory.decodeFile(pathName, options);
        // Math.ceil表示获取与它最近的整数（向上取值 如：4.1->5 4.9->5）
        int widthRatio = (int) Math.ceil(options.outWidth / maxWidth);
        int heightRatio = (int) Math.ceil(options.outHeight / maxHeight);

        // 设置最终加载的像素比例，表示最终显示的像素个数为总个数的
        if (widthRatio > 1 || heightRatio > 1) {
            if (widthRatio > heightRatio) {
                options.inSampleSize = widthRatio;
            } else {
                options.inSampleSize = heightRatio;
            }
        }
        // 解码像素的模式，在该模式下可以直接按照option的配置取出像素点
        options.inJustDecodeBounds = false;
        result = BitmapFactory.decodeFile(pathName, options);
        return result;
    }


    class UploadFileResponseHandler extends TextHttpResponseHandler {
        String filePath;

        public UploadFileResponseHandler(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void onFailure(int statusCode, Header[] headers,
                              String responseString, Throwable throwable) {

            loadingFragmentDialog.dismiss();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers,
                              String responseString) {
            // TODO Auto-generated method stub
            String succeed = HttpParser.getSucceed(responseString);
            String message = HttpParser.getMessage(responseString);
            if ("true".equals(succeed)) {

                String upToken = HttpParser.getUpToken(responseString);
                // qiniu SDK自带方法上传
                UploadManager uploadManager = new UploadManager();
//                final String imgFileKey = "UserIcon/"+UUID.randomUUID().toString();
                //{account}/{type}/{data}/{timestamp}/{filename}
                String fileName = UUID.randomUUID().toString();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
                String date = sdf.format(new Date());
                final String imgFileKey = user.account+"/icon/"+date + "/"+ System.currentTimeMillis()+"/"+fileName;
                uploadManager.put(filePath, imgFileKey, upToken,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String key,
                                                 ResponseInfo info, JSONObject response) {
                                // TODO Auto-generated method stub
                                System.out.println("上传头像图片成功了");
                                System.out.println(key + "     " + info
                                        + "      " + response);

                                String iconUrl = RequestUrl.QiniuHttp + imgFileKey;
                                LogUtil.d("UploadFileResponseHandler", "头像在7牛服务器的url:"+iconUrl);

                                HttpManager.updateUserIcon(sp.getString("connecTionId", ""), iconUrl, new UpdateUserIconHandler());
                                LogUtil.d("ClipImageViewActivity", "发起了上传头像的http请求");
                            }
                        }, null);

            }else {
                Toast.makeText(ClipImageViewActivity.this, "网络有问题", Toast.LENGTH_SHORT).show();
                loadingFragmentDialog.dismiss();
            }
        }
    }


    class UpdateUserIconHandler extends TextHttpResponseHandler {
        @Override
        public void onFailure(int statusCode, Header[] headers,
                              String responseString, Throwable throwable) {
            loadingFragmentDialog.dismiss();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers,
                              String responseString) {
            String succeed = HttpParser.getSucceed(responseString);
            String message = HttpParser.getMessage(responseString);

            if ("true".equals(succeed)) {
                loadingFragmentDialog.dismiss();
                LogUtil.d("UpdateUserIconHandler", "头像修改成功了");
                EventBus.getDefault().post(new UserIconUpdate());
                ClipImageViewActivity.this.finish();

            } else {
                LogUtil.d("UpdateUserIconHandler", "头像修改失败了");
                loadingFragmentDialog.dismiss();
                Toast.makeText(ClipImageViewActivity.this, "头像上传失败", Toast.LENGTH_SHORT).show();
            }
        }
    }


    class ProcessBitmapTask extends AsyncTask {

        @Override
        protected String doInBackground(Object[] params) {
            Bitmap bitmap = (Bitmap) params[0];
            String dirStr = Environment.getExternalStorageDirectory() + File.separator + "m7/iconfile/";

            File dir = new File(dirStr);
            if(!dir.exists()) {
                dir.mkdirs();

            }

            File file = new File(dir, UUID.randomUUID().toString() + "usericon.png");

            String filePath = file.getAbsolutePath();
            OutputStream os = null;
            try {
                os = new FileOutputStream(filePath);
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, os);
                os.flush();
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return filePath;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            String filePath = (String)o;
            //上传7牛
            HttpManager.getQiNiuToken(sp.getString("connecTionId", ""),
                    filePath, new UploadFileResponseHandler(filePath));

        }
    }
}
