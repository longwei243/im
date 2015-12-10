package com.moor.im.ui.activity;

import java.io.File;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.http.HttpManager;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.ui.view.LoadingView;

import de.greenrobot.event.EventBus;

public class UpdateActivity extends Activity{
	
	public final static int HasUpdate = 0;
	public final static int LatestUpdate = 1;
	public final static int ProgessBar_Visible = 10;
	public final static int ProgessBar_Max = 11;
	public final static int ProgessBar_Progress = 12;
	public final static int DownLoad_Finish = 13;
	public final static int DownLoad_Error = -1;
	
	private SharedPreferences sp;
	
	private static LoadingView loadingView;
	private static TextView textView;
	private static ProgressBar progressBar;
	private Thread initLoadingThread;
	private static Button parentdialog_cancel_Button,
			parentdialog_confirm_Button;
	private String version;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		sp = getSharedPreferences("SP", 0);
		setContentView(R.layout.parentdialog);
		
		initViews();
		
		getVersionFromNet();
	}
	
	private void initViews() {
		TextView title = (TextView) findViewById(R.id.parentdialog_title_TextView);
		title.setText("请稍候");
		parentdialog_cancel_Button = (Button) findViewById(R.id.parentdialog_cancel_Button);
		parentdialog_cancel_Button.setOnClickListener(clickListener);
		parentdialog_confirm_Button = (Button) findViewById(R.id.parentdialog_confirm_Button);
		parentdialog_confirm_Button.setVisibility(View.GONE);
		parentdialog_confirm_Button.setOnClickListener(clickListener);
		View view = LayoutInflater.from(this).inflate(
				R.layout.update_versioncheck, null);
		FrameLayout parentdialog_content_FrameLayout = (FrameLayout) findViewById(R.id.parentdialog_content_FrameLayout);
		parentdialog_content_FrameLayout.addView(view);
		loadingView = (LoadingView) view
				.findViewById(R.id.update_versioncheck_LoadingView);
		textView = (TextView) view
				.findViewById(R.id.update_versioncheck_TextView);
		progressBar = (ProgressBar) view
				.findViewById(R.id.update_versioncheck_ProgressBar);
		initLoadingImages();
	}
	
	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			initLoadingThread.interrupt();
			switch (v.getId()) {
			case R.id.parentdialog_cancel_Button:
				finish();
				break;
			case R.id.parentdialog_confirm_Button:
				switch ((Integer) v.getTag(v.getId())) {
				case HasUpdate:
					//需要更新
					//url是下载的地址
					String url = "http://7xjsdj.dl1.z0.glb.clouddn.com/7moor_android_v"+version+".apk";
//					System.out.println("下载apk的地址是："+url);
					downLoadApk(url);
					break;
				case DownLoad_Finish:
					//下载完成，安装apk
					install(filePath);
					finish();
					break;
				case DownLoad_Error:
					//下载出错
					finish();
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
		}
	};
	private String filePath;
	

	private void initLoadingImages() {
		int[] imageIds = new int[8];
		imageIds[0] = R.drawable.ic_spinner1;
		imageIds[1] = R.drawable.ic_spinner2;
		imageIds[2] = R.drawable.ic_spinner3;
		imageIds[3] = R.drawable.ic_spinner4;
		imageIds[4] = R.drawable.ic_spinner5;
		imageIds[5] = R.drawable.ic_spinner6;
		imageIds[6] = R.drawable.ic_spinner7;
		imageIds[7] = R.drawable.ic_spinner8;
		loadingView.setImageIds(imageIds);
		initLoadingThread = new Thread() {
			@Override
			public void run() {
				loadingView.startAnim();
			}
		};
		initLoadingThread.start();
	}
	
	
	
	
	/**
	 * 从网络获取版本号
	 */
	public void getVersionFromNet() {
		HttpManager.getVersion(sp.getString("connecTionId", ""),
				new GetVersionResponseHandler());
	}
	
	class GetVersionResponseHandler extends TextHttpResponseHandler {

		@Override
		public void onFailure(int statusCode, Header[] headers,
				String responseString, Throwable throwable) {
			Toast.makeText(UpdateActivity.this, "请检查您的网络问题！！！", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
				String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			String message = HttpParser.getMessage(responseString);
			if ("true".equals(succeed)) {
				try {
					JSONObject jsonObject = new JSONObject(responseString);
					JSONObject jb = (JSONObject) jsonObject.get("AppVersion");
					version = jb.getString("android");
					if(!getVersion().equals(version)) {
						//有更新
						loadingView.setVisibility(View.GONE);
						
						textView.setText("发现新版本， 确定更新？");
						
						parentdialog_confirm_Button.setVisibility(View.VISIBLE);
						parentdialog_confirm_Button.setTag(
								R.id.parentdialog_confirm_Button, HasUpdate);
					}else {
						//没有更新
						loadingView.setVisibility(View.GONE);
						textView.setText("已是最新版本");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
//				Toast.makeText(UpdateActivity.this, message, Toast.LENGTH_SHORT)
//						.show();
			}
		}
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
	/**
	 * 安装apk
	 * @param filePath apk在本地的文件路径
	 */
	public void install(String filePath) {  
        Intent intent = new Intent(Intent.ACTION_VIEW);  
        intent.setDataAndType(Uri.fromFile(new File(filePath)),  
                "application/vnd.android.package-archive");  
        startActivity(intent);  
    }  
	/**
	 * 下载apk文件
	 * @param url
	 */
	public void downLoadApk(String url) {
		AsyncHttpClient httpclient = MobileApplication.httpclient;
		final String dirStr = Environment.getExternalStorageDirectory() + File.separator + "m7/downloadfile/";
		
		File dir = new File(dirStr);
		if(!dir.exists()) {
			dir.mkdirs();
			
		}
		
		File file = new File(dir, "7moor.apk");
		if(file.exists()) {
			file.delete();
			file = new File(dir, "7moor.apk");
		}
		filePath = file.getAbsolutePath();
		
		
		httpclient.get(url, new FileAsyncHttpResponseHandler(file) {
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, File file) {
//				System.out.println("下载文件成功了");
				textView.setVisibility(View.VISIBLE);
				textView.setText("下载完成,点击确认安装");
				progressBar.setVisibility(View.GONE);
				parentdialog_confirm_Button.setClickable(true);
				parentdialog_confirm_Button.setVisibility(View.VISIBLE);
				parentdialog_confirm_Button.setTag(
						R.id.parentdialog_confirm_Button,
						DownLoad_Finish);
			}
			
			

			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				// TODO Auto-generated method stub
				super.onProgress(bytesWritten, totalSize);
//				System.out.println("当前进度是："+bytesWritten);
				textView.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
				parentdialog_confirm_Button.setClickable(false);
				progressBar.setProgress(bytesWritten);
				progressBar.setMax(totalSize);
			}



			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, File file) {
//				System.out.println("下载文件失败了");
				textView.setText("下载出现错误");
				progressBar.setVisibility(View.GONE);
				parentdialog_cancel_Button.setVisibility(View.GONE);
				parentdialog_confirm_Button.setClickable(true);
				parentdialog_confirm_Button.setVisibility(View.VISIBLE);
				parentdialog_confirm_Button.setTag(
						R.id.parentdialog_confirm_Button,
						DownLoad_Error);
			}
		});  
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN
				&& isOutOfBounds(this, event)) {
			return true;
		}
		return super.onTouchEvent(event);
	}

	private boolean isOutOfBounds(Activity context, MotionEvent event) {
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		final int slop = ViewConfiguration.get(context)
				.getScaledWindowTouchSlop();
		final View decorView = context.getWindow().getDecorView();
		return (x < -slop) || (y < -slop)
				|| (x > (decorView.getWidth() + slop))
				|| (y > (decorView.getHeight() + slop));
	}
}
