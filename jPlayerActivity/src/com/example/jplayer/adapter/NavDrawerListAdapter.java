package com.example.jplayer.adapter;

import java.util.ArrayList;

import android.R.anim;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jplayer.R;
import com.example.jplayer.model.NavDrawerItem;

public class NavDrawerListAdapter extends BaseAdapter {
    
    private Context context;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private int selectedPosition;
     
    public int getSelectedPosition() {
		return selectedPosition;
	}

	public void setSelectedPosition(int selectedPosition) {
		this.selectedPosition = selectedPosition;
		notifyDataSetChanged();
	}

	public NavDrawerListAdapter(Context context, ArrayList<NavDrawerItem> navDrawerItems){
        this.context = context;
        this.navDrawerItems = navDrawerItems;
    }
 
    @Override
    public int getCount() {
        return navDrawerItems.size();
    }
 
    @Override
    public Object getItem(int position) {       
        return navDrawerItems.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	ViewHolder holder;
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
            holder=new ViewHolder();
            holder.itemLogo=(ImageView) convertView.findViewById(R.id.icon);
            holder.itemText=(TextView) convertView.findViewById(R.id.title); 
            convertView.setTag(holder);
        }
        else
        {
        	holder=(ViewHolder)convertView.getTag();
        }
        
        if(selectedPosition==position)
        {
        	holder.itemText.setTextColor(this.context.getResources().getColor(R.color.navitemtextcolor_selected));
        }
        else
        {
        	holder.itemText.setTextColor(this.context.getResources().getColor(R.color.navitemtextcolor_normal));
        }
                 
        holder.itemLogo.setImageResource(navDrawerItems.get(position).getIcon());        
        holder.itemText.setText(navDrawerItems.get(position).getTitle());
         
         
        return convertView;
    }
    
    
    private static class ViewHolder
    {
    	ImageView itemLogo;
    	TextView itemText;
    }
 
}
