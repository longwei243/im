package com.moor.im.ui.adapter;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.moor.im.R;
import com.moor.im.model.entity.Contacts;
import com.moor.im.ui.view.RoundImageView;
import com.moor.im.utils.LogUtil;

/**
 * 联系人列表的adapter
 * @author LongWei
 *
 */
public class ContactListViewAdapter extends BaseAdapter implements SectionIndexer{
	private List<Contacts> list = null;  
    private Context mContext;  
      
    public ContactListViewAdapter(Context mContext, List<Contacts> list) {  
        this.mContext = mContext;  
        this.list = list;  
    }  
      
    /** 
     * 当ListView数据发生变化时,调用此方法来更新ListView 
     * @param list 
     */  
    public void updateListView(List<Contacts> list){  
        this.list = list;  
        notifyDataSetChanged();  
    }  
  
    public int getCount() {  
        return list.size();  
    }  
  
    public Object getItem(int position) {  
        return list.get(position);  
    }  
  
    public long getItemId(int position) {  
        return position;  
    }  
  
    public View getView(final int position, View convertView, ViewGroup arg2) {  
        ViewHolder viewHolder = null;  
        final Contacts contacts = list.get(position);  
        if (convertView == null) {  
            viewHolder = new ViewHolder();  
            convertView = LayoutInflater.from(mContext).inflate(R.layout.contact_listview_item, null);  
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.contact_listview_item_textview);  
            viewHolder.tvExten = (TextView) convertView.findViewById(R.id.contact_listview_item_textview_exten);  
            viewHolder.tvLetter = (TextView) convertView.findViewById(R.id.catalog);
            viewHolder.line = convertView.findViewById(R.id.item_line);
            viewHolder.contact_listview_item_iamgeview = (ImageView) convertView.findViewById(R.id.contact_listview_item_iamgeview);
            convertView.setTag(viewHolder);  
        } else {  
            viewHolder = (ViewHolder) convertView.getTag();  
        }  
          
        //根据position获取分类的首字母的char ascii值  
        int section = getSectionForPosition(position);  
          
        //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现  
        if(position == getPositionForSection(section)){  
            viewHolder.tvLetter.setVisibility(View.VISIBLE);  
            viewHolder.tvLetter.setText(contacts.pinyin.toUpperCase().substring(0,1));
            
        }else{  
            viewHolder.tvLetter.setVisibility(View.GONE);  
        }  
      
        viewHolder.tvTitle.setText(this.list.get(position).displayName);  
        viewHolder.tvExten.setText(this.list.get(position).exten);

        String im_icon = list.get(position).im_icon;
        if(im_icon != null && !"".equals(im_icon)) {
            Glide.with(mContext).load(im_icon + "?imageView2/0/w/100/h/100").asBitmap().placeholder(R.drawable.head_default_local).into(viewHolder.contact_listview_item_iamgeview);
        }else {
            Glide.with(mContext).load(R.drawable.head_default_local).asBitmap().into(viewHolder.contact_listview_item_iamgeview);
        }

        return convertView;  
  
    }  
      
  
  
    final static class ViewHolder {  
        TextView tvLetter;  
        TextView tvTitle; 
        TextView tvExten; 
        View line;

        ImageView contact_listview_item_iamgeview;
    }  
  
  
    /** 
     * 根据ListView的当前位置获取分类的首字母的char ascii值 
     */  
    @Override
    public int getSectionForPosition(int position) {
        return list.get(position).pinyin.toUpperCase().charAt(0);
    }  
  
    /** 
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置 
     */  
    @Override
    public int getPositionForSection(int section) {  
        for (int i = 0; i < getCount(); i++) {  
            String sortStr = list.get(i).pinyin;  
            char firstChar = sortStr.toUpperCase().charAt(0);  
            if (firstChar == section) {  
                return i;  
            }  
        }  
          
        return -1;  
    }  
      
  
    @Override  
    public Object[] getSections() {  
        return null;  
    }  
}
