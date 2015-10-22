package com.moor.im.ui.actionbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.ActionProvider;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.ui.activity.AddDiscussionActivity;
import com.moor.im.ui.activity.AddGourpActivity;
import com.moor.im.ui.activity.DepartmentAddActivity;
import com.moor.im.ui.dialog.LoginOffDialog;

/**
 * 自定义的actionbar上+中的菜单项
 * @author LongWei
 *
 */
@SuppressLint("NewApi")
public class PlusActionProvider extends ActionProvider{
	private Context context;

    private SharedPreferences sp;
	  
    public PlusActionProvider(Context context) {  
        super(context);  
        this.context = context;
        sp = context.getSharedPreferences("SP", 0);
    }  
  
    @Override  
    public View onCreateActionView() {  
        return null;  
    }  
  
    @Override  
    public void onPrepareSubMenu(SubMenu subMenu) {  
        subMenu.clear();
        subMenu.add(context.getString(R.string.plus_group))
                .setIcon(R.drawable.ofm_group_chat_icon)
                .setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        String product = sp.getString("product", "");
                        if("zj".equals(product)) {
                            boolean isAdmin = sp.getBoolean("isAdmin", false);
                            if(isAdmin) {
                                Intent intent = new Intent(context, AddGourpActivity.class);
                                context.startActivity(intent);
                            }else {
                                Toast.makeText(context, "您没有该权限", Toast.LENGTH_SHORT).show();
                            }
                        }else if("cc".equals(product)) {
                            String type = sp.getString("type", "");
                            if("manager".equals(type)) {
                                Intent intent = new Intent(context, AddGourpActivity.class);
                                context.startActivity(intent);
                            }else {
                                Toast.makeText(context, "您没有该权限", Toast.LENGTH_SHORT).show();
                            }
                        }

                        return true;
                    }
                });
        subMenu.add(context.getString(R.string.plus_discussion))
                .setIcon(R.drawable.ofm_add_icon)
                .setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        String product = sp.getString("product", "");
                        if("zj".equals(product)) {
                            boolean isAdmin = sp.getBoolean("isAdmin", false);
                            if(isAdmin) {
                                Intent intent = new Intent(context, AddDiscussionActivity.class);
                                context.startActivity(intent);
                            }else {
                                Toast.makeText(context, "您没有该权限", Toast.LENGTH_SHORT).show();
                            }
                        }else if("cc".equals(product)) {
                            String type = sp.getString("type", "");
                            if("manager".equals(type)) {
                                Intent intent = new Intent(context, AddDiscussionActivity.class);
                                context.startActivity(intent);
                            }else {
                                Toast.makeText(context, "您没有该权限", Toast.LENGTH_SHORT).show();
                            }
                        }

                        return true;
                    }
                });
    }  

  
    @Override  
    public boolean hasSubMenu() {  
        return true;  
    }  
}
