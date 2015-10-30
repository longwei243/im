package com.moor.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.view.ContextMenu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.CacheKey;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.dao.UserDao;
import com.moor.im.http.HttpManager;
import com.moor.im.model.entity.Group;
import com.moor.im.model.entity.User;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.ui.adapter.GroupAdapter;
import com.moor.im.utils.GroupActivityUtil;
import com.moor.im.utils.NullUtil;

import org.apache.http.Header;

import java.util.List;

/**
 * Created by long on 2015/7/16.
 */
public class GroupActivity extends Activity{

    private SharedPreferences sp;
    private ListView mListView;

    GroupAdapter adapter;

    ImageView title_btn_back, title_btn_add;

    User user = UserDao.getInstance().getUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        GroupActivityUtil.getInstance().add(this);
        setContentView(R.layout.activity_group);
        sp = getSharedPreferences("SP", 4);
        mListView = (ListView) findViewById(R.id.group_list);

        title_btn_back = (ImageView) findViewById(R.id.title_btn_back);
        title_btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        title_btn_add = (ImageView) findViewById(R.id.title_btn_add);
        String product = user.product;
        if("zj".equals(NullUtil.checkNull(product))) {
            boolean isAdmin = user.isAdmin;
            if(!isAdmin) {
                title_btn_add.setVisibility(View.GONE);
            }
        }else if("cc".equals(NullUtil.checkNull(product))) {
            String type = user.type;
            if(!"manager".equals(NullUtil.checkNull(type))) {
                title_btn_add.setVisibility(View.GONE);
            }
        }

        title_btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupActivity.this, AddGourpActivity.class);
                startActivity(intent);
            }
        });

        getGroupDataFromLocal();

    }

    private void getGroupDataFromLocal() {
        if(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_GROUP) != null) {
            final List<Group> groups = HttpParser.getGroups(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_GROUP));
            adapter = new GroupAdapter(GroupActivity.this, groups);
            mListView.setAdapter(adapter);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent chatIntent = new Intent(GroupActivity.this, ChatActivity.class);
                    chatIntent.putExtra("type", "Group");
                    chatIntent.putExtra("_id", NullUtil.checkNull(groups.get(position)._id));
                    chatIntent.putExtra("otherName", NullUtil.checkNull(groups.get(position).title));
                    startActivity(chatIntent);
                    finish();
                }
            });

            getGroupDataFromNet();
        }else {
            getGroupDataFromNet();
        }

    }

    public void getGroupDataFromNet() {
        HttpManager.getGroupByUser(sp.getString("connecTionId", ""),
                new GetGroupResponseHandler());
    }

    class GetGroupResponseHandler extends TextHttpResponseHandler {

        @Override
        public void onFailure(int statusCode, Header[] headers,
                              String responseString, Throwable throwable) {
//            Toast.makeText(GroupActivity.this, "请检查您的网络问题！！！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers,
                              String responseString) {
            String succeed = HttpParser.getSucceed(responseString);
            String message = HttpParser.getMessage(responseString);
            if ("true".equals(succeed)) {
                //将数据存到本地
              MobileApplication.cacheUtil.put(CacheKey.CACHE_GROUP, responseString);
              final List<Group> groups = HttpParser.getGroups(responseString);
              adapter = new GroupAdapter(GroupActivity.this, groups);
              mListView.setAdapter(adapter);
              adapter.notifyDataSetChanged();

              mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                  @Override
                  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                      Intent chatIntent = new Intent(GroupActivity.this, ChatActivity.class);
                      chatIntent.putExtra("type", "Group");
                      chatIntent.putExtra("_id", NullUtil.checkNull(groups.get(position)._id));
                      chatIntent.putExtra("otherName", NullUtil.checkNull(groups.get(position).title));
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
