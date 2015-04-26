package com.example.jplayer.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.jplayer.R;
import com.example.jplayer.imageloaderhelper.MemoryCache;
import com.example.jplayer.model.Song;
import com.example.jplayer.model.SongHeader;
import com.example.jplayer.utils.MusicUtil;


public class ExpandablePlayerAdapter extends  BaseExpandableListAdapter {
	
	static final String TAG="ExpandablePlayerAdapter";

	public Context context;
	public ArrayList<SongHeader> arrSongHeader;
	public HashMap<SongHeader, ArrayList<Song>> musicMap;
	public Bitmap thumbnail=null;
	public static MemoryCache cache=new MemoryCache();
	public LayoutInflater inflater;
	
	public ExpandablePlayerAdapter(Context context, ArrayList<SongHeader> arrSongHeader, HashMap<SongHeader, ArrayList<Song>> musicMap)
	{
		this.context=context;
		this.arrSongHeader=arrSongHeader;
		this.musicMap=musicMap;		
		this.inflater=(LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		if(childPosition==0)
			return null;
		return musicMap.get(arrSongHeader.get(groupPosition)).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		if(childPosition==0)
		{
			convertView=inflater.inflate(R.layout.childlistheader_layout, null);
			return convertView;
		}
		ChildHolder chldHolder;
		Song song = ((Song) getChild(groupPosition, childPosition));		
		
        if (convertView == null || !(convertView.getTag() instanceof ChildHolder)) 
        {
            convertView = this.inflater.inflate(R.layout.player_child_list, null);
            chldHolder=new ChildHolder();
            chldHolder.childText=(TextView) convertView.findViewById(R.id.childSongName);
            chldHolder.childDuration=(TextView) convertView.findViewById(R.id.childSongDuration);
            convertView.setTag(chldHolder);
        }
        else
        {       	
        	chldHolder=(ChildHolder)convertView.getTag();
        }
        chldHolder.childText.setText(song.getTitle());
        chldHolder.childDuration.setText(MusicUtil.milliSecondsToTimer(song.getDuration()));
        return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return musicMap.get(arrSongHeader.get(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return arrSongHeader.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return arrSongHeader.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public View getGroupView(int  groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		GroupHolder grpHolder;
		String songHeader = ((SongHeader) getGroup(groupPosition)).getSongHeader();
		long albumid = ((SongHeader) getGroup(groupPosition)).getAlbumid();
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.player_group_list, null);
            grpHolder=new GroupHolder();
            grpHolder.groupText=(TextView) convertView.findViewById(R.id.groupMusicTitle);
            grpHolder.groupSongCount=(TextView) convertView.findViewById(R.id.groupSongCount);
            grpHolder.groupImage= (ImageView) convertView.findViewById(R.id.groupAlbumArt);
            grpHolder.dividerAbove=(View) convertView.findViewById(R.id.groupDividerAbove);
            //grpHolder.dividerBelow=(View) convertView.findViewById(R.id.groupDividerBelow);
            
            convertView.setTag(grpHolder);
            
        }
        else
        {
        	grpHolder=(GroupHolder)convertView.getTag();
        }
        
        if(groupPosition==0)
        {
        	grpHolder.dividerAbove.setVisibility(View.GONE);
        }
        else
        {
        	grpHolder.dividerAbove.setVisibility(View.VISIBLE);
        }
        
        grpHolder.groupText.setText(songHeader);
        int songCount=getChildrenCount(groupPosition)-1;
        grpHolder.groupSongCount.setText(songCount==1?songCount+" song":songCount+" songs");
        Log.d(TAG, "album id "+albumid);
//        if(albumid!=-1)
//        {
        	grpHolder.groupImage.setImageBitmap(MusicUtil.getArtworkQuick(this.context,albumid, 40, 40));
        	
       // }        
 
        return convertView; 
	}

	@Override
	public boolean hasStableIds() { 
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}
	
	public ArrayList<Song> getAllChildFromGroup(SongHeader songHeader)
	{
		return musicMap.get(songHeader);
	}

    public ArrayList<SongHeader> getArrSongHeader() {
		return arrSongHeader;
	}

	public void setArrSongHeader(ArrayList<SongHeader> arrSongHeader) {
		this.arrSongHeader = arrSongHeader;
	}

	public HashMap<SongHeader, ArrayList<Song>> getMusicMap() {
		return musicMap;
	}

	public void setMusicMap(HashMap<SongHeader, ArrayList<Song>> musicMap) {
		this.musicMap = musicMap;
	}
	

	private static class GroupHolder
	{
		ImageView  groupImage;
		TextView groupText;
		TextView groupSongCount;
		View dividerAbove,dividerBelow;
	}
	
	private static class ChildHolder
	{
		ImageView  childImage;
		TextView childText;
		TextView childDuration;
	}

	
}
