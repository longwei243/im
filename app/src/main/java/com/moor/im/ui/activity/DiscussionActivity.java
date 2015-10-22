package com.moor.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.CacheKey;
import com.moor.im.app.MobileApplication;
import com.moor.im.http.HttpManager;
import com.moor.im.model.entity.Discussion;
import com.moor.im.model.entity.Group;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.ui.adapter.DiscussionAdapter;
import com.moor.im.ui.adapter.GroupAdapter;
import com.moor.im.utils.DiscussionActivityUtil;
import com.moor.im.utils.GroupActivityUtil;
import com.moor.im.utils.LogUtil;

import org.apache.http.Header;

import java.util.List;

/**
 * Created by long on 2015/7/22.
 */
public class DiscussionActivity extends Activity{

    private SharedPreferences sp;
    private ListView mListView;

    DiscussionAdapter adapter;

    ImageView title_btn_back, title_btn_add;

    TextView title_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        DiscussionActivityUtil.getInstance().add(this);
        setContentView(R.layout.activity_group);
        sp = getSharedPreferences("SP", 4);



        mListView = (ListView) findViewById(R.id.group_list);
        title_name = (TextView) findViewById(R.id.title_name);
        title_name.setText("讨论组");

        title_btn_back = (ImageView) findViewById(R.id.title_btn_back);
        title_btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        title_btn_add = (ImageView) findViewById(R.id.title_btn_add);
//        String product = sp.getString("product", "");
//        if("zj".equals(product)) {
//            boolean isAdmin = sp.getBoolean("isAdmin", false);
//            if(!isAdmin) {
//                title_btn_add.setVisibility(View.GONE);
//            }
//        }else if("cc".equals(product)) {
//            String type = sp.getString("type", "");
//            if(!"manager".equals(type)) {
//                title_btn_add.setVisibility(View.GONE);
//            }
//        }

        title_btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DiscussionActivity.this, AddDiscussionActivity.class);
                startActivity(intent);
            }
        });
        getDiscussionDataFromLocal();

    }

    private void getDiscussionDataFromLocal() {
        if(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_DISCUSSION) != null) {
            final List<Discussion> discussions = HttpParser.getDiscussion(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_DISCUSSION));
            adapter = new DiscussionAdapter(DiscussionActivity.this, discussions);
            mListView.setAdapter(adapter);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent chatIntent = new Intent(DiscussionActivity.this, ChatActivity.class);
                    chatIntent.putExtra("type", "Discussion");
                    chatIntent.putExtra("_id", discussions.get(position)._id);
                    chatIntent.putExtra("otherName", discussions.get(position).title);
                    startActivity(chatIntent);
                    finish();
                }
            });

            getDiscussionDataFromNet();
        }else {
            getDiscussionDataFromNet();
        }
    }

    public void getDiscussionDataFromNet() {
        HttpManager.getDiscussionByUser(sp.getString("connecTionId", ""),
                new GetDiscussionResponseHandler());
    }

    class GetDiscussionResponseHandler extends TextHttpResponseHandler {

        @Override
        public void onFailure(int statusCode, Header[] headers,
                              String responseString, Throwable throwable) {
//            Toast.makeText(DiscussionActivity.this, "请检查您的网络问题！！！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers,
                              String responseString) {
            String succeed = HttpParser.getSucceed(responseString);
            String message = HttpParser.getMessage(responseString);
            LogUtil.d("DiscussionActivity", "获取讨论组返回结果:" + responseString);
            if ("true".equals(succeed)) {
                //将数据存到本地
                MobileApplication.cacheUtil.put(CacheKey.CACHE_DISCUSSION, responseString);
                final List<Discussion> discussions = HttpParser.getDiscussion(responseString);
                adapter = new DiscussionAdapter(DiscussionActivity.this, discussions);
                mListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent chatIntent = new Intent(DiscussionActivity.this, ChatActivity.class);
                        chatIntent.putExtra("type", "Discussion");
                        chatIntent.putExtra("_id", discussions.get(position)._id);
                        chatIntent.putExtra("otherName", discussions.get(position).title);
                        startActivity(chatIntent);
                        finish();
                    }
                });

                //通知消息页重新刷新一次数据
                Message msg = new Message();
                msg.obj = "msg";
                MobileApplication.getHandler().sendMessage(msg);
            } else {
            }
        }
    }
}


